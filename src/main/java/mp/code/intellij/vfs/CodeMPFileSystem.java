package mp.code.intellij.vfs;

import com.google.common.collect.Sets;
import com.intellij.credentialStore.Credentials;
import com.intellij.openapi.vfs.*;
import lombok.Getter;
import mp.code.BufferController;
import mp.code.Workspace;
import mp.code.data.TextChange;
import mp.code.exceptions.ConnectionException;
import mp.code.exceptions.ConnectionRemoteException;
import mp.code.exceptions.ControllerException;
import mp.code.intellij.CodeMP;
import mp.code.intellij.exceptions.ide.NotConnectedException;
import mp.code.intellij.settings.CodeMPSettings;
import mp.code.intellij.util.FileUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.BiConsumer;

/**
 * VFS implementation representing a remote CodeMP workspace.
 * TODO: KNOWN PROBLEMS
 *  - Pretty sure we need a folder registry somewhere. Doubt he tracks them itself.
 *  - Already open remote module will crash if not for that janky try-catch in {@link #findFileByPath(String)}, maybe
 *    try to connect quietly?
 */
@Getter // implements NonPhysicalFileSystem?
public class CodeMPFileSystem extends VirtualFileSystem { // implements NonPhysicalFileSystem {
	public static String PROTOCOL = "codemp";
	private final Set<VirtualFileListener> listeners;

	public CodeMPFileSystem() {
		this.listeners = Sets.newConcurrentHashSet();
	}

	@Override
	public @NonNls @NotNull String getProtocol() {
		return PROTOCOL; //TODO: should be same as KeyedLazyInstance.key wtf is that
	}

	@Override
	public @Nullable CodeMPFile findFileByPath(@NotNull @NonNls String path) {
		CodeMPPath cmpPath = new CodeMPPath(path);
		try {
			return this.fileOrFolderByPath(cmpPath);
		} catch(NotConnectedException ex) {
			try {
				CodeMPSettings.State state = Objects.requireNonNull(CodeMPSettings.getInstance().getState());
				Credentials credentials = Objects.requireNonNull(state.getCredentials());
				CodeMP.connect(
					Objects.requireNonNull(credentials.getUserName()),
					Objects.requireNonNull(credentials.getPasswordAsString())
				);
				return this.fileOrFolderByPath(cmpPath);
			} catch(ConnectionException | NullPointerException e) {
				return null;
			} // TODO this sucks
		}
	}

	private CodeMPFile fileOrFolderByPath(CodeMPPath cmpPath) {
		return null;
		/* TODO
		return CodeMP.getClient("file seek")
			.getWorkspace(cmpPath.getWorkspaceName())
			.filter(ws -> ws.getFileTree(Optional.ofNullable(cmpPath.getRealPath()), true).length != 0).map(ws -> new CodeMPFile(this, cmpPath))
			.orElseGet(() -> new CodeMPDirectory(this, cmpPath));

		 */
	}


	@Override
	public void refresh(boolean asynchronous) {
		// TODO find out if and where ij stores filetree
		// this is a no-op
	}

	@Override
	public @Nullable CodeMPFile refreshAndFindFileByPath(@NotNull String path) {
		this.refresh(false);
		return this.findFileByPath(path);
	}

	@Override
	public void addVirtualFileListener(@NotNull VirtualFileListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeVirtualFileListener(@NotNull VirtualFileListener listener) {
		this.listeners.remove(listener);
	}

	@Override
	protected void deleteFile(Object requester, @NotNull VirtualFile vFile) throws IOException {
		if(vFile instanceof CodeMPFile cmpFile) {
			try {
				Optional<Workspace> ws = CodeMP.getClient("delete file")
					.getWorkspace(cmpFile.path.getWorkspaceName());
				if(ws.isPresent()) {
					ws.get().deleteBuffer(vFile.getPath());
				} else {
					throw new IOException("failed to find workspace!"); // TODO do it better
				}
			} catch(ConnectionRemoteException e) {
				throw new IOException(e);
			}
		}
	}

	@Override
	protected void moveFile(Object requester, @NotNull VirtualFile vFile, @NotNull VirtualFile newParent) throws IOException {
		throw new RuntimeException("RENAME NOT SUPPORTED YET!"); // TODO
	}

	@Override
	protected void renameFile(Object requester, @NotNull VirtualFile vFile, @NotNull String newName) throws IOException {
		throw new RuntimeException("RENAME NOT SUPPORTED YET!"); // TODO
	}

	@Override
	protected @NotNull CodeMPFile createChildFile(Object requester, @NotNull VirtualFile vDir, @NotNull String fileName) throws IOException {
		if(vDir instanceof CodeMPDirectory parent) {
			try {
				Optional<Workspace> ws = CodeMP.getClient("delete file").getWorkspace(parent.path.getWorkspaceName());
				if(ws.isPresent()) {
					CodeMPPath newFilePath = parent.path.resolve(fileName);
					ws.get().createBuffer(newFilePath.getRealPath());
					ws.get().attachBuffer(newFilePath.getRealPath());
					return new CodeMPFile(this, newFilePath);
				} else {
					throw new IOException("failed to find workspace!"); // TODO do it better
				}
			} catch(ConnectionException e) {
				throw new IOException(e);
			}
		} else {
			throw new IOException("Can only create children in CodeMP folders!");
		}
	}

	@Override
	protected @NotNull CodeMPDirectory createChildDirectory(
		Object requester,
		@NotNull VirtualFile vDir,
		@NotNull String dirName
	) throws IOException {
		if(vDir instanceof CodeMPDirectory parent) {
			return new CodeMPDirectory(
				this,
				parent.path.resolve(dirName)
			);
		} else {
			throw new IOException("Can only create children in CodeMP folders!");
		}
	}

	@Override
	protected @NotNull CodeMPFile copyFile(Object requester, @NotNull VirtualFile virtualFile, @NotNull VirtualFile newParent, @NotNull String copyName) throws IOException {
		if(virtualFile instanceof CodeMPFile cfile) {
			try {
				CodeMPFile newFile = this.createChildFile(requester, newParent, copyName);
				BufferController oldController = FileUtil.getRelevantBufferController(cfile.path)
					.orElseThrow(() -> new IOException("Non existing buffer for old file!"));
				BufferController destinationController = FileUtil.getRelevantBufferController(cfile.path)
					.orElseThrow(() -> new IOException("Non existing buffer for new file!"));
				destinationController.send(new TextChange(0, 0, oldController.getContent()));
				return newFile;
			} catch(ControllerException ex) {
				throw new IOException(ex);
			}
		}

		throw new IOException("Bad VirtualFile type!");
	}

	@Override
	public boolean isReadOnly() {
		return false; // TODO doesnt exist yet
	}

	private void dispatchEvent(
		BiConsumer<VirtualFileListener, VirtualFileEvent> fun,
		Object requester,
		VirtualFile file,
		VirtualFile parent,
		long oldModificationStamp,
		long newModificationStamp
	) {
		this.listeners.forEach(listener -> fun.accept(listener, new VirtualFileEvent(
			requester,
			file,
			parent,
			oldModificationStamp,
			newModificationStamp
		)));
	}

	@Override
	public @Nullable Path getNioPath(@NotNull VirtualFile file) {
		return file.toNioPath();
	}


}
