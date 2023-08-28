mod error;

use std::sync::Arc;
use codemp::prelude::*;
use rifgen::rifgen_attr::generate_interface;
use crate::error::ErrorWrapper;

pub const JAVA_PACKAGE: &str = "com.codemp.intellij";
pub const JAVA_FOLDER: &str = "com/codemp/intellij";

// #[generate_interface_doc] //TODO
struct CodeMPHandler {}

impl CodeMPHandler {
	#[generate_interface(constructor)]
	fn new() -> CodeMPHandler {
		CodeMPHandler {}
	}

	#[generate_interface]
	fn connect(addr: String) {
		match CODEMP_INSTANCE.connect(&addr) {
			Ok(()) => (),
			Err(err) => ErrorWrapper(err) //.throw(env)
		}
	}

	#[generate_interface]
	fn join(session: String) -> CursorHandler {
		let controller = CODEMP_INSTANCE.join(&session)?.unwrap();
		CursorHandler { controller } //TODO error handling
		/*match CODEMP_INSTANCE.join(&session) {
			Ok(cursor) => CursorHandler { cursor },
			//Err(err) => ErrorWrapper(err)
		}*/
	}

	#[generate_interface]
	fn create(path: String) {
		CODEMP_INSTANCE.create(&path, None);
	}

	#[generate_interface]
	fn create_with_content(path: String, content: String) {
		CODEMP_INSTANCE.create(&path, Some(&content))
	}

	#[generate_interface]
	fn attach(path: String) -> BufferHandler {
		let controller = CODEMP_INSTANCE.attach(&path)?.unwrap();
		BufferHandler { controller }
	}

	#[generate_interface]
	fn get_cursor() -> CursorHandler {
		let controller = CODEMP_INSTANCE.get_cursor()?.unwrap();
		CursorHandler { controller }
	}

	#[generate_interface]
	fn get_buffer(path: String) -> BufferHandler {
		let controller = CODEMP_INSTANCE.get_buffer(&path)?.unwrap();
		BufferHandler { controller }
	}

	#[generate_interface]
	fn leave_workspace() {
		CODEMP_INSTANCE.leave_workspace()?.unwrap()
	}

	#[generate_interface]
	fn disconnect_buffer(path: String) -> bool {
		CODEMP_INSTANCE.disconnect_buffer(&path)?.unwrap();
	}
}

struct CursorHandler {
	controller: Arc<CodempCursorController>
}

struct BufferHandler {
	buffer: Arc<CodempBufferController>
}