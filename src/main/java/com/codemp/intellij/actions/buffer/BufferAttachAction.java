package com.codemp.intellij.actions.buffer;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.jni.BufferHandler;
import com.codemp.intellij.jni.CodeMPHandler;
import com.codemp.intellij.task.TaskManager;
import com.codemp.intellij.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class BufferAttachAction extends AnAction {
	public static void attach(AnActionEvent e, String buffer, boolean silent) throws Exception {
		BufferHandler bufferHandler = CodeMPHandler.attach(buffer);
		if(!silent) Messages.showInfoMessage(String.format("Attached to buffer to %s!", buffer),
			"CodeMP Buffer Attach");
		CodeMP.LOGGER.debug("Attached to buffer to {}!", buffer);

		//TODO "get" the Editor corresponding to buffer, for now use the current one
		Editor editor = ActionUtil.getCurrentEditor(e);

		TaskManager
			.getOrCreateBufferTask(ActionUtil.getCurrentProject(e))
			.registerListener(bufferHandler, editor);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String buffer = Messages.showInputDialog(
			"Buffer name:",
			"Attach to CodeMP Buffer",
			Messages.getQuestionIcon());
		try {
			attach(e, buffer, false);
		} catch(Exception ex) {
			Messages.showErrorDialog(String.format(
				"Failed to attach to buffer %s: %s!",
				buffer, ex.getMessage()), "Attach to CodeMP Buffer");
		}
	}
}
