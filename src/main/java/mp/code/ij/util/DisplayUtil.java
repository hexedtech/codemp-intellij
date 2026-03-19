package mp.code.ij.util;

import mp.code.proto.WorkspaceIdentifier;

public class DisplayUtil {
	public static String[] displayableWorkspaceNames(WorkspaceIdentifier... ids) {
		String[] printableWorkspaces = new String[ids.length];
		for(int i = 0; i < ids.length; i++) {
			printableWorkspaces[i] = displayableWorkspaceName(ids[i]);
		}

		return printableWorkspaces;
	}

	public static String displayableWorkspaceName(WorkspaceIdentifier id) {
		return String.format("%s/%s", id.user, id.workspace);
	}

	public static WorkspaceIdentifier identifierFromDisplayableName(String name) {
		int firstSlash = name.indexOf('/');
		if(firstSlash == -1) {
			return null;
		}

		return new WorkspaceIdentifier(
			name.substring(0, firstSlash),
			name.substring(firstSlash + 1)
		);
	}
}
