use codemp::Error;
use codemp::prelude::CodempError;

pub struct ErrorWrapper(CodempError);

impl From::<CodempError> for ErrorWrapper {
    fn from(value: CodempError) -> Self {
        ErrorWrapper(value)
    }
}

impl ErrorWrapper {
    pub fn get_error_message(&self) ->  String {
        match &self.0 {
            CodempError::Transport { status, message } =>
                format!("Error {}: {}", status, message),
            CodempError::InvalidState { msg } => msg.to_string(),
            CodempError::Filler { message } => message.to_string(),
					Error::Deadlocked => { "Error: deadlocked! (safe to retry)".to_string() }
            CodempError::Channel { send } => {
                if *send {
                    "Error while sending message on channel: the channel was closed!".to_string()
                } else {
                    "Error while reading message from channel: the channel was closed!".to_string()
                }
            }
				}
    }
}
