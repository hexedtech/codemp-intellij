package mp.code.intellij.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import mp.code.BufferController;
import mp.code.exceptions.ConnectionException;
import mp.code.intellij.CodeMP;
import mp.code.intellij.vfs.CodeMPPath;

import java.util.Arrays;
import java.util.Optional;

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

	/**
	 * Will first check if such a buffer exists.
	 * If it does, it will try to get the relevant controller and,
	 * if necessary, will attach to the buffer.
	 * @return the relevant {@link BufferController}, if it could be obtained
	 */
	public static Optional<BufferController> getRelevantBufferController(CodeMPPath path) {
		return CodeMP.getClient("buffer access")
			.getWorkspace(path.getWorkspaceName())
			.flatMap(ws -> {
				String[] matches = ws.getFileTree(Optional.of(path.getRealPath()), true);
				if(matches.length == 0) return Optional.empty();
				Optional<BufferController> controller = ws.getBuffer(path.getRealPath());
				if(controller.isPresent()) return controller;
				try {
					return Optional.of(ws.attachToBuffer(path.getRealPath()));
				} catch(ConnectionException e) {
					return Optional.empty();
				}
			});
	}
}
