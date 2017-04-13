package io.github.anyways.mojo.rm;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo( name = "rm", requiresProject = false )
public class RmMojo extends AbstractMojo {

	@Parameter( readonly = true, required = true, property = "target" )
	private File targetDirectory;

	public void execute() throws MojoExecutionException, MojoFailureException {
		if (targetDirectory.exists()) {
			getLog().info("Removing directory: [" + targetDirectory + "]");
			deleteTarget(targetDirectory);
			getLog().info("Removed directory: [" + targetDirectory + "]");
		} else {
			getLog().info("Target directory does not exist.");
		}
	}

	private void deleteTarget(File target) {
		if (target.isDirectory()) {
			File[] fs = target.listFiles();
			for (File f : fs) {
				deleteTarget(f);
			}
		}
		getLog().debug("Removes " + target.getAbsolutePath());
		target.delete();
	}
}
