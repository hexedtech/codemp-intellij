package mp.code.ij.util.cb;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import lombok.RequiredArgsConstructor;
import mp.code.CursorController;
import mp.code.exceptions.ControllerException;
import mp.code.ij.CodeMP;
import mp.code.ij.util.ColorUtil;
import mp.code.ij.util.FileUtil;
import mp.code.ij.util.InteractionUtil;
import mp.code.proto.CursorEvent;
import mp.code.proto.CursorPosition;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
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
					CursorEvent event = controller.tryRecv();
					if(event == null) {
						break;
					}

					CodeMP.LOGGER.debug(String.format(
						"Cursor event received in the background: %s",
						event
					));

					try {
						ApplicationManager.getApplication().runReadAction(() -> {
							Editor editor = FileUtil.getActiveEditorByPath(this.project, event.position.buffer);
							disposeOfPreviousHighlighters(event.user);
							if(editor == null) {
								return;
							}

							List<RangeHighlighter> cursor = new ArrayList<>();
							for(CursorPosition pos : event.position.cursors) {
								int startOffset = editor.getDocument().getLineStartOffset(pos.start.row) + pos.start.col;
								int endOffset = editor.getDocument().getLineStartOffset(pos.end.row) + pos.end.col;

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


								cursor.add(editor.getMarkupModel().addRangeHighlighter(
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
								));
							}

							CodeMP.HIGHLIGHTER_MAP.put(event.user, cursor);
						});
					} catch(IndexOutOfBoundsException ignored) {} // don't crash over a bad cursor event
				}
			} catch(ControllerException ex) {
				InteractionUtil.notifyError(project, "Error receiving change", ex);
			}
		});
	}

	public static void disposeOfPreviousHighlighters(String user) {
		List<RangeHighlighter> previous = CodeMP.HIGHLIGHTER_MAP.remove(user);
		if(previous != null) {
			for(RangeHighlighter rh : previous) {
				rh.dispose();
			}
		}
	}
}
