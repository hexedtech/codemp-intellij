package com.codemp.intellij.listeners;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.jni.CursorHandler;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import org.jetbrains.annotations.NotNull;

public class CursorEventListener implements CaretListener {

	private final CursorHandler cursorHandler;

	public CursorEventListener(CursorHandler cursorHandler) {
		this.cursorHandler = cursorHandler;
	}

	@Override
	public void caretPositionChanged(@NotNull CaretEvent event) {
		Caret caret = event.getCaret();
		if(caret == null)
			return;

		try {
			VisualPosition startPos = caret.getSelectionStartPosition();
			VisualPosition endPos = caret.getSelectionEndPosition();
			CodeMP.LOGGER.debug("Caret moved from {}x {}y to {}x {}y",
				startPos.line, startPos.column, endPos.line, endPos.column);
			this.cursorHandler.send(
				CodeMP.ACTIVE_BUFFERS_REVERSE.get(event.getEditor()),
				startPos.line, startPos.column,
				endPos.line, endPos.column
			);
		} catch(Exception ex) {
			throw new RuntimeException(ex);
		}
	}
}
