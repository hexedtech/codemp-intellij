package mp.code.intellij.ui;

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
import mp.code.data.TextChange;
import mp.code.exceptions.ControllerException;
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
import java.io.IOException;
import java.util.Objects;
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
						InteractionUtil.attachWorkspace(
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

						InteractionUtil.createBuffer(proj, path);
						CodeMPToolPanel.this.redraw(project);
						Optional<BufferController> controller = InteractionUtil.bufferAttach(proj, CodeMP.getActiveWorkspace(), path);
						if(controller.isEmpty()) {
							Messages.showErrorDialog(
								"An unknown error has occurred!",
								"CodeMP Buffer Share"
							);
							return;
						}

						try {
							Editor ed = ((TextEditor) currentEditor).getEditor();
							controller.get().send(new TextChange(
								0,
								0,
								ed.getDocument().getText()
							));
							ApplicationManager.getApplication().runWriteAction(() -> {
								try {
									FileUtil.getAndRegisterBufferEquivalent(this, proj, controller.get());
								} catch(ControllerException | IOException ex) {
									throw new RuntimeException(ex);
								} catch(UnsupportedOperationException ignored) {}
							});
							controller.get().callback(buf -> new BufferCallback(proj).accept(buf));
						} catch(ControllerException ex) {
							throw new RuntimeException(ex);
						}
					}
				}), BorderLayout.NORTH);

				Workspace ws = CodeMP.getActiveWorkspace();
				JTree tree = drawTree(ws.id(), ws.searchBuffers(Optional.empty()));
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
								} catch(ControllerException | IOException ex) {
									throw new RuntimeException(ex);
								} catch(UnsupportedOperationException ignored) {}
								});
							controller.callback(buf -> new BufferCallback(project).accept(buf));
						});
					}
				});

				this.add(tree, BorderLayout.CENTER);

				JList<String> userlist = new JBList<>(CodeMP.HIGHLIGHTER_MAP.keySet()); // TODO shouldn't use this
				this.add(userlist, BorderLayout.SOUTH);
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
