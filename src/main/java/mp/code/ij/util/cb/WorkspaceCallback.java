package mp.code.ij.util.cb;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import lombok.RequiredArgsConstructor;
import mp.code.Workspace;
import mp.code.exceptions.ControllerException;
import mp.code.ij.util.FileUtil;
import mp.code.ij.util.InteractionUtil;
import mp.code.proto.WorkspaceEvent;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static mp.code.proto.WorkspaceEventKind.*;

@RequiredArgsConstructor
public class WorkspaceCallback implements Consumer<Workspace> {
	private static final Executor WORKSPACE_EXECUTOR = Executors.newSingleThreadExecutor();
	private final Project project;

	@Override
	public void accept(Workspace workspace) {
		WORKSPACE_EXECUTOR.execute(() -> {
			try {
				while(true) {
					WorkspaceEvent event = workspace.tryRecv();
					if(event == null) return;

					switch(event.kind) { // TODO: make these do stuff
						case USER_JOIN_WORKSPACE -> {}
						case USER_LEAVE_WORKSPACE -> {
							CursorCallback.disposeOfPreviousHighlighters(event.user);
							InteractionUtil.refreshToolWindow(this.project);
						}
						case FILE_CREATE -> {}
						case FILE_RENAME -> {}
						case FILE_DELETE -> {}
						case USER_JOIN_BUFFER -> {}
						case USER_LEAVE_BUFFER -> {
							Editor editor = FileUtil.getActiveEditorByPath(this.project, event.path);
							if(editor != null) {
								CursorCallback.disposeOfPreviousHighlighters(event.user);
								InteractionUtil.refreshToolWindow(this.project);
							}
						}
					}
				}
			} catch(ControllerException ex) {
					InteractionUtil.notifyError(project, "Error receiving event", ex);
			}
		});
	}
}
