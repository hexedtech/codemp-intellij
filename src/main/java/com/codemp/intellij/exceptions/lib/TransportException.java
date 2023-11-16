package com.codemp.intellij.exceptions.rust;

import com.codemp.intellij.exceptions.CodeMPException;

public class TransportException extends CodeMPException {
    public TransportException(String message) {
        super(message);
    }
}