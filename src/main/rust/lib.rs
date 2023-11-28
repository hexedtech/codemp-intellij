use std::sync::Arc;
use std::time::Duration;
use codemp::prelude::*;
use codemp::tools;
use rifgen::rifgen_attr::{generate_access_methods, generate_interface, generate_interface_doc};

pub mod glue { //rifgen generated code
	include!(concat!(env!("OUT_DIR"), "/glue.rs"));
}

#[generate_interface_doc]
struct CodeMPHandler;

impl CodeMPHandler {
	#[generate_interface(constructor)]
	fn new() -> CodeMPHandler {
		CodeMPHandler {}
	}

	#[generate_interface]
	fn connect(addr: String) -> CodempResult<()> {
		CODEMP_INSTANCE.connect(&addr)
	}

	#[generate_interface]
	fn join(session: String) -> CodempResult<CursorHandler> {
		CODEMP_INSTANCE.join(&session).map(|controller| CursorHandler { cursor: controller })
	}

	#[generate_interface]
	fn create(path: String) -> CodempResult<()> {
		CODEMP_INSTANCE.create(&path, None)
	}

	#[generate_interface]
	fn create_with_content(path: String, content: String) -> CodempResult<()> {
		CODEMP_INSTANCE.create(&path, Some(&content))
	}

	#[generate_interface]
	fn attach(path: String) -> CodempResult<BufferHandler> {
		CODEMP_INSTANCE.attach(&path).map(|b| BufferHandler { buffer: b })
	}

	#[generate_interface]
	fn detach(path: String) -> CodempResult<bool> {
		CODEMP_INSTANCE.disconnect_buffer(&path)
	}

	#[generate_interface]
	fn get_cursor() -> CodempResult<CursorHandler> {
		CODEMP_INSTANCE.get_cursor().map(|c| CursorHandler { cursor: c })
	}

	#[generate_interface]
	fn get_buffer(path: String) -> CodempResult<BufferHandler> {
		CODEMP_INSTANCE.get_buffer(&path).map(|b| BufferHandler { buffer: b })
	}

	#[generate_interface]
	fn leave_workspace() -> CodempResult<()> {
		CODEMP_INSTANCE.leave_workspace()
	}

	#[generate_interface]
	fn disconnect_buffer(path: String) -> CodempResult<bool> {
		CODEMP_INSTANCE.disconnect_buffer(&path)
	}

	#[generate_interface]
	fn select_buffer(mut buffer_ids: StringVec, timeout: i64) -> CodempResult<Option<String>> {
		let mut buffers = Vec::new();
		for id in buffer_ids.v.iter_mut() {
			match CODEMP_INSTANCE.get_buffer(id.as_str()) {
				Ok(buf) => buffers.push(buf),
				Err(_) => continue
			}
		}
		CODEMP_INSTANCE.rt().block_on(
			tools::select_buffer_timeout(&buffers, Duration::from_millis(timeout as u64)))
	}
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
		unimplemented!()
	}

	#[generate_interface]
	fn recv(&self) -> CodempResult<CursorEventWrapper>  {
		match self.cursor.blocking_recv(CODEMP_INSTANCE.rt()) {
			Err(err) => Err(err),
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
	fn send(&self, buffer: String, start_row: i32, start_col: i32, end_row: i32, end_col: i32) -> CodempResult<()> {
		self.cursor.send(CodempCursorPosition {
			buffer,
			start: CodempRowCol::wrap(start_row, start_col),
			end: CodempRowCol::wrap(end_row, end_col)
		})
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
		unimplemented!()
	}

	#[generate_interface]
	fn get_name(&self) -> String {
		self.buffer.name.clone()
	}

	#[generate_interface]
	fn get_content(&self) -> String {
		self.buffer.content()
	}

	#[generate_interface]
	fn try_recv(&self) -> CodempResult<Option<TextChangeWrapper>> {
		match self.buffer.try_recv() {
			Err(err) => Err(err),
			Ok(None) => Ok(None),
			Ok(Some(change)) => Ok(Some(TextChangeWrapper {
				start: change.span.start,
				end: change.span.end,
				content: change.content.clone()
			}))
		}
	}

	#[generate_interface]
	fn recv(&self) -> CodempResult<TextChangeWrapper> {
		match self.buffer.blocking_recv(CODEMP_INSTANCE.rt()) {
			Err(err) => Err(err),
			Ok(change) => Ok(TextChangeWrapper {
				start: change.span.start,
				end: change.span.end,
				content: change.content.clone()
			})
		}
	}

	#[generate_interface]
	fn send(&self, start_offset: usize, end_offset: usize, content: String) -> CodempResult<()> {
		self.buffer.send(CodempTextChange { span: start_offset..end_offset, content })
	}
}

#[generate_interface_doc]
struct StringVec { //jni moment
	v: Vec<String>
}

impl StringVec {
	#[generate_interface(constructor)]
	fn new() -> StringVec {
		Self { v: Vec::new() }
	}

	#[generate_interface]
	fn push(&mut self, s: String) {
		self.v.push(s);
	}
}