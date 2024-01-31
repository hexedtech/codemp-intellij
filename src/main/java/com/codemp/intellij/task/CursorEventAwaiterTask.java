package com.codemp.intellij.task;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.jni.CursorEventWrapper;
import com.codemp.intellij.jni.CursorHandler;
import com.codemp.intellij.util.ColorUtil;
import com.codemp.intellij.util.FileUtil;
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
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//TODO this is janky as it shows a progress bar it doesn't use tbh
//implements disposable so i can use it as lifetime ig
public class CursorEventAwaiterTask extends Task.Backgroundable implements Disposable {
	private final CursorHandler handler;
	private final Map<String, RangeHighlighter> highlighterMap = new ConcurrentHashMap<>();

	public CursorEventAwaiterTask(@NotNull Project project, @NotNull CursorHandler handler) {
		super(project, "Awaiting CodeMP cursor events", false);
		this.handler = handler;
	}

	@Override
	@SuppressWarnings("InfiniteLoopStatement")
	public void run(@NotNull ProgressIndicator indicator) {
		while(true) {
			CursorEventWrapper event = this.handler.recv();
			Editor editor = FileUtil.getActiveEditorByPath(this.myProject, event.getBuffer());
			if(editor == null)
				continue;

			CodeMP.LOGGER.debug(
				"Cursor moved by user {}! Start pos: {}x {}y; end pos: {}x {}y in buffer {}!",
				event.getUser(),
				event.getStartCol(), event.getStartCol(),
				event.getEndRow(), event.getEndCol(),
				event.getBuffer());

			try {
				int startOffset = editor.getDocument()
					.getLineStartOffset(event.getStartRow()) + event.getStartCol();
				int endOffset = editor.getDocument()
					.getLineStartOffset(event.getEndRow()) + event.getEndCol();

				ApplicationManager.getApplication().invokeLater(() -> {
					int documentLength = editor.getDocument().getTextLength();
					if(startOffset > documentLength || endOffset > documentLength) {
						CodeMP.LOGGER.debug(
							"Out of bounds cursor: start was {}, end was {}, document length was {}!",
							startOffset, endOffset, documentLength);
						return;
					}

					RangeHighlighter previous = this.highlighterMap.put(event.getUser(), editor
						.getMarkupModel()
						.addRangeHighlighter(
							startOffset,
							endOffset,
							HighlighterLayer.SELECTION,
							new TextAttributes(
								null,
								ColorUtil.hashColor(event.getUser()),
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
