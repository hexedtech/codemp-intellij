package com.codemp.intellij.task;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.exceptions.lib.ChannelException;
import com.codemp.intellij.exceptions.lib.DeadlockedException;
import com.codemp.intellij.jni.BufferHandler;
import com.codemp.intellij.jni.CodeMPHandler;
import com.codemp.intellij.jni.StringVec;
import com.codemp.intellij.jni.TextChangeWrapper;
import com.codemp.intellij.listeners.BufferEventListener;
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
	private final Map<String, Disposable> bufferListeners = new ConcurrentHashMap<>();

	public BufferEventAwaiterTask(@NotNull Project project) {
		super(project, "Awaiting CodeMP buffer events", false);
	}

	public void registerListener(BufferHandler handler, Editor editor) {
		CodeMP.ACTIVE_BUFFERS.put(handler.getName(), editor); //mark as active
		CodeMP.ACTIVE_BUFFERS_REVERSE.put(editor, handler.getName());

		Disposable disposable = Disposer
			.newDisposable(this, String.format("codemp-buffer-%s", handler.getName()));

		editor.getDocument()
			.addDocumentListener(new BufferEventListener(handler), disposable);

		bufferListeners.put(handler.getName(), disposable);
	}

	public void unregisterListener(String name) {
		CodeMP.ACTIVE_BUFFERS_REVERSE.remove(CodeMP.ACTIVE_BUFFERS.remove(name));
		Disposable listener = this.bufferListeners.remove(name);
		if(listener != null)
			listener.dispose();
	}

	@Override
	public void dispose() {}

	@Override
	@SuppressWarnings({"InfiniteLoopStatement", "UnstableApiUsage"})
	public void run(@NotNull ProgressIndicator indicator) {
		try {
			while(true) {
				StringVec buffers = new StringVec(); //jni moment
				CodeMP.ACTIVE_BUFFERS.keySet().forEach(buffers::push);

				Optional<BufferHandler> bufferOptional = CodeMPHandler.selectBuffer(buffers, 100L);
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

				Editor bufferEditor = CodeMP.ACTIVE_BUFFERS.get(buffer.getName());
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
			TaskManager.nullBufferTask();
			Disposer.dispose(this); //stopped
		}
	}
}
