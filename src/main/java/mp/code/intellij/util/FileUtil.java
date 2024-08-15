package mp.code.intellij.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Arrays;

public class FileUtil {
	public static String getRelativePath(Project project, VirtualFile vf) {
		if(ProjectRootManager.getInstance(project).getFileIndex().isInContent(vf))
			return Arrays.stream(ProjectRootManager.getInstance(project).getContentRoots())
				.filter(r -> VfsUtilCore.isAncestor(r, vf, false))
				.findAny()
				.map(root -> VfsUtilCore.getRelativePath(vf, root))
				.orElse(null);
		return null;
	}

	public static Editor getActiveEditorByPath(Project project, String path) {
		return Arrays.stream(FileEditorManager.getInstance(project).getAllEditors())
			.filter(fe -> fe instanceof TextEditor)
			.map(fe -> ((TextEditor) fe).getEditor())
			.filter(editor -> path.equals(FileUtil.getRelativePath(editor.getProject(), editor.getVirtualFile())))
			.findFirst()
			.orElse(null);
	}
}
