package mp.code.intellij.actions.workspace;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
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
		}

		int choice = Messages.showDialog( // TODO NOT THE ONE
			e.getProject(),
			"Please choose a workspace to delete:",
			"CodeMP Delete Workspace",
			availableWorkspaces,
			0,
			Messages.getQuestionIcon()
		);

		InteractionUtil.deleteWorkspace(e.getProject(), availableWorkspaces[choice], null);
	}
}
