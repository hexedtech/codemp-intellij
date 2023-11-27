package com.codemp.intellij.actions.workspace;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.jni.CodeMPHandler;
import com.codemp.intellij.jni.CursorHandler;
import com.codemp.intellij.listeners.CursorEventListener;
import com.codemp.intellij.task.CursorEventAwaiterTask;
import com.codemp.intellij.task.TaskManager;
import com.codemp.intellij.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class WorkspaceJoinAction extends AnAction {
	public static void join(AnActionEvent e, String workspace, boolean silent) {
		CursorHandler cursorHandler = CodeMPHandler.join(workspace);

		if(!silent) ActionUtil.notify(e,
			"Success", String.format("Joined workspace %s!", workspace));
		CodeMP.LOGGER.debug("Joined workspace {}!", workspace);

		CursorEventAwaiterTask task = TaskManager
			.getOrCreateCursorTask(ActionUtil.getCurrentProject(e), cursorHandler);

		EditorFactory.getInstance()
			.getEventMulticaster()
			.addCaretListener(new CursorEventListener(cursorHandler), task);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String session = Messages.showInputDialog(
			"Session to connect to:",
			"CodeMP Join",
			Messages.getQuestionIcon());

		try {
			join(e, session, false);
		} catch(Exception ex) {
			ActionUtil.notifyError(e, String.format(
				"Failed to join session %s!",
				session), ex);
		}
	}
}
