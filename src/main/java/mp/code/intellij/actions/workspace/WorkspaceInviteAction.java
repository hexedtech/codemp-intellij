package mp.code.intellij.actions.workspace;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.InteractionUtil;
import org.jetbrains.annotations.NotNull;

public class WorkspaceInviteAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String[] availableWorkspaces = InteractionUtil.listWorkspaces(e.getProject(), true, false);
		if(availableWorkspaces.length == 0) {
			Messages.showErrorDialog(
				"You do not own any workspaces. Ensure you own at least one!",
				"CodeMP Invite To Workspace"
			);
			return;
		}

		int choice = Messages.showChooseDialog(
			"Please choose a workspace to invite to:",
			"CodeMP Invite To Workspace",
			availableWorkspaces,
			"",
			Messages.getQuestionIcon()
		);

		String userName = Messages.showInputDialog(
			"Input name of user to invite:",
			"CodeMP Invite To Workspace",
			Messages.getQuestionIcon()
		);

		if(userName == null) {
			Messages.showErrorDialog(
				"Username cannot be null!",
				"CodeMP Invite To Workspace"
			);
			return;
		}

		InteractionUtil.inviteToWorkspace(e.getProject(), availableWorkspaces[choice], userName, null);
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabled(CodeMP.isConnected());
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.EDT;
	}
}
