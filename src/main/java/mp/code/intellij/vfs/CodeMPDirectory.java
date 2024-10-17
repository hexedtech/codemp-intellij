package mp.code.intellij.vfs;

import lombok.Getter;
import mp.code.intellij.CodeMP;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Optional;

@Getter
public class CodeMPDirectory extends CodeMPFile {

	public CodeMPDirectory(CodeMPFileSystem fileSystem, CodeMPPath path) {
		super(fileSystem, path, true);
	}

	@Override
	public boolean isWritable() {
		return false;
	}

	@Override
	public boolean isDirectory() {
		return true;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public CodeMPFile[] getChildren() {
		return CodeMP.getClient("get folder children")
			.getWorkspace(this.path.getWorkspaceName())
			.map(ws ->
				Arrays.stream(ws.searchBuffers(Optional.of(this.path.getRealPath())))
					.map(p -> new CodeMPPath(this.path.getWorkspaceName(), p))
					.map(CodeMPPath::join)
					.map(this.fileSystem::findFileByPath)
					.toArray(CodeMPFile[]::new)
			).orElseGet(() -> new CodeMPFile[0]);
	}

	@Override
	public @NotNull OutputStream getOutputStream(Object o, long l, long l1) throws IOException {
		throw new RuntimeException("WHAT FOLDER OUTPUT");
	}

	@Override
	public byte @NotNull [] contentsToByteArray() throws IOException {
		return new byte[0];
	}

	@Override
	public long getTimeStamp() {
		return 0;
	}

	@Override
	public long getLength() {
		return 0;
	}

	@Override
	public void refresh(boolean asynchronous, boolean recursive, @Nullable Runnable postRunnable) {
		for(CodeMPFile vf : this.getChildren()) {
			if(recursive || !this.isDirectory()) {
				vf.refresh(asynchronous, recursive, postRunnable);
			}
		}
	}

	@Override
	public @NotNull InputStream getInputStream() throws IOException {
		throw new RuntimeException("WHAT FOLDER INPUT");
	}

}
