package mp.code.intellij.actions.buffer;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import mp.code.BufferController;
import mp.code.data.TextChange;
import mp.code.exceptions.ControllerException;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.FileUtil;
import mp.code.intellij.util.InteractionUtil;
import mp.code.intellij.util.cb.BufferCallback;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;
import java.util.OptionalLong;

public class BufferAttachAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Project proj = e.getProject();

		String[] filetree = CodeMP.getActiveWorkspace().getFileTree(Optional.empty(), false);
		int choice = Messages.showChooseDialog(
			"Attach to which buffer?",
			"CodeMP Buffer Attach",
			filetree,
			"",
			Messages.getQuestionIcon()
		);

		// TODO check out of bounds but should be guaranteed by intellij
		String path = filetree[choice];

		Editor editor = FileUtil.getActiveEditorByPath(proj, path);

		if (editor == null) {
			FileEditor currentEditor = FileEditorManager.getInstance(proj).getSelectedEditor();
			if(currentEditor == null) {
				Messages.showErrorDialog(
					"No file is currently open!",
					"CodeMP Buffer Attach"
				);
				return;
			}
			if (!(currentEditor instanceof TextEditor)) {
				Messages.showErrorDialog(
					"No text file is currently open!",
					"CodeMP Buffer Attach"
				);
				return;
			}
			editor = ((TextEditor) currentEditor).getEditor();
		}

		Optional<BufferController> controller = InteractionUtil.bufferAttach(proj, CodeMP.getActiveWorkspace(), path);
		if(controller.isEmpty()) {
			Messages.showErrorDialog(
				"An unknown error has occurred!",
				"CodeMP Buffer Attach"
			);
			return;
		}

		String remoteContent;
		try {
			remoteContent = controller.get().getContent();
		} catch (ControllerException ex) {
			throw new RuntimeException(ex);
		}
		int localContentLen = editor.getDocument().getTextLength();

		final Editor ed = editor;
		ApplicationManager.getApplication().runWriteAction(() -> {
			CommandProcessor.getInstance().executeCommand(
				proj,
				() -> ed.getDocument().replaceString(0, localContentLen, remoteContent),
				"CodeMPBufferReceive",
				"codemp-buffer-receive",
				ed.getDocument()
			);
			try {

				FileUtil.getAndRegisterBufferEquivalent(this, proj, controller.get());
			} catch(Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		controller.get().callback(buf -> new BufferCallback(proj).accept(buf));
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabled(CodeMP.isInWorkspace());
	}
}
