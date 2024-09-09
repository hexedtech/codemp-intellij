package mp.code.intellij.task;

import lombok.SneakyThrows;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.FileUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import mp.code.BufferController;
import mp.code.Workspace;
import mp.code.data.TextChange;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class BufferEventAwaiterTask extends Task.Backgroundable implements Disposable {
	public final Map<String, Disposable> activeBuffers;
	private final Workspace handler;
	public BufferEventAwaiterTask(@NotNull Project project, @NotNull Workspace handler) {
		super(project, "Awaiting CodeMP buffer events", false);
		this.activeBuffers = new ConcurrentHashMap<>();
		this.handler = handler;
	}

	@Override
	@SneakyThrows
	@SuppressWarnings("InfiniteLoopStatement")
	public void run(@NotNull ProgressIndicator indicator) {
		while(true) {
			Optional<BufferController> bufferOptional;
			bufferOptional = this.handler.selectBuffer(100L);

			if(bufferOptional.isEmpty())
				continue;
			BufferController buffer = bufferOptional.get();

			List<TextChange> changeList = new ArrayList<>();
			while(true) {
				Optional<TextChange> changeOptional;
				try {
					changeOptional = buffer.tryRecv();
				} catch(Exception e) {
					throw new RuntimeException(e);
				}

				if(changeOptional.isEmpty())
					break;
				TextChange change = changeOptional.get();
				CodeMP.LOGGER.debug("Received text change {} from offset {} to {}!",
					change.content, change.start, change.end);
				changeList.add(change);
			}

			Editor bufferEditor = FileUtil.getActiveEditorByPath(this.myProject, buffer.getName());
			ApplicationManager.getApplication().invokeLaterOnWriteThread(() ->
				ApplicationManager.getApplication().runWriteAction(() ->
					CommandProcessor.getInstance().executeCommand(
						this.myProject,
						() -> changeList.forEach((change) ->
							bufferEditor.getDocument().replaceString(
								(int) change.start, (int) change.end, change.content)
						),
						"CodeMPBufferReceive",
						"codemp-buffer-receive", //TODO: mark this with the name
						bufferEditor.getDocument()
				)));
			}
	}

	@Override
	public void dispose() {
		this.activeBuffers.values().forEach(Disposable::dispose);
		this.activeBuffers.clear();
	}
}
