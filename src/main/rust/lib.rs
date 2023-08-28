mod error;

use std::sync::Arc;
use codemp::prelude::*;
use rifgen::rifgen_attr::generate_interface;
use crate::error::ErrorWrapper;

// #[generate_interface_doc] //TODO
struct CodeMPHandler {}

impl CodeMPHandler {
	#[generate_interface(constructor)]
	async fn new() -> CodeMPHandler {
		CodeMPHandler {}
	}

	#[generate_interface]
	async fn connect(addr: String) -> Result<(), String> {
		convert(CODEMP_INSTANCE.connect(&addr).await)
	}

	#[generate_interface]
	async fn join(session: String) -> Result<CursorHandler, String> {
		convert_cursor(CODEMP_INSTANCE.join(&session).await)
	}

	#[generate_interface]
	async fn create(path: String) -> Result<(), String> {
		convert(CODEMP_INSTANCE.create(&path, None).await)
	}

	#[generate_interface]
	async fn create_with_content(path: String, content: String) -> Result<(), String> {
		convert(CODEMP_INSTANCE.create(&path, Some(&content)).await)
	}

	#[generate_interface]
	async fn attach(path: String) -> Result<BufferHandler, String> {
		convert_buffer(CODEMP_INSTANCE.attach(&path).await)
	}

	#[generate_interface]
	async fn get_cursor() -> Result<CursorHandler, String> {
		convert_cursor(CODEMP_INSTANCE.get_cursor().await)
	}

	#[generate_interface]
	async fn get_buffer(path: String) -> Result<BufferHandler, String> {
		convert_buffer(CODEMP_INSTANCE.get_buffer(&path).await)
	}

	#[generate_interface]
	async fn leave_workspace() -> Result<(), String> {
		convert(CODEMP_INSTANCE.leave_workspace().await)
	}

	#[generate_interface]
	async fn disconnect_buffer(path: String) -> Result<bool, String> {
		convert(CODEMP_INSTANCE.disconnect_buffer(&path).await)
	}
}

fn convert_buffer(result: Result<Arc<CodempBufferController>, CodempError>) -> Result<BufferHandler, String> {
	convert(result).map(|val| BufferHandler { buffer: Some(val) })
}

fn convert_cursor(result: Result<Arc<CodempCursorController>, CodempError>) -> Result<CursorHandler, String> {
	convert(result).map(|val| CursorHandler { cursor: Some(val) })
}

fn convert<T>(result: Result<T, CodempError>) -> Result<T, String> {
	result.map_err(|err| ErrorWrapper::from(err).get_error_message())
}

struct CursorHandler {
	cursor: Option<Arc<CodempCursorController>>
}

impl CursorHandler {
	#[generate_interface(constructor)]
	async fn new() -> CursorHandler { //TODO: this sucks but whatever
		CursorHandler { cursor: None }
	}
}


struct BufferHandler {
	buffer: Option<Arc<CodempBufferController>>
}

impl BufferHandler {
	#[generate_interface(constructor)]
	async fn new() -> BufferHandler { //TODO: this sucks but whatever
		BufferHandler { buffer: None }
	}
}