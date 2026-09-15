package options;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class fixOptionsTxtSensitivity {

    public static String fix(Path instancePath) {

        Double sensitivity =
            getOptionsTxtSensitivity.get(instancePath);

        if (sensitivity == null) {
            return "";
        }

        if (Double.compare(
            sensitivity,
            0.02291165
        ) == 0) {
            return "";
        }

        Path file =
            instancePath.resolve("options.txt");

        try {
            String content =
                new String(
                    Files.readAllBytes(file),
                    StandardCharsets.UTF_8
                );

            String updated =
                content.replaceFirst(
                    "(?m)^mouseSensitivity:\\s*[0-9.]+",
                    "mouseSensitivity:0.02291165"
                );

            if (content.equals(updated)) {
                return "";
            }

            Files.write(
                file,
                updated.getBytes(StandardCharsets.UTF_8)
            );

            return "options.txt sensitivity: "
                + sensitivity
                + " → 0.02291165\n";

        } catch (Exception e) {
            return "";
        }
    }
}