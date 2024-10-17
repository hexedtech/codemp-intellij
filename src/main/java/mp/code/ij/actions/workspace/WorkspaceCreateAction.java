package mp.code.ij.actions.workspace;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;

import mp.code.ij.CodeMP;
import mp.code.ij.util.InteractionUtil;
import org.jetbrains.annotations.NotNull;

public class WorkspaceCreateAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String workspaceName = Messages.showInputDialog(
			"Input name of new workspace",
			"CodeMP Create Workspace",
			Messages.getQuestionIcon()
		);

		if(workspaceName == null) {
			Messages.showErrorDialog(
				"Workspace name cannot be null!",
				"CodeMP Create Workspace"
			);
			return;
		}

		InteractionUtil.createWorkspace(e.getProject(), workspaceName, null);
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
