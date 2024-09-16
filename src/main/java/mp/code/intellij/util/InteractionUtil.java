package mp.code.intellij.util;

import com.intellij.credentialStore.Credentials;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.EditorEventMulticaster;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import mp.code.BufferController;
import mp.code.Client;
import mp.code.Workspace;
import mp.code.data.Cursor;
import mp.code.exceptions.ConnectionException;
import mp.code.exceptions.ConnectionRemoteException;
import mp.code.exceptions.ControllerException;
import mp.code.intellij.CodeMP;
import mp.code.intellij.listeners.BufferEventListener;
import mp.code.intellij.listeners.CursorEventListener;
import mp.code.intellij.settings.CodeMPSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
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

					if(after != null) after.run();

					notifyInfo(
						project,
						"Success",
						String.format("Connected to %s!", state.getServerUrl())
					);
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
				} catch(ConnectionException e) {
					InteractionUtil.notifyError(project, String.format(
						"Failed to join workspace %s!",
						workspaceId
					), e);
					return;
				}

				EditorEventMulticaster eventMulticaster = EditorFactory.getInstance().getEventMulticaster();

				eventMulticaster.addDocumentListener(new BufferEventListener()); // TODO disposable
				eventMulticaster.addCaretListener(new CursorEventListener()); // TODO disposable

				CodeMP.getActiveWorkspace().getCursor().callback(controller -> {
					ApplicationManager.getApplication().invokeLater(() -> {
						try {
							while(true) {
								Optional<Cursor> c = controller.tryRecv();
								if(c.isEmpty()) break;
								Cursor event = c.get();

								CodeMP.LOGGER.debug(
									"Cursor moved by user {}! Start pos: {}x {}y; end pos: {}x {}y in buffer {}!",
									event.user,
									event.startCol, event.startRow,
									event.endCol, event.endRow,
									event.buffer
								);

								try {
									ApplicationManager.getApplication().invokeLater(() -> {
										Editor editor = FileUtil.getActiveEditorByPath(this.myProject, event.buffer);
										if(editor == null) return;

										int startOffset = editor.getDocument()
											.getLineStartOffset(event.startRow) + event.startCol;
										int endOffset = editor.getDocument()
											.getLineStartOffset(event.startRow) + event.startCol;

										int documentLength = editor.getDocument().getTextLength();
										if(startOffset > documentLength || endOffset > documentLength) {
											CodeMP.LOGGER.debug(
												"Out of bounds cursor: start was {}, end was {}, document length was {}!",
												startOffset, endOffset, documentLength);
											return;
										}

										RangeHighlighter previous = CodeMP.HIGHLIGHTER_MAP.put(
											event.user,
											editor.getMarkupModel().addRangeHighlighter(
												startOffset,
												endOffset,
												HighlighterLayer.SELECTION,
												new TextAttributes(
													null,
													ColorUtil.hashColor(event.user),
													null,
													null,
													Font.PLAIN
												), HighlighterTargetArea.EXACT_RANGE
											)
										);

										if(previous != null)
											previous.dispose();
									});
								} catch(IndexOutOfBoundsException ignored) {}
							}
						} catch(ControllerException ex) {
							notifyError(project, "Error receiving change", ex);
						}
					});
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

	private static void notifyInfo(Project project, String title, String msg) {
		Notifications.Bus.notify(new Notification(
			"CodeMP", title, msg, NotificationType.INFORMATION
		), project);
	}

	private static void notifyError(Project project, String title, Throwable t) {
		Notifications.Bus.notify(new Notification(
			"CodeMP", title,
			String.format("%s: %s", t.getClass().getCanonicalName(), t.getMessage()),
			NotificationType.ERROR
		), project);
		CodeMP.LOGGER.error(title, t);
	}
}
