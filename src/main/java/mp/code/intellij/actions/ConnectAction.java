package mp.code.intellij.actions;

import mp.code.intellij.CodeMP;
import mp.code.intellij.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import mp.code.exceptions.CodeMPException;
import org.jetbrains.annotations.NotNull;

public class ConnectAction extends AnAction {
	public static void connect(AnActionEvent e, String url, String username, String password, boolean silent) throws CodeMPException {
		System.out.printf("%s %s %s", url, username, password);
		CodeMP.connect(url, username, password);
		if(!silent) ActionUtil.notify(e,
			"Success", String.format("Connected to %s!", url));
		CodeMP.LOGGER.debug("Connected to {}!", url);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		/*
		LoginDialog dialog = new LoginDialog(e.getProject(), "Please input your login credentials!", "Connect to CodeMP server");
		if(dialog.showAndGet()) {
			try {
				connect(e, dialog.urlField.getText(), dialog.usernameField.getText(), dialog.passwordField.getText(), false);
			} catch(Exception exception) {
				ActionUtil.notifyError(
					e,
					String.format("Failed to connect to %s!", dialog.urlField.getText()),
					exception
				);
			}
		}*/
	}
}
