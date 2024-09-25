package mp.code.intellij.settings;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
	name = "mp.code.intellij.settings.CodeMPSettings",
	storages = @Storage("codemp.xml")
)
public class CodeMPSettings implements PersistentStateComponent<CodeMPSettings.State> {
	private State currentState = new State();
	public static CodeMPSettings getInstance() {
		return ApplicationManager.getApplication()
			.getService(CodeMPSettings.class);
	}

	@Override
	public State getState() {
		return this.currentState;
	}

	@Override
	public void loadState(@NotNull State state) {
		this.currentState = state;
	}

	@Getter
	@Setter
	public static class State {
		private static CredentialAttributes createCredentialAttributes() {
			return new CredentialAttributes(CredentialAttributesKt.generateServiceName(
				"CodeMP",
				"login"
			));
		}

		public @Nullable Credentials getCredentials() {
			CredentialAttributes attr = createCredentialAttributes();
			return PasswordSafe.getInstance().get(attr);
		}

		public void setCredentials(Credentials creds) {
			CredentialAttributes attributes = createCredentialAttributes();
			PasswordSafe.getInstance().set(attributes, creds);
		}
	}
}
