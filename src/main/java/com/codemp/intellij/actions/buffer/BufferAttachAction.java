package com.codemp.intellij.actions.buffer;

import com.codemp.intellij.jni.BufferHandler;
import com.codemp.intellij.jni.CodeMPHandler;
import com.codemp.intellij.jni.TextChangeWrapper;
import com.codemp.intellij.listeners.BufferEventListener;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import javax.print.Doc;
import java.util.Objects;

public class BufferAttachAction extends AnAction {

	public void attach(AnActionEvent e, String buffer) throws Exception {
		BufferHandler bufferHandler = CodeMPHandler.attach(buffer);
		//Messages.showInfoMessage(String.format("Connected to %s!", url), "CodeMP");

		//register buffer change listener
		//TODO "get" the Document corresponding to buffer, for now use the current one
		BufferEventListener listener = new BufferEventListener(buffer);
		assert e.getProject() != null;
		Editor editor = FileEditorManager.getInstance(e.getProject()).getSelectedTextEditor();
		assert editor != null;
		Document document = editor.getDocument();
		document.addDocumentListener(listener);

		ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "Awaiting CodeMP buffer events") {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				while(true) {
					try {
						TextChangeWrapper event = bufferHandler.recv();
						ApplicationManager.getApplication().invokeLaterOnWriteThread(() ->
							ApplicationManager.getApplication().runWriteAction(() -> {
								System.out.printf("Received text change %s from offset %d to %d!\n",
									event.getContent(), event.getStart(), event.getEnd());
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
		System.out.printf("Created buffer %s!\n", buffer);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String buffer = Messages.showInputDialog(
			"Buffer name:",
			"Attach to CodeMP Buffer",
			Messages.getQuestionIcon());

		try {
			this.attach(e, buffer);
		} catch(Exception ex) {
			Messages.showErrorDialog(String.format(
				"Failed to attach to buffer %s: %s!",
				buffer, ex.getMessage()), "Attach to CodeMP Buffer");
		}
	}
}
