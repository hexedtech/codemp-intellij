use codemp::prelude::CodempError;
use jni::JNIEnv;
use crate::JAVA_FOLDER;

pub struct ErrorWrapper(CodempError);

impl From::<CodempError> for ErrorWrapper {
    fn from(value: CodempError) -> Self {
        ErrorWrapper(value)
    }
}

impl ErrorWrapper {
    pub(crate) fn throw(&self, mut env: JNIEnv) {
        let exception_package: String = format!("{}/exceptions", JAVA_FOLDER);
        let res = match &self.0 {
            CodempError::Transport { status, message } => env.throw_new(format!("{}/TransportException", exception_package), format!("Error {}: {}", status, message)),
            CodempError::InvalidState { msg } => env.throw_new(format!("{}/InvalidStateException", exception_package), msg),
            CodempError::Filler { message } => env.throw_new(format!("{}/CodeMPException", exception_package), message),
            CodempError::Channel { send } => {
                let class_name:String = if *send {
                    format!("{}/ChannelException/Send", exception_package)
                } else {
                    format!("{}/ChannelException/Read", exception_package)
                };
                env.throw_new(class_name, "The requested channel was closed!")
            }
        };

        if let Err(e) = res {
            panic!("An error occurred while converting a Rust error to a Java Exception: {}", e);
        }
    }
}