package mp.code.intellij.vfs;

import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import mp.code.exceptions.ControllerException;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.FileUtil;
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

	@Override
	public @NotNull @NlsSafe String getName() {
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
		return CodeMP.getClient("validity check")
			.getWorkspace(this.path.getWorkspaceName())
			.map(ws -> ws.getFileTree(Optional.of(this.path.getRealPath())))
			.map(buf -> buf.length != 0)
			.orElse(false);
	}

	@Override
	public @Nullable CodeMPFolder getParent() {
		return this.path.getParent()
			.map(parent -> new CodeMPFolder(this.fileSystem, parent))
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
