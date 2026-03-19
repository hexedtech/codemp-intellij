package mp.code.ij.actions.buffer;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
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
import mp.code.Workspace;
import mp.code.exceptions.ControllerException;
import mp.code.ij.CodeMP;
import mp.code.ij.util.FileUtil;
import mp.code.ij.util.InteractionUtil;
import mp.code.ij.util.cb.BufferCallback;
import org.jetbrains.annotations.NotNull;

public class BufferAttachAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Project proj = e.getProject();

		Workspace ws = CodeMP.getActiveWorkspace();
		if(ws == null) {
			Messages.showErrorDialog(
				"Not currently in a workspace!",
				"CodeMP Buffer Attach"
			);
			return;
		}

		String[] filetree = ws.searchBuffers(null);
		int choice = Messages.showChooseDialog( // TODO: good to know it looks awful, now can i know where to go?
			"Attach to which buffer?",
			"CodeMP Buffer Attach",
			filetree,
			"",
			Messages.getQuestionIcon()
		);

		String path = filetree[choice];

		Editor editor = FileUtil.getActiveEditorByPath(proj, path);

		if(editor == null) {
			FileEditor currentEditor = FileEditorManager.getInstance(proj).getSelectedEditor();
			if(currentEditor == null) {
				Messages.showErrorDialog(
					"No file is currently open!",
					"CodeMP Buffer Attach"
				);
				return;
			}
			if(!(currentEditor instanceof TextEditor)) {
				Messages.showErrorDialog(
					"No text file is currently open!",
					"CodeMP Buffer Attach"
				);
				return;
			}
			editor = ((TextEditor) currentEditor).getEditor();
		}

		BufferController controller = InteractionUtil.bufferAttach(proj, CodeMP.getActiveWorkspace(), path);
		if(controller == null) {
			Messages.showErrorDialog(
				"An unknown error has occurred!",
				"CodeMP Buffer Attach"
			);
			return;
		}

		String remoteContent;
		try {
			remoteContent = controller.content();
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

				FileUtil.getAndRegisterBufferEquivalent(this, proj, controller);
			} catch(Exception ex) {
				throw new RuntimeException(ex);
			}
		});
		controller.callback(buf -> new BufferCallback(proj).accept(buf));
	}

	@Override
	public void update(@NotNull AnActionEvent e) {
		e.getPresentation().setEnabled(CodeMP.isInWorkspace());
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.EDT;
	}
}
