package com.codemp.intellij.listeners;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.jni.CursorHandler;
import com.codemp.intellij.util.FileUtil;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import org.jetbrains.annotations.NotNull;

public class CursorEventListener implements CaretListener {

	private final CursorHandler handler;

	public CursorEventListener(CursorHandler handler) {
		this.handler = handler;
	}

	@Override
	public void caretPositionChanged(@NotNull CaretEvent event) {
		Caret caret = event.getCaret();
		if(caret == null)
			return;

		VisualPosition startPos = caret.getSelectionStartPosition();
		VisualPosition endPos = caret.getSelectionEndPosition();
		CodeMP.LOGGER.debug("Caret moved from {}x {}y to {}x {}y",
			startPos.line, startPos.column, endPos.line, endPos.column);

		Editor editor = event.getEditor();
		this.handler.send(
			FileUtil.getRelativePath(editor.getProject(), editor.getVirtualFile()),
			startPos.line, startPos.column,
			endPos.line, endPos.column
		);
	}
}
