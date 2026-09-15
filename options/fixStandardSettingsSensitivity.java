package options;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class fixStandardSettingsSensitivity {

    public static String fix(Path instancePath) {

        Double sensitivity =
            getStandardSettingsSensitivity.get(instancePath);

        if (sensitivity == null) {
            return "";
        }

        Path file =
            instancePath.resolve("config")
                .resolve("mcsr")
                .resolve("standardsettings.json");

        try {
            String content =
                new String(
                    Files.readAllBytes(file),
                    StandardCharsets.UTF_8
                );

            String updated =
                content.replaceFirst(
                    "(?s)(\"mouseSensitivity\"\\s*:\\s*)\\{.*?\\}",
                    "$1" + "0.02291165"
                );

            if (content.equals(updated)) {
                updated =
                    content.replaceFirst(
                        "(\"mouseSensitivity\"\\s*:\\s*)[0-9.]+",
                        "$1" + "0.02291165"
                    );
            }

            if (content.equals(updated)) {
                return "";
            }

            Files.write(
                file,
                updated.getBytes(StandardCharsets.UTF_8)
            );

            return "standardsettings.json sensitivity: "
                + sensitivity
                + " → 0.02291165\n";

        } catch (Exception e) {
            return "";
        }
    }
}