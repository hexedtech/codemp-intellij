package mp.code.intellij.util;

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
import mp.code.BufferController;
import mp.code.Client;
import mp.code.Workspace;
import mp.code.exceptions.ConnectionException;
import mp.code.exceptions.ConnectionRemoteException;
import mp.code.intellij.CodeMP;
import mp.code.intellij.listeners.BufferEventListener;
import mp.code.intellij.listeners.CursorEventListener;
import mp.code.intellij.settings.CodeMPSettings;
import mp.code.intellij.util.cb.CursorCallback;
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
	public static void connect(@Nullable Project project, @Nullable Runnable after) {
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

	public static void disconnect(@Nullable Project project) {
		CodeMP.disconnect();
		MemoryManager.endClientLifetime();
		notifyInfo(project, "Success", "Disconnected from server!");
	}

	public static void joinWorkspace(Project project, @NotNull String workspaceId, @Nullable Runnable after) {
		ProgressManager.getInstance().run(new Task.Backgroundable(project, String.format("Joining workspace %s...", workspaceId)) {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				if(project == null) {
					Notifications.Bus.notify(new Notification(
						"CodeMP",
						"No project found",
						"Please ensure that you have an open project before attempting to join a workspace.",
						NotificationType.ERROR
					), null);
					return;
				}

				try {
					CodeMP.joinWorkspace(workspaceId);
					MemoryManager.startWorkspaceLifetime(workspaceId);
				} catch(ConnectionException e) {
					InteractionUtil.notifyError(project, String.format(
						"Failed to join workspace %s!",
						workspaceId
					), e);
					return;
				}

				Disposable lifetime = MemoryManager.getWorkspaceLifetime(workspaceId);
				assert lifetime != null; // can never fail

				EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();

				eventMulticaster.addDocumentListener(new BufferEventListener(), lifetime);
				eventMulticaster.addCaretListener(new CursorEventListener(), lifetime);

				CodeMP.getActiveWorkspace().getCursor().callback(controller -> {
					new CursorCallback(this.myProject).accept(controller);
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

	public static void leaveWorkspace(Project project, String workspaceId) {
		CodeMP.leaveWorkspace();
		MemoryManager.endWorkspaceLifetime(workspaceId);
		notifyInfo(
			project,
			"Success",
			String.format("Left workspace %s!", workspaceId)
		);
	}

	public static String[] listWorkspaces(Project project) {
		try {
			Client client = CodeMP.getClient("drawActiveWorkspaces");
			return client.listWorkspaces(true, true);
		} catch(ConnectionRemoteException exception) {
			notifyError(project, "Failed to list workspaces!", exception);
			return new String[0];
		}
	}

	public static Optional<BufferController> bufferAttach(Project project, Workspace workspace, String path) {
		try {
			BufferController controller = workspace.attachToBuffer(path);
			MemoryManager.startBufferLifetime(workspace.getWorkspaceId(), path);
			notifyInfo(project, "Success!", String.format(
				"Successfully attached to buffer %s on workspace %s!",
				path,
				workspace.getWorkspaceId())
			);
			return Optional.of(controller);
		} catch(ConnectionException e) {
			notifyError(project, "Failed to attach to buffer!", e);
			return Optional.empty();
		}
	}

	public static void bufferCreate(Project project, String path) {
		try {
			Workspace workspace = CodeMP.getActiveWorkspace();
			workspace.createBuffer(path);
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
}
