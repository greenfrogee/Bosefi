package gui;

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
import javax.swing.JFrame;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.BorderFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

public class runBosefi {

    public static void main(String[] args) {
        boolean debug = java.util.Arrays.asList(args).contains("--debug");

        SwingUtilities.invokeLater(() -> createWindow(debug));
    }

    private static void createWindow(boolean debug) {
        JFrame frame = new JFrame("Bosefi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ImageIcon icon = new ImageIcon(
            runBosefi.class.getResource("/bosefi.png")
        );
        frame.setIconImage(icon.getImage());

        JButton applyButton = new JButton("Apply Boateye Settings");

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.add(applyButton);

        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        applyButton.addActionListener(e -> {
            applyButton.setEnabled(false);

            try {
                applyBoateyeSettings(debug, frame);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                    frame,
                    "An unexpected error occurred:\n" + ex.getMessage(),
                    "Bosefi",
                    JOptionPane.ERROR_MESSAGE
                );
            } finally {
                if (frame.isDisplayable()) {
                    applyButton.setEnabled(true);
                }
            }
        });
    }

    public static void applyBoateyeSettings(boolean debug, JFrame frame) {
        String errorMessage = "";

        int pid = getProcessId.getMinecraftPID();

        if (pid == -1) {
            errorMessage +=
                "ERROR: Minecraft is not currently running! Please launch it, then run Bosefi again.\n";

            if (debug) {
                System.out.println("[Debug] Minecraft process not found.");
            }
        }

        boolean toolscreenConfigExists =
            getToolscreenSensitivity.exists();

        if (!toolscreenConfigExists) {
            errorMessage +=
                "ERROR: You have not installed Toolscreen! Please install it, then run Bosefi again.\n";
        }

        if (!errorMessage.isEmpty()) {
            if (debug) {
                System.out.println("Error message is not nothing; telling user errors");
            }

            JOptionPane.showMessageDialog(
                frame,
                errorMessage,
                "Bosefi",
                JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        String minecraftVersion =
            getMinecraftVersion.get(pid, debug);

        Path instancePath =
            getInstancePath.getInstancePath(pid);

        boolean optionsTxt =
            getOptionsTxt.exists(instancePath);

        boolean standardSettings =
            getStandardSettings.exists(instancePath);

        if (debug) {
            System.out.println();
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
        }

        Double standardSettingsMouseSensitivity = null;
        Double optionsTxtMouseSensitivity = null;

        if (standardSettings) {
            standardSettingsMouseSensitivity =
                getStandardSettingsSensitivity.get(instancePath);

            if (debug) {
                System.out.println();
                System.out.println("STANDARD SETTINGS MOUSE SENSITIVITY:");
                System.out.println(standardSettingsMouseSensitivity);
            }
        }

        if (optionsTxt) {
            optionsTxtMouseSensitivity =
                getOptionsTxtSensitivity.get(instancePath);

            if (debug) {
                System.out.println();
                System.out.println("OPTIONS.TXT MOUSE SENSITIVITY:");
                System.out.println(optionsTxtMouseSensitivity);
            }
        }

        Double toolscreenSensitivity =
            getToolscreenSensitivity.get(
                optionsTxtMouseSensitivity,
                standardSettingsMouseSensitivity
            );

        if (debug) {
            System.out.println();
            System.out.println("TOOLSCREEN SENSITIVITY:");
            System.out.println(toolscreenSensitivity);
        }

        String activeProfile =
            getActiveProfile.get();

        if (activeProfile == null) {
            JOptionPane.showMessageDialog(
                frame,
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

        if (debug) {
            System.out.println();
            System.out.println("ACTIVE TOOLSCREEN PROFILE:");
            System.out.println(activeProfile);
        }

        String changes = "";

        if (standardSettingsMouseSensitivity != null) {
            changes +=
                fixStandardSettingsSensitivity.fix(
                    instancePath,
                    standardSettingsMouseSensitivity
                );
        }

        if (optionsTxtMouseSensitivity != null) {
            changes +=
                fixOptionsTxtSensitivity.fix(
                    instancePath,
                    optionsTxtMouseSensitivity
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
            boateyeSettings.updateSettings(minecraftVersion);

        if (changes.isEmpty()) {
            changes = "No changes made.";
        }

        if (!changes.isEmpty()) {
            changes = changes
                .replace("null", "Disabled")
                .replace("false", "Disabled")
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
                .replace("sigma_boat", "Standard deviation for boat throws")

                .replace("1.0E-3", "0.001")
                .replace("7.0E-4", "0.0007");
        }

        if (debug) {
            System.out.println();
            System.out.println("CHANGES:");
            System.out.println(changes);
        }

        if (!changes.isEmpty()) {
            changes += "\n\nMake sure to relaunch Minecraft! Also relaunch Ninjabrain Bot and click Reset!";
            changes += "\n\nIf you're still missing after running Bosefi, use /stillmissing in Discord for more solutions.";
        }

        JOptionPane.showMessageDialog(
                frame,
                "Changes made:\n"
                        + changes,
                "Bosefi",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}