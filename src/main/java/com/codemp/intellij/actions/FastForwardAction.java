package com.codemp.intellij.actions;

import com.codemp.intellij.CodeMP;
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
		ConnectAction.connect(e, "http://alemi.dev:50052", true);
		WorkspaceJoinAction.join(e, "default", true);
		BufferAttachAction.attach(e, "fucl", true);
		CodeMP.LOGGER.debug("Completed quick startup for testing!");
	}
}
