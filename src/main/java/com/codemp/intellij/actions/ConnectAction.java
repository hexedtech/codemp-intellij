package com.codemp.intellij.actions;

import com.codemp.intellij.jni.CodeMPHandler;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import cz.adamh.utils.NativeUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ConnectAction extends AnAction {
	public ConnectAction() {
		super();
		/*try {
			NativeUtils.loadLibraryFromJar("/resources/libHelloJNI.so");
		} catch(IOException e) {
			throw new RuntimeException(e);
		}*/
		//System.load("/home/zaaarf/dev/irl/rust/codemp/client/intellij/target/debug/libcodemp_intellij.so");
		System.load("O:/dev/IRL/Rust/codemp/client/intellij/target/debug/codemp_intellij.dll");
	}

	public void connect(String url) throws Exception {
		CodeMPHandler.connect(url);
		//Messages.showInfoMessage(String.format("Connected to %s!", url), "CodeMP");
		System.out.printf("Connected to %s!\n", url);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String url = Messages.showInputDialog("URL to CodeMP instance:", "CodeMP Connect",
			Messages.getQuestionIcon());

		try {
			this.connect(url);
		} catch(Exception ex) {
			Messages.showErrorDialog(String.format("Failed to connect to %s: %s!", url, ex.getMessage()), "CodeMP");
		}
	}
}
