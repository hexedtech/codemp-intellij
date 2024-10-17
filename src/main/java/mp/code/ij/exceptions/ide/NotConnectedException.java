package mp.code.ij.exceptions.ide;

import mp.code.ij.exceptions.CodeMPIJException;

/**
 * Fired when trying to access the CodeMP client without first connecting
 * to a server.
 */
public class NotConnectedException extends CodeMPIJException {

	public NotConnectedException(String service) {
		super(String.format("Failed to %s, you are not connected to a server!", service));
	}
}
