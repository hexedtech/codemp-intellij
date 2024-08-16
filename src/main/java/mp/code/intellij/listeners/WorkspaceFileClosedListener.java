package mp.code.intellij.listeners;

import mp.code.intellij.task.BufferEventAwaiterTask;
import mp.code.intellij.util.FileUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import mp.code.Workspace;
import org.jetbrains.annotations.NotNull;

public class WorkspaceFileClosedListener implements FileEditorManagerListener.Before {
	private final Workspace handler;
	private final BufferEventAwaiterTask task;

	public WorkspaceFileClosedListener(Workspace handler, BufferEventAwaiterTask task) {
		this.handler = handler;
		this.task = task;
	}

	@Override
	public void beforeFileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
		String path = FileUtil.getRelativePath(source.getProject(), file);
		if(path == null) return;

		Disposable disp = this.task.activeBuffers.remove(path);
		if(disp == null) return;

		this.handler.detachFromBuffer(path);
		Disposer.dispose(disp);
	}
}
