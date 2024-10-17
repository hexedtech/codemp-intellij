package mp.code.intellij;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import mp.code.Extensions;
import mp.code.Workspace;
import mp.code.data.Config;
import mp.code.exceptions.ConnectionException;
import mp.code.intellij.exceptions.ide.NotConnectedException;
import mp.code.Client;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CodeMP {
	public static final Logger LOGGER = Logger.getInstance(CodeMP.class);
	private static Client CLIENT = null;
	private static String ACTIVE_WORKSPACE_ID = null;

	// TODO this sucks
	public static BiMap<Path, String> BUFFER_MAPPER = Maps.synchronizedBiMap(HashBiMap.create());
	public static final Map<String, RangeHighlighter> HIGHLIGHTER_MAP = new ConcurrentHashMap<>();

	public static void connect(String username, String password) throws ConnectionException {
		CLIENT = Client.connect(new Config(username, password, "api.codemp.dev", 50053, false)); // TODO don't hardcode
		new Thread(() -> Extensions.drive(true)).start();
	}

	public static void disconnect() {
		CLIENT = null;
	}

	public static Client getClient(String reason) throws NotConnectedException {
		if(CLIENT == null) throw new NotConnectedException(reason);
		return CLIENT;
	}

	public static boolean isConnected() {
		return CLIENT != null;
	}

	public static boolean isInWorkspace() {
		return ACTIVE_WORKSPACE_ID != null;
	}

	public static Workspace getActiveWorkspace() {
		return CodeMP.getClient("get workspace").getWorkspace(ACTIVE_WORKSPACE_ID)
			.orElseThrow(IllegalStateException::new);
	}

	public static void attachWorkspace(String workspaceId) throws ConnectionException {
		CodeMP.getClient("attach workspace").attachWorkspace(workspaceId);
		ACTIVE_WORKSPACE_ID = workspaceId;
	}

	public static void leaveWorkspace() {
		CodeMP.getClient("leave workspace").leaveWorkspace(ACTIVE_WORKSPACE_ID);
		ACTIVE_WORKSPACE_ID = null;
	}
}
