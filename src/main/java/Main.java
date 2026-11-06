import com.formdev.flatlaf.FlatDarkLaf;
import core.askEyeWiggle;
import gui.bosefiMenu;

import javax.swing .*;
import java.awt.*;
import java.net.URI;

public class Main {
    public static void main(String[] args) {
        boolean debug = java.util.Arrays.asList(args).contains("--debug");

        SwingUtilities.invokeLater(() -> {
            FlatDarkLaf.setup();

            bosefiMenu app = new bosefiMenu();

            JFrame frame = new JFrame("Bosefi v1.1.1");

            app.applyBoateyeSettingsButton.addActionListener(e ->
                    askEyeWiggle.ask(frame, debug));

            app.stillMissingButton.addActionListener(e -> {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/greenfrogee/Bosefi/blob/main/stillmissing.md"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            frame.setIconImage(new ImageIcon(bosefiMenu.class.getResource("/bosefi.png")).getImage());
            frame.setContentPane(app.rootPanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(300, 200);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}