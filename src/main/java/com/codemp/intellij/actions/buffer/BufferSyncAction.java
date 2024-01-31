package com.codemp.intellij.actions.buffer;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;

public class BufferSyncAction extends AnAction {
	public static void sync(AnActionEvent e, String bufferName, boolean silent) {
		Editor editor = ActionUtil.getCurrentEditor(e);
		ApplicationManager.getApplication().runWriteAction(() ->
			CommandProcessor.getInstance().executeCommand(
				editor.getProject(),
				() -> editor.getDocument().setText(CodeMPHandler.getBuffer(bufferName).getContent()),
				"CodeMPBufferSync",
				"codemp-buffer-sync",
				editor.getDocument()
			));

		if(!silent) ActionUtil.notify(e,
			String.format("Synced buffer %s", bufferName),
			"The buffer was synced successfully.");
		CodeMP.LOGGER.debug("The buffer {} was synced successfully.", bufferName);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Editor editor = ActionUtil.getCurrentEditor(e);
		String bufferName = CodeMP.ACTIVE_BUFFERS_REVERSE.get(editor);
		try {
			sync(e, bufferName, false);
		} catch(Exception ex) {
			ActionUtil.notifyError(e, String.format(
				"Failed to attach to buffer %s!",
				bufferName), ex);
		}
	}
}
