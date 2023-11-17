package com.codemp.intellij.task;

import com.codemp.intellij.jni.CursorHandler;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;

public class TaskManager {
	private static CursorEventAwaiterTask cursorTask = null;

	//TODO in the future joining a workspace will give you a project matching remote
	public static CursorEventAwaiterTask getOrCreateCursorTask(Project project, CursorHandler handler) {
		if(cursorTask != null)
			return cursorTask;
		cursorTask = new CursorEventAwaiterTask(project, handler);
		ProgressManager.getInstance().run(cursorTask);
		return cursorTask;
	}

	public static void nullCursorTask() {
		cursorTask = null;
	}

	private static BufferEventAwaiterTask bufferTask = null;

	public static BufferEventAwaiterTask getBufferTask() {
		return bufferTask;
	}

	public static BufferEventAwaiterTask getOrCreateBufferTask(Project project) {
		if(bufferTask != null)
			return bufferTask;
		bufferTask = new BufferEventAwaiterTask(project);
		ProgressManager.getInstance().run(bufferTask);
		return bufferTask;
	}

	public static void nullBufferTask() {
		bufferTask = null;
	}
}
