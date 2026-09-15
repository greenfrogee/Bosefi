package options;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class getOptionsTxtSensitivity {

    public static Double get(Path instancePath) {
        if (instancePath == null) {
            return null;
        }

        Path file =
            instancePath.resolve("options.txt");

        try {
            String content =
                new String(
                    Files.readAllBytes(file),
                    StandardCharsets.UTF_8
                );

            Pattern pattern =
                Pattern.compile("(?m)^mouseSensitivity:\\s*([0-9.]+)");

            Matcher matcher =
                pattern.matcher(content);

            if (matcher.find()) {
                return Double.parseDouble(
                    matcher.group(1)
                );
            }

        } catch (Exception e) {
            return null;
        }

        return null;
    }
}