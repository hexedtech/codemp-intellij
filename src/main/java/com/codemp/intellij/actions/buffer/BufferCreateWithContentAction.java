package com.codemp.intellij.actions.buffer;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.jni.CodeMPHandler;
import com.codemp.intellij.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class BufferCreateWithContentAction extends AnAction {

	public static void createWithContent(AnActionEvent event, String buffer, boolean silent) throws Exception {
		String content = ActionUtil.getCurrentEditor(event).getDocument().getText();
		CodeMPHandler.createWithContent(buffer, content);

		if(!silent) ActionUtil.notify(event, "Success", String.format(
			"Created buffer %s with content %s!", buffer, content));
		CodeMP.LOGGER.debug("Created buffer {} with content {}!", buffer, content);
	}
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String buffer = Messages.showInputDialog(
			"Buffer name:",
			"Create CodeMP Buffer with Content",
			Messages.getQuestionIcon());

		try {
			createWithContent(e, buffer, false);
		} catch(Exception ex) {
			ActionUtil.notifyError(e, String.format(
				"Failed to create buffer with name %s!",
				buffer), ex);
		}
	}
}
