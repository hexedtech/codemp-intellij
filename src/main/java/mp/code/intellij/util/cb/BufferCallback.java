package mp.code.intellij.util.cb;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import lombok.RequiredArgsConstructor;
import mp.code.BufferController;
import mp.code.data.TextChange;
import mp.code.exceptions.ControllerException;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class BufferCallback implements Consumer<BufferController> {
	private static final Executor BUFFER_EXECUTOR = Executors.newSingleThreadExecutor();
	private final Project project;

	@Override
	public void accept(BufferController bufferController) {
		BUFFER_EXECUTOR.execute(() -> {
			ApplicationManager.getApplication().runReadAction(() -> {
				Editor editor = FileUtil.getActiveEditorByPath(this.project, bufferController.getName());
				ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
					List<TextChange> changeList = new ArrayList<>();
					while(true) {
						Optional<TextChange> changeOptional;
						try {
							changeOptional = bufferController.tryRecv();
						} catch(ControllerException ex) {
							throw new RuntimeException(ex);
						}

						if(changeOptional.isEmpty())
							break;
						TextChange change = changeOptional.get();
						CodeMP.LOGGER.debug("Received text change {} from offset {} to {}!",
							change.content, change.start, change.end);
						changeList.add(change);
					}

					ApplicationManager.getApplication().runWriteAction(() ->
						CommandProcessor.getInstance().executeCommand(
							this.project,
							() -> changeList.forEach((change) ->
								editor.getDocument().replaceString(
									(int) change.start, (int) change.end, change.content)
							),
							"CodeMPBufferReceive",
							"codemp-buffer-receive",
							editor.getDocument()
						)
					);
				});
			});
		});
	}
}
