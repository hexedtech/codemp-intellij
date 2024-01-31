package com.codemp.intellij;

import com.codemp.intellij.exceptions.ide.NotConnectedException;
import com.codemp.intellij.jni.ClientHandler;
import com.codemp.intellij.workspace.Workspace;
import com.intellij.openapi.util.SystemInfo;
import cz.adamh.utils.NativeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CodeMP {
	public static Logger LOGGER = LoggerFactory.getLogger(CodeMP.class);
	public static final Map<String, Workspace> ACTIVE_WORKSPACES =  new ConcurrentHashMap<>();
	private static ClientHandler CLIENT = null;

	public static void connect(String url) {
		CodeMP.loadLibrary(); //will only load it the first time
		CLIENT = new ClientHandler(url);
	}

	public static void disconnect() {
		CLIENT = null;
	}

	public static ClientHandler getClient(String reason) throws NotConnectedException {
		if(CLIENT == null) throw new NotConnectedException(reason);
		return CLIENT;
	}

	private static boolean loadedLibrary = false;

	public static void loadLibrary() {
		if(!loadedLibrary) {
			try {
				if(SystemInfo.isWindows)
					NativeUtils.loadLibraryFromJar("/natives/codemp_intellij.dll");
				else NativeUtils.loadLibraryFromJar("/natives/libcodemp_intellij.so");
			} catch(IOException e) {
				throw new RuntimeException(e);
			} finally {
				LOGGER.info("Loaded CodeMP library!");
				loadedLibrary = false;
			}
		}
	}
}
