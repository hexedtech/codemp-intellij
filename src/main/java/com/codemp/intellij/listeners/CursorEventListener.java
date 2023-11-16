package com.codemp.intellij.listeners;

import com.codemp.intellij.jni.CodeMPHandler;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import org.jetbrains.annotations.NotNull;

public class CursorEventListener implements CaretListener {
	@Override
	public void caretPositionChanged(@NotNull CaretEvent event) {
		System.out.println("called!");
		Caret caret = event.getCaret();
		if(caret == null)
			return;
		System.out.println("valid caret!");

		try {
			VisualPosition startPos = caret.getSelectionStartPosition();
			VisualPosition endPos = caret.getSelectionEndPosition();
			System.out.printf("start %dx %dy end %dx %dy", startPos.line, startPos.column, endPos.line, endPos.column);
			CodeMPHandler.getCursor().send(
				"", startPos.line, startPos.column, endPos.line, endPos.column
			);
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
