package mp.code.intellij.actions;

import com.intellij.credentialStore.Credentials;
import mp.code.intellij.CodeMP;
import mp.code.intellij.settings.CodeMPSettings;
import mp.code.intellij.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import mp.code.exceptions.CodeMPException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ConnectAction extends AnAction {
	public static void connect(AnActionEvent e, boolean silent) throws NullPointerException, CodeMPException {
		CodeMPSettings.State state = Objects.requireNonNull(CodeMPSettings.getInstance().getState());
		Credentials creds = Objects.requireNonNull(state.getCredentials());
		CodeMP.connect(
			Objects.requireNonNull(state.getServerUrl()),
			Objects.requireNonNull(creds.getUserName()),
			Objects.requireNonNull(creds.getPasswordAsString())
		);

		if(!silent) ActionUtil.notify(e,
			"Success", String.format("Connected to %s!", state.getServerUrl()));
		CodeMP.LOGGER.debug("Connected to {}!", state.getServerUrl());
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		try {
			connect(e, false);
		} catch(NullPointerException ex) {
			ActionUtil.notifyError(e, "Invalid credentials!", "Please configure your credentials before connecting.");
		} catch(Exception exception) {
			ActionUtil.notifyError(e, "Failed to connect to server!", exception);
		}
	}
}
