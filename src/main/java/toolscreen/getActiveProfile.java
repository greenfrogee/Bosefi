package toolscreen;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class getActiveProfile {

    public static boolean exists() {
        Path file =
            Paths.get(
                System.getProperty("user.home"),
                ".config",
                "toolscreen",
                "profiles.toml"
            );

        return Files.isRegularFile(file);
    }

    public static String get() {
        Path file =
            Paths.get(
                System.getProperty("user.home"),
                ".config",
                "toolscreen",
                "profiles.toml"
            );

        try {
            TomlParseResult result =
                Toml.parse(file);

            return result.getString("activeProfile");

        } catch (Exception e) {
            return null;
        }
    }
}