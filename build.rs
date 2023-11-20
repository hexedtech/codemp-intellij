use flapigen::{JavaConfig, LanguageConfig};
use std::{env, fs, path::Path};
use rifgen::{Generator as RifgenGenerator, TypeCases, Language};
use flapigen::Generator as FlapigenGenerator;

fn main() {
	let out_dir_var = env::var("OUT_DIR")
		.expect("no OUT_DIR, but cargo should provide it");
	let out_dir = Path::new(&out_dir_var);
	let generated_glue_file = out_dir.join("generated_glue.in");

	let src_dir = Path::new("src")
		.join("main")
		.join("rust");
	let glue_file = src_dir.join("glue.in");

	RifgenGenerator::new(TypeCases::CamelCase,Language::Java, vec!(src_dir))
		.generate_interface(&generated_glue_file);

	let jni_path = Path::new("src")
		.join("main")
		.join("java")
		.join("com")
		.join("codemp")
		.join("intellij")
		.join("jni");

	//create folder if it doesn't exist
	fs::create_dir_all(&jni_path)
		.expect("An error occurred while creating the JNI folder!");

	let java_gen = FlapigenGenerator::new(LanguageConfig::JavaConfig(
		JavaConfig::new(
			jni_path,
			"com.codemp.intellij.jni".into()
		))).rustfmt_bindings(true);

	java_gen.expand_many(
		"codemp-intellij",
		&[&generated_glue_file, &glue_file],
		out_dir.join("glue.rs"),
	);

	println!("cargo:rerun-if-changed={}", generated_glue_file.display());
}
