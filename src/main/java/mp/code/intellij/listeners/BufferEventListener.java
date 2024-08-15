package mp.code.intellij.listeners;

import mp.code.intellij.CodeMP;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import mp.code.BufferController;
import mp.code.data.TextChange;
import mp.code.exceptions.CodeMPException;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BufferEventListener implements DocumentListener {

	private final BufferController controller;

	public BufferEventListener(BufferController controller) {
		this.controller = controller;
	}

	@Override
	public void documentChanged(@NotNull DocumentEvent event) {
		CodeMP.LOGGER.debug("Changed {} to {} at offset {}",
			event.getOldFragment(), event.getNewFragment(), event.getOffset());

		Object group = CommandProcessor.getInstance().getCurrentCommandGroupId();
		if(group instanceof String groupString)
			if(groupString.startsWith("codemp-buffer-receive") || groupString.startsWith("codemp-buffer-sync"))
				return;

		//TODO move actions break
		int changeOffset = event.getOffset();
		CharSequence newFragment = event.getNewFragment();
		try {
			this.controller.send(new TextChange(
				changeOffset,
				changeOffset + event.getOldFragment().length(),
				newFragment.toString(),
				0L
			));
		} catch(CodeMPException ignored) {
			// TODO actually give a shit
		}
	}
}
