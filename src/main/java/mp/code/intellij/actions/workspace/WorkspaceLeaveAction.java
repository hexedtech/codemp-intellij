package mp.code.intellij.actions.workspace;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.InteractionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WorkspaceLeaveAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String workspaceId = CodeMP.getActiveWorkspace().getWorkspaceId();
		InteractionUtil.leaveWorkspace(Objects.requireNonNull(e.getProject()), workspaceId, null);
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
