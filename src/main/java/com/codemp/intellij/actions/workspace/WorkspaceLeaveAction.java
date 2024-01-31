package com.codemp.intellij.actions.workspace;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

public class WorkspaceLeaveAction extends AnAction {
	public static void leave(AnActionEvent e, String workspaceId, boolean silent) {
		CodeMP.getClient("leave workspace")
			.leaveWorkspace(workspaceId);
		Disposer.dispose(CodeMP.ACTIVE_WORKSPACES.remove(workspaceId));

		if(!silent) ActionUtil.notify(e, "Success", String.format("Left workspace %s!", workspaceId));
		CodeMP.LOGGER.debug("Left workspace!");
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String workspaceId = Messages.showInputDialog(
			"Workspace to leave:",
			"CodeMP Workspace Leave",
			Messages.getQuestionIcon());

		try {
			leave(e, workspaceId, false);
		} catch(Exception ex) {
			ActionUtil.notifyError(e, "Failed to leave workspace!", ex);
		}
	}
}
