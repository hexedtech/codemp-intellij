package mp.code.ij.vfs;

import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mp.code.exceptions.ControllerException;
import mp.code.ij.util.FileUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public class CodeMPFile extends VirtualFile {
	protected final CodeMPFileSystem fileSystem;
	protected final CodeMPPath path;
	protected final boolean isDirectory; // TODO exists ONLY for ez debugging, remove afterwards

	public CodeMPFile(CodeMPFileSystem fs, CodeMPPath p) {
		this(fs, p, false);
	}

	@Override
	public @NotNull String getName() {
		return this.path.getFileName();
	}

	@Override
	public @NonNls @NotNull String getPath() {
		return this.path.join();
	}

	@Override
	public boolean isWritable() {
		return true; // TODO permissions!
	}

	@Override
	public boolean isDirectory() {
		return false; // TODO ????
	}

	@Override
	public boolean isValid() {
		return false;
		/* TODO
		return CodeMP.getClient("validity check")
			.getWorkspace(this.path.getWorkspaceName())
			.map(ws -> ws.getBuffer(this.path.getRealPath()).isPresent())
			.map(ws -> ws.getFileTree(Optional.of(this.path.getRealPath()), true))
			.map(buf -> buf.length != 0)
			.orElse(false);
		 */
	}

	@Override
	public @Nullable CodeMPDirectory getParent() {
		return this.path.getParent()
			.map(parent -> new CodeMPDirectory(this.fileSystem, parent))
			.orElse(null);
	}

	@Override
	public CodeMPFile[] getChildren() {
		return null;
	}

	@Override
	public @NotNull OutputStream getOutputStream(Object requester, long newModificationStamp, long newTimeStamp) throws IOException {
		throw new RuntimeException("WHAT OUTPUT");
	}

	@Override
	public byte @NotNull [] contentsToByteArray() throws IOException {
		return FileUtil.getRelevantBufferController(this.path).flatMap(c -> {
			try {
				return Optional.of(c.getContent().getBytes());
			} catch(ControllerException e) {
				return Optional.empty();
			}
		}).orElseThrow(() -> new IOException("Buffer " + this.path.join() + "did not exist or was inaccessible!"));
	}

	@Override
	public long getTimeStamp() {
		return System.currentTimeMillis();
	}

	@Override
	public long getLength() {
		try {
			return this.contentsToByteArray().length;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void refresh(boolean asynchronous, boolean recursive, @Nullable Runnable postRunnable) {
		// TODO
	}

	@Override
	public @NotNull InputStream getInputStream() throws IOException {
		throw new RuntimeException("WHAT INPUT");
	}

	@Override
	public @NotNull Path toNioPath() {
		return this.path.toNioPath();
	}
}
