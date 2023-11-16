package com.codemp.intellij.actions.buffer;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.jni.BufferHandler;
import com.codemp.intellij.jni.CodeMPHandler;
import com.codemp.intellij.jni.TextChangeWrapper;
import com.codemp.intellij.listeners.BufferEventListener;
import com.codemp.intellij.util.ActionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class BufferAttachAction extends AnAction {

	public static void attach(AnActionEvent e, String buffer, boolean silent) throws Exception {
		BufferHandler bufferHandler = CodeMPHandler.attach(buffer);
		if(!silent) Messages.showInfoMessage(String.format("Attached to buffer to %s!", buffer),
			"CodeMP Buffer Attach");
		CodeMP.LOGGER.debug("Attached to buffer to {}!", buffer);

		//register buffer change listener
		//TODO "get" the Document corresponding to buffer, for now use the current one
		BufferEventListener listener = new BufferEventListener(buffer);
		Document document = ActionUtil.getCurrentEditor(e).getDocument();
		document.addDocumentListener(listener);

		ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "Awaiting CodeMP buffer events") {
			@Override
			@SuppressWarnings({"InfiniteLoopStatement", "UnstableApiUsage"})
			public void run(@NotNull ProgressIndicator indicator) {
				while(true) {
					try {
						TextChangeWrapper event = bufferHandler.recv();
						ApplicationManager.getApplication().invokeLaterOnWriteThread(() ->
							ApplicationManager.getApplication().runWriteAction(() -> {
								CodeMP.LOGGER.debug("Received text change {} from offset {} to {}!\n",
									event.getContent(), event.getStart(), event.getEnd());
								CodeMP.LOGGER.info("is writable: {}", document.isWritable());
								document.replaceString( //TODO this doesn't work
									(int) event.getStart(),
									(int) event.getEnd(),
									event.getContent()
								);
							}));
					} catch(Exception ex) {
						throw new RuntimeException(ex);
					}
				}
			}
		});
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String buffer = Messages.showInputDialog(
			"Buffer name:",
			"Attach to CodeMP Buffer",
			Messages.getQuestionIcon());
		try {
			attach(e, buffer, false);
		} catch(Exception ex) {
			Messages.showErrorDialog(String.format(
				"Failed to attach to buffer %s: %s!",
				buffer, ex.getMessage()), "Attach to CodeMP Buffer");
		}
	}
}
