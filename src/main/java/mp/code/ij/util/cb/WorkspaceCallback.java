package mp.code.ij.util.cb;

import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.project.Project;
import lombok.RequiredArgsConstructor;
import mp.code.Workspace;
import mp.code.exceptions.ControllerException;
import mp.code.ij.CodeMP;
import mp.code.ij.util.InteractionUtil;

import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@RequiredArgsConstructor
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class WorkspaceCallback implements Consumer<Workspace> {
	private static final Executor WORKSPACE_EXECUTOR = Executors.newSingleThreadExecutor();
	private final Project project;

	@Override
	public void accept(Workspace workspace) {
		WORKSPACE_EXECUTOR.execute(() -> {
			try {
				while(true) {
					Optional<Workspace.Event> possibleEvent = workspace.tryRecv();
					if(possibleEvent.isEmpty()) return;
					Workspace.Event event = possibleEvent.get();

					switch(event.getType()) {
						case USER_JOIN, FILE_TREE_UPDATED -> {}
						case USER_LEAVE -> {
							RangeHighlighter prev = CodeMP.HIGHLIGHTER_MAP.remove(event.getUserLeft().get());
							if(prev != null) prev.dispose();
							InteractionUtil.refreshToolWindow(this.project);
						}
					}
					InteractionUtil.refreshToolWindow(this.project);
				}
			} catch(ControllerException ex) {
					InteractionUtil.notifyError(project, "Error receiving event", ex);
			}
		});
	}
}
