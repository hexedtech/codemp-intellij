package com.codemp.intellij.actions.buffer;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.jni.CodeMPHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class BufferCreateAction extends AnAction {
	public static void create(String buffer, boolean silent) throws Exception {
		CodeMPHandler.create(buffer);
		if(!silent) Messages.showInfoMessage(String.format("Created buffer %s!", buffer),
			"Create CodeMP Buffer" );
		CodeMP.LOGGER.debug("Created buffer {}!", buffer);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String buffer = Messages.showInputDialog(
			"Buffer name:",
			"Create CodeMP Buffer",
			Messages.getQuestionIcon());

		try {
			create(buffer, false);
		} catch(Exception ex) {
			Messages.showErrorDialog(String.format(
				"Failed to create buffer with name %s: %s!",
				buffer, ex.getMessage()), "Create CodeMP Buffer");
		}
	}
}
