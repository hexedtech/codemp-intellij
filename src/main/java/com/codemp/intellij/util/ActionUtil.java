package com.codemp.intellij.util;

import com.codemp.intellij.exceptions.ide.BadActionEventStateException;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;

public class ActionUtil {
	public static Project getCurrentProject(AnActionEvent event) {
		Project project = event.getProject();
		if(project == null)
			throw new BadActionEventStateException("Project was null!");
		return project;
	}

	public static Editor getCurrentEditor(AnActionEvent event) {
		Editor editor = FileEditorManager.getInstance(getCurrentProject(event))
			.getSelectedTextEditor();
		if(editor == null)
			throw new BadActionEventStateException("Editor was null!");
		return editor;
	}
}
