package core;

import instance.getProcessId;
import toolscreen.getToolscreenSensitivity;

import javax.swing.*;

public class askEyeWiggle {
    public static void ask(JFrame frame, boolean debug) {
        String errorMessage = "";

        int pid = getProcessId.getMinecraftPID();

        if (pid == -1) {
            errorMessage +=
                    "ERROR: Minecraft is not currently running! Please launch it and try again.\n";

            if (debug) {
                System.out.println("Minecraft process not found.");
            }
        }

        boolean toolscreenConfigExists =
                getToolscreenSensitivity.exists();

        if (!toolscreenConfigExists) {
            errorMessage +=
                    "ERROR: You have not installed Toolscreen! Please install it and try again.\n";
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

        int answer = JOptionPane.showConfirmDialog(
                frame,
                "Do you know how to deal with eye wiggle?",
                "Bosefi",
                JOptionPane.YES_NO_OPTION
        );

        if (answer != JOptionPane.YES_OPTION && answer != JOptionPane.NO_OPTION) {
            return;
        }

        boolean doWeKnowHowToDealWithEyeWiggle = (answer == JOptionPane.YES_OPTION);

        runBosefi.applyBoateyeSettings(debug, frame, doWeKnowHowToDealWithEyeWiggle);
    }
}