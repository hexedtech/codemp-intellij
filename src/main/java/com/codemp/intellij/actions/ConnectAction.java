package com.codemp.intellij.actions;

import com.codemp.intellij.jni.CodeMPHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class ConnectAction extends AnAction {

	static {
		System.load("O:/dev/IRL/Rust/codemp/client/intellij/target/debug/codemp_intellij.dll");
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String url = Messages.showInputDialog("URL to CodeMP instance:", "CodeMP", Messages.getQuestionIcon());
		try {
			CodeMPHandler.connect(url);
			Messages.showInfoMessage(String.format("Connected to %s!", url), "CodeMP");
		} catch(Exception ex) {
			Messages.showErrorDialog(String.format("Failed to connect to %s: %s!", url, ex.getMessage()), "CodeMP");
		}
	}
}
