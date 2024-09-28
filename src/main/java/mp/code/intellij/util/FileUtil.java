package mp.code.intellij.util;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import mp.code.BufferController;
import mp.code.exceptions.ConnectionException;
import mp.code.exceptions.ControllerException;
import mp.code.intellij.CodeMP;
import mp.code.intellij.vfs.CodeMPPath;

import java.io.IOException;
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

	public static FileEditor getAndRegisterBufferEquivalent(Object requestor, Project project, BufferController buffer) throws ControllerException, IOException {
		VirtualFile contentRoot = ProjectRootManager.getInstance(project).getContentRoots()[0];
		String bufferName = buffer.getName();

		VirtualFile found = contentRoot.findFileByRelativePath(bufferName);
		if(found == null) {
			VirtualFile lastParent = contentRoot;
			String[] path = bufferName.split("/");
			for(int i = 0; i < path.length - 1; i++) {
				VirtualFile current = lastParent.findChild(path[i]);
				if(current == null) current = lastParent.createChildDirectory(requestor, path[i]);
				lastParent = current;
			}
			found = lastParent.createChildData(requestor, path[path.length - 1]);
		}

		found.setBinaryContent(buffer.getContent().getBytes());

		CodeMP.BUFFER_MAPPER.put(found.toNioPath(), bufferName);

		return FileEditorManager.getInstance(project).openEditor(
			new OpenFileDescriptor(project, found, 0),
			true
		).get(0);
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
