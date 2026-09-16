package registry;

import java.util.prefs.Preferences;

public class boateyeSettings {

    private static final Preferences userRoot =
        Preferences.userRoot();

    private static final String NODE_NAME =
        "ninjabrainbot";

    private static Preferences preferences() {
        return userRoot.node(NODE_NAME);
    }

    public static Boolean exists() {
        try {
            return userRoot.nodeExists(NODE_NAME);
        } catch (Exception e) {
            return null;
        }
    }

    public static String setValue(
        String name,
        String value
    ) {
        String current =
            preferences().get(name, null);

        if (value.equals(current)) {
            return "";
        }

        preferences().put(name, value);

        return "Ninjabrainbot "
            + name
            + ": "
            + current
            + " → "
            + value
            + "\n";
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
            "1"
        );

        changes += setValue(
            "angle_adjustment_type",
            "1"
        );

        changes += setValue(
            "boat_error",
            "0.03"
        );

        changes += setValue(
            "crosshair_correction",
            "0.0"
        );

        changes += setValue(
            "default_boat_type",
            "2"
        );

        changes += setValue(
            "mc_version",
            String.valueOf(mcVersion)
        );

        changes += setValue(
            "resolution_height",
            "16384.0"
        );

        changes += setValue(
            "sensitivity",
            "0.02291165"
        );

        changes += setValue(
            "sigma_boat",
            "7.0E-4"
        );

        changes += setValue(
            "use_precise_angle",
            "true"
        );

        changes += setValue(
            "enable_http_server",
            "true"
        );

        changes += setValue(
            "use_adv_statistics",
            "true"
        );

        return changes;
    }
}