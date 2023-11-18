mod error;

use std::sync::Arc;
use codemp::prelude::*;
use rifgen::rifgen_attr::{generate_access_methods, generate_interface, generate_interface_doc};
use crate::error::ErrorWrapper;

pub mod glue { //rifgen generated code
	include!(concat!(env!("OUT_DIR"), "/glue.rs"));
}

#[generate_interface_doc]
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
	fn detach(path: String) -> Result<bool, String> {
		convert(CODEMP_INSTANCE.disconnect_buffer(&path))
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

	#[generate_interface]
	fn select_buffer() -> Result<String, String> {
		convert(CODEMP_INSTANCE.select_buffer())
	}
}

fn convert_buffer(result: Result<Arc<CodempBufferController>, CodempError>) -> Result<BufferHandler, String> {
	convert(result).map(|val| BufferHandler { buffer: val })
}

fn convert_cursor(result: Result<Arc<CodempCursorController>, CodempError>) -> Result<CursorHandler, String> {
	convert(result).map(|val| CursorHandler { cursor: val })
}

fn convert<T>(result: Result<T, CodempError>) -> Result<T, String> {
	result.map_err(|err| ErrorWrapper::from(err).get_error_message())
}

#[generate_interface_doc]
#[generate_access_methods]
struct CursorEventWrapper {
	user: String,
	buffer: String,
	start_row: i32,
	start_col: i32,
	end_row: i32,
	end_col: i32
}


#[generate_interface_doc]
struct CursorHandler {
	#[allow(unused)]
	cursor: Arc<CodempCursorController>
}

impl CursorHandler {
	#[generate_interface(constructor)]
	fn new() -> CursorHandler {
		panic!("Default constructor for CursorHandler should never be called!")
	}

	#[generate_interface]
	fn recv(&self) -> Result<CursorEventWrapper, String>  {
		match self.cursor.blocking_recv(CODEMP_INSTANCE.rt()) {
			Err(err) => Err(ErrorWrapper::from(err).get_error_message()),
			Ok(event) => Ok(CursorEventWrapper {
				user: event.user,
				buffer: event.position.as_ref().unwrap().buffer.clone(),
				start_row: event.position.as_ref().unwrap().start().row,
				start_col: event.position.as_ref().unwrap().start().col,
				end_row: event.position.as_ref().unwrap().end().row,
				end_col: event.position.as_ref().unwrap().end().col
			})
		}
	}

	#[generate_interface]
	fn send(&self, buffer: String, start_row: i32, start_col: i32, end_row: i32, end_col: i32) -> Result<(), String> {
		self.cursor.send(CodempCursorPosition {
			buffer,
			start: CodempRowCol::wrap(start_row, start_col),
			end: CodempRowCol::wrap(end_row, end_col)
		}).map_err(|err| ErrorWrapper::from(err).get_error_message())
	}
}

#[generate_interface_doc]
#[generate_access_methods]
struct TextChangeWrapper {
	start: usize,
	end: usize, //not inclusive
	content: String
}

#[generate_interface_doc]
struct BufferHandler {
	#[allow(unused)]
	buffer: Arc<CodempBufferController>
}

impl BufferHandler {
	#[generate_interface(constructor)]
	fn new() -> BufferHandler {
		panic!("Default constructor for BufferHandler should never be called!")
	}

	#[generate_interface]
	fn get_name(&self) -> String {
		self.buffer.name.clone()
	}

	#[generate_interface]
	fn try_recv(&self) -> Result<Option<TextChangeWrapper>, String> {
		match self.buffer.try_recv() {
			Err(err) => Err(ErrorWrapper::from(err).get_error_message()),
			Ok(None) => Ok(None),
			Ok(Some(change)) => Ok(Some(TextChangeWrapper {
				start: change.span.start,
				end: change.span.end,
				content: change.content.clone()
			}))
		}
	}

	#[generate_interface]
	fn recv(&self) -> Result<TextChangeWrapper, String> {
		match self.buffer.blocking_recv(CODEMP_INSTANCE.rt()) {
			Err(err) => Err(ErrorWrapper::from(err).get_error_message()),
			Ok(change) => Ok(TextChangeWrapper {
				start: change.span.start,
				end: change.span.end,
				content: change.content.clone()
			})
		}
	}

	#[generate_interface]
	fn send(&self, start_offset: usize, end_offset: usize, content: String) -> Result<(), String> {
		self.buffer.send(CodempTextChange { span: start_offset..end_offset, content })
			.map_err(|err| ErrorWrapper::from(err).get_error_message())
	}
}
