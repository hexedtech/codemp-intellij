package com.codemp.intellij.task;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.exceptions.CodeMPException;
import com.codemp.intellij.jni.CursorEventWrapper;
import com.codemp.intellij.jni.CursorHandler;
import com.codemp.intellij.util.ColorUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
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
	public void dispose() {}

	@Override
	@SuppressWarnings("InfiniteLoopStatement")
	public void run(@NotNull ProgressIndicator indicator) {
		assert myProject != null; //will never fail
		try {
			while(true) {
				CursorEventWrapper event = handler.recv();

				Editor editor = CodeMP.ACTIVE_BUFFERS.get(event.getBuffer());
				if(editor == null)
					continue;

				CodeMP.LOGGER.debug(
					"Cursor moved by user {}! Start pos: {}x {}y; end pos: {}x {}y in buffer {}!",
					event.getUser(),
					event.getStartCol(), event.getStartCol(),
					event.getEndRow(), event.getEndCol(),
					event.getBuffer());

				int startOffset = editor.getDocument().getLineStartOffset(event.getStartRow()) + event.getStartCol();
				int endOffset = editor.getDocument().getLineStartOffset(event.getEndRow()) + event.getEndCol();

				ApplicationManager.getApplication().invokeLater(() -> {
					try {
						RangeHighlighter highlighter = this.highlighterMap.get(event.getUser());
						if(highlighter != null)
							highlighter.dispose();

						this.highlighterMap.put(event.getUser(), editor
							.getMarkupModel()
							.addRangeHighlighter(
								startOffset,
								endOffset,
								HighlighterLayer.SELECTION,
								new TextAttributes(
									null,
									ColorUtil.colorFromUsername(event.getUser()),
									null,
									null,
									Font.PLAIN
								), HighlighterTargetArea.EXACT_RANGE
							));
					} catch(IllegalArgumentException ex) {
						//suppress if the cursor only exceeds length by one, it's probably just him adding something at EOF
						if(endOffset - editor.getDocument().getTextLength() != 1)
							throw ex;
					}
				});
			}
		} catch(Exception ex) { //exited
			this.highlighterMap.forEach((s, r) -> r.dispose());
			TaskManager.nullCursorTask();
			Disposer.dispose(this);
		}
	}
}
