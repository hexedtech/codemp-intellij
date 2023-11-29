package com.codemp.intellij.listeners;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.exceptions.CodeMPException;
import com.codemp.intellij.jni.BufferHandler;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import org.jetbrains.annotations.NotNull;

public class BufferEventListener implements DocumentListener {

	private final BufferHandler bufferHandler;

	public BufferEventListener(BufferHandler bufferHandler) {
		this.bufferHandler = bufferHandler;
	}

	@Override
	public void documentChanged(@NotNull DocumentEvent event) throws CodeMPException {
		CodeMP.LOGGER.debug("Changed {} to {} at offset {}",
			event.getOldFragment(), event.getNewFragment(), event.getOffset());

		Object group = CommandProcessor.getInstance().getCurrentCommandGroupId();
		if(group instanceof String groupString)
			if(groupString.startsWith("codemp-buffer-receive") || groupString.startsWith("codemp-buffer-sync"))
				return;

		//TODO move actions break
		int changeOffset = event.getOffset();
		CharSequence newFragment = event.getNewFragment();
		this.bufferHandler.send(changeOffset,
			changeOffset + event.getOldFragment().length(),
			newFragment.toString());
	}
}
