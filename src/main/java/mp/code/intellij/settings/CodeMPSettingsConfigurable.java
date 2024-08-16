package mp.code.intellij.settings;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.credentialStore.OneTimeString;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

final class CodeMPSettingsConfigurable implements Configurable {

	private Component component;

	@Nls(capitalization = Nls.Capitalization.Title)
	@Override
	public String getDisplayName() {
		return "CodeMP";
	}

	@Override
	public JComponent getPreferredFocusedComponent() {
		return this.component.serverUrlField;
	}

	@Nullable
	@Override
	public JComponent createComponent() {
		this.component = new Component();
		return component.mainPanel;
	}

	@Override
	public boolean isModified() {
		CodeMPSettings.State state = Objects.requireNonNull(CodeMPSettings.getInstance().getState());
		Credentials creds = state.getCredentials();
		return !this.component.serverUrlField.getText().equals(state.serverUrl)
			|| (creds == null && (this.component.userNameField.getText() != null || this.component.passwordField.getPassword() != null))
			|| creds != null && (
				!Objects.equals(creds.getUserName(), this.component.userNameField.getText())
					|| !Objects.equals(creds.getPassword(), new OneTimeString(this.component.passwordField.getPassword()))
			);
	}

	@Override
	public void apply() {
		CodeMPSettings.State state = Objects.requireNonNull(CodeMPSettings.getInstance().getState());
		state.serverUrl = this.component.serverUrlField.getText();
		CredentialAttributes attributes = CodeMPSettings.createCredentialAttributes();
		PasswordSafe.getInstance().set(attributes, new Credentials(
			this.component.userNameField.getText(),
			this.component.passwordField.getPassword()
		));
	}

	@Override
	public void reset() {
		CodeMPSettings.State state = Objects.requireNonNull(CodeMPSettings.getInstance().getState());
		this.component.serverUrlField.setText(state.serverUrl);

		Credentials cred = state.getCredentials();
		if(cred != null) {
			this.component.userNameField.setText(cred.getUserName());
			this.component.passwordField.setText(cred.getPasswordAsString());
		}

	}

	@Override
	public void disposeUIResources() {
		this.component = null;
	}

	private static class Component {
		final JPanel mainPanel;
		final JBTextField serverUrlField = new JBTextField();
		final JBTextField userNameField = new JBTextField();
		final JBPasswordField passwordField = new JBPasswordField();

		Component() {
			this.mainPanel = FormBuilder.createFormBuilder()
				.addLabeledComponent(new JBLabel("Server address:"), this.serverUrlField, 1, false)
				.addLabeledComponent(new JBLabel("Username:"), this.userNameField, 1, false)
				.addLabeledComponent(new JBLabel("Password:"), this.passwordField, 1, false)
				.addComponentFillVertically(new JPanel(), 0)
				.getPanel();
		}
	}
}
