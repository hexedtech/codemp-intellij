package mp.code.intellij.vfs;

import com.intellij.platform.workspace.jps.entities.ModuleEntity;
import com.intellij.platform.workspace.storage.EntityStorage;
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileIndexContributor;
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileKind;
import com.intellij.workspaceModel.core.fileIndex.WorkspaceFileSetRegistrar;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("UnstableApiUsage")
public class CodeMPFileIndexContributor implements WorkspaceFileIndexContributor<ModuleEntity> {
	@Override
	public @NotNull Class<ModuleEntity> getEntityClass() {
		return ModuleEntity.class;
	}

	@Override
	public void registerFileSets(
		@NotNull ModuleEntity moduleEntity,
		@NotNull WorkspaceFileSetRegistrar workspaceFileSetRegistrar,
		@NotNull EntityStorage entityStorage
	) {
		moduleEntity.getContentRoots().forEach(contentRootEntity -> {
			workspaceFileSetRegistrar.registerFileSet(
				contentRootEntity.getUrl(),
				WorkspaceFileKind.CONTENT,
				moduleEntity,
				null
			);
		});
	}
}
