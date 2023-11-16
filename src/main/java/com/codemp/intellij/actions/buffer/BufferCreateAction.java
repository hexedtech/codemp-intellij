package com.codemp.intellij.actions.buffer;

import com.codemp.intellij.jni.CodeMPHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class BufferCreateAction extends AnAction {

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String buffer = Messages.showInputDialog(
			"Buffer name:",
			"Create CodeMP Buffer",
			Messages.getQuestionIcon());

		try {
			CodeMPHandler.create(buffer);
			//Messages.showInfoMessage(String.format("Created buffer %s!", url), "Create CodeMP Buffer" );
			System.out.printf("Created buffer %s!\n", buffer);
		} catch(Exception ex) {
			Messages.showErrorDialog(String.format(
				"Failed to create buffer with name %s: %s!",
				buffer, ex.getMessage()), "Create CodeMP Buffer");
		}
	}
}
