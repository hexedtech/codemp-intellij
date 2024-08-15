package mp.code.intellij.exceptions.ide;

import mp.code.intellij.exceptions.CodeMPIJException;

/**
 * Thrown upon failure to detach from a buffer.
 */
public class BufferDetachException extends CodeMPIJException {

	public BufferDetachException(String name) {
		super(String.format("Could not detach from buffer named \"%s\"!", name));
	}
}
