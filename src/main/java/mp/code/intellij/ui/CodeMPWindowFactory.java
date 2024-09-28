package mp.code.intellij.ui;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import mp.code.intellij.CodeMP;
import org.jetbrains.annotations.NotNull;

public class CodeMPWindowFactory implements ToolWindowFactory, DumbAware {
	@Override
	public void createToolWindowContent(
		@NotNull Project project,
		@NotNull ToolWindow toolWindow
	) {
		ContentFactory contentFactory = ContentFactory.getInstance();
		Content content = contentFactory.createContent(
			new CodeMPToolWindow(project),
			"",
			false
		);

		toolWindow.getContentManager().addContent(content);
	}

	@Override
	public boolean shouldBeAvailable(@NotNull Project project) {
		return true;
	}

	public static WindowState getWindowState() {
		if(!CodeMP.isConnected()) return WindowState.DISCONNECTED;
		if(!CodeMP.isInWorkspace()) return WindowState.CONNECTED;
		return WindowState.JOINED;
	}

	public enum WindowState {
		DISCONNECTED,
		CONNECTED,
		JOINED
	}
}
