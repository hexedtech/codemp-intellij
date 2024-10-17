package mp.code.ij.actions.buffer;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import mp.code.exceptions.ConnectionRemoteException;
import mp.code.ij.CodeMP;
import org.jetbrains.annotations.NotNull;

public class BufferDeleteAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String name = Messages.showInputDialog(
			"Buffer path",
			"CdeMP Buffer Delete",
			Messages.getQuestionIcon()
		);
		try {
			CodeMP.getActiveWorkspace().deleteBuffer(name);
			Messages.showInfoMessage("Deleted buffer " + name, "CodeMP Buffer Create");
		} catch (ConnectionRemoteException ex) {
			Messages.showErrorDialog("Error deleting buffer: " + ex.toString(), "CodeMP Buffer Create");
		}
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabled(CodeMP.isInWorkspace());
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.EDT;
	}
}
