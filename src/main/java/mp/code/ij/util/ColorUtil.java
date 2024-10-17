package mp.code.ij.util;

import com.intellij.ui.JBColor;

import java.awt.Color;

public class ColorUtil {
	public static JBColor hashColor(String username) {
		int hash = username.hashCode();
		Color hashColor = new Color(hash | (0xFF << 24));
		return new JBColor(hashColor, hashColor.darker());
	}
}
