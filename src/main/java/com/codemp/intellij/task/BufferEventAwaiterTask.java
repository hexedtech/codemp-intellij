package com.codemp.intellij.task;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.exceptions.lib.ChannelException;
import com.codemp.intellij.exceptions.lib.DeadlockedException;
import com.codemp.intellij.jni.BufferHandler;
import com.codemp.intellij.jni.StringVec;
import com.codemp.intellij.jni.TextChangeWrapper;
import com.codemp.intellij.jni.WorkspaceHandler;
import com.codemp.intellij.util.FileUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BufferEventAwaiterTask extends Task.Backgroundable implements Disposable {
	public final Map<String, Disposable> activeBuffers;
	private final WorkspaceHandler handler;
	public BufferEventAwaiterTask(@NotNull Project project, @NotNull WorkspaceHandler handler) {
		super(project, "Awaiting CodeMP buffer events", false);
		this.activeBuffers = new ConcurrentHashMap<>();
		this.handler = handler;
	}

	@Override
	@SuppressWarnings("InfiniteLoopStatement")
	public void run(@NotNull ProgressIndicator indicator) {
		try {
			while(true) {
				StringVec buffers = new StringVec(); //jni moment
				this.activeBuffers.keySet().forEach(buffers::push);

				Optional<BufferHandler> bufferOptional = this.handler.selectBuffer(buffers, 100L);
				if(bufferOptional.isEmpty())
					continue;
				BufferHandler buffer = bufferOptional.get();

				List<TextChangeWrapper> changeList = new ArrayList<>();
				while(true) {
					Optional<TextChangeWrapper> changeOptional;
					try {
						 changeOptional = buffer.tryRecv();
					} catch(DeadlockedException e) {
						CodeMP.LOGGER.error(e.getMessage());
						continue;
					}
					if(changeOptional.isEmpty())
						break;
					TextChangeWrapper change = changeOptional.get();
					CodeMP.LOGGER.debug("Received text change {} from offset {} to {}!",
						change.getContent(), change.getStart(), change.getEnd());
					changeList.add(change);
				}

				Editor bufferEditor = FileUtil.getActiveEditorByPath(this.myProject, buffer.getName());
				ApplicationManager.getApplication().invokeLaterOnWriteThread(() ->
					ApplicationManager.getApplication().runWriteAction(() ->
						CommandProcessor.getInstance().executeCommand(
							this.myProject,
							() -> changeList.forEach((change) ->
								bufferEditor.getDocument().replaceString(
									(int) change.getStart(), (int) change.getEnd(), change.getContent())
							),
							"CodeMPBufferReceive",
							"codemp-buffer-receive", //TODO: mark this with the name
							bufferEditor.getDocument()
					)));
			}
		} catch(ChannelException ex) { //exited
			//TODO handle stop
			Disposer.dispose(this); //stopped
		}
	}

	@Override
	public void dispose() {
		this.activeBuffers.values().forEach(Disposable::dispose);
		this.activeBuffers.clear();
	}
}
