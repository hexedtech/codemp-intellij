package com.codemp.intellij.exceptions.ide;

import com.codemp.intellij.exceptions.CodeMPException;

/**
 * Fired when trying to access the CodeMP client without first connecting
 * to a server.
 */
public class NotConnectedException extends CodeMPException {

	public NotConnectedException(String service) {
		super(String.format("Failed to %s, you are not connected to a server!", service));
	}
}
