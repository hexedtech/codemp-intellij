package mp.code.ij.actions.workspace;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import mp.code.ij.CodeMP;
import mp.code.ij.util.InteractionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class WorkspaceAttachAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String[] availableWorkspaces = InteractionUtil.listWorkspaces(e.getProject(), true, true);
		if(availableWorkspaces.length == 0) {
			Messages.showErrorDialog(
				"There are no available workspaces. Ensure you have rights to access at least one!",
				"CodeMP Attach To Workspace"
			);
			return;
		}

		int choice = Messages.showChooseDialog(
			"Please choose a workspace to attach to:",
			"CodeMP Attach Workspace",
			availableWorkspaces,
			"",
			Messages.getQuestionIcon()
		);

		InteractionUtil.attachWorkspace(e.getProject(), availableWorkspaces[choice], null);
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
