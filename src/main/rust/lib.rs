use std::sync::Arc;
use tokio::sync::RwLock;
use std::time::Duration;
use codemp::prelude::*;
use codemp::tools;
use lazy_static::lazy_static;
use rifgen::rifgen_attr::{generate_access_methods, generate_interface, generate_interface_doc};
use uuid::Uuid;

pub mod glue { //rifgen generated code
	include!(concat!(env!("OUT_DIR"), "/glue.rs"));
}

lazy_static! {
	/// the tokio runtime, since we can't easily have Java and Rust async work together
	static ref RT: tokio::runtime::Runtime = tokio::runtime::Runtime::new()
		.expect("could not start tokio runtime");
}

#[generate_interface_doc]
/// the handler class that represent an instance of a CodeMP client
struct CodeMPHandler {
	client: CodempClient,
	url: String
}

impl CodeMPHandler {
	#[generate_interface(constructor)]
	/// constructor required by flapigen, DO NOT CALL THIS
	fn new(address: &str) -> CodeMPHandler {
		CodeMPHandler {
			client: RT.block_on(CodempClient::new(address)).unwrap(),
			url: address.to_string()
		}
	}

	#[generate_interface]
	/// create a new workspace
	fn create_workspace(&mut self, workspace_id: &str) -> CodempResult<WorkspaceHandler> {
		RT.block_on(self.client.create_workspace(workspace_id))
			.map(|workspace| WorkspaceHandler { workspace })
	}

	#[generate_interface]
	/// join a workspace by name
	fn join_workspace(&mut self, workspace_id: &str) -> CodempResult<WorkspaceHandler> {
		RT.block_on(self.client.join_workspace(workspace_id))
			.map(|workspace| WorkspaceHandler { workspace })
	}

	#[generate_interface]
	/// leave a workspace
	fn leave_workspace(&mut self, workspace_id: &str) -> CodempResult<()> {
		RT.block_on(self.client.leave_workspace(workspace_id))
	}

	#[generate_interface]
	/// get the url you are currently connected to
	fn get_url(&self) -> String {
		self.url.clone()
	}
}

#[generate_interface_doc]
/// wraps a [codemp::workspace::Workspace] to be handled by Java
struct WorkspaceHandler {
	workspace: Arc<RwLock<CodempWorkspace>>
}

impl WorkspaceHandler {
	#[generate_interface(constructor)]
	/// constructor required by flapigen, DO NOT CALL THIS
	fn new() -> WorkspaceHandler {
		unimplemented!()
	}

	#[generate_interface]
	/// create a new buffer in current workspace
	fn create_buffer(&mut self, path: &str) -> CodempResult<Option<BufferHandler>> {
		RT.block_on(RT.block_on(self.workspace.write()).create(path))?;
		Ok(self.get_buffer(path))
	}

	#[generate_interface]
	/// attach to a buffer and get a [crate::BufferHandler] for it
	fn attach_buffer(&mut self, path: &str) -> CodempResult<BufferHandler> {
		RT.block_on(RT.block_on(self.workspace.write()).attach(path))
			.map(|buffer| BufferHandler { buffer })
	}

	#[generate_interface]
	/// get a buffer's contents as a flat string
	fn get_buffer_snapshot(&self , path: &str) -> CodempResult<String> {
		RT.block_on(RT.block_on(self.workspace.read()).snapshot(path))
	}

	#[generate_interface]
	/// updates the local list of the workspace's buffers
	fn fetch_buffers(&mut self) -> CodempResult<()> {
		RT.block_on(RT.block_on(self.workspace.write()).fetch_buffers())
	}

	#[generate_interface]
	/// updates the local list of the workspace's users
	fn fetch_users(&mut self) -> CodempResult<()> {
		RT.block_on(RT.block_on(self.workspace.write()).fetch_users())
	}

	#[generate_interface]
	/// gets a list of all users in a buffer
	fn list_buffer_users(&mut self, path: &str) -> CodempResult<StringVec> {
		let mut res = StringVec::new();
		RT.block_on(RT.block_on(self.workspace.write())
			.list_buffer_users(path))?
			.iter().for_each(|u| res.push(Uuid::from(u.clone()).to_string()));
		Ok(res)
	}

	#[generate_interface]
	/// delete a buffer
	fn delete_buffer(&mut self, path: &str) -> CodempResult<()> {
		RT.block_on(RT.block_on(self.workspace.write()).delete(path))
	}

	#[generate_interface]
	/// detach from a buffer
	fn detach_buffer(&mut self, path: &str) -> bool {
		RT.block_on(self.workspace.write()).detach(path)
	}

	#[generate_interface]
	/// get the workspace id
	fn get_workspace_id(&self) -> String {
		RT.block_on(self.workspace.read()).id().clone()
	}

	#[generate_interface]
	/// get a [crate::CursorHandler] for the workspace's cursor
	fn get_cursor(&self) -> CursorHandler {
		CursorHandler { cursor: RT.block_on(self.workspace.read()).cursor().clone() }
	}

	#[generate_interface]
	/// get a [crate::BufferHandler] for one of the workspace's buffers
	fn get_buffer(&self, path: &str) -> Option<BufferHandler> {
		RT.block_on(self.workspace.read()).buffer_by_name(path)
			.map(|b| BufferHandler { buffer: b })
	}

	#[generate_interface]
	/// get the names of all buffers available in the workspace
	fn get_filetree(&self) -> StringVec {
		StringVec { v: RT.block_on(self.workspace.read()).filetree() }
	}

	#[generate_interface]
	/// polls a list of buffers, returning the first ready one
	fn select_buffer(&mut self, mut buffer_ids: StringVec, timeout: i64) -> CodempResult<Option<BufferHandler>> {
		let mut buffers = Vec::new();
		for id in buffer_ids.v.iter_mut() {
			match self.get_buffer(id.as_str()) {
				Some(buf) => buffers.push(buf.buffer),
				None => continue
			}
		}

		let result = RT.block_on(tools::select_buffer(
			buffers.as_slice(),
			Some(Duration::from_millis(timeout as u64))
		));

		match result {
			Err(e) => Err(e),
			Ok(buffer) =>
				Ok(buffer.map(|buffer| BufferHandler { buffer }))
		}
	}
}

#[generate_interface_doc]
#[generate_access_methods]
/// wraps a [codemp::proto::cursor::CursorEvent] to be handled by Java
struct CursorEventWrapper {
	user: String,
	buffer: String,
	start_row: i32,
	start_col: i32,
	end_row: i32,
	end_col: i32
}


#[generate_interface_doc]
/// a handler providing Java access to [codemp::cursor::Controller] methods
struct CursorHandler {
	pub cursor: Arc<CodempCursorController>
}

impl CursorHandler {
	#[generate_interface(constructor)]
	/// constructor required by flapigen, DO NOT CALL THIS
	fn new() -> CursorHandler {
		unimplemented!()
	}

	#[generate_interface]
	/// get next cursor event from current workspace, or block until one is available
	fn recv(&self) -> CodempResult<CursorEventWrapper>  {
		match RT.block_on(self.cursor.recv()) {
			Err(err) => Err(err),
			Ok(event) => Ok(CursorEventWrapper {
				user: Uuid::from(event.user).to_string(),
				buffer: event.position.buffer.clone(),
				start_row: event.position.start.row,
				start_col: event.position.start.col,
				end_row: event.position.end.row,
				end_col: event.position.end.col
			})
		}
	}

	#[generate_interface]
	/// broadcast a cursor event
	/// will automatically fix start and end if they are accidentally inverted
	fn send(&self, buffer: String, start_row: i32, start_col: i32, end_row: i32, end_col: i32) -> CodempResult<()> {
		self.cursor.send(CodempCursorPosition {
			buffer,
			start: CodempRowCol::from((start_row, start_col)),
			end: CodempRowCol::from((end_row, end_col))
		})
	}
}

#[generate_interface_doc]
#[generate_access_methods]
/// wraps a [codemp::api::change::TextChange] to make it accessible from Java
struct TextChangeWrapper {
	start: usize,
	end: usize, //not inclusive
	content: String
}

#[generate_interface_doc]
/// a handler providing Java access to [codemp::buffer::Controller] methods
struct BufferHandler {
	pub buffer: Arc<CodempBufferController>
}

impl BufferHandler {
	#[generate_interface(constructor)]
	/// constructor required by flapigen, DO NOT CALL THIS
	fn new() -> BufferHandler {
		unimplemented!()
	}

	#[generate_interface]
	/// get the name of the buffer
	fn get_name(&self) -> String {
		self.buffer.name().to_string()
	}

	#[generate_interface]
	/// get the contents of the buffer
	fn get_content(&self) -> String {
		self.buffer.content()
	}

	#[generate_interface]
	/// if a text change is available on the buffer, return it immediately
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
	/// broadcast a text change on the buffer
	fn send(&self, start_offset: usize, end_offset: usize, content: String) -> CodempResult<()> {
		self.buffer.send(CodempTextChange { span: start_offset..end_offset, content })
	}
}

#[generate_interface_doc]
/// a convenience struct allowing Java access to a Rust vector
struct StringVec { //jni moment
	v: Vec<String>
}

impl StringVec {
	#[generate_interface(constructor)]
	/// initialize an empty vector
	fn new() -> StringVec {
		Self { v: Vec::new() }
	}

	#[generate_interface]
	/// push a new value onto the vector
	fn push(&mut self, s: String) {
		self.v.push(s);
	}

	#[generate_interface]
	/// get the length of the underlying vector
	fn length(&self) -> i64 {
		self.v.len() as i64
	}

	#[generate_interface]
	/// access the element at a given index
	fn get(&self, idx: i64) -> Option<String> {
		let elem: Option<&String> = self.v.get(idx as usize);
		elem.map(|s| s.clone())
	}
}
