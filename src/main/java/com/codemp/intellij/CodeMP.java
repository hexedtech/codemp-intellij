package com.codemp.intellij;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.SystemInfo;
import cz.adamh.utils.NativeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CodeMP {
	public static Logger LOGGER = LoggerFactory.getLogger(CodeMP.class);

	public static Map<String, Editor> ACTIVE_BUFFERS = new ConcurrentHashMap<>();
	public static Map<Editor, String> ACTIVE_BUFFERS_REVERSE = new ConcurrentHashMap<>(); //TODO jank

	private static boolean loadedLibrary = false;
	public static void loadLibrary() {
		if(!loadedLibrary) {
			try {
				if(SystemInfo.isWindows) //TODO on win for some reason it bundles it twice
					NativeUtils.loadLibraryFromJar("/codemp_intellij.dll");
				else NativeUtils.loadLibraryFromJar("/libcodemp_intellij.so");
			} catch(IOException e) {
				throw new RuntimeException(e);
			} finally {
				LOGGER.info("Loaded CodeMP library!");
				loadedLibrary = false;
			}
		}
	}
}
