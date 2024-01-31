package com.codemp.intellij.actions.buffer;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.exceptions.ide.BufferDetachException;
import com.codemp.intellij.task.BufferEventAwaiterTask;
import com.codemp.intellij.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class BufferDetachAction extends AnAction {
	public static void detach(AnActionEvent e, String buffer, boolean silent) {
		boolean res = CodeMPHandler.detach(buffer);
		if(!res) throw new BufferDetachException(buffer);

		CodeMP.ACTIVE_BUFFERS.remove(buffer);
		if(task != null) {
			task.unregisterListener(buffer);
			if(!silent) ActionUtil.notify(e, "Success",
				String.format("Detached from buffer %s!", buffer)
			);
			CodeMP.LOGGER.debug("Detached from buffer {}!", buffer);
		} else {
			if(!silent) ActionUtil.notifyError(e, String.format("Failed to detach from %s", buffer),
				"Buffer event task was dead!");
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
			detach(e, buffer, false);
		} catch(Exception ex) {
			ActionUtil.notifyError(e, String.format(
				"Failed to detach from buffer with name %s!",
				buffer), ex);
		}
	}
}
