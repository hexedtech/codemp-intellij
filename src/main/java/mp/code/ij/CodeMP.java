package mp.code.ij;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import mp.code.Extensions;
import mp.code.Workspace;
import mp.code.proto.Config;
import mp.code.exceptions.ConnectionException;
import mp.code.ij.exceptions.ide.NotConnectedException;
import mp.code.Client;
import mp.code.proto.WorkspaceIdentifier;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class CodeMP {
	public static final Logger LOGGER = Logger.getInstance(CodeMP.class);
	private static Client CLIENT = null;
	private static WorkspaceIdentifier ACTIVE_WORKSPACE_ID = null;

	// TODO this sucks
	public static BiMap<Path, String> BUFFER_MAPPER = Maps.synchronizedBiMap(HashBiMap.create());
	public static final Map<String, List<RangeHighlighter>> HIGHLIGHTER_MAP = new ConcurrentHashMap<>();

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
		if(!isInWorkspace()) {
			return null;
		}

		// this should never be null
		return Objects.requireNonNull(CodeMP.getClient("get workspace")
			.getWorkspace(ACTIVE_WORKSPACE_ID.user, ACTIVE_WORKSPACE_ID.workspace));
	}

	public static void attachWorkspace(WorkspaceIdentifier id) throws ConnectionException {
		Workspace ws = CodeMP.getClient("attach workspace").attachWorkspace(id.user, id.workspace);
		ACTIVE_WORKSPACE_ID = ws.id();
	}

	public static void leaveWorkspace() {
		if(!isInWorkspace()) {
			return;
		}

		CodeMP.getClient("leave workspace").leaveWorkspace(
			ACTIVE_WORKSPACE_ID.user,
			ACTIVE_WORKSPACE_ID.workspace
		);

		ACTIVE_WORKSPACE_ID = null;
	}
}
