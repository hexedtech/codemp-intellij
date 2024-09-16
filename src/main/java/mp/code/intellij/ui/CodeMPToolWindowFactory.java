package mp.code.intellij.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.treeStructure.Tree;
import mp.code.data.TextChange;
import mp.code.exceptions.ControllerException;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.FileUtil;
import mp.code.intellij.util.InteractionUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CodeMPToolWindowFactory implements ToolWindowFactory, DumbAware {
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
								controller.callback(bufferController -> {
									ApplicationManager.getApplication().runReadAction(() -> {
										Editor editor = FileUtil.getActiveEditorByPath(project, bufferController.getName());
										ApplicationManager.getApplication().invokeLaterOnWriteThread(() -> {
											List<TextChange> changeList = new ArrayList<>();
											while(true) {
												Optional<TextChange> changeOptional;
												try {
													changeOptional = bufferController.tryRecv();
												} catch(ControllerException ex) {
													throw new RuntimeException(ex);
												}

												if(changeOptional.isEmpty())
													break;
												TextChange change = changeOptional.get();
												CodeMP.LOGGER.debug("Received text change {} from offset {} to {}!",
													change.content, change.start, change.end);
												changeList.add(change);
											}

											ApplicationManager.getApplication().runWriteAction(() ->
												CommandProcessor.getInstance().executeCommand(
													project,
													() -> changeList.forEach((change) ->
														editor.getDocument().replaceString(
															(int) change.start, (int) change.end, change.content)
													),
													"CodeMPBufferReceive",
													"codemp-buffer-receive",
													editor.getDocument()
												)
											);
										});
									});

								});
							});
						}
					});
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
