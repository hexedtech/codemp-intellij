package mp.code.ij.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBList;
import com.intellij.ui.treeStructure.Tree;
import mp.code.BufferController;
import mp.code.Workspace;
import mp.code.ij.util.DisplayUtil;
import mp.code.proto.TextChange;
import mp.code.exceptions.ControllerException;
import mp.code.ij.CodeMP;
import mp.code.ij.util.FileUtil;
import mp.code.ij.util.InteractionUtil;
import mp.code.ij.util.cb.BufferCallback;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Objects;

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
				JTree tree = drawTree("all workspaces", DisplayUtil.displayableWorkspaceNames(InteractionUtil.listWorkspaces(project, true, true)));
				tree.addMouseListener(new SimpleMouseListener() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(e.getClickCount() < 2) return;
						TreePath path = tree.getPathForLocation(e.getX(), e.getY());
						if(path == null) return;
						String workspaceName = path.getLastPathComponent().toString();
						InteractionUtil.attachWorkspace(
							project,
							Objects.requireNonNull(DisplayUtil.identifierFromDisplayableName(workspaceName)),
							() -> CodeMPToolPanel.this.redraw(project)
						);
					}
				});
				this.add(tree);
			}
			case JOINED -> {
				this.setLayout(new BorderLayout(1, 0));
				this.add(new JButton(new AbstractAction("Share buffer") {
					@Override
					public void actionPerformed(ActionEvent e) {
						Project proj = Objects.requireNonNull(project);
						FileEditor currentEditor = FileEditorManager.getInstance(proj).getSelectedEditor();
						if(currentEditor == null) {
							Messages.showErrorDialog(
								"No file is currently open!",
								"CodeMP Buffer Share"
							);
							return;
						}

						String path = FileUtil.getRelativePath(proj, currentEditor.getFile());
						if(path == null) {
							Messages.showErrorDialog(
								"File must belong to project!",
								"CodeMP Buffer Share"
							);
							return;
						}

						InteractionUtil.createBuffer(proj, path, false); // TODO: handle ephemeral
						CodeMPToolPanel.this.redraw(project);
						BufferController controller = InteractionUtil.bufferAttach(proj, CodeMP.getActiveWorkspace(), path);
						if(controller == null) {
							Messages.showErrorDialog(
								"An unknown error has occurred!",
								"CodeMP Buffer Share"
							);
							return;
						}

						try {
							Editor ed = ((TextEditor) currentEditor).getEditor();
							controller.send(new TextChange(
								0,
								0,
								ed.getDocument().getText()
							));
							ApplicationManager.getApplication().runWriteAction(() -> {
								try {
									FileUtil.getAndRegisterBufferEquivalent(this, proj, controller);
								} catch(ControllerException | IOException ex) {
									throw new RuntimeException(ex);
								} catch(UnsupportedOperationException ignored) {}
							});
							controller.callback(buf -> new BufferCallback(proj).accept(buf));
						} catch(ControllerException ex) {
							throw new RuntimeException(ex);
						}
					}
				}), BorderLayout.NORTH);

				Workspace ws = CodeMP.getActiveWorkspace();
				JTree tree = drawTree(DisplayUtil.displayableWorkspaceName(ws.id()), ws.searchBuffers(null));
				tree.addMouseListener(new SimpleMouseListener() {
					@Override
					public void mouseClicked(MouseEvent e) {
						if(e.getClickCount() < 2) return;
						TreePath path = tree.getPathForLocation(e.getX(), e.getY());
						if(path == null) return;

						BufferController controller = InteractionUtil.bufferAttach(
							project,
							CodeMP.getActiveWorkspace(),
							path.getLastPathComponent().toString()
						);

						if(controller != null) {
							try {
								Thread.sleep(1000); // TODO: this sucks
							} catch(InterruptedException ignored) {}
							ApplicationManager.getApplication().runWriteAction(() -> {
								try {
									FileUtil.getAndRegisterBufferEquivalent(this, project, controller);
								} catch(ControllerException | IOException ex) {
									throw new RuntimeException(ex);
								} catch(UnsupportedOperationException ignored) {}
								});
							controller.callback(buf -> new BufferCallback(project).accept(buf));
						}
					}
				});

				this.add(tree, BorderLayout.CENTER);

				JList<String> userlist = new JBList<>(CodeMP.HIGHLIGHTER_MAP.keySet()); // TODO shouldn't use this
				this.add(userlist, BorderLayout.SOUTH);
			}
		}
	}

	private static JTree drawTree(String parent, String[] contents) {
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(parent);
		for(String content : contents) {
			root.add(new DefaultMutableTreeNode(content));
		}

		return new Tree(root);
	}
}
