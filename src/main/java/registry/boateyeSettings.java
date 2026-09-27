package registry;

import java.util.prefs.Preferences;

public class boateyeSettings {

    private static final Preferences preferences = Preferences.userRoot().node("ninjabrainbot");

    public static String setValue( String name, String value, String defaultValue) {
        
        String current = preferences.get(name, null);

        if (value.equals(current)) {
            return "";
        }

        preferences.put(name, value);

        if (current == null && defaultValue == null) {
            return "";
        }

        String displayCurrent = current == null ? defaultValue : current;

        return "Ninjabrainbot " + name + ": " + displayCurrent + " → " + value + "\n";
    }

    public static String updateSettings(
        String minecraftVersion
    ) {

        String changes = "";

        int mcVersion = 0;

        if (minecraftVersion != null) {
            String[] parts =
                minecraftVersion.split("\\.");

            if (parts.length >= 2) {
                int major =
                    Integer.parseInt(parts[0]);

                int minor =
                    Integer.parseInt(parts[1]);

                if (
                    major > 1
                    || (major == 1 && minor >= 19)
                ) {
                    mcVersion = 1;
                }
            }
        }

        changes += setValue(
            "angle_adjustment_display_type",
            "1",
            "0"
        );

        changes += setValue(
            "angle_adjustment_type",
            "1",
            "0"
        );

        changes += setValue(
            "boat_error",
            "0.03",
            null
        );

        changes += setValue(
            "crosshair_correction",
            "0.0",
            null
        );

        changes += setValue(
            "default_boat_type",
            "2",
            "0"
        );

        changes += setValue(
            "mc_version",
            String.valueOf(mcVersion),
            null
        );

        changes += setValue(
            "resolution_height",
            "16384.0",
            null
        );

        changes += setValue(
            "sensitivity",
            "0.02291165",
            "0.0127275968"
        );

        changes += setValue(
            "sigma_boat",
            "7.0E-4",
            "1.0E-3"
        );

        changes += setValue(
            "use_precise_angle",
            "true",
            "false"
        );

        changes += setValue(
            "enable_http_server",
            "true",
            "false"
        );

        changes += setValue(
            "use_adv_statistics",
            "true",
            null
        );

        return changes;
    }
}