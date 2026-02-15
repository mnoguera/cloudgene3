package cloudgene.mapred.wdl;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

public class WdlReader {

	public static WdlApp loadAppFromString(String filename, String content) throws IOException {

		LoaderOptions options = new LoaderOptions();
		options.setAllowDuplicateKeys(false);
		Constructor constructor = new Constructor(WdlApp.class, options);
		Yaml yaml = new Yaml(constructor);
		WdlApp app = yaml.loadAs(new StringReader(content), WdlApp.class);

		updateApp(filename, app);

		return app;

	}

	public static WdlApp loadAppFromFile(String filename) throws IOException {

		LoaderOptions options = new LoaderOptions();
		options.setAllowDuplicateKeys(false);
		Constructor constructor = new Constructor(WdlApp.class, options);
		Yaml yaml = new Yaml(constructor);
		WdlApp app = yaml.loadAs(new FileReader(filename), WdlApp.class);

		updateApp(filename, app);

		return app;

	}

	private static void updateApp(String filename, WdlApp app) throws IOException {

		String path = new File(new File(filename).getAbsolutePath()).getParentFile().getAbsolutePath();
		app.setPath(path);
		app.setManifestFile(filename);
		if (app.getId() == null || app.getId().isEmpty()) {
			throw new IOException("No field 'id' found in file '" + filename + "'.");
		}
		if (app.getVersion() == null || app.getVersion().isEmpty()) {
			throw new IOException("No field 'version' found in file '" + filename + "'.");
		}
		if (app.getName() == null || app.getName().isEmpty()) {
			throw new IOException("No field 'name' found in file '" + filename + "'.");
		}
	}

}
