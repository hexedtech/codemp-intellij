package mp.code.intellij.actions.workspace;

import mp.code.intellij.CodeMP;
import mp.code.intellij.util.ActionUtil;
import mp.code.intellij.workspace.IJWorkspace;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import mp.code.exceptions.CodeMPException;
import org.jetbrains.annotations.NotNull;

public class WorkspaceJoinAction extends AnAction {
	public static void join(AnActionEvent e, String workspaceId, boolean silent) throws CodeMPException {
		CodeMP.ACTIVE_WORKSPACES.put(workspaceId, new IJWorkspace(
			workspaceId, CodeMP.getClient("join workspace"),
			false, e.getProject() //TODO: implement remote projects
		));

		if(!silent) ActionUtil.notify(e,
			"Success", String.format("Joined workspace %s!", workspaceId));
		CodeMP.LOGGER.debug("Joined workspace {}!", workspaceId);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String workspaceId = Messages.showInputDialog(
			"Workspace to connect to:",
			"CodeMP Join",
			Messages.getQuestionIcon());

		try {
			join(e, workspaceId, false);
		} catch(Exception ex) {
			ActionUtil.notifyError(e, String.format(
				"Failed to join workspace %s!",
				workspaceId), ex);
		}
	}
}
