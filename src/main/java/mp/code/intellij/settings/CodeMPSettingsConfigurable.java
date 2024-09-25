package mp.code.intellij.settings;

import com.intellij.credentialStore.Credentials;
import com.intellij.credentialStore.OneTimeString;
import com.intellij.openapi.options.Configurable;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBFont;
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
		return (creds == null && (this.component.userNameField.getText() != null || this.component.passwordField.getPassword() != null))
			|| creds != null && (
				!Objects.equals(creds.getUserName(), this.component.userNameField.getText())
					|| !Objects.equals(creds.getPassword(), new OneTimeString(this.component.passwordField.getPassword()))
			);
	}

	@Override
	public void apply() {
		CodeMPSettings.State state = Objects.requireNonNull(CodeMPSettings.getInstance().getState());
		state.setCredentials(new Credentials(
			this.component.userNameField.getText(),
			this.component.passwordField.getPassword()
		));
	}

	@Override
	public void reset() {
		CodeMPSettings.State state = Objects.requireNonNull(CodeMPSettings.getInstance().getState());
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
		final JBTextField userNameField = new JBTextField();
		final JBPasswordField passwordField = new JBPasswordField();

		Component() {
			this.mainPanel = FormBuilder.createFormBuilder()
				.addComponent(new JBLabel("Connection").withFont(JBFont.h2().asBold()))
				.addLabeledComponent(new JBLabel("Username:"), this.userNameField, 1, false)
				.addLabeledComponent(new JBLabel("Password:"), this.passwordField, 1, false)
				.addComponentFillVertically(new JPanel(), 0)
				.getPanel();
		}
	}
}
