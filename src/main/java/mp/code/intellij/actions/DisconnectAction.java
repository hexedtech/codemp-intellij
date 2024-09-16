package mp.code.intellij.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.InteractionUtil;
import org.jetbrains.annotations.NotNull;

public class DisconnectAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		InteractionUtil.disconnect(e.getProject());
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
