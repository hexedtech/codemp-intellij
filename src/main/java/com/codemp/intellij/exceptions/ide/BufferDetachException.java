package com.codemp.intellij.exceptions.ide;

import com.codemp.intellij.exceptions.CodeMPException;

/**
 * Thrown upon failure to detach from a buffer.
 */
public class BufferDetachException extends CodeMPException {

	public BufferDetachException(String name) {
		super(String.format("Could not detach from buffer named \"%s\"!", name));
	}
}
