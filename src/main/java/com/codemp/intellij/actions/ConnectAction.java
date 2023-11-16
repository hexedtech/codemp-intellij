package com.codemp.intellij.actions;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.jni.CodeMPHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class ConnectAction extends AnAction {
	public static void connect(String url, boolean silent) throws Exception {
		CodeMP.loadLibrary(); //will only load it the first time
		CodeMPHandler.connect(url);
		if(!silent) Messages.showInfoMessage(String.format("Connected to %s!", url), "CodeMP");
		CodeMP.LOGGER.debug("Connected to {}!", url);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String url = Messages.showInputDialog("URL to CodeMP instance:", "CodeMP Connect",
			Messages.getQuestionIcon());
		try {
			connect(url, false);
		} catch(Exception ex) {
			Messages.showErrorDialog(String.format("Failed to connect to %s: %s!", url, ex.getMessage()), "CodeMP");
		}
	}
}
