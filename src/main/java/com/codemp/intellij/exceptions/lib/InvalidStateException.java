package com.codemp.intellij.exceptions.lib;

import com.codemp.intellij.exceptions.CodeMPException;

public class InvalidStateException extends CodeMPException {
    public InvalidStateException(String message) {
        super(message);
    }
}
