import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import javax.swing.*;
import javax.sound.sampled.*;

public class SpaceInvaders extends JPanel implements ActionListener, KeyListener, MouseListener {
    private final int BOARD_WIDTH = 800;
    private final int BOARD_HEIGHT = 1000;

    private static class Shelter {
        int x;
        int y;
        int width = 100;
        int height = 100;
        int stage = 0;
        Image image;

        Shelter(int x, int y, Image image) {
            this.x = x;
            this.y = y;
            this.image = image;
        }

        public Rectangle getBounds() {
            return new Rectangle(x, y, width, height);
        }
    }

    private static class Background {
        Image image;

        Background(Image image) {
            this.image = image;
        }
    }

    private class PauseButton {
        int x = BOARD_WIDTH - 40;
        int y = 10;
        int width = 30;
        int height = 30;
        Image image;

        PauseButton(Image image) {
            this.image = image;
        }
    }

    private class Player {
        int x = BOARD_WIDTH/2 - 25;
        int y = (int) (BOARD_HEIGHT /1.2);
        int width = 50;
        int height = 50;
        Image image;

        Player(Image image) {
            this.image = image;
        }

    }

    private static class Bullet {
        int x = 1500;
        int y = 1500;
        int width = 5;
        int height = 20;

    }

    private static class Enemy {
        static int width = 40;
        static int height = 32;
        int points;
        Image image;

        public int getPoints() {
            return points;
        }
    }

    private static class RedEnemy extends Enemy {
        RedEnemy(Image image) {
            this.points = 40;
            this.image = image;
        }
    }

    private static class GreenEnemy extends Enemy {
        GreenEnemy(Image image) {
            this.points = 20;
            this.image = image;
        }
    }

    private static class YellowEnemy extends Enemy {
        YellowEnemy(Image image) {
            this.points = 10;
            this.image = image;
        }
    }

    private final Image fullShelter;

    private final Background background;
    private final PauseButton pauseButton;
    private final ArrayList<Shelter> shelters = new ArrayList<>();
    private final Rectangle pauseRect;
    private final Player player;
    private final Bullet playerBullet;
    private int playerVelocityX = 0;
    private int playerBulletVelocityY = 0;
    private int score = 0;

    private final RedEnemy redEnemy;
    private final YellowEnemy yellowEnemy;
    private final GreenEnemy greenEnemy;
    private final Bullet enemyBullet;

    private int enemyBulletVelocityY = 0;

    private final ArrayList<int[]> enemies = new ArrayList<>();
    private final HashMap<Integer, Enemy> typeOfEnemy = new HashMap<>();
    private final HashMap<Integer, Image> stageOfShelter = new HashMap<>();
    private final int redIdentifier = 0;
    private final int greenIdentifier = 1;
    private final int yellowIdentifier = 2;

    private char currentDirection = 'r';
    private boolean hitRight = false;
    private boolean hitLeft = false;
    private boolean justHitLeft = false;
    private boolean justHitRight = false;
    private boolean justFired = false;
    private boolean enemyJustShoot = false;
    private boolean gameOver = false;
    private boolean paused = false;

    private Timer gameLoop;
    private Timer enemyLoop;
    private Timer checkWin;

    private final Color baseColour;
    private final Color hoverColour;
    private Color resumeRectColour;
    private Color mainMenuRectColour;
    private boolean hoveringResume = false;
    private boolean hoveringMainMenu = false;
    private Rectangle resumeRect;
    private Rectangle mainMenuRect;
    private boolean rectsInitialised = false;
    private final MouseMotionListener pauseMenuListener;

    private boolean showWaveText = false;
    private String waveText = "";
    private boolean showCountdownText = false;
    private String countdownText = "";
    private int currentWave = 0;
    private boolean inTransition = false;
    private Timer waveTransitionTimer;
    private Timer countdownTimer;

    private Clip playerExplosion;
    private Clip invaderKilled;
    private Clip shoot;

    SpaceInvaders() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);

        Image backgroundImage = new ImageIcon(getClass().getResource("./background.png")).getImage();
        Image pauseButtonImage = new ImageIcon(getClass().getResource("./pause button.png")).getImage();
        Image playerImage = new ImageIcon(getClass().getResource("./player.png")).getImage();
        Image greenEnemyImage = new ImageIcon(getClass().getResource("./green.png")).getImage();
        Image redEnemyImage = new ImageIcon(getClass().getResource("./red.png")).getImage();
        Image yellowEnemyImage = new ImageIcon(getClass().getResource("./yellow.png")).getImage();
        fullShelter = new ImageIcon(getClass().getResource("./full shelter.png")).getImage();
        Image shelterStage1 = new ImageIcon(getClass().getResource("./shelter stage 1.png")).getImage();
        Image shelterStage2 = new ImageIcon(getClass().getResource("./shelter stage 2.png")).getImage();
        Image shelterStage3 = new ImageIcon(getClass().getResource("./shelter stage 3.png")).getImage();

        background = new Background(backgroundImage);
        pauseButton = new PauseButton(pauseButtonImage);
        player = new Player(playerImage);
        playerBullet = new Bullet();
        redEnemy = new RedEnemy(redEnemyImage);
        greenEnemy = new GreenEnemy(greenEnemyImage);
        yellowEnemy = new YellowEnemy(yellowEnemyImage);
        enemyBullet = new Bullet();

        playerExplosion = loadAudio("C:\\Users\\david\\OneDrive\\Documents\\DB\\" +
                "QUB\\CSC1025\\Intellij projects\\Space Invaders\\Sounds\\explosion.wav");
        invaderKilled = loadAudio("C:\\Users\\david\\OneDrive\\Documents\\DB\\" +
                "QUB\\CSC1025\\Intellij projects\\Space Invaders\\Sounds\\invaderkilled.wav");
        shoot = loadAudio("C:\\Users\\david\\OneDrive\\Documents\\DB\\" +
                "QUB\\CSC1025\\Intellij projects\\Space Invaders\\Sounds\\shoot.wav");

        int x = BOARD_WIDTH / 8 ;
        int y = (int) (BOARD_HEIGHT / 1.5);
        for (int i = 0; i < 3; i++) {
            shelters.add(new Shelter(x, y, fullShelter));
            x += 5 * BOARD_WIDTH / 16;
        }

        typeOfEnemy.put(redIdentifier, redEnemy);
        typeOfEnemy.put(greenIdentifier, greenEnemy);
        typeOfEnemy.put(yellowIdentifier, yellowEnemy);

        stageOfShelter.put(1, shelterStage1);
        stageOfShelter.put(2, shelterStage2);
        stageOfShelter.put(3, shelterStage3);

        pauseRect = new Rectangle(pauseButton.x, pauseButton.y, pauseButton.width, pauseButton.height);
        baseColour = new Color(28, 52, 69, 242);
        hoverColour = baseColour.brighter();
        resumeRectColour = baseColour;
        mainMenuRectColour = baseColour;

        pauseMenuListener = new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (resumeRect.contains(e.getPoint()) && !hoveringResume) {
                    hoveringResume = true;
                    resumeRectColour = hoverColour;
                    System.out.println("Hover resume on");
                } else if (!resumeRect.contains(e.getPoint()) && hoveringResume) {
                    hoveringResume = false;
                    resumeRectColour = baseColour;
                    System.out.println("Hover resume off");
                }

                if (mainMenuRect.contains(e.getPoint()) && !hoveringMainMenu) {
                    hoveringMainMenu = true;
                    mainMenuRectColour = hoverColour;
                } else if (!mainMenuRect.contains(e.getPoint()) && hoveringMainMenu) {
                    hoveringMainMenu = false;
                    mainMenuRectColour = baseColour;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
            }
        };

        int startXPosition = 50;
        createEnemyWave(startXPosition);

        gameLoop = new Timer(1000/60, this);
        gameLoop.start();

        enemyLoop = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!inTransition && !gameOver) {
                    moveEnemy();
                    if (!enemyJustShoot) {
                        enemyShoot();
                    }
                }
            }
        });
        enemyLoop.start();

        checkWin = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!inTransition && !gameOver && checkIfWin()) {
                    System.out.println("Wave completed! Starting transition...");
                    inTransition = true;
                    currentWave++;

                    // Stop game timers immediately
                    if (enemyLoop != null && enemyLoop.isRunning()) {
                        enemyLoop.stop();
                    }

                    resetConditions();

                    startWaveTransition(currentWave, () -> {
                        System.out.println("Creating new wave...");
                        createEnemyWave(startXPosition);

                        // Restart timers
                        if (enemyLoop != null) {
                            enemyLoop.setDelay(500 - currentWave * 25);
                            enemyLoop.start();
                        }

                        inTransition = false;
                        System.out.println("Transition complete!");
                    });
                }
            }
        });
        checkWin.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGame(g);

        if (paused) {
            drawPauseMenu(g);
        }
    }

    public void drawGame(Graphics g) {
        g.drawImage(background.image, 0, 0, BOARD_WIDTH, BOARD_HEIGHT, null);

        g.setColor(Color.BLACK);
        g.fillRect(pauseRect.x, pauseRect.y, pauseRect.width, pauseRect.height);
        g.drawImage(pauseButton.image, pauseButton.x, pauseButton.y, pauseButton.width, pauseButton.height,
                null);


        g.drawImage(player.image, player.x, player.y, player.width, player.height, null);

        g.setColor(Color.WHITE);
        g.fillRect(playerBullet.x, playerBullet.y, playerBullet.width, playerBullet.height);

        g.setColor(Color.RED);
        g.fillRect(enemyBullet.x, enemyBullet.y, enemyBullet.width, enemyBullet.height);

        for (int[] points: enemies) {
            if (points != null) {
                if (points[2] == redIdentifier) {
                    g.drawImage(redEnemy.image, points[0], points[1], Enemy.width, Enemy.height, null);
                } else if (points[2] == greenIdentifier) {
                    g.drawImage(greenEnemy.image, points[0], points[1], Enemy.width, Enemy.height,
                            null);
                } else if (points[2] == yellowIdentifier) {
                    g.drawImage(yellowEnemy.image, points[0], points[1], Enemy.width, Enemy.height,
                            null);
                }
            }
        }

        for (Shelter shelter: shelters) {
            g.drawImage(shelter.image, shelter.x, shelter.y, shelter.width, shelter.height, null);
        }


        g.setColor(Color.WHITE);
        g.setFont(StartMenu.font.deriveFont(32f));

        if (showWaveText) {
            g.drawString(waveText, (BOARD_WIDTH - g.getFontMetrics().stringWidth(waveText)) / 2,
                    BOARD_HEIGHT / 2);
        } else if (showCountdownText) {
            g.drawString(countdownText, (BOARD_WIDTH - g.getFontMetrics().stringWidth(countdownText)) / 2,
                    BOARD_HEIGHT / 2);
        }

        if (gameOver) {
            g.drawString("Game over, final score: " + score, (BOARD_WIDTH -
                    g.getFontMetrics().stringWidth("Game over, final score: " + score)) / 2, BOARD_HEIGHT / 2);
            g.drawString("Press enter to restart", (BOARD_WIDTH -
                    g.getFontMetrics().stringWidth("Press enter to restart")) / 2, BOARD_HEIGHT / 2 + 50);
        } else {
            g.drawString("Score: " + score, 20, 30);
        }
    }

    public void drawPauseMenu(Graphics g) {
        g.setFont(StartMenu.font.deriveFont(40f));

        int pauseMenuX = (BOARD_WIDTH - 500) / 2;
        int pauseMenuY = (BOARD_HEIGHT - 400) / 2;
        int pausedX = pauseMenuX + (500 - g.getFontMetrics().stringWidth("Game paused")) / 2;
        int pausedY = pauseMenuY + 500 / 8;

        if (!rectsInitialised) {
            int resumeX = pauseMenuX + (500 - g.getFontMetrics().stringWidth("Resume")) / 2;
            int resumeY = pauseMenuY + (5 * 500 / 8);
            int mainMenuX = pauseMenuX + (500 - g.getFontMetrics().stringWidth("Main menu")) / 2;
            int mainMenuY = pauseMenuY + (6 * 500 / 8);

            resumeRect = new Rectangle(resumeX, resumeY - 40, g.getFontMetrics().stringWidth("Resume"), 50);
            mainMenuRect = new Rectangle(mainMenuX, mainMenuY - 40, g.getFontMetrics().stringWidth("Main menu"), 50);

            rectsInitialised = true;
        }

        g.setColor(baseColour);
        g.fillRect(pauseMenuX, pauseMenuY, 500, 400);
        g.setColor(Color.WHITE);
        g.drawString("Game paused", pausedX, pausedY);
        g.setColor(resumeRectColour);
        g.fillRect(resumeRect.x, resumeRect.y, resumeRect.width, resumeRect.height);
        g.setColor(mainMenuRectColour);
        g.fillRect(mainMenuRect.x, mainMenuRect.y, mainMenuRect.width, mainMenuRect.height);
        g.setColor(Color.WHITE);
        g.drawString("Resume", resumeRect.x, resumeRect.y + 35);
        g.drawString("Main menu", mainMenuRect.x, mainMenuRect.y + 35);

        repaint();
    }

    private Clip loadAudio(String filepath) {
        try {
            File audioFile = new File(filepath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    private void playAudio(Clip clip) {
       if (clip != null) {
           clip.setFramePosition(0);
           clip.start();
       }
    }

    private void startWaveTransition(int currentWave, Runnable spawnNextWave) {
        // Stop any existing transition timers
        if (waveTransitionTimer != null && waveTransitionTimer.isRunning()) {
            waveTransitionTimer.stop();
        }
        if (countdownTimer != null && countdownTimer.isRunning()) {
            countdownTimer.stop();
        }

        waveText = "Wave " + currentWave  + " Completed!";
        showWaveText = true;
        repaint();

        waveTransitionTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showWaveText = false;
                repaint();
                waveTransitionTimer.stop();
                startCountdown(spawnNextWave);
            }
        });
        waveTransitionTimer.setRepeats(false);
        waveTransitionTimer.start();
    }

    private void startCountdown(Runnable afterCountdown) {
        showCountdownText = true;

        final int[] count = {3};
        countdownText = "Next wave in " + count[0];
        repaint();

        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                count[0]--;

                if (count[0] > 0) {
                    countdownText = "Next wave in " + count[0];
                    repaint();
                } else if (count[0] == 0) {
                    countdownText = "Go!";
                    repaint();
                } else {
                    countdownTimer.stop();
                    showCountdownText = false;
                    repaint();
                    afterCountdown.run();
                }
            }
        });
        countdownTimer.start();
    }

    private void pauseGame() {
        paused = true;
        inTransition = true;
        gameLoop.stop();
        enemyLoop.stop();
        checkWin.stop();
        addMouseMotionListener(pauseMenuListener);
        repaint();
    }

    private void resumeGame() {
        paused = false;
        inTransition = false;
        gameLoop.start();
        enemyLoop.start();
        checkWin.start();
        removeMouseMotionListener(pauseMenuListener);
        repaint();

    }

    public void resetConditions() {
        hitRight = false;
        hitLeft = false;
        justHitLeft = false;
        justHitRight = false;
        justFired = false;
        enemyJustShoot = false;

        enemies.clear();
        shelters.clear();
        enemyBullet.x = 1500;
        currentDirection = 'r';
    }

    public void createEnemyWave(int startXPosition) {
        int redYPosition = 50;
        int greenYPosition1 = 100;
        int greenYPosition2 = 150;
        int yellowYPosition1 = 200;
        int yellowYPosition2 = 250;

        for (int i = 1; i < 51; i++) {
            startXPosition += 50;
            if (i % 10 == 0) {
                startXPosition = 50;
            }

            if (i <= 10) {
                enemies.add(new int[] {startXPosition, redYPosition, redIdentifier});
            } else if (i <= 20) {
                enemies.add(new int[] {startXPosition, greenYPosition1, greenIdentifier});
            } else if (i <= 30) {
                enemies.add(new int[] {startXPosition, greenYPosition2, greenIdentifier});
            } else if (i <= 40) {
                enemies.add(new int[] {startXPosition, yellowYPosition1, yellowIdentifier});
            } else {
                enemies.add(new int[] {startXPosition, yellowYPosition2, yellowIdentifier});
            }
        }

        int x = BOARD_WIDTH / 8 ;
        int y = (int) (BOARD_HEIGHT / 1.5);
        for (int i = 0; i < 3; i++) {
            shelters.add(new Shelter(x, y, fullShelter));
            x += 5 * BOARD_WIDTH / 16;
        }

    }

    public void move() {
        player.x += playerVelocityX;
        if (player.x <= 0) {
            player.x = 0;
        } else if (player.x >= BOARD_WIDTH - player.width) {
            player.x = BOARD_WIDTH - player.width;
        }

        playerBullet.y -= playerBulletVelocityY;
        enemyBullet.y += enemyBulletVelocityY;

        Rectangle playerRect = new Rectangle(player.x, player.y, player.width, player.height);
        Rectangle playerBulletRect = new Rectangle(playerBullet.x, playerBullet.y, playerBullet.width,
                playerBullet.height);
        Rectangle enemyBulletRect = new Rectangle(enemyBullet.x, enemyBullet.y, enemyBullet.width,
                enemyBullet.height);

        for (int[] points: enemies) {
            if (points != null) {
                Rectangle enemyRect = new Rectangle(points[0], points[1], Enemy.width, Enemy.height);
                if (enemyRect.intersects(playerBulletRect)) {
                    playAudio(invaderKilled);
                    Enemy enemyType = typeOfEnemy.get(points[2]);
                    score += enemyType.getPoints();

                    enemies.set(enemies.indexOf(points), null);
                    playerBullet.x = 1500;
                    playerBullet.y = 1500;
                    justFired = false;
                }
            }
        }

        for (int i = shelters.size() - 1; i >= 0; i--) {
            Shelter shelter = shelters.get(i);

            if (shelter.getBounds().intersects(enemyBulletRect)) {
                enemyBullet.x = 1500;
                shelter.stage++;

                if (shelter.stage >= 4) {
                    shelters.remove(i);
                    continue;
                }

                shelter.image = stageOfShelter.get(shelter.stage);
            }
        }


        if (playerBullet.y <= 0) {
            playerBullet.x = 1500;
            playerBullet.y = 1500;
            justFired = false;
        }

        if (playerRect.intersects(enemyBulletRect)) {
            playAudio(playerExplosion);
            gameOver = true;
            enemyJustShoot = false;
        } else if (enemyBullet.y >= BOARD_HEIGHT) {
            enemyBullet.x = 1500;
            enemyBullet.y = 1500;
            enemyJustShoot = false;
        }
    }

    public boolean checkIfWin() {
        if (enemies.isEmpty()) {
            return false;
        }

        int deathCounter = 0;

        for (int[] points: enemies) {
            if (points == null) {
                deathCounter += 1;
            }
        }
        return deathCounter == 50 && enemies.size() == 50;
    }

    public void moveEnemy() {
        for (int[] points: enemies) {
            if (points != null) {
                if (points[0] >= BOARD_WIDTH - 60) {
                    if (!justHitRight) {
                        hitRight = true;
                        break;
                    }
                } else if (points[0] <= 25) {
                    if (!justHitLeft) {
                        hitLeft = true;
                        break;
                    }
                }
            }
        }

        if (hitLeft) {
            currentDirection = 'r';
            for (int[] points : enemies) {
                if (points != null) {
                    points[1] += 20;
                }
            }
            justHitLeft = true;
            justHitRight = false;
            hitLeft = false;
            if (enemyLoop.getDelay() > 200) {
                enemyLoop.setDelay(enemyLoop.getDelay() - 25);
            } else {
                enemyLoop.setDelay(200);
            }


        } else if (hitRight){
            currentDirection = 'l';
            for (int[] points: enemies) {
                if (points != null) {
                    points[1] += 20;
                }
            }
            justHitRight = true;
            justHitLeft = false;
            hitRight = false;
            if (enemyLoop.getDelay() > 200) {
                enemyLoop.setDelay(enemyLoop.getDelay() - 25);
            } else {
                enemyLoop.setDelay(200);
            }

        } else {
            for (int[] points : enemies) {
                if (currentDirection == 'r') {
                    if (points != null) {
                        points[0] += 20;
                    }
                } else {
                    if (points != null) {
                        points[0] -= 20;
                    }
                }
            }
        }
    }

    public void enemyShoot() {
        // Safety check - don't shoot if no enemies
        if (enemies.isEmpty()) {
            return;
        }

        boolean nonNull = false;
        Random random = new Random();
        int randomIndex;
        int[] enemyPoint = {};

        int attempts = 0;
        while (!nonNull && attempts < 100) {
            randomIndex = random.nextInt(0, enemies.size());

            if (enemies.get(randomIndex) != null) {
                nonNull = true;
                enemyPoint = enemies.get(randomIndex);
            }
            attempts++;
        }

        if (!nonNull) {
            return; // No valid enemy found
        }

        enemyBullet.x = enemyPoint[0] + (Enemy.width / 2) - (enemyBullet.width / 2);
        enemyBullet.y = enemyPoint[1] + 5;

        enemyBulletVelocityY = 10;
        enemyJustShoot = true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!inTransition && !gameOver) {
            move();
        }
        repaint();

        if (gameOver) {
            resetConditions();
            enemyBullet.x = 1500;

            if (checkWin != null) {
                checkWin.stop();
            }
            if (enemyLoop != null) {
                enemyLoop.stop();
            }
            if (gameLoop != null) {
                gameLoop.stop();
            }
            if (waveTransitionTimer != null && waveTransitionTimer.isRunning()) {
                waveTransitionTimer.stop();
            }
            if (countdownTimer != null && countdownTimer.isRunning()) {
                countdownTimer.stop();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
                playerVelocityX = 5;
                break;

            case KeyEvent.VK_LEFT:
                playerVelocityX = -5;
                break;

            case KeyEvent.VK_SPACE:
                if (!justFired) {
                    playAudio(shoot);
                    playerBullet.x = player.x + (player.width / 2) - (playerBullet.width / 2);
                    playerBullet.y = player.y - 5;
                    playerBulletVelocityY = 50;
                    justFired = true;
                }
                break;

            case KeyEvent.VK_ENTER:
                if (gameOver) {
                    SwingUtilities.invokeLater(() -> {
                        JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);

                        // Stop ALL timers
                        if (gameLoop != null) {
                            gameLoop.stop();
                        }
                        if (enemyLoop != null) {
                            enemyLoop.stop();
                        }
                        if (checkWin != null) {
                            checkWin.stop();
                        }
                        if (waveTransitionTimer != null) {
                            waveTransitionTimer.stop();
                        }
                        if (countdownTimer != null) {
                            countdownTimer.stop();
                        }

                        SpaceInvaders newGame = new SpaceInvaders();
                        frame.remove(this);

                        frame.add(newGame);
                        frame.revalidate();
                        frame.repaint();

                        newGame.requestFocusInWindow();
                    });
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT) {
            playerVelocityX = 0;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (pauseRect.contains(e.getPoint())) {
            pauseGame();
        }

        if (resumeRect != null) {
            if (resumeRect.contains(e.getPoint())) {
                resumeGame();
            }
        }

        if (mainMenuRect != null) {
            if (mainMenuRect.contains(e.getPoint())) {
                SwingUtilities.getWindowAncestor(this).dispose();
                SwingUtilities.invokeLater(Main::new);
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}