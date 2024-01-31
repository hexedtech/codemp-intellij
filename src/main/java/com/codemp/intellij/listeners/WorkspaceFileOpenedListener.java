package com.codemp.intellij.listeners;

import com.codemp.intellij.jni.BufferHandler;
import com.codemp.intellij.jni.WorkspaceHandler;
import com.codemp.intellij.task.BufferEventAwaiterTask;
import com.codemp.intellij.util.FileUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileOpenedSyncListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.ex.FileEditorWithProvider;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WorkspaceFileOpenedListener implements FileOpenedSyncListener {

	private final WorkspaceHandler handler;
	private final BufferEventAwaiterTask task;

	public WorkspaceFileOpenedListener(WorkspaceHandler handler, BufferEventAwaiterTask task) {
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

				BufferHandler bufferHandler = this.handler.attachToBuffer(path);
				Disposable disp = Disposer.newDisposable(String.format("codemp-buffer-%s", path));
				editor.getDocument().addDocumentListener(new BufferEventListener(bufferHandler), disp);

				editor.getDocument().setText(""); //empty it so we can start receiving
				this.task.activeBuffers.put(path, disp);
			});
	}
}
