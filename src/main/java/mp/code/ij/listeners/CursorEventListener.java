package mp.code.ij.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.vfs.VirtualFile;
import mp.code.data.Selection;
import mp.code.exceptions.ControllerException;
import mp.code.ij.CodeMP;
import mp.code.ij.util.FileUtil;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CursorEventListener implements CaretListener {
	private boolean once = false;

	@Override
	public void caretPositionChanged(@NotNull CaretEvent event) {
		// TODO instead of returning, should un-set remote cursor position (once)

		Caret caret = event.getCaret();
		if(caret == null) return;

		Editor editor = event.getEditor();
		VirtualFile file = editor.getVirtualFile();
		if(file == null) return;

		try {
			if(
				Optional.ofNullable(CodeMP.BUFFER_MAPPER.get(file.toNioPath()))
					.flatMap(n -> CodeMP.getActiveWorkspace().getBuffer(n))
					.isEmpty()
			) {
				if(!this.once) {
					this.sendEmptyCursor();
					this.once = true;
				}

				return;
			}
		} catch(UnsupportedOperationException ex) {
			// probably won't be like this long term, but for now we work with real physical files
			// so converting to nio path is always legal when it's the right file
			if(!this.once) {
				this.sendEmptyCursor();
				this.once = true;
			}
			return;
		}

		this.once = false;

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
				CodeMP.getActiveWorkspace().cursor().send(new Selection(
					startPos.line,
					startPos.column,
					endPos.line,
					endPos.column,
					FileUtil.getRelativePath(editor.getProject(), editor.getVirtualFile())
				));
			} catch(ControllerException e) {
				throw new RuntimeException(e);
			}
		})).start();
	}

	// temp fix
	private void sendEmptyCursor() {
		new Thread(() -> ApplicationManager.getApplication().runReadAction(() -> {
			try {
				CodeMP.getActiveWorkspace()
					.cursor()
					.send(new Selection(0, 0, 0, 0, ""));
			} catch(ControllerException e) {
				throw new RuntimeException(e);
			}
		})).start();
	}
}
