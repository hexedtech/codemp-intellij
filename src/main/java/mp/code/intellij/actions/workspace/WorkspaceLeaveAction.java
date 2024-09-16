package mp.code.intellij.actions.workspace;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.InteractionUtil;
import org.jetbrains.annotations.NotNull;

public class WorkspaceLeaveAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String workspaceId = Messages.showInputDialog(
			"Workspace to leave:",
			"CodeMP Workspace Leave",
			Messages.getQuestionIcon());

		InteractionUtil.leaveWorkspace(e.getProject(), workspaceId);
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
