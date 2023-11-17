package com.codemp.intellij.util;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry holding {@link Disposable Disposables} used within the plugin,
 * since there's no other way to keep track of them across different sections.
 * Only parentless {@link Disposable Disposables} are handled here, since
 * those with a parent will be disposed automatically.
 */
public class DisposableRegistry {
	private static final Map<String, Disposable> DISPOSABLE_MAP = new ConcurrentHashMap<>();

	public static boolean exists(String name) {
		return DISPOSABLE_MAP.containsKey(name);
	}

	public static Disposable get(String name) {
		return DISPOSABLE_MAP.get(name);
	}

	public static Disposable create(String name) {
		disposeOf(name); //get rid of existing ones, if there is one, to prevent memory leaks
		Disposable res = Disposer.newDisposable(name);
		DISPOSABLE_MAP.put(name, res);
		return res;
	}

	public static Disposable getOrCreate(String name) {
		Disposable disposable = DISPOSABLE_MAP.get(name);
		if(disposable == null)
			disposable = create(name);
		return disposable;
	}

	public static boolean track(String name, Disposable disposable) {
		boolean replaced = exists(name);
		if(replaced)
			disposeOf(name);
		DISPOSABLE_MAP.put(name, disposable);
		return replaced;
	}

	public static boolean disposeOf(String name) {
		if(exists(name)) {
			Disposable disposable = DISPOSABLE_MAP.remove(name);
			Disposer.dispose(disposable);
			return true;
		} else return false;
	}

	public static boolean disposeOf(Disposable disposable) {
		if(DISPOSABLE_MAP.containsValue(disposable)) {
			return DISPOSABLE_MAP.entrySet().removeIf(entry -> {
				if(entry.getValue().equals(disposable)) {
					Disposer.dispose(disposable, false);
					return true;
				} else return false;
			});
		} else return false;
	}
}
