package mp.code.intellij.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Allows usage of {@link MouseListener} without implementing
 * all methods.
 */
class SimpleMouseListener implements MouseListener {
	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
