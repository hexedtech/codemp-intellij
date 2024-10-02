package mp.code.intellij.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.CoroutinesKt;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.SlowOperations;
import lombok.SneakyThrows;
import mp.code.exceptions.ControllerException;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.FileUtil;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.VisualPosition;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import mp.code.data.Cursor;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

public class CursorEventListener implements CaretListener {

	@Override
	@SneakyThrows
	public void caretPositionChanged(@NotNull CaretEvent event) {
		// TODO instead of returning, should un-set remote cursor position (once)

		Caret caret = event.getCaret();
		if (caret == null) return;

		VirtualFile file = event.getEditor().getVirtualFile();
		if (file == null) return;

		Path nioPath = file.toNioPath();
		if (nioPath == null) return;

		String buffer_name = CodeMP.BUFFER_MAPPER.get(nioPath);
		if (buffer_name == null) return;

		if(CodeMP.getActiveWorkspace().getBuffer(buffer_name).isEmpty()) return;

		VisualPosition startPos = caret.getSelectionStartPosition();
		VisualPosition endPos = caret.getSelectionEndPosition();
		CodeMP.LOGGER.debug("Caret moved from {}x {}y to {}x {}y",
			startPos.line, startPos.column, endPos.line, endPos.column
		);

		new Thread(() -> {
			ApplicationManager.getApplication().runReadAction(() -> {
				Editor editor = event.getEditor();
				try {
					CodeMP.getActiveWorkspace().getCursor().send(new Cursor(
						startPos.line,
						startPos.column,
						endPos.line,
						endPos.column,
						FileUtil.getRelativePath(editor.getProject(), editor.getVirtualFile()),
						null
					));
				} catch(ControllerException e) {
					throw new RuntimeException(e);
				}
			});
		}).start();
	}
}
