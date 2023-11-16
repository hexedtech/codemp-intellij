package com.codemp.intellij.exceptions.ide;

import com.codemp.intellij.exceptions.CodeMPException;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Fired when trying to use {@link com.intellij.openapi.actionSystem.AnActionEvent}'s context
 * from a state where that use is not supported.
 */
public class BadActionEventStateException extends CodeMPException {
	public BadActionEventStateException(String s) {
		super(s);
	}
}
