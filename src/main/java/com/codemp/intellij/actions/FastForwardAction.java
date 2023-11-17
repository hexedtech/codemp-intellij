package com.codemp.intellij.actions;

import com.codemp.intellij.actions.buffer.BufferAttachAction;
import com.codemp.intellij.actions.workspace.WorkspaceJoinAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Used exclusively to streamline debugging.
 */
public class FastForwardAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		try {
			ConnectAction.connect("http://alemi.dev:50052", true);
			WorkspaceJoinAction.join(e, "default", true);
			BufferAttachAction.attach(e, "test", true);
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
