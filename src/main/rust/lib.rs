mod error;

use std::sync::Arc;
use codemp::prelude::*;
use rifgen::rifgen_attr::generate_interface;
use crate::error::ErrorWrapper;

pub mod glue {
	include!(concat!(env!("OUT_DIR"), "/glue.rs"));
}

// #[generate_interface_doc] //TODO
struct CodeMPHandler {}

impl CodeMPHandler {
	#[generate_interface(constructor)]
	fn new() -> CodeMPHandler {
		CodeMPHandler {}
	}

	#[generate_interface]
	fn connect(addr: String) -> Result<(), String> {
		convert(CODEMP_INSTANCE.connect(&addr))
	}

	#[generate_interface]
	fn join(session: String) -> Result<CursorHandler, String> {
		convert_cursor(CODEMP_INSTANCE.join(&session))
	}

	#[generate_interface]
	fn create(path: String) -> Result<(), String> {
		convert(CODEMP_INSTANCE.create(&path, None))
	}

	#[generate_interface]
	fn create_with_content(path: String, content: String) -> Result<(), String> {
		convert(CODEMP_INSTANCE.create(&path, Some(&content)))
	}

	#[generate_interface]
	fn attach(path: String) -> Result<BufferHandler, String> {
		convert_buffer(CODEMP_INSTANCE.attach(&path))
	}

	#[generate_interface]
	fn get_cursor() -> Result<CursorHandler, String> {
		convert_cursor(CODEMP_INSTANCE.get_cursor())
	}

	#[generate_interface]
	fn get_buffer(path: String) -> Result<BufferHandler, String> {
		convert_buffer(CODEMP_INSTANCE.get_buffer(&path))
	}

	#[generate_interface]
	fn leave_workspace() -> Result<(), String> {
		convert(CODEMP_INSTANCE.leave_workspace())
	}

	#[generate_interface]
	fn disconnect_buffer(path: String) -> Result<bool, String> {
		convert(CODEMP_INSTANCE.disconnect_buffer(&path))
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
	#[allow(unused)]
	cursor: Option<Arc<CodempCursorController>>
}

impl CursorHandler {
	#[generate_interface(constructor)]
	fn new() -> CursorHandler { //TODO: this sucks but whatever
		panic!("Default constructor for CursrorHandler should never be called!")
	}
}

struct BufferHandler {
	#[allow(unused)]
	buffer: Option<Arc<CodempBufferController>>
}

impl BufferHandler {
	#[generate_interface(constructor)]
	fn new() -> BufferHandler { //TODO: this sucks but whatever
		panic!("Default constructor for BufferHandler should never be called!")
	}
}
