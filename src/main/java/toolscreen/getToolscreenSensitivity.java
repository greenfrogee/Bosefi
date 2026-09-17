package toolscreen;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class getToolscreenSensitivity {

    public static boolean exists() {
        Path file =
            Paths.get(
                System.getProperty("user.home"),
                ".config",
                "toolscreen",
                "config.toml"
            );

        return Files.isRegularFile(file);
    }

    public static Double get(
        Double optionsTxtMouseSensitivity,
        Double standardSettingsMouseSensitivity
    ) {
        Double sens = optionsTxtMouseSensitivity;

        if (sens == null) {
            sens = standardSettingsMouseSensitivity;
        }

        if (sens == null) {
            return null;
        }

        String activeProfile =
            getActiveProfile.get();

        if (activeProfile == null) {
            return null;
        }

        Path profile =
            Paths.get(
                System.getProperty("user.home"),
                ".config",
                "toolscreen",
                "profiles",
                activeProfile + ".toml"
            );

        try {
            TomlParseResult result =
                Toml.parse(profile);

            Double globalSensitivity =
                result.getDouble("mouseSensitivity");

            if (globalSensitivity == null) {
                return null;
            }

            double numerator =
                Math.pow((0.6 * sens) + 0.2, 3) * 1.2;

            double denominator =
                Math.pow((0.6 * 0.02291165) + 0.2, 3) * 1.2;

            double toolscreenSensitivity =
                Math.round(
                    (numerator / denominator) * 100.0
                ) / 100.0;

            return toolscreenSensitivity * globalSensitivity;

        } catch (Exception e) {
            return null;
        }
    }
}