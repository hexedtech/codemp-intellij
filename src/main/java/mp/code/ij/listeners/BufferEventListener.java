package mp.code.ij.listeners;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.vfs.VirtualFile;
import lombok.SneakyThrows;
import mp.code.exceptions.ControllerException;
import mp.code.ij.CodeMP;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import mp.code.data.TextChange;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class BufferEventListener implements DocumentListener {

	@Override
	@SneakyThrows
	public void documentChanged(@NotNull DocumentEvent event) {
		CodeMP.LOGGER.debug(String.format(
			"Changed %s to %s at offset %d",
			event.getOldFragment(),
			event.getNewFragment(),
			event.getOffset()
		));

		Object group = CommandProcessor.getInstance().getCurrentCommandGroupId();
		if(group instanceof String groupString)
			if(groupString.startsWith("codemp-buffer-receive") || groupString.startsWith("codemp-buffer-sync"))
				return;

		VirtualFile file = EditorFactory.getInstance().editors(event.getDocument())
			.map(Editor::getVirtualFile)
			.filter(Objects::nonNull)
			.filter(vf -> vf.getFileSystem().getNioPath(vf) != null)
			.findFirst()
			.orElse(null);

		if(file == null || !file.isInLocalFileSystem()) return;

		Optional.ofNullable(CodeMP.BUFFER_MAPPER.get(file.toNioPath()))
			.flatMap(c -> CodeMP.getActiveWorkspace().getBuffer(c))
			.ifPresent(controller -> {
				int changeOffset = event.getOffset();
				CharSequence newFragment = event.getNewFragment();
				try {
					controller.send(new TextChange(
						changeOffset,
						changeOffset + event.getOldFragment().length(),
						newFragment.toString()
					));
				} catch(ControllerException e) {
					throw new RuntimeException(e);
				}
			});
	}
}
