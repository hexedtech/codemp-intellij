package com.codemp.intellij.actions;

import com.codemp.intellij.CodeMP;
import com.codemp.intellij.jni.CodeMPHandler;
import com.codemp.intellij.jni.CursorEventWrapper;
import com.codemp.intellij.jni.CursorHandler;
import com.codemp.intellij.listeners.CursorEventListener;
import com.codemp.intellij.util.ActionUtil;
import com.codemp.intellij.util.ColorUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class JoinAction extends AnAction {

	private static final Map<String, RangeHighlighter> highlighterMap = new HashMap<>();

	public static void join(AnActionEvent e, String session, boolean silent) throws Exception {
		CursorHandler cursorHandler = CodeMPHandler.join(session);

		if(!silent) Messages.showInfoMessage(String.format("Joined session %s!", session), "CodeMP");
		else CodeMP.LOGGER.debug("Joined session {}!", session);

		Editor editor = ActionUtil.getCurrentEditor(e);

		CursorEventAwaiter task = new CursorEventAwaiter(
			e.getProject(),
			"Awaiting CodeMP cursor events",
			cursorHandler,
			editor
		);

		EditorFactory.getInstance()
			.getEventMulticaster()
			.addCaretListener(new CursorEventListener(), task);

		ProgressManager.getInstance().run(task);
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String session = Messages.showInputDialog(
			"Session to connect to:",
			"CodeMP Join",
			Messages.getQuestionIcon());

		try {
			join(e, session, false);
		} catch(Exception ex) {
			Messages.showErrorDialog(String.format(
					"Failed to join session %s: %s!",
					session,
					ex.getMessage()),
				"CodeMP Join");
		}
	}

	//TODO this is janky as it shows a progress bar it doesn't use tbh
	//implements disposable so i can use it as lifetime ig
	private static class CursorEventAwaiter extends Task.Backgroundable implements Disposable {

		private final CursorHandler handler;
		private final Editor editor;

		public CursorEventAwaiter(Project project, String title, CursorHandler handler, Editor editor) {
			super(project, title);
			this.handler = handler;
			this.editor = editor;
		}

		@Override
		public void dispose() {}

		@Override
		@SuppressWarnings("InfiniteLoopStatement")
		public void run(@NotNull ProgressIndicator indicator) {
			while(true) {
				try {
					CursorEventWrapper event = handler.recv();
					ApplicationManager.getApplication().invokeLater(() -> {
						try {
							RangeHighlighter highlighter = highlighterMap.get(event.getUser());
							if(highlighter != null)
								highlighter.dispose();

							CodeMP.LOGGER.debug(
								"Cursor moved by user {}! Start pos: {}x {}y; end pos: {}x {}y with buffer {}!",
								event.getUser(),
								event.getStartCol(), event.getStartCol(),
								event.getEndRow(), event.getEndCol(),
								event.getBuffer());

							highlighterMap.put(event.getUser(), this.editor
								.getMarkupModel()
								.addRangeHighlighter(
									this.editor.getDocument().getLineStartOffset(event.getStartRow()) + event.getStartCol(),
									this.editor.getDocument().getLineStartOffset(event.getEndRow()) + event.getEndCol(),
									HighlighterLayer.SELECTION,
									new TextAttributes(
										null,
										ColorUtil.colorFromUsername(event.getUser()),
										null,
										null,
										Font.PLAIN
									), HighlighterTargetArea.EXACT_RANGE
								));
						} catch(Exception ex) {
							throw new RuntimeException();
						}
					});
				} catch(Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}
	}
}
