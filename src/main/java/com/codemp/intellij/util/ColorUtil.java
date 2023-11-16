package com.codemp.intellij.util;

import com.intellij.ui.JBColor;

import java.awt.Color;

public class ColorUtil {
	public static JBColor colorFromUsername(String username) {
		int hash = username.hashCode();

		Color hashColor = Color.decode( //Integer.toHexString(((hash >> 24) & 0xFF)) +
			Integer.toHexString(((hash >> 16) & 0xFF)) +
			Integer.toHexString(((hash >> 8) & 0xFF)) +
			Integer.toHexString((hash & 0xFF))
		);

		return new JBColor(hashColor, hashColor.darker());
	}
}
