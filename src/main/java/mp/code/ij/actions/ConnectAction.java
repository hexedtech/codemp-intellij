package mp.code.ij.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import mp.code.ij.util.InteractionUtil;
import org.jetbrains.annotations.NotNull;

public class ConnectAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		InteractionUtil.connect(e.getProject(), null);
	}
}
