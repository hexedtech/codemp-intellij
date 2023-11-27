package com.codemp.intellij.actions.workspace;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.jni.CodeMPHandler;
import com.codemp.intellij.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class WorkspaceLeaveAction extends AnAction {
	public static void leave(AnActionEvent e, boolean silent) {
		CodeMPHandler.leaveWorkspace();

		if(!silent) ActionUtil.notify(e, "Success", "Left workspace");
		CodeMP.LOGGER.debug("Left workspace!");
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		try {
			leave(e,  false);
		} catch(Exception ex) {
			ActionUtil.notifyError(e, "Failed to leave workspace!", ex);
		}
	}
}
