use flapigen::{JavaConfig, LanguageConfig};
use std::{env, path::Path};
use rifgen::{Generator, TypeCases, Language};

fn main() {
    let out_dir_var = env::var("OUT_DIR")
        .expect("no OUT_DIR, but cargo should provide it");
    let out_dir = Path::new(&out_dir_var);
    let source_folder = "src/main/rust/";

    let glue_file = out_dir.join("glue.in");
    Generator::new(TypeCases::CamelCase,Language::Java,source_folder)
        .generate_interface(&glue_file);

    let jni_path = Path::new("src")
        .join("main")
        .join("java")
        .join("com")
        .join("codemp")
        .join("intellij")
        .join("jni");

    //create folder if it doesn't exist
    std::fs::create_dir_all(&jni_path)
        .expect("An error occurred while creating the JNI folder!");

    let java_gen = flapigen::Generator::new(LanguageConfig::JavaConfig(
        JavaConfig::new(
            jni_path,
            "com.codemp.intellij.jni".into()
        ))).rustfmt_bindings(true);

    java_gen.expand(
        "codemp-intellij",
        &glue_file,
        out_dir.join("glue.rs"),
    );

    println!(
        "cargo:rerun-if-changed={}",
        Path::new("src/main").join(&glue_file).display()
    );
}