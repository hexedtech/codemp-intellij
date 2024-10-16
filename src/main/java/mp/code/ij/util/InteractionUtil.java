package mp.code.ij.util;

import com.intellij.credentialStore.Credentials;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentContainer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import mp.code.BufferController;
import mp.code.Client;
import mp.code.Workspace;
import mp.code.exceptions.ConnectionException;
import mp.code.exceptions.ConnectionRemoteException;
import mp.code.ij.CodeMP;
import mp.code.ij.listeners.BufferEventListener;
import mp.code.ij.listeners.CursorEventListener;
import mp.code.ij.settings.CodeMPSettings;
import mp.code.ij.ui.CodeMPToolPanel;
import mp.code.ij.util.cb.CursorCallback;
import mp.code.ij.util.cb.WorkspaceCallback;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * Contains utility classes for interacting with CodeMP in contexts
 * where the user should be shown the output, taking care of things
 * like notifications and error handling.
 */
public class InteractionUtil {
	public static void connect(@NotNull Project project, @Nullable Runnable after) {
		ProgressManager.getInstance().run(new Task.Backgroundable(project, "Connecting to CodeMP server...") {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				try {
					CodeMPSettings.State state = Objects.requireNonNull(CodeMPSettings.getInstance().getState());
					Credentials credentials = Objects.requireNonNull(state.getCredentials());
					CodeMP.connect(
						Objects.requireNonNull(credentials.getUserName()),
						Objects.requireNonNull(credentials.getPasswordAsString())
					);
					MemoryManager.startClientLifetime();

					if(after != null) after.run();

					refreshToolWindow(project);
					notifyInfo(project, "Success", "Connected to server!");
				} catch(NullPointerException e) {
					Notifications.Bus.notify(new Notification(
						"CodeMP",
						"Invalid credentials!",
						"Please configure your credentials before connecting.",
						NotificationType.ERROR
					), project);
				} catch(ConnectionException e) {
					notifyError(project, "Failed to leave workspace!", e);
				}
			}
		});
	}

	public static void disconnect(@NotNull Project project) {
		CodeMP.disconnect();
		MemoryManager.endClientLifetime();
		refreshToolWindow(project);
		notifyInfo(project, "Success", "Disconnected from server!");
	}

	public static void createWorkspace(@NotNull Project project, @NotNull String workspaceId, @Nullable Runnable after) {
		ProgressManager.getInstance().run(new Task.Backgroundable(project, String.format("Creating workspace %s...", workspaceId)) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				try {
					CodeMP.getClient("workspace create").createWorkspace(workspaceId);
					if(after != null) after.run();
					refreshToolWindow(project);
					notifyInfo(
						project,
						"Success",
						String.format("Created workspace %s!", workspaceId)
					);
				} catch(ConnectionException e) {
					InteractionUtil.notifyError(project, String.format(
						"Failed to create workspace %s!",
						workspaceId
					), e);
				}
			}
		});
	}

	public static void inviteToWorkspace(@NotNull Project project, @NotNull String workspaceId, @NotNull String userName, @Nullable Runnable after) {
		ProgressManager.getInstance().run(new Task.Backgroundable(project, String.format("Inviting %s to workspace %s...", userName, workspaceId)) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				try {
					CodeMP.getClient("workspace invite").inviteToWorkspace(workspaceId, userName);
					if(after != null) after.run();
					refreshToolWindow(project);
					notifyInfo(
						project,
						"Success",
						String.format("Invited %s to workspace %s!", userName, workspaceId)
					);
				} catch(ConnectionException e) {
					InteractionUtil.notifyError(project, String.format(
						"Failed to invite to workspace %s!",
						workspaceId
					), e);
				}
			}
		});
	}

	public static void attachWorkspace(@NotNull Project project, @NotNull String workspaceId, @Nullable Runnable after) {
		ProgressManager.getInstance().run(new Task.Backgroundable(project, String.format("Joining workspace %s...", workspaceId)) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				try {
					CodeMP.attachWorkspace(workspaceId);
					MemoryManager.startWorkspaceLifetime(workspaceId);
					refreshToolWindow(project);
				} catch(ConnectionException e) {
					InteractionUtil.notifyError(project, String.format(
						"Failed to attach to workspace %s!",
						workspaceId
					), e);
					return;
				}

				Disposable lifetime = MemoryManager.getWorkspaceLifetime(workspaceId);
				assert lifetime != null; // can never fail

				EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();

				eventMulticaster.addDocumentListener(new BufferEventListener(), lifetime);
				eventMulticaster.addCaretListener(new CursorEventListener(), lifetime);

				CodeMP.getActiveWorkspace().cursor().callback(controller -> {
					new CursorCallback(this.myProject).accept(controller);
				});

				CodeMP.getActiveWorkspace().callback(receiver -> {
					new WorkspaceCallback(this.myProject).accept(receiver);
				});

				if(after != null) after.run();

				notifyInfo(
					project,
					"Success",
					String.format("Joined workspace %s!", workspaceId)
				);
			}
		});
	}

	public static void deleteWorkspace(@NotNull Project project, @NotNull String workspaceId, @Nullable Runnable after) {
		ProgressManager.getInstance().run(new Task.Backgroundable(project, String.format("Deleting workspace %s...", workspaceId)) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				try {
					Client client = CodeMP.getClient("workspace delete");
					client.deleteWorkspace(workspaceId);

					Optional<Workspace> ws = client.getWorkspace("workspace leave");
					if(ws.isPresent() && ws.get().id().equals(workspaceId)) {
						CodeMP.leaveWorkspace();
						MemoryManager.endWorkspaceLifetime(workspaceId);
					}

					if(after != null) after.run();

					refreshToolWindow(project);

					notifyInfo(
						project,
						"Success",
						String.format("Deleted workspace %s!", workspaceId)
					);
				} catch(ConnectionException e) {
					InteractionUtil.notifyError(project, String.format(
						"Failed to delete workspace %s!",
						workspaceId
					), e);
				}
			}
		});
	}

	public static void leaveWorkspace(@NotNull Project project, @NotNull String workspaceId, @Nullable Runnable after) {
		ProgressManager.getInstance().run(new Task.Backgroundable(project, String.format("Leaving workspace %s...", workspaceId)) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				CodeMP.leaveWorkspace();
				MemoryManager.endWorkspaceLifetime(workspaceId);
				if(after != null) after.run();
				refreshToolWindow(project);
				notifyInfo(
					project,
					"Success",
					String.format("Left workspace %s!", workspaceId)
				);
			}
		});
	}

	public static String[] listWorkspaces(Project project, boolean owned, boolean joined) {
		try {
			Client client = CodeMP.getClient("drawActiveWorkspaces");

			String[] ownedWorkspaces;
			if(owned) ownedWorkspaces = client.fetchOwnedWorkspaces();
			else ownedWorkspaces = new String[0];

			String[] joinedWorkspaces;
			if(joined) joinedWorkspaces = client.fetchJoinedWorkspaces();
			else joinedWorkspaces = new String[0];

			return ArrayUtils.addAll(ownedWorkspaces, joinedWorkspaces);
		} catch(ConnectionRemoteException exception) {
			notifyError(project, "Failed to list workspaces!", exception);
			return new String[0];
		}
	}

	public static Optional<BufferController> bufferAttach(Project project, Workspace workspace, String path) {
		try {
			BufferController controller = workspace.attachBuffer(path);
			MemoryManager.startBufferLifetime(workspace.id(), path);
			notifyInfo(project, "Success!", String.format(
				"Successfully attached to buffer %s on workspace %s!",
				path,
				workspace.id())
			);
			return Optional.of(controller);
		} catch(ConnectionException e) {
			notifyError(project, "Failed to attach to buffer!", e);
			return Optional.empty();
		}
	}

	public static void createBuffer(Project project, String path) {
		try {
			Workspace workspace = CodeMP.getActiveWorkspace();
			workspace.createBuffer(path);
			refreshToolWindow(project);
		} catch(ConnectionRemoteException e) {
			notifyError(project, "Failed to create a buffer!", e);
		}
	}

	public static void notifyInfo(Project project, String title, String msg) {
		Notifications.Bus.notify(new Notification(
			"CodeMP", title, msg, NotificationType.INFORMATION
		), project);
	}

	public static void notifyError(Project project, String title, Throwable t) {
		Notifications.Bus.notify(new Notification(
			"CodeMP", title,
			String.format("%s: %s", t.getClass().getCanonicalName(), t.getMessage()),
			NotificationType.ERROR
		), project);
		CodeMP.LOGGER.error(title, t);
	}

	public static void refreshToolWindow(Project project) {
		Optional.ofNullable(ToolWindowManager.getInstance(project).getToolWindow("CodeMP"))
			.map(ToolWindow::getContentManager)
			.map(cm -> cm.getContent(0))
			.map(ComponentContainer::getComponent)
			.ifPresent(w -> ((CodeMPToolPanel) w).redraw(project));
	}
}
