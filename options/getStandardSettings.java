package options;

import java.nio.file.Files;
import java.nio.file.Path;

public class getStandardSettings {

    public static boolean exists(Path instancePath) {
        if (instancePath == null) {
            return false;
        }

        return Files.isRegularFile(
            instancePath.resolve("config").resolve("mcsr").resolve("standardsettings.json")
        );
    }
}