package mp.code.intellij.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import com.jgoodies.forms.layout.FormLayout;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.cb.BufferCallback;
import mp.code.intellij.util.FileUtil;
import mp.code.intellij.util.InteractionUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Optional;

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

	public static class CodeMPToolWindow extends JPanel {
		public CodeMPToolWindow(Project project) {
			this.draw(project);
		}

		private void redraw(Project project) {
			this.draw(project);
			this.repaint();
		}

		private void draw(Project project) {
			this.removeAll();
			switch(getWindowState()) {
				case DISCONNECTED -> {
					JButton connectButton = new JButton(new AbstractAction("Connect...") {
						@Override
						public void actionPerformed(ActionEvent e) {
							InteractionUtil.connect(project, () -> CodeMPToolWindow.this.redraw(project));
						}
					});
					this.add(connectButton);
				}
				case CONNECTED -> {
					this.setLayout(new GridLayout(0, 1));
					JTree tree = drawTree(InteractionUtil.listWorkspaces(project));
					tree.addMouseListener(new SimpleMouseListener() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(e.getClickCount() < 2) return;
							TreePath path = tree.getPathForLocation(e.getX(), e.getY());
							if(path == null) return;
							String workspaceName = path.getLastPathComponent().toString();
							InteractionUtil.joinWorkspace(
								project,
								workspaceName,
								() -> CodeMPToolWindow.this.redraw(project)
							);
						}
					});
					this.add(tree);
				}
				case JOINED -> {
					JButton createButton = new JButton(new AbstractAction("Create buffer") {
						@Override
						public void actionPerformed(ActionEvent e) {
							String bufferPath = Messages.showInputDialog(
								"Name of buffer:",
								"CodeMP Buffer Create",
								Messages.getQuestionIcon()
							);

							InteractionUtil.bufferCreate(project, bufferPath);
							CodeMPToolWindow.this.redraw(project);
						}
					});
					createButton.setSize(createButton.getPreferredSize());

					JTree tree = drawTree(CodeMP.getActiveWorkspace().getFileTree(Optional.empty(), false));
					tree.addMouseListener(new SimpleMouseListener() {
						@Override
						public void mouseClicked(MouseEvent e) {
							if(e.getClickCount() < 2) return;
							TreePath path = tree.getPathForLocation(e.getX(), e.getY());
							if(path == null) return;
							InteractionUtil.bufferAttach(
								project,
								CodeMP.getActiveWorkspace(),
								path.getLastPathComponent().toString()
							).ifPresent(controller -> {
								try {
									Thread.sleep(1000); // TODO: this sucks
								} catch(InterruptedException ignored) {}
								ApplicationManager.getApplication().runWriteAction(() -> {
									try {
										FileUtil.getAndRegisterBufferEquivalent(this, project, controller);
									} catch(Exception ex) {
										throw new RuntimeException(ex);
									}
								});
								controller.callback(buf -> new BufferCallback(project).accept(buf));
							});
						}
					});

					this.add(createButton);
					this.add(tree);
				}
			}
		}

		private JTree drawTree(String[] contents) {
			DefaultMutableTreeNode root = new DefaultMutableTreeNode();
			for(String content : contents) {
				root.add(new DefaultMutableTreeNode(content));
			}

			return new Tree(root);
		}
	}
}
