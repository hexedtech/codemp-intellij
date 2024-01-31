package com.codemp.intellij.actions;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class ConnectAction extends AnAction {
	public static void connect(AnActionEvent e, String url, boolean silent) {
		CodeMP.connect(url);
		if(!silent) ActionUtil.notify(e,
			"Success", String.format("Connected to %s!", url));
		CodeMP.LOGGER.debug("Connected to {}!", url);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String url = Messages.showInputDialog("URL to CodeMP instance:", "CodeMP Connect",
			Messages.getQuestionIcon());
		try {
			connect(e, url, false);
		} catch(Exception ex) {
			ActionUtil.notifyError(e, String.format(
				"Failed to connect to %s!",
				url), ex);
		}
	}
}
