package com.codemp.intellij.actions;

import com.codemp.intellij.actions.buffer.BufferAttachAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class FastForwardAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		ConnectAction connectAction = new ConnectAction();
		JoinAction joinAction = new JoinAction();
		BufferAttachAction attachAction = new BufferAttachAction();
		try {
			connectAction.connect("http://alemi.dev:50051");
			joinAction.join(e, "default");
			attachAction.attach(e, "test");
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
