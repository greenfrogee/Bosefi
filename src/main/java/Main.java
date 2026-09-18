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
import toolscreen.getActiveProfile;

import registry.boateyeSettings;

import javax.swing.JOptionPane;
import javax.swing.JLabel;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.awt.Desktop;
import java.net.URI;

public class Main {

public static void main(String[] args) {

    String errorMessage = "";

    int pid = getProcessId.getMinecraftPID();
    System.out.println(pid);

    if (pid == -1) {
        errorMessage +=
            "ERROR: Minecraft is not currently running! Please launch it, then run Bosefi again.\n";

        System.out.println("Minecraft process not found.");
    }

    Boolean boateyeSettingsExists =
        boateyeSettings.exists();

    System.out.println(
        "DO BOATEYE SETTINGS EXIST?"
        + boateyeSettingsExists
    );

    if (
        boateyeSettingsExists == null
        || !boateyeSettingsExists
    ) {
        errorMessage +=
            "ERROR: You have not installed Ninjabrainbot! Please run it once, then run Bosefi again.\n";
    }

    boolean toolscreenConfigExists =
        getToolscreenSensitivity.exists();

    if (!toolscreenConfigExists) {
        errorMessage +=
            "ERROR: You have not installed Toolscreen! Please install it, then run Bosefi again.\n";
    }

    if (!errorMessage.isEmpty()) {
        System.out.println(
            "error message is not nothing; telling user errors"
        );

        JOptionPane.showMessageDialog(
            null,
            errorMessage,
            "Bosefi",
            JOptionPane.ERROR_MESSAGE
        );

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
            getStandardSettingsSensitivity.get(
                instancePath
            );

        System.out.println();
        System.out.println(
            "STANDARD SETTINGS MOUSE SENSITIVITY:"
        );
        System.out.println(
            standardSettingsMouseSensitivity
        );
    }

    if (optionsTxt) {
        optionsTxtMouseSensitivity =
            getOptionsTxtSensitivity.get(
                instancePath
            );

        System.out.println();
        System.out.println(
            "OPTIONS.TXT MOUSE SENSITIVITY:"
        );
        System.out.println(
            optionsTxtMouseSensitivity
        );
    }

    Double toolscreenSensitivity =
        getToolscreenSensitivity.get(
            optionsTxtMouseSensitivity,
            standardSettingsMouseSensitivity
        );

    System.out.println();
    System.out.println("TOOLSCREEN SENSITIVITY:");
    System.out.println(toolscreenSensitivity);

    String activeProfile =
        getActiveProfile.get();

    if (activeProfile == null) {
        JOptionPane.showMessageDialog(
            null,
            "ERROR: Could not determine the active Toolscreen profile.",
            "Bosefi",
            JOptionPane.ERROR_MESSAGE
        );

        return;
    }

    Path toolscreenProfile =
        Paths.get(
            System.getProperty("user.home"),
            ".config",
            "toolscreen",
            "profiles",
            activeProfile + ".toml"
        );

    System.out.println();
    System.out.println("ACTIVE TOOLSCREEN PROFILE:");
    System.out.println(activeProfile);

    int confirmation =
        JOptionPane.showConfirmDialog(
            null,
            "Are you sure you want to change to boateye settings?",
            "Bosefi",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.INFORMATION_MESSAGE
        );

    if (confirmation != JOptionPane.OK_OPTION) {
        return;
    }

    String changes = "";

    if (standardSettingsMouseSensitivity != null) {
        changes +=
            fixStandardSettingsSensitivity.fix(
                instancePath
            );
    }

    if (optionsTxtMouseSensitivity != null) {
        changes +=
            fixOptionsTxtSensitivity.fix(
                instancePath
            );
    }

    if (toolscreenSensitivity != null) {
        changes +=
            fixToolscreenSensitivity.fix(
                toolscreenProfile,
                toolscreenSensitivity,
                "Toolscreen profile " + activeProfile
            );
    }

    changes +=
        boateyeSettings.updateSettings(
            minecraftVersion
        );

    if (changes.isEmpty()) {
        changes =
            "No changes made.";
    }

    if (!changes.isEmpty()) {
        changes = changes
            .replace("null", "Disabled")
            .replace("true", "Enabled")

            .replace("mc_version", "Minecraft version")
            .replace("crosshair_correction", "Crosshair correction")
            .replace("use_adv_statistics", "Use advanced stronghold statistics")
            .replace("enable_http_server", "Enable API")
            .replace("angle_adjustment_display_type", "Adjustment display type")
            .replace("angle_adjustment_type", "Pixel adjustment type")
            .replace("resolution_height", "Resolution height")
            .replace("use_precise_angle", "Enable boat measurements")
            .replace("Ninjabrainbot sensitivity", "Ninjabrainbot Sensitivity 1.13+")
            .replace("default_boat_type", "Default boat mode")
            .replace("boat_error", "Allowable boat angle error")
            .replace("sigma_boat", "Standard deviation for boat throws");
    }

    JLabel message =
        new JLabel(
            "<html>"
            + "Changes Made:<br>"
            + changes.replace("\n", "<br>")
            + "<br><br>"
            + "Make sure you're aware of eye wiggle when measuring! "
            + "See here for more info: "
            + "<a href='https://frontcage.com/t/what-to-do-about-eye-wiggle/14'>"
            + "https://frontcage.com/t/what-to-do-about-eye-wiggle/14"
            + "</a>"
            + "</html>"
        );

    message.addMouseListener(
        new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(
                java.awt.event.MouseEvent e
            ) {
                try {
                    Desktop.getDesktop().browse(
                        new URI(
                            "https://frontcage.com/t/what-to-do-about-eye-wiggle/14"
                        )
                    );
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    );

    Thread exitThread =
        new Thread(() -> {
            try {
                Thread.sleep(60000);
                System.exit(0);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    );

    exitThread.setDaemon(true);
    exitThread.start();

    JOptionPane.showMessageDialog(
        null,
        message,
        "Bosefi",
        JOptionPane.INFORMATION_MESSAGE
        );
    }
}