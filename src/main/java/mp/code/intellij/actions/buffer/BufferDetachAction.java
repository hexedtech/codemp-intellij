package mp.code.intellij.actions.buffer;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import mp.code.BufferController;
import mp.code.intellij.CodeMP;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class BufferDetachAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String[] active_buffers = CodeMP.getActiveWorkspace().activeBuffers();
		int choice = Messages.showChooseDialog(
			"Detach from which buffer?",
			"CodeMP Buffer Detach",
			active_buffers,
			"",
			Messages.getQuestionIcon()
		);
		String path = active_buffers[choice];

		Optional<BufferController> controller = CodeMP.getActiveWorkspace().getBuffer(path);

		if (controller.isEmpty()) {
			Messages.showErrorDialog("No controller for buffer " + path, "CodeMP Buffer Detach");
			return;
		}

		controller.get().clearCallback();
		for (Map.Entry<Path, String> entry : CodeMP.BUFFER_MAPPER.entrySet()) {
			if (entry.getValue().equals(path)) {
				CodeMP.BUFFER_MAPPER.remove(entry.getKey());
				break;
			}
		}

		Messages.showInfoMessage("Detached from buffer " + path, "CodeMP Buffer Detach");
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabled(CodeMP.isInWorkspace());
	}
}
