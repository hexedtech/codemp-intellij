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
@SuppressWarnings("UnusedReturnValue")
public class MemoryManager {
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
		clientDisposable.workspaces.put(workspace, new WorkspaceDisposable());
		return true;
	}

	public static @Nullable Disposable getWorkspaceLifetime(String workspace) {
		return clientDisposable.workspaces.get(workspace);
	}

	public static boolean endWorkspaceLifetime(String workspace) {
		WorkspaceDisposable ws = clientDisposable.workspaces.remove(workspace);
		if(ws == null) return false;
		Disposer.dispose(ws);
		return true;
	}

	public static boolean startBufferLifetime(String workspace, String buffer) {
		WorkspaceDisposable ws = (WorkspaceDisposable) getWorkspaceLifetime(workspace);
		if(ws == null || ws.buffers.containsKey(buffer)) return false;
		ws.buffers.put(buffer, Disposer.newDisposable());
		return true;
	}

	public static @Nullable Disposable getBufferLifetime(String workspace, String buffer) {
		WorkspaceDisposable ws = (WorkspaceDisposable) getWorkspaceLifetime(workspace);
		if(ws == null) return null;
		return ws.buffers.get(buffer);
	}

	public static boolean endBufferLifetime(String workspace, String buffer) {
		WorkspaceDisposable ws = (WorkspaceDisposable) getWorkspaceLifetime(workspace);
		if(ws == null) return false;
		Disposable buf = ws.buffers.get(buffer);
		if(buf == null) return false;
		Disposer.dispose(buf);
		return true;
	}

	private static class ClientDisposable implements Disposable {
		private final Map<String, WorkspaceDisposable> workspaces = new ConcurrentHashMap<>();
		@Override
		public void dispose() {
			this.workspaces.values().forEach(Disposer::dispose);
			this.workspaces.clear();
		}
	}

	private static class WorkspaceDisposable implements Disposable {
		private final Map<String, Disposable> buffers = new ConcurrentHashMap<>();
		@Override
		public void dispose() {
			this.buffers.values().forEach(Disposer::dispose);
			this.buffers.clear();
		}
	}
}
