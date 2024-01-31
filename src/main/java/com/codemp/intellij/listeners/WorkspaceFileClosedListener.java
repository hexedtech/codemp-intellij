package com.codemp.intellij.listeners;

import com.codemp.intellij.jni.WorkspaceHandler;
import com.codemp.intellij.task.BufferEventAwaiterTask;
import com.codemp.intellij.util.FileUtil;
import com.codemp.intellij.workspace.Workspace;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class WorkspaceFileClosedListener implements FileEditorManagerListener.Before {
	private final WorkspaceHandler handler;
	private final BufferEventAwaiterTask task;

	public WorkspaceFileClosedListener(WorkspaceHandler handler, BufferEventAwaiterTask task) {
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
