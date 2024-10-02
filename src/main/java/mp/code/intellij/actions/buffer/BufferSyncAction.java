package mp.code.intellij.actions.buffer;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import mp.code.BufferController;
import mp.code.exceptions.ControllerException;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.FileUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BufferSyncAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		// TODO if current buffer is managed, sync that instead of making user choose

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
			Messages.showErrorDialog("No active controller for path " + path, "CodeMP Buffer Sync");
			return;
		}

		Editor editor = FileUtil.getActiveEditorByPath(e.getProject(), path);
		if (editor == null) {
			Messages.showErrorDialog("No active buffer for path " + path, "CodeMP Buffer Sync");
			return;
		}

		String remoteContent;
		try {
			remoteContent = controller.get().getContent();
		} catch (ControllerException ex) {
			Messages.showErrorDialog("Controller failed returning path: " + path, "CodeMP Buffer Sync");
			return;
		}

		ApplicationManager.getApplication().runWriteAction(() -> {
			CommandProcessor.getInstance().executeCommand(
				e.getProject(),
				() -> editor.getDocument().replaceString(0, editor.getDocument().getTextLength(), remoteContent),
				"CodeMPBufferReceive",
				"codemp-buffer-receive",
				editor.getDocument()
			);
		});

		Messages.showInfoMessage("Synched buffer " + path, "CodeMP Buffer Sync");
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
