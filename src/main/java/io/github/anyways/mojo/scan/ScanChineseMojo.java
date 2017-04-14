package io.github.anyways.mojo.scan;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "scancn", requiresProject = false)
public class ScanChineseMojo extends AbstractMojo {

	@Parameter(readonly = true, required = true, property = "base")
	private File baseDirectory;

	@Parameter(readonly = true, required = false, property = "target")
	private File targetFile;

	private List<File> javaFiles = new ArrayList<File>();

	private Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]+");

	private Pattern ps = Pattern.compile("/\\*");

	private Pattern pe = Pattern.compile("\\*/");

	private Pattern pa = Pattern.compile("//");

	private List<T> finds = new ArrayList<T>();

	public void execute() throws MojoExecutionException, MojoFailureException {
		searchFiles(baseDirectory);
		for (File javaFile : javaFiles) {
			scanJavaFile(javaFile);
		}
		BufferedWriter bw = null;
		if (targetFile != null) {
			if (!targetFile.exists()) {
				try {
					targetFile.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				bw = new BufferedWriter(new FileWriter(targetFile));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		String line = null;
		for (T t : finds) {
			line = String.format("%s:%d:%s", t.getJavaFile().getPath(), t.getLine(), t.getContent());
			getLog().info(line);
			if (targetFile != null) {
				try {
					bw.write(line);
					bw.newLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		if (bw != null) {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void searchFiles(File target) {
		if (target.exists()) {
			if (target.isDirectory()) {
				for (File file : target.listFiles()) {
					searchFiles(file);
				}
			} else {
				if (target.getName().endsWith(".java")) {
					javaFiles.add(target);
				}
			}
		}
	}

	private void scanJavaFile(File javaFile) {
		InputStream in;
		InputStreamReader ir = null;
		try {
			in = new FileInputStream(javaFile);
			ir = new InputStreamReader(in, "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		String line = null;
		BufferedReader br = new BufferedReader(ir);
		try {
			int i = 0;
			boolean skip = false;
			while ((line = br.readLine()) != null) {
				i++;
				Matcher ma = pa.matcher(line);
				Matcher me = pe.matcher(line);
				Matcher ms = ps.matcher(line);
				if (ma.find()) {
					skip = true;
				}
				if (ms.find()) {
					skip = true;
				}
				if (me.find()) {
					skip = false;
				}
				if (skip) {
					continue;
				}
				Matcher m = pattern.matcher(line);
				if (m.find()) {
					finds.add(new T(javaFile, i, m.group()));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

class T {
	private File javaFile;
	private int line;
	private String content;
	public T(File javaFile, int line, String content) {
		this.javaFile = javaFile;
		this.line = line;
		this.content = content;
	}
	public File getJavaFile() {
		return javaFile;
	}
	public void setJavaFile(File javaFile) {
		this.javaFile = javaFile;
	}
	public int getLine() {
		return line;
	}
	public void setLine(int line) {
		this.line = line;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
