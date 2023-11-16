package com.codemp.intellij.exceptions.rust;

import com.codemp.intellij.exceptions.CodeMPException;

public class InvalidStateException extends CodeMPException {
    public InvalidStateException(String message) {
        super(message);
    }
}