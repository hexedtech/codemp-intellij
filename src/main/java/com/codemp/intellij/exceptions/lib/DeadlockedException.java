package com.codemp.intellij.exceptions.lib;

import com.codemp.intellij.exceptions.CodeMPException;

public class DeadlockedException extends CodeMPException {
	public DeadlockedException(String s) {
		super(s);
	}
}
