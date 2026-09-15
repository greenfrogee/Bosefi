import instance.getProcessId;
import instance.getInstancePath;
import instance.getMinecraftVersion;

import options.getOptionsTxt;
import options.getStandardSettings;
import options.getStandardSettingsSensitivity;
import options.getOptionsTxtSensitivity;
import options.fixStandardSettingsSensitivity;
import options.fixOptionsTxtSensitivity;

import toolscreen.getToolscreenSensitivity;
import toolscreen.fixToolscreenSensitivity;

import registry.boateyeSettings;

import javax.swing.JOptionPane;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {

        int pid = getProcessId.getMinecraftPID();

        if (pid == -1) {
            System.out.println("Minecraft process not found.");
            return;
        }

        String minecraftVersion =
            getMinecraftVersion.get(pid);

        Path instancePath =
            getInstancePath.getInstancePath(pid);

        boolean optionsTxt =
            getOptionsTxt.exists(instancePath);

        boolean standardSettings =
            getStandardSettings.exists(instancePath);

        String changes = "";

        System.out.println("PID:");
        System.out.println(pid);

        System.out.println();
        System.out.println("MINECRAFT VERSION:");
        System.out.println(minecraftVersion);

        System.out.println();
        System.out.println("INSTANCE PATH:");
        System.out.println(instancePath);

        System.out.println();
        System.out.println("OPTIONS.TXT:");
        System.out.println(optionsTxt);

        System.out.println();
        System.out.println("STANDARDSETTINGS.JSON:");
        System.out.println(standardSettings);

        Double standardSettingsMouseSensitivity = null;
        Double optionsTxtMouseSensitivity = null;

        if (standardSettings) {
            standardSettingsMouseSensitivity =
                getStandardSettingsSensitivity.get(instancePath);

            System.out.println();
            System.out.println("STANDARD SETTINGS MOUSE SENSITIVITY:");
            System.out.println(standardSettingsMouseSensitivity);

            if (standardSettingsMouseSensitivity != null) {
                changes +=
                    fixStandardSettingsSensitivity.fix(instancePath);
            }
        }

        if (optionsTxt) {
            optionsTxtMouseSensitivity =
                getOptionsTxtSensitivity.get(instancePath);

            System.out.println();
            System.out.println("OPTIONS.TXT MOUSE SENSITIVITY:");
            System.out.println(optionsTxtMouseSensitivity);

            if (optionsTxtMouseSensitivity != null) {
                changes +=
                    fixOptionsTxtSensitivity.fix(instancePath);
            }
        }

        Double toolscreenSensitivity =
            getToolscreenSensitivity.get(
                optionsTxtMouseSensitivity,
                standardSettingsMouseSensitivity
            );

        System.out.println();
        System.out.println("TOOLSCREEN SENSITIVITY:");
        System.out.println(toolscreenSensitivity);

        Path toolscreenConfig =
            Paths.get(
                System.getProperty("user.home"),
                ".config",
                "toolscreen",
                "config.toml"
            );

        Path toolscreenProfile =
            Paths.get(
                System.getProperty("user.home"),
                ".config",
                "toolscreen",
                "profiles",
                "Default.toml"
            );

        changes +=
            fixToolscreenSensitivity.fix(
                toolscreenConfig,
                toolscreenSensitivity,
                "Toolscreen config sensitivity"
            );

        changes +=
            fixToolscreenSensitivity.fix(
                toolscreenProfile,
                toolscreenSensitivity,
                "Toolscreen default profile sensitivity"
            );

        changes +=
            boateyeSettings.updateSettings(minecraftVersion);

        if (changes.isEmpty()) {
            changes = "No changes made.";
        }

        JOptionPane.showMessageDialog(
            null,
            "Changes Made:\n" + changes,
            "Bosefi",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}