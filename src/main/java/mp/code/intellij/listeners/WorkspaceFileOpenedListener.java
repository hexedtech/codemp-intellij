package mp.code.intellij.listeners;

import mp.code.intellij.task.BufferEventAwaiterTask;
import mp.code.intellij.util.FileUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileOpenedSyncListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.ex.FileEditorWithProvider;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import mp.code.BufferController;
import mp.code.Workspace;
import mp.code.exceptions.CodeMPException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WorkspaceFileOpenedListener implements FileOpenedSyncListener {
	private final Workspace handler;
	private final BufferEventAwaiterTask task;

	public WorkspaceFileOpenedListener(Workspace handler, BufferEventAwaiterTask task) {
		this.handler = handler;
		this.task = task;
	}

	@Override
	public void fileOpenedSync(@NotNull FileEditorManager source,
	                           @NotNull VirtualFile file,
	                           @NotNull List<FileEditorWithProvider> editorsWithProviders) {
		editorsWithProviders
			.stream()
			.map(FileEditorWithProvider::component1)
			.filter(fe -> fe instanceof TextEditor)
			.map(fe -> (TextEditor) fe)
			.map(TextEditor::getEditor)
			.forEach(editor -> {
				String path = FileUtil.getRelativePath(editor.getProject(), file);
				if(path == null) return;

				BufferController bufferController = this.getBufferForPath(path);
				Disposable disp = Disposer.newDisposable(String.format("codemp-buffer-%s", path));
				editor.getDocument().addDocumentListener(new BufferEventListener(bufferController), disp);

				editor.getDocument().setText(""); //empty it so we can start receiving
				this.task.activeBuffers.put(path, disp);
			});
	}

	/**
	 * Attach to a buffer or, if it does not exist, implicitly create it.
	 * @param path the buffer's name (which is the path relative to project root)
	 * @return the {@link BufferController} for it
	 */
	private BufferController getBufferForPath(String path) {
		try {
			return this.handler.attachToBuffer(path);
		} catch (CodeMPException ignored) {
			try {
				return this.handler.createBuffer(path);
			} catch(CodeMPException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
