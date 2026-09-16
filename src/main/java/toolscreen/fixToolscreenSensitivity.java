package toolscreen;

import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

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

            TomlTable eyezoom =
                toml.getTable("eyezoom");

            TomlArray modes =
                toml.getArray("mode");

            if (eyezoom == null || modes == null) {
                return "";
            }

            if (!eyezoom.contains("windowHeight")) {
                return "";
            }

            boolean eyeZoomModeExists = false;

            for (int i = 0; i < modes.size(); i++) {
                TomlTable mode =
                    modes.getTable(i);

                if (mode == null) {
                    continue;
                }

                String id =
                    mode.getString("id");

                if ("EyeZoom".equals(id)) {
                    eyeZoomModeExists = true;
                    break;
                }
            }

            if (!eyeZoomModeExists) {
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

            boolean inEyezoom =
                false;

            boolean foundEyezoomHeight =
                false;

            int modeStart =
                -1;

            int modeEnd =
                lines.size();

            for (int i = 0; i < lines.size(); i++) {

                String trimmed =
                    lines.get(i).trim();

                if (trimmed.equals("[eyezoom]")) {
                    inEyezoom = true;
                    continue;
                }

                if (trimmed.startsWith("[[")
                    && trimmed.endsWith("]]")) {

                    if (modeStart != -1) {
                        modeEnd = i;

                        if (isEyeZoomMode(
                            lines,
                            modeStart,
                            modeEnd
                        )) {
                            replaceModeHeight(
                                lines,
                                modeStart,
                                modeEnd
                            );
                        }

                        modeStart = -1;
                    }

                    inEyezoom = false;

                    if (trimmed.equals("[[mode]]")) {
                        modeStart = i;
                    }

                    continue;
                }

                if (trimmed.startsWith("[")
                    && trimmed.endsWith("]")) {

                    inEyezoom = false;
                }

                if (inEyezoom
                    && isKey(
                        trimmed,
                        "windowHeight"
                    )) {

                    lines.set(
                        i,
                        replaceValue(
                            lines.get(i),
                            "windowHeight",
                            "16384"
                        )
                    );

                    foundEyezoomHeight = true;
                }
            }

            if (modeStart != -1) {
                modeEnd = lines.size();

                if (isEyeZoomMode(
                    lines,
                    modeStart,
                    modeEnd
                )) {
                    replaceModeHeight(
                        lines,
                        modeStart,
                        modeEnd
                    );
                }
            }

            boolean foundEyeZoomModeHeight =
                false;

            for (int i = 0; i < lines.size(); i++) {
                String trimmed =
                    lines.get(i).trim();

                if (!trimmed.equals("[[mode]]")) {
                    continue;
                }

                int end =
                    lines.size();

                for (int j = i + 1; j < lines.size(); j++) {
                    String next =
                        lines.get(j).trim();

                    if (next.equals("[[mode]]")) {
                        end = j;
                        break;
                    }

                    if (next.startsWith("[")
                        && !next.startsWith("[[")
                        && next.endsWith("]")) {
                        end = j;
                        break;
                    }
                }

                if (isEyeZoomMode(
                    lines,
                    i,
                    end
                )) {
                    for (int j = i; j < end; j++) {
                        if (isKey(
                            lines.get(j).trim(),
                            "height"
                        )) {
                            lines.set(
                                j,
                                replaceValue(
                                    lines.get(j),
                                    "height",
                                    "16384"
                                )
                            );

                            foundEyeZoomModeHeight =
                                true;

                            break;
                        }
                    }

                    break;
                }
            }

            if (!foundEyezoomHeight
                || !foundEyeZoomModeHeight) {
                return "";
            }

            StringBuilder builder =
                new StringBuilder();

            for (int i = 0; i < lines.size(); i++) {
                builder.append(lines.get(i));

                if (i < lines.size() - 1) {
                    builder.append(System.lineSeparator());
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

            if (Double.compare(
                oldSensitivity,
                newSensitivityValue
            ) != 0) {
                changes +=
                    name
                    + " sensitivity: "
                    + oldSensitivity
                    + " → "
                    + newSensitivityValue
                    + "\n";
            }

            changes +=
                name
                + ": EyeZoom windowHeight → 16384\n";

            changes +=
                name
                + ": EyeZoom mode height → 16384\n";

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

    private static void replaceModeHeight(
        List<String> lines,
        int start,
        int end
    ) {

        for (int i = start; i < end; i++) {

            String trimmed =
                lines.get(i).trim();

            if (isKey(trimmed, "height")) {
                lines.set(
                    i,
                    replaceValue(
                        lines.get(i),
                        "height",
                        "16384"
                    )
                );

                return;
            }
        }
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

        if (value.startsWith("'")
            && value.endsWith("'")) {

            return value.substring(
                1,
                value.length() - 1
            );
        }

        if (value.startsWith("\"")
            && value.endsWith("\"")) {

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