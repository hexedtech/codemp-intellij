package com.codemp.intellij.util;

import com.codemp.intellij.exceptions.ide.BadActionEventStateException;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
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

	public static void notify(AnActionEvent event, String title, String msg) {
		Notifications.Bus.notify(new Notification(
			"CodeMP", title, msg, NotificationType.INFORMATION
		), event.getProject());
	}

	public static void notifyError(AnActionEvent event, String title, String msg) {
		Notifications.Bus.notify(new Notification(
			"CodeMP", title, msg, NotificationType.ERROR
		), event.getProject());
	}

	public static void notifyError(AnActionEvent event, String title, Throwable t) {
		Notifications.Bus.notify(new Notification(
			"CodeMP", title, t.getMessage(), NotificationType.ERROR
		), event.getProject());
	}
}
