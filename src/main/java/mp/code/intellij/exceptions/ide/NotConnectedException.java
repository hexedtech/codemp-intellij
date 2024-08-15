package mp.code.intellij.exceptions.ide;

import mp.code.intellij.exceptions.CodeMPIJException;

/**
 * Fired when trying to access the CodeMP client without first connecting
 * to a server.
 */
public class NotConnectedException extends CodeMPIJException {

	public NotConnectedException(String service) {
		super(String.format("Failed to %s, you are not connected to a server!", service));
	}
}
