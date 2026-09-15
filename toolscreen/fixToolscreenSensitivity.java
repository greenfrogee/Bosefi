package toolscreen;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class fixToolscreenSensitivity {

    public static String fix(
        Path file,
        Double toolscreenSensitivity,
        String name
    ) {

        if (file == null || toolscreenSensitivity == null) {
            return "";
        }

        String newSensitivity =
            String.format(
                java.util.Locale.US,
                "%.8f",
                toolscreenSensitivity
            );

        try {
            String content =
                new String(
                    Files.readAllBytes(file),
                    StandardCharsets.UTF_8
                );

            Pattern pattern =
                Pattern.compile(
                    "(?m)^mouseSensitivity\\s*=\\s*([0-9.]+)"
                );

            Matcher matcher =
                pattern.matcher(content);

            if (!matcher.find()) {
                return "";
            }

            double oldSensitivity =
                Double.parseDouble(matcher.group(1));

            double newSensitivityValue =
                Double.parseDouble(newSensitivity);

            if (Double.compare(
                oldSensitivity,
                newSensitivityValue
            ) == 0) {
                return "";
            }

            String updated =
                matcher.replaceFirst(
                    "mouseSensitivity = "
                        + newSensitivity
                );

            Files.write(
                file,
                updated.getBytes(StandardCharsets.UTF_8)
            );

            return name
                + ": "
                + oldSensitivity
                + " → "
                + newSensitivityValue
                + "\n";

        } catch (Exception e) {
            return "";
        }
    }
}