package mp.code.intellij.util.cb;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import lombok.RequiredArgsConstructor;
import mp.code.BufferController;
import mp.code.Extensions;
import mp.code.data.BufferUpdate;
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
					List<BufferUpdate> updateList = new ArrayList<>();
					while(true) {
						Optional<BufferUpdate> updateOptional;
						try {
							updateOptional = bufferController.tryRecv();
						} catch(ControllerException ex) {
							throw new RuntimeException(ex);
						}

						if(updateOptional.isEmpty())
							break;
						BufferUpdate update = updateOptional.get();
						CodeMP.LOGGER.debug(String.format(
							"Received text change %s from offset %d to %d!",
							update.change.content,
							update.change.startIdx,
							update.change.endIdx
						));
						updateList.add(update);
					}

					ApplicationManager.getApplication().runWriteAction(() -> {
						CommandProcessor.getInstance().executeCommand(
							this.project,
							() -> updateList.forEach((update) -> {
								editor.getDocument().replaceString(
									(int) update.change.startIdx,
									(int) update.change.endIdx,
									update.change.content
								);
								bufferController.ack(update.version);
								// check for validity, force-sync if mismatch
								// TODO: prompt instead of doing it silently
								if(update.hash.isPresent() && update.hash.getAsLong() != Extensions.hash(editor.getDocument().getText())) {
									try {
										editor.getDocument().setText(bufferController.getContent());
									} catch(ControllerException ignored) {} // ignore exception
								}
							}),
							"CodeMPBufferReceive",
							"codemp-buffer-receive",
							editor.getDocument()
						);
					});
				});
			});
		});
	}
}
