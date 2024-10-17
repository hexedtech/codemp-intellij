package mp.code.intellij.actions.workspace;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

import mp.code.intellij.CodeMP;
import mp.code.intellij.util.InteractionUtil;
import org.jetbrains.annotations.NotNull;

public class WorkspaceDeleteAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String[] availableWorkspaces = InteractionUtil.listWorkspaces(e.getProject(), true, false);
		if(availableWorkspaces.length == 0) {
			Messages.showErrorDialog(
				"You do not own any workspaces. Ensure you own at least one!",
				"CodeMP Delete Workspace"
			);
			return;
		}

		int choice = Messages.showChooseDialog(
			"Please choose a workspace to delete:",
			"CodeMP Delete Workspace",
			availableWorkspaces,
			"",
			Messages.getQuestionIcon()
		);

		InteractionUtil.deleteWorkspace(e.getProject(), availableWorkspaces[choice], null);
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
