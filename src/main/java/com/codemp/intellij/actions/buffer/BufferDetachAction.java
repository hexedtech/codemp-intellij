package com.codemp.intellij.actions.buffer;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.exceptions.ide.BufferDetachException;
import com.codemp.intellij.jni.CodeMPHandler;
import com.codemp.intellij.task.BufferEventAwaiterTask;
import com.codemp.intellij.task.TaskManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class BufferDetachAction extends AnAction {
	public static void detach(String buffer, boolean silent) throws Exception {
		boolean res = CodeMPHandler.detach(buffer);
		if(!res) throw new BufferDetachException(buffer);

		CodeMP.ACTIVE_BUFFERS.remove(buffer);
		BufferEventAwaiterTask task = TaskManager.getBufferTask();
		if(task != null) {
			task.unregisterListener(buffer);
			if(!silent) Messages.showInfoMessage(String.format("Detached from buffer %s!", buffer),
				"Detach from CodeMP Buffer");
			CodeMP.LOGGER.debug("Detached from buffer {}!", buffer);
		} else {
			if(!silent) Messages.showErrorDialog(
				String.format("Failed to detach from %s: buffer event task was dead!", buffer),
				"Detach from CodeMP Buffer");
			CodeMP.LOGGER.debug("Failed to detach from {}: buffer event task was dead!", buffer);
		}
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String buffer = Messages.showInputDialog(
			"Buffer name:",
			"Detach from CodeMP Buffer",
			Messages.getQuestionIcon());

		try {
			detach(buffer, false);
		} catch(Exception ex) {
			Messages.showErrorDialog(String.format(
				"Failed to detach from buffer with name %s: %s!",
				buffer, ex.getMessage()), "Detach from CodeMP Buffer");
		}
	}
}
