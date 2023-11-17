package com.codemp.intellij.actions.workspace;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.jni.CodeMPHandler;
import com.codemp.intellij.util.DisposableRegistry;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class WorkspaceLeaveAction extends AnAction {
	public static void leave(boolean silent) throws Exception {
		CodeMPHandler.leaveWorkspace();

		//dispose of listener's associated disposable
		DisposableRegistry.disposeOf("codemp-cursor");

		if(!silent) Messages.showInfoMessage("Left workspace!",
			"Detach from CodeMP Buffer" );
		CodeMP.LOGGER.debug("Left workspace!");
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		try {
			leave(false);
		} catch(Exception ex) {
			Messages.showErrorDialog(String.format(
				"Failed to leave workspace: %s!",
				ex.getMessage()), "Leave CodeMP Workspace");
		}
	}
}
