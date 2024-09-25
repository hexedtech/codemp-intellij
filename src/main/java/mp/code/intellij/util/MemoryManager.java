package mp.code.intellij.util;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Cleaner;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Allows association of IntelliJ {@link Disposable Disposables} with CodeMP-related
 * lifetimes (which are managed by a {@link Cleaner}).
 */
public class CodeMPMemoryManager {
	private static ClientDisposable clientDisposable = null;

	public static boolean startClientLifetime() {
		if(clientDisposable != null) return false;
		clientDisposable = new ClientDisposable();
		return true;
	}

	public static @Nullable Disposable getClientLifetime() {
		return clientDisposable;
	}

	public static boolean endClientLifetime() {
		if(clientDisposable == null) return false;
		ClientDisposable tmp = clientDisposable;
		clientDisposable = null;
		Disposer.dispose(tmp);
		return true;
	}

	public static boolean startWorkspaceLifetime(String workspace) {
		if(clientDisposable.workspaces.containsKey(workspace)) return false;
		clientDisposable.workspaces.put(workspace, new DisposableWorkspace());
		return true;
	}

	public static @Nullable Disposable getWorkspaceLifetime(String workspace) {
		return clientDisposable.workspaces.get(workspace);
	}

	public static boolean endWorkspaceLifetime(String workspace, String buffer) {
		if(clientDisposable == null) return false;
		ClientDisposable tmp = clientDisposable;
		clientDisposable = null;
		Disposer.dispose(tmp);
		return true;
	}

	public static boolean startBufferLifetime(String workspace, String buffer) {

	}

	public static @Nullable Disposable getBufferLifetime(String workspace, String buffer) {

	}

	public static boolean endBufferLifetime(String workspace, String buffer) {

	}

	private static class ClientDisposable implements Disposable {
		private final Map<String, DisposableWorkspace> workspaces = new ConcurrentHashMap<>();

		@Override
		public void dispose() {
			this.workspaces.values().forEach(Disposer::dispose);
			this.workspaces.clear();
		}
	}

	private static class DisposableWorkspace implements Disposable {
		private final Map<String, Disposable> buffers = new ConcurrentHashMap<>();

		@Override
		public void dispose() {
			this.buffers.values().forEach(Disposer::dispose);
			this.buffers.clear();
		}
	}
}
