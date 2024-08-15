package mp.code.intellij.listeners;

import mp.code.intellij.CodeMP;
import mp.code.intellij.util.FileUtil;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import mp.code.CursorController;
import mp.code.data.Cursor;
import mp.code.exceptions.CodeMPException;
import org.jetbrains.annotations.NotNull;

public class CursorEventListener implements CaretListener {

	private final CursorController controller;

	public CursorEventListener(CursorController controller) {
		this.controller = controller;
	}

	@Override
	public void caretPositionChanged(@NotNull CaretEvent event) {
		Caret caret = event.getCaret();
		if(caret == null)
			return;

		VisualPosition startPos = caret.getSelectionStartPosition();
		VisualPosition endPos = caret.getSelectionEndPosition();
		CodeMP.LOGGER.debug("Caret moved from {}x {}y to {}x {}y",
			startPos.line, startPos.column, endPos.line, endPos.column
		);

		Editor editor = event.getEditor();
		try {
			this.controller.send(new Cursor(
				startPos.line,
				startPos.column,
				endPos.line,
				endPos.column,
				FileUtil.getRelativePath(editor.getProject(), editor.getVirtualFile()),
				null
			));
		} catch(CodeMPException e) {
			// TODO zzzzz
		}
	}
}
