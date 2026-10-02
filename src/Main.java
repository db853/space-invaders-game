import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    private final int BOARD_WIDTH = 600;
    private final int BOARD_HEIGHT = 500;
    private final Timer checkGameStart;

    Main() {
        JFrame frame = new JFrame();
        frame.setSize(BOARD_WIDTH, BOARD_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        StartMenu startMenu = new StartMenu();
        frame.add(startMenu);
        frame.pack();
        frame.setVisible(true);

        checkGameStart = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (startMenu.isStartGame()) {
                    checkGameStart.stop();

                    frame.remove(startMenu);
                    frame.setSize(BOARD_WIDTH * 5/3, BOARD_HEIGHT * 2);
                    SpaceInvaders spaceInvaders = new SpaceInvaders();
                    frame.add(spaceInvaders);
                    frame.revalidate();
                    frame.setLocationRelativeTo(null);
                    frame.pack();

                    spaceInvaders.requestFocusInWindow();
                }
            }
        });
        checkGameStart.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
