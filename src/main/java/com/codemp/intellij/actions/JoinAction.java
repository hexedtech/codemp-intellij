package com.codemp.intellij.actions;

import com.codemp.intellij.jni.CodeMPHandler;
import com.codemp.intellij.jni.CursorEventWrapper;
import com.codemp.intellij.jni.CursorHandler;
import com.codemp.intellij.listeners.CursorEventListener;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JoinAction extends AnAction {

	private final Map<String, RangeHighlighter> highlighterMap = new HashMap<>();

	private static final TextAttributes HIGHLIGHTED = new TextAttributes(
		null, JBColor.BLUE, null, null, Font.PLAIN
	);

	public void join(AnActionEvent e, String session) throws Exception {
		CursorHandler cursorHandler = CodeMPHandler.join(session);
		EditorFactory.getInstance()
			.getEventMulticaster()
			.addCaretListener(new CursorEventListener());
		//Messages.showInfoMessage(String.format("Joined %s!", session), "CodeMP");
		System.out.printf(String.format("Joined %s!\n", session));
		Editor editor = FileEditorManager.getInstance(Objects.requireNonNull(e.getProject()))
			.getSelectedTextEditor();
		assert editor != null;
		Document document = editor.getDocument();
		ProgressManager.getInstance().run(new Task.Backgroundable(e.getProject(), "Awaiting CodeMP cursor events") {
			@Override
			public void run(@NotNull ProgressIndicator indicator) {
				while(true) {
					try {
						CursorEventWrapper event = cursorHandler.recv();
						ApplicationManager.getApplication().invokeLater(() -> {
							try {
								RangeHighlighter h = highlighterMap.get(event.getUser());
								if(h != null)
									h.dispose();

								System.out.printf(
									"Cursor moved by user %s! Start pos: x%d y%d; end pos: x%d y%d with buffer %s!\n",
									event.getUser(),
									event.getStartCol(), event.getStartCol(),
									event.getEndRow(), event.getEndCol(),
									event.getBuffer());

								highlighterMap.put(event.getUser(), editor
									.getMarkupModel()
									.addRangeHighlighter(TextAttributesKey.createTextAttributesKey("codemp", HIGHLIGHTED),
										document.getLineStartOffset(event.getStartRow()) + event.getStartCol(),
										document.getLineStartOffset(event.getEndRow()) + event.getEndCol(),
										HighlighterLayer.SELECTION,
										HighlighterTargetArea.EXACT_RANGE
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
		});
	}

	@Override
	public void actionPerformed(@NotNull AnActionEvent e) {
		String session = Messages.showInputDialog(
			"Session to connect to:",
			"CodeMP Join",
			Messages.getQuestionIcon());

		try {
			this.join(e, session);
		} catch(Exception ex) {
			Messages.showErrorDialog(String.format(
					"Failed to join session %s: %s!",
					session,
					ex.getMessage()),
				"CodeMP Join");
		}
	}
}
