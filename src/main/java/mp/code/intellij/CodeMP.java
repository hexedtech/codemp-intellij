package mp.code.intellij;

import mp.code.exceptions.ConnectionException;
import mp.code.intellij.exceptions.ide.NotConnectedException;
import mp.code.intellij.workspace.IJWorkspace;
import mp.code.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CodeMP {
	public static Logger LOGGER = LoggerFactory.getLogger(CodeMP.class);
	public static final Map<String, IJWorkspace> ACTIVE_WORKSPACES =  new ConcurrentHashMap<>();
	private static Client CLIENT = null;

	public static void connect(String url, String username, String password) throws ConnectionException {
		CLIENT = Client.connect(url, username, password);
	}

	public static void disconnect() {
		CLIENT = null;
	}

	public static Client getClient(String reason) throws NotConnectedException {
		if(CLIENT == null) throw new NotConnectedException(reason);
		return CLIENT;
	}
}
