package com.codemp.intellij.workspace;

import com.codemp.intellij.jni.ClientHandler;
import com.codemp.intellij.jni.WorkspaceHandler;
import com.codemp.intellij.listeners.*;
import com.codemp.intellij.task.BufferEventAwaiterTask;
import com.codemp.intellij.task.CursorEventAwaiterTask;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.fileEditor.FileOpenedSyncListener;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;

public class Workspace implements Disposable {
	public final String id;
	public final String url;
	public final boolean isRemote;
	public final WorkspaceHandler handler;
	public final Project project;
	public final BufferEventAwaiterTask bufferTask;
	public final CursorEventAwaiterTask cursorTask;

	/**
	 * The constructor, that will also take care of creating the tasks and listeners associated with it.
	 * @param id unique id of the workspace on the server
	 * @param client the {@link ClientHandler} to use
	 * @param isRemote whether the project is remote
	 * @param project the {@link Project} to use
	 */
	public Workspace(String id, ClientHandler client, boolean isRemote, Project project) {
		this.id = id;
		this.url = client.getUrl();
		this.isRemote = isRemote;
		this.handler = client.joinWorkspace(id);
		this.project = project;

		this.cursorTask = new CursorEventAwaiterTask(project, this.handler.getCursor());
		ProgressManager.getInstance().run(this.cursorTask);

		this.bufferTask = new BufferEventAwaiterTask(project, this.handler);
		ProgressManager.getInstance().run(this.bufferTask);

		// buffer listening
		MessageBusConnection conn = this.project.getMessageBus().connect(this);
		conn.subscribe(FileOpenedSyncListener.TOPIC,
			new WorkspaceFileOpenedListener(this.handler, this.bufferTask));
		conn.subscribe(FileEditorManagerListener.Before.FILE_EDITOR_MANAGER,
			new WorkspaceFileClosedListener(this.handler, this.bufferTask));

		// cursor listening
		EditorFactory.getInstance()
			.getEventMulticaster()
			.addCaretListener(new CursorEventListener(this.handler.getCursor()), this.cursorTask);
	}

	@Override
	public void dispose() {}
}
