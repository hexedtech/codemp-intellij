package mp.code.intellij.task;

import mp.code.intellij.CodeMP;
import mp.code.intellij.util.ColorUtil;
import mp.code.intellij.util.FileUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import mp.code.CursorController;
import mp.code.data.Cursor;
import mp.code.exceptions.CodeMPException;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//TODO this is janky as it shows a progress bar it doesn't use tbh
//implements disposable so i can use it as lifetime ig
public class CursorEventAwaiterTask extends Task.Backgroundable implements Disposable {
	private final CursorController handler;
	private final Map<String, RangeHighlighter> highlighterMap = new ConcurrentHashMap<>();

	public CursorEventAwaiterTask(@NotNull Project project, @NotNull CursorController handler) {
		super(project, "Awaiting CodeMP cursor events", false);
		this.handler = handler;
	}

	@Override
	@SuppressWarnings("InfiniteLoopStatement")
	public void run(@NotNull ProgressIndicator indicator) {
		while(true) {
			Cursor event;
			try {
				event = this.handler.recv();
			} catch(CodeMPException ex) {
				continue; // TODO proper handling
			}
			Editor editor = FileUtil.getActiveEditorByPath(this.myProject, event.buffer);
			if(editor == null)
				continue;

			CodeMP.LOGGER.debug(
				"Cursor moved by user {}! Start pos: {}x {}y; end pos: {}x {}y in buffer {}!",
				event.user,
				event.startCol, event.startRow,
				event.endCol, event.endRow,
				event.buffer
			);

			try {
				int startOffset = editor.getDocument()
					.getLineStartOffset(event.startRow) + event.startCol;
				int endOffset = editor.getDocument()
					.getLineStartOffset(event.startRow) + event.startCol;

				ApplicationManager.getApplication().invokeLater(() -> {
					int documentLength = editor.getDocument().getTextLength();
					if(startOffset > documentLength || endOffset > documentLength) {
						CodeMP.LOGGER.debug(
							"Out of bounds cursor: start was {}, end was {}, document length was {}!",
							startOffset, endOffset, documentLength);
						return;
					}

					RangeHighlighter previous = this.highlighterMap.put(event.user, editor
						.getMarkupModel()
						.addRangeHighlighter(
							startOffset,
							endOffset,
							HighlighterLayer.SELECTION,
							new TextAttributes(
								null,
								ColorUtil.hashColor(event.user),
								null,
								null,
								Font.PLAIN
							), HighlighterTargetArea.EXACT_RANGE
						));

					if(previous != null)
						previous.dispose();
				});
			} catch(IndexOutOfBoundsException ignored) {}
		}
	}

	@Override
	public void dispose() {
		this.highlighterMap.values().forEach(RangeMarker::dispose);
	}
}
