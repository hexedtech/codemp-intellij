package com.codemp.intellij.listeners;

import com.codemp.intellij.jni.BufferHandler;
import com.codemp.intellij.jni.CodeMPHandler;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import org.jetbrains.annotations.NotNull;

public class BufferEventListener implements DocumentListener {

	private final String bufferName;

	public BufferEventListener(String bufferName) {
		this.bufferName = bufferName;
	}

	@Override
	public void documentChanged(@NotNull DocumentEvent event) {
		try {
			int changeOffset = event.getOffset();
			CharSequence newFragment = event.getNewFragment();
			BufferHandler bufferHandler = CodeMPHandler.getBuffer(this.bufferName);
			bufferHandler.send(changeOffset, changeOffset + event.getOldFragment().length(),
				newFragment.toString());
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
