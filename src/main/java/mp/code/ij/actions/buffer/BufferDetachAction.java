package mp.code.ij.actions.buffer;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import mp.code.BufferController;
import mp.code.Workspace;
import mp.code.ij.CodeMP;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Map;

public class BufferDetachAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Workspace ws = CodeMP.getActiveWorkspace();
		if(ws == null) {
			Messages.showErrorDialog(
				"Not currently in a workspace!",
				"CodeMP Buffer Detach"
			);
			return;
		}

		String[] activeBuffers = ws.activeBuffers();
		int choice = Messages.showChooseDialog(
			"Detach from which buffer?",
			"CodeMP Buffer Detach",
			activeBuffers,
			"",
			Messages.getQuestionIcon()
		);

		String path = activeBuffers[choice];

		BufferController controller = CodeMP.getActiveWorkspace().getBuffer(path);

		if (controller == null) {
			Messages.showErrorDialog("No controller for buffer " + path, "CodeMP Buffer Detach");
			return;
		}

		controller.clearCallback();
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

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.EDT;
	}
}
