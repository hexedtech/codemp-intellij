package mp.code.intellij.vfs;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A utility class representing a path as implemented in CodeMP.
 * To represent them in an IntelliJ-compatible way, we use the workspace name
 * as "root folder" of all workspace contents.
 * Thus, a CodeMP URI looks like this: <code>codemp://[workspace]/[path]</code>.
 * This helper class manages just that.
 */
@Getter @Setter
public class CodeMPPath {
	/**
	 * The name of the workspace that contains this path.
	 */
	private final String workspaceName;
	/**
	 * The real path. May never be null, but may be empty.
	 * It is guaranteed to not have any trailing slashes.
	 */
	private final String realPath;

	/**
	 * Builds a new {@link CodeMPPath} from its separate components.
	 * @param workspaceName the name of the workspace
	 * @param realPath the name of the underlying path
	 */
	public CodeMPPath(String workspaceName, String realPath) {
		this.workspaceName = workspaceName;
		if(!realPath.isEmpty()) realPath = stripTrailingSlashes(realPath);

		this.realPath = realPath;
	}

	/**
	 * Builds a new {@link CodeMPPath} from a unified path containing both the real path and
	 * the workspace name.
	 * @param pathWithWorkspace the unified path
	 */
	public CodeMPPath(String pathWithWorkspace) {
		this.workspaceName = extractWorkspace(pathWithWorkspace);
		this.realPath = extractRealPath(pathWithWorkspace);
	}

	/**
	 * Joins back into a single string workspace name and path.
	 * @return the resulting string
	 */
	public String join() {
		return this.workspaceName + '/' + this.realPath;
	}

	/**
	 * Recovers just the name of the current file.
	 * @return a string containing the name
	 */
	public String getFileName() {
		int lastSlashPos = this.realPath.lastIndexOf('/');
		if(lastSlashPos == -1) return this.realPath;
		else return this.realPath.substring(lastSlashPos + 1, this.realPath.length() - 1);
	}

	/**
	 * Gets the parent, if it is present.
	 * @return the parent
	 */
	public Optional<CodeMPPath> getParent() {
		int lastSlash = this.realPath.lastIndexOf('/');
		if(this.realPath.isEmpty()) return Optional.empty();
		else if(lastSlash == -1) return Optional.of(new CodeMPPath(this.workspaceName, ""));
		else return Optional.of(new CodeMPPath(
			this.workspaceName,
			this.realPath.substring(0, lastSlash)
		));
	}

	/**
	 * Resolves one or multiple children against this, assuming that this
	 * path represents a folder.
	 * @param firstChild the first, mandatory child to resolve against
	 * @param children other, eventual children
	 * @return the build {@link CodeMPPath}
	 */
	public CodeMPPath resolve(String firstChild, String... children) {
		StringBuilder pathBuilder = new StringBuilder(this.realPath)
			.append('/')
			.append(stripTrailingSlashes(firstChild));
		if(children != null)
			for(String c : children)
				pathBuilder.append('/').append(stripTrailingSlashes(c));
		return new CodeMPPath(
			this.workspaceName,
			pathBuilder.toString()
		);
	}

	/**
	 * Converts this to a {@link Path}, accounting for system differences in separator.
	 * @return the built {@link Path}
	 */
	public @NotNull Path toNioPath() {
		String currentSystemSeparator = FileSystems.getDefault().getSeparator();
		return Path.of(this.realPath.replace("/", currentSystemSeparator));
	}

	private static String extractWorkspace(@NotNull String path) {
		int firstSlashPosition = path.indexOf('/');
		if(firstSlashPosition == -1) return path;
		return path.substring(0, path.indexOf('/'));
	}

	private static String extractRealPath(@NotNull String path) {
		return path.substring(path.indexOf('/') + 1);
	}

	private static String stripTrailingSlashes(@NotNull String s) {
		while(s.charAt(s.length() - 1) == '/') s = s.substring(0, s.length() - 1);
		return s;
	}
}
