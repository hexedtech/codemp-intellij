package mp.code.intellij.actions.workspace;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootModificationUtil;
import com.intellij.openapi.vfs.VirtualFileManager;
import mp.code.exceptions.ConnectionException;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.ActionUtil;
import mp.code.intellij.vfs.CodeMPPath;
import mp.code.intellij.vfs.CodeMPFileSystem;
import mp.code.intellij.vfs.CodeMPFolder;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class WorkspaceJoinAction extends AnAction {
	public static void join(AnActionEvent e, String workspaceId, boolean silent) throws ConnectionException, IOException {
		CodeMP.getClient("join workspace").joinWorkspace(workspaceId);
		CodeMPFileSystem fs = (CodeMPFileSystem) VirtualFileManager.getInstance().getFileSystem(CodeMPFileSystem.PROTOCOL);
		CodeMPFolder root = new CodeMPFolder(fs, new CodeMPPath(workspaceId, Strings.EMPTY));

		Project proj = e.getProject();

		assert proj != null;
		Module someModule = ModuleManager.getInstance(proj).getModules()[0];

		ModuleRootModificationUtil.addContentRoot(someModule, root);

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
