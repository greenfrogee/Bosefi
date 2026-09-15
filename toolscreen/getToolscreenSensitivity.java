package toolscreen;

public class getToolscreenSensitivity {

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

        double numerator =
            Math.pow((0.6 * sens) + 0.2, 3) * 1.2;

        double denominator =
            Math.pow((0.6 * 0.02291165) + 0.2, 3) * 1.2;

        return Math.round((numerator / denominator) * 100.0) / 100.0;
    }
}