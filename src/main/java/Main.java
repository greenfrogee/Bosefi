import com.formdev.flatlaf.FlatDarkLaf;
import gui.bosefiMenu;

import javax.swing .*;

public class Main {
    public static void main(String[] args) {
        boolean debug = java.util.Arrays.asList(args).contains("--debug");

        SwingUtilities.invokeLater(() -> {
            FlatDarkLaf.setup();

            bosefiMenu app = new bosefiMenu();

            JFrame frame = new JFrame("Bosefi v1.1.0");

            app.applyBoateyeSettingsButton.addActionListener(e -> core.askEyeWiggle.ask(frame, debug));

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