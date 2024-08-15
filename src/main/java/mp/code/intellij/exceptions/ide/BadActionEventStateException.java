package mp.code.intellij.exceptions.ide;

import mp.code.intellij.exceptions.CodeMPIJException;

/**
 * Fired when trying to use {@link com.intellij.openapi.actionSystem.AnActionEvent}'s context
 * from a state where that use is not supported.
 */
public class BadActionEventStateException extends CodeMPIJException {
	public BadActionEventStateException(String s) {
		super(s);
	}
}
