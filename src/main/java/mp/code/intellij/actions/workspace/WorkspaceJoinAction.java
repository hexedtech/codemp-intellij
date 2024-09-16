package mp.code.intellij.actions.workspace;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.InteractionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class WorkspaceJoinAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String[] availableWorkspaces = InteractionUtil.listWorkspaces(e.getProject());
		if(availableWorkspaces.length == 0) {
			Messages.showErrorDialog(
				"There are no available workspaces. Ensure you have rights to access at least one!",
				"CodeMP Join Workspace"
			);
		}

		int choice = Messages.showDialog( // TODO NOT THE ONE
			e.getProject(),
			"Please choose a workspace to join:",
			"CodeMP Join Workspace",
			availableWorkspaces,
			0,
			Messages.getQuestionIcon()
		);

		InteractionUtil.joinWorkspace(e.getProject(), availableWorkspaces[choice], null);
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
