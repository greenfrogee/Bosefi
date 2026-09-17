package toolscreen;

import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
            Locale.US,
            "%.8f",
            toolscreenSensitivity
        );

    try {
        TomlParseResult toml =
            Toml.parse(file);

        if (toml.hasErrors()) {
            return "";
        }

        String content =
            new String(
                Files.readAllBytes(file),
                StandardCharsets.UTF_8
            );

        Pattern sensitivityPattern =
            Pattern.compile(
                "(?m)^mouseSensitivity\\s*=\\s*([0-9.]+)"
            );

        Matcher sensitivityMatcher =
            sensitivityPattern.matcher(content);

        if (!sensitivityMatcher.find()) {
            return "";
        }

        double oldSensitivity =
            Double.parseDouble(
                sensitivityMatcher.group(1)
            );

        double newSensitivityValue =
            Double.parseDouble(
                newSensitivity
            );

        String updated =
            sensitivityMatcher.replaceFirst(
                "mouseSensitivity = "
                    + newSensitivity
            );

        List<String> lines =
            new ArrayList<>(
                java.util.Arrays.asList(
                    updated.split(
                        "\\R",
                        -1
                    )
                )
            );

        boolean foundEyeZoomMode =
            false;

        String oldEyeZoomHeight =
            null;

        int modeStart =
            -1;

        for (int i = 0; i < lines.size(); i++) {

            String trimmed =
                lines.get(i).trim();

            if (
                trimmed.equals("[[mode]]")
            ) {
                if (modeStart != -1) {

                    if (
                        isEyeZoomMode(
                            lines,
                            modeStart,
                            i
                        )
                    ) {
                        foundEyeZoomMode = true;

                        oldEyeZoomHeight =
                            replaceModeHeight(
                                lines,
                                modeStart,
                                i
                            );

                        break;
                    }
                }

                modeStart = i;
            }
        }

        if (
            !foundEyeZoomMode
            && modeStart != -1
        ) {
            if (
                isEyeZoomMode(
                    lines,
                    modeStart,
                    lines.size()
                )
            ) {
                foundEyeZoomMode = true;

                oldEyeZoomHeight =
                    replaceModeHeight(
                        lines,
                        modeStart,
                        lines.size()
                    );
            }
        }

        if (
            !foundEyeZoomMode
            || oldEyeZoomHeight == null
        ) {
            return "";
        }

        StringBuilder builder =
            new StringBuilder();

        for (int i = 0; i < lines.size(); i++) {

            builder.append(
                lines.get(i)
            );

            if (i < lines.size() - 1) {
                builder.append(
                    System.lineSeparator()
                );
            }
        }

        String finalContent =
            builder.toString();

        if (content.equals(finalContent)) {
            return "";
        }

        Files.write(
            file,
            finalContent.getBytes(
                StandardCharsets.UTF_8
            )
        );

        String changes = "";

        if (
            Double.compare(
                oldSensitivity,
                newSensitivityValue
            ) != 0
        ) {
            changes +=
                name
                + " sensitivity: "
                + oldSensitivity
                + " → "
                + newSensitivityValue
                + "\n";
        }

        if (
            !oldEyeZoomHeight.equals("16384")
        ) {
            changes +=
                name
                + " EyeZoom height: "
                + oldEyeZoomHeight
                + " → 16384\n";
        }

        return changes;

    } catch (Exception e) {
        return "";
    }
}

private static boolean isEyeZoomMode(
    List<String> lines,
    int start,
    int end
) {

    for (int i = start; i < end; i++) {

        String trimmed =
            lines.get(i).trim();

        if (isKey(trimmed, "id")) {
            return "EyeZoom".equals(
                getValue(trimmed)
            );
        }
    }

    return false;
}

private static String replaceModeHeight(
    List<String> lines,
    int start,
    int end
) {

    for (int i = start; i < end; i++) {

        String trimmed =
            lines.get(i).trim();

        if (isKey(trimmed, "height")) {

            String oldHeight =
                getValue(trimmed);

            lines.set(
                i,
                replaceValue(
                    lines.get(i),
                    "height",
                    "16384"
                )
            );

            return oldHeight;
        }
    }

    return null;
}

private static boolean isKey(
    String line,
    String key
) {

    int equals =
        line.indexOf('=');

    if (equals == -1) {
        return false;
    }

    String actualKey =
        line.substring(
            0,
            equals
        ).trim();

    return key.equals(actualKey);
}

private static String getValue(
    String line
) {

    int equals =
        line.indexOf('=');

    if (equals == -1) {
        return "";
    }

    String value =
        line.substring(
            equals + 1
        ).trim();

    if (
        value.startsWith("'")
        && value.endsWith("'")
    ) {
        return value.substring(
            1,
            value.length() - 1
        );
    }

    if (
        value.startsWith("\"")
        && value.endsWith("\"")
    ) {
        return value.substring(
            1,
            value.length() - 1
        );
    }

    return value;
}

private static String replaceValue(
    String line,
    String key,
    String value
) {

    int equals =
        line.indexOf('=');

    String prefix =
        line.substring(
            0,
            equals + 1
        );

    return prefix + " " + value;
    }
}