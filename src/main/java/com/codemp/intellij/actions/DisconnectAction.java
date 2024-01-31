package com.codemp.intellij.actions;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class DisconnectAction extends AnAction {
	public static void disconnect(AnActionEvent e, boolean silent) {
		String url = CodeMP.getClient("disconnect").getUrl();

		CodeMP.disconnect();

		if(!silent) ActionUtil.notify(e,
			"Success", String.format("Disconnected from %s!", url));
		CodeMP.LOGGER.debug("Connected to {}!", url);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		try {
			disconnect(e, false);
		} catch(Exception ex) {
			ActionUtil.notifyError(e, "Failed to disconnect!", ex);
		}
	}
}
