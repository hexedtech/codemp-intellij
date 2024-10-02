package mp.code.intellij.actions.buffer;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
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

public class BufferShareAction extends AnAction {
	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		Project proj = e.getProject();
		FileEditor currentEditor = FileEditorManager.getInstance(proj).getSelectedEditor();
		if(currentEditor == null) {
			Messages.showErrorDialog(
				"No file is currently open!",
				"CodeMP Buffer Share"
			);
			return;
		}

		String path = FileUtil.getRelativePath(proj, currentEditor.getFile());
		if(path == null) {
			Messages.showErrorDialog(
				"File must belong to project!",
				"CodeMP Buffer Share"
			);
			return;
		}

		InteractionUtil.createBuffer(proj, path);
		Optional<BufferController> controller = InteractionUtil.bufferAttach(proj, CodeMP.getActiveWorkspace(), path);
		if(controller.isEmpty()) {
			Messages.showErrorDialog(
				"An unknown error has occurred!",
				"CodeMP Buffer Share"
			);
			return;
		}

		try {
			controller.get().send(new TextChange(
				0,
				0,
				new String(currentEditor.getFile().contentsToByteArray()),
				OptionalLong.empty()
			));
			ApplicationManager.getApplication().runWriteAction(() -> {
				try {
					FileUtil.getAndRegisterBufferEquivalent(this, proj, controller.get());
				} catch(Exception ex) {
					throw new RuntimeException(ex);
				}
			});
			controller.get().callback(buf -> new BufferCallback(proj).accept(buf));
		} catch(ControllerException | IOException ex) {
			throw new RuntimeException(ex);
		}
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
