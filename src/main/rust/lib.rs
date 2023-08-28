mod error;

use std::ffi::c_char;
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
	async fn connect(addr: String) {
		CODEMP_INSTANCE.connect(&addr).await;
		/*match CODEMP_INSTANCE.connect(&addr) {
			Ok(()) => (),
			Err(err) => ErrorWrapper(err) //.throw(env)
		}*/
	}

	#[generate_interface]
	async fn join(session: String) -> CursorHandler {
		let controller = CODEMP_INSTANCE.join(&session).await.unwrap();
		CursorHandler { cursor: Some(controller) } //TODO error handling
		/*match CODEMP_INSTANCE.join(&session) {
			Ok(cursor) => CursorHandler { cursor },
			//Err(err) => ErrorWrapper(err)
		}*/
	}

	#[generate_interface]
	async fn create(path: String) {
		CODEMP_INSTANCE.create(&path, None).await;
	}

	#[generate_interface]
	async fn create_with_content(path: String, content: String) {
		CODEMP_INSTANCE.create(&path, Some(&content)).await;
	}

	#[generate_interface]
	async fn attach(path: String) -> BufferHandler {
		let controller = CODEMP_INSTANCE.attach(&path).await.unwrap();
		BufferHandler { buffer: Some(controller) }
	}

	#[generate_interface]
	async fn get_cursor() -> CursorHandler {
		let controller = CODEMP_INSTANCE.get_cursor().await.unwrap();
		CursorHandler { cursor: Some(controller) }
	}

	#[generate_interface]
	async fn get_buffer(path: String) -> BufferHandler {
		let controller = CODEMP_INSTANCE.get_buffer(&path).await.unwrap();
		BufferHandler { buffer: Some(controller) }
	}

	#[generate_interface]
	async fn leave_workspace() {
		CODEMP_INSTANCE.leave_workspace().await.unwrap()
	}

	#[generate_interface]
	async fn disconnect_buffer(path: String) -> bool {
		CODEMP_INSTANCE.disconnect_buffer(&path).await.unwrap()
	}
}

struct CursorHandler {
	cursor: Option<Arc<CodempCursorController>>
}

impl CursorHandler {
	#[generate_interface(constructor)]
	fn new() -> CursorHandler { //TODO this sucks but whatever
		CursorHandler { cursor: None }
	}
}


struct BufferHandler {
	buffer: Option<Arc<CodempBufferController>>
}

impl BufferHandler {
	#[generate_interface(constructor)]
	fn new() -> BufferHandler { //TODO this sucks but whatever
		BufferHandler { buffer: None }
	}
}