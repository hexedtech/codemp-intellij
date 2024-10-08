package mp.code.intellij.util.cb;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import lombok.RequiredArgsConstructor;
import mp.code.CursorController;
import mp.code.data.Cursor;
import mp.code.exceptions.ControllerException;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.ColorUtil;
import mp.code.intellij.util.FileUtil;
import mp.code.intellij.util.InteractionUtil;

import java.awt.*;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class CursorCallback implements Consumer<CursorController> {
	private static final Executor CURSOR_EXECUTOR = Executors.newSingleThreadExecutor();
	private final Project project;

	@Override
	public void accept(CursorController controller) {
		CURSOR_EXECUTOR.execute(() -> { // necessary
			try {
				while(true) {
					Optional<Cursor> c = controller.tryRecv();
					if(c.isEmpty()) break;
					Cursor event = c.get();

					CodeMP.LOGGER.debug(String.format(
						"Cursor moved by user %s! Start pos: %dx %dy; end pos: %dx %dy in buffer %s!",
						event.user,
						event.startCol,
						event.startRow,
						event.endCol,
						event.endRow,
						event.buffer
					));

					try {
						ApplicationManager.getApplication().runReadAction(() -> {
							Editor editor = FileUtil.getActiveEditorByPath(this.project, event.buffer);
							if(editor == null) return;

							int startOffset = editor.getDocument().getLineStartOffset(event.startRow) + event.startCol;
							int endOffset = editor.getDocument().getLineStartOffset(event.endRow) + event.endCol;

							int documentLength = editor.getDocument().getTextLength();
							if(startOffset > documentLength || endOffset > documentLength) {
								CodeMP.LOGGER.debug(String.format(
									"Out of bounds cursor: start was %d, end was %d, document length was %d!",
									startOffset,
									endOffset,
									documentLength
								));
								return;
							}

							RangeHighlighter previous = CodeMP.HIGHLIGHTER_MAP.put(
								event.user,
								editor.getMarkupModel().addRangeHighlighter(
									startOffset,
									endOffset,
									HighlighterLayer.SELECTION,
									new TextAttributes(
										null,
										ColorUtil.hashColor(event.user),
										null,
										null,
										Font.PLAIN
									),
									HighlighterTargetArea.EXACT_RANGE
								)
							);

							if(previous != null) previous.dispose();
						});
					} catch(IndexOutOfBoundsException ignored) {} // don't crash over a bad cursor event
				}
			} catch(ControllerException ex) {
				InteractionUtil.notifyError(project, "Error receiving change", ex);
			}
		});
	}
}
