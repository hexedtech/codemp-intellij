package mp.code.intellij.actions;

import mp.code.intellij.CodeMP;
import mp.code.intellij.actions.workspace.WorkspaceJoinAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import mp.code.exceptions.CodeMPException;
import org.jetbrains.annotations.NotNull;

/**
 * Used exclusively to streamline debugging.
 */
public class FastForwardAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		try {
			ConnectAction.connect(e, "http://alemi.dev:50053", "", "", true);
			WorkspaceJoinAction.join(e, "glue", true);
			CodeMP.LOGGER.debug("Completed quick startup for testing!");
		} catch(CodeMPException ex) {
			throw new RuntimeException(ex);
		}
	}
}
