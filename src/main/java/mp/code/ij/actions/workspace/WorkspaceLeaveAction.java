package mp.code.ij.actions.workspace;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import mp.code.Workspace;
import mp.code.ij.CodeMP;
import mp.code.ij.util.InteractionUtil;
import mp.code.proto.WorkspaceIdentifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WorkspaceLeaveAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Workspace ws = CodeMP.getActiveWorkspace();
		if(ws == null) {
			Messages.showErrorDialog(
				"Not currently in a workspace!",
				"CodeMP Workspace Leave"
			);
			return;
		}

		WorkspaceIdentifier workspaceId = ws.id();
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
