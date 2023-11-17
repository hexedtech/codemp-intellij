package com.codemp.intellij.actions.buffer;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.jni.CodeMPHandler;
import com.codemp.intellij.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class BufferCreateAction extends AnAction {
	public static void create(AnActionEvent e, String buffer, boolean silent) throws Exception {
		CodeMPHandler.create(buffer);
		if(!silent) ActionUtil.notify(e, "Success",
			String.format("Created buffer %s!", buffer)
		);
		CodeMP.LOGGER.debug("Created buffer {}!", buffer);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String buffer = Messages.showInputDialog(
			"Buffer name:",
			"Create CodeMP Buffer",
			Messages.getQuestionIcon());

		try {
			create(e, buffer, false);
		} catch(Exception ex) {
			ActionUtil.notifyError(e, String.format(
				"Failed to create buffer with name %s!",
				buffer), ex);
		}
	}
}
