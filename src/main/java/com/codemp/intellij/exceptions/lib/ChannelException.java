package com.codemp.intellij.exceptions.rust;

import com.codemp.intellij.exceptions.CodeMPException;

public class ChannelException extends CodeMPException {
	public ChannelException(String input) {
		super(input);
	}

	public static class Send extends ChannelException {
		public Send(String input) {
			super(input);
		}
	}

	public static class Read extends ChannelException {
		public Read(String input) {
			super(input);
		}
	}
}
