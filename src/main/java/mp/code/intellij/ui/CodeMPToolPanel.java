package mp.code.intellij.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.treeStructure.Tree;

import mp.code.Workspace;
import mp.code.intellij.CodeMP;
import mp.code.intellij.util.FileUtil;
import mp.code.intellij.util.InteractionUtil;
import mp.code.intellij.util.cb.BufferCallback;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Optional;

public class CodeMPToolPanel extends JPanel {
	public CodeMPToolPanel(Project project) {
		this.draw(project);
	}

	public void redraw(Project project) {
		this.draw(project);
		this.repaint();
	}

	private void draw(Project project) {
		this.removeAll();
		switch(CodeMPWindowFactory.getWindowState()) {
			case DISCONNECTED -> {
				JButton connectButton = new JButton(new AbstractAction("Connect...") {
					@Override
					public void actionPerformed(ActionEvent e) {
						InteractionUtil.connect(project, () -> CodeMPToolPanel.this.redraw(project));
					}
				});
				this.add(connectButton);
			}
			case CONNECTED -> {
				this.setLayout(new GridLayout(0, 1));
				JTree tree = drawTree("all workspaces", InteractionUtil.listWorkspaces(project, true, true));
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
							() -> CodeMPToolPanel.this.redraw(project)
						);
					}
				});
				this.add(tree);
			}
			case JOINED -> {
				this.setLayout(new BorderLayout(1, 0));
				JButton createButton = new JButton(new AbstractAction("Create buffer") {
					@Override
					public void actionPerformed(ActionEvent e) {
						String bufferPath = Messages.showInputDialog(
							"Name of buffer:",
							"CodeMP Buffer Create",
							Messages.getQuestionIcon()
						);

						InteractionUtil.createBuffer(project, bufferPath);
						CodeMPToolPanel.this.redraw(project);
					}
				});
				// createButton.setSize(createButton.getPreferredSize());
				this.add(createButton, BorderLayout.NORTH);

				Workspace ws = CodeMP.getActiveWorkspace();
				JTree tree = drawTree(ws.getWorkspaceId(), ws.getFileTree(Optional.empty(), false));
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
							} catch(InterruptedException ignored) {
							}
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

				this.add(tree, BorderLayout.CENTER);
			}
		}
	}

	private JTree drawTree(String workspace, String[] contents) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(workspace);
		for(String content : contents) {
			root.add(new DefaultMutableTreeNode(content));
		}

		return new Tree(root);
	}
}
