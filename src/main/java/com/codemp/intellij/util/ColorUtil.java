package com.codemp.intellij.util;

import com.intellij.ui.JBColor;

import java.awt.Color;

public class ColorUtil {
	public static JBColor colorFromUsername(String username) {
		int hash = username.hashCode();

		@SuppressWarnings("all")
		Color hashColor = new Color(hash | (0xFF << 24));

		return new JBColor(hashColor, hashColor.darker());
	}
}
