package mp.code.ij.actions.buffer;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import mp.code.Workspace;
import mp.code.exceptions.ConnectionRemoteException;
import mp.code.ij.CodeMP;
import org.jetbrains.annotations.NotNull;

public class BufferCreateAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Workspace ws = CodeMP.getActiveWorkspace();
		if(ws == null) {
			Messages.showErrorDialog(
				"Not currently in a workspace!",
				"CodeMP Buffer Create"
			);
			return;
		}

		String name = Messages.showInputDialog(
			"Buffer path",
			"CodeMP Buffer Create",
			Messages.getQuestionIcon()
		);

		try {
			ws.createBuffer(name, false); // TODO ephemeral handling
			Messages.showInfoMessage("Created buffer " + name, "CodeMP Buffer Create");
		} catch (ConnectionRemoteException ex) {
			Messages.showErrorDialog("Error creating buffer: " + ex, "CodeMP Buffer Create");
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
