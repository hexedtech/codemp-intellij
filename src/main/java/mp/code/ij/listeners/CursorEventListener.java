package mp.code.ij.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.vfs.VirtualFile;
import mp.code.Workspace;
import mp.code.exceptions.ControllerException;
import mp.code.ij.CodeMP;
import mp.code.ij.util.FileUtil;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;

import mp.code.proto.CursorPosition;
import mp.code.proto.CursorUpdate;
import mp.code.proto.RowCol;
import org.jetbrains.annotations.NotNull;


public class CursorEventListener implements CaretListener {
	@Override
	public void caretPositionChanged(@NotNull CaretEvent event) {
		// TODO instead of returning, should un-set remote cursor position (once)

		Caret caret = event.getCaret();
		if(caret == null) return;

		Editor editor = event.getEditor();
		VirtualFile file = editor.getVirtualFile();
		if(file == null) return;

		Workspace ws = CodeMP.getActiveWorkspace();
		if(ws == null) return;

		LogicalPosition startPos = editor.offsetToLogicalPosition(caret.getSelectionStart());
		LogicalPosition endPos = editor.offsetToLogicalPosition(caret.getSelectionEnd());

		CodeMP.LOGGER.debug(String.format(
			"Caret moved from %dx %dy to %dx %dy",
			startPos.line,
			startPos.column,
			endPos.line,
			endPos.column
		));

		new Thread(() -> ApplicationManager.getApplication().runReadAction(() -> {
			try {
				ws.cursor().send(new CursorUpdate(
					FileUtil.getRelativePath(editor.getProject(), editor.getVirtualFile()),
					new CursorPosition[]{new CursorPosition(
						new RowCol(startPos.line, startPos.column),
						new RowCol(endPos.line, endPos.column)
					)}
				));
			} catch(ControllerException e) {
				throw new RuntimeException(e);
			}
		})).start();
	}
}
