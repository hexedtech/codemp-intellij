package com.codemp.intellij;

import com.intellij.openapi.util.SystemInfo;
import cz.adamh.utils.NativeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CodeMP {
	public static Logger LOGGER = LoggerFactory.getLogger(CodeMP.class);

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
