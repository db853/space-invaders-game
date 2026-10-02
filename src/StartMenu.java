import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

public class StartMenu extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
    private final int BOARD_WIDTH = 600;
    private final int BOARD_HEIGHT = 500;

    protected static Font font;
    private final Rectangle rect;
    private final Color baseColour;
    private final Color hoverColour;
    private Color rectColour;

    private boolean startGame = false;
    private boolean hovering = false;

    StartMenu() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        addMouseListener(this);
        addMouseMotionListener(this);

        rect = new Rectangle((BOARD_WIDTH - 300) / 2, 3 * ((BOARD_HEIGHT - 75) / 4), 300, 75);
        baseColour = Color.decode("#082031");
        hoverColour = baseColour.brighter();
        rectColour = baseColour;

        try {
            String pathname = "C:\\Users\\david\\OneDrive\\Documents\\DB\\" +
                    "QUB\\CSC1025\\Intellij projects\\Space Invaders\\Fonts\\ElectronPulseItalic-6YJX1.ttf";
            font = Font.createFont(Font.TRUETYPE_FONT, new File(pathname)).deriveFont(64f);
        } catch (FontFormatException | IOException e) {
            System.out.println("Error");
        }

        Timer startTimer = new Timer(1000 / 60, this);
        startTimer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.setColor(Color.decode("#082031"));
        g.fillRect(0, 0, BOARD_WIDTH, BOARD_HEIGHT);

        g.setFont(font);
        g.setColor(Color.WHITE);
        int titleX = (BOARD_WIDTH - g.getFontMetrics().stringWidth("Space Invaders")) / 2;
        int titleY = BOARD_HEIGHT / 4;
        g.drawString("Space Invaders", titleX, titleY);

        g.setColor(rectColour);
        g.fillRect(rect.x, rect.y, rect.width, rect.height);

        int startX = (BOARD_WIDTH - g.getFontMetrics().stringWidth("Start")) / 2;
        int startY = 3 * BOARD_HEIGHT / 4;
        g.setColor(Color.WHITE);
        g.drawString("Start", startX, startY);
    }

    public boolean isStartGame() {
        return startGame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (rect.contains(e.getPoint())) {
            startGame = true;
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (rect.contains(e.getPoint()) && !hovering) {
            hovering = true;
            rectColour = hoverColour;
        } else if (!rect.contains(e.getPoint()) && hovering) {
            hovering = false;
            rectColour = baseColour;
        }

    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

}
