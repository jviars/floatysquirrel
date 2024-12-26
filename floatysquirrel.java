import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.prefs.Preferences;

public class FloatySquirrel extends JFrame {
    private static final int WIDTH = 1200;
    private static final int HEIGHT = 600;
    private static final int GROUND_LEVEL = HEIGHT - 100;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private int highScore = 0;
    private Preferences prefs;
    private boolean soundEnabled = true;
    private Clip scoreSound;

    // Modern color scheme
    public static final Color ACCENT_COLOR = new Color(165, 107, 79);   // Warm brown
    public static final Color DARK_ACCENT = new Color(101, 67, 33);     // Dark brown
    public static final Color LIGHT_ACCENT = new Color(203, 166, 147);  // Light brown
    public static final Color BG_COLOR = new Color(241, 233, 218);      // Cream
    public static final Color TEXT_SHADOW = new Color(0, 0, 0, 60);     // Soft shadow

    public FloatySquirrel() {
        prefs = Preferences.userNodeForPackage(FloatySquirrel.class);
        highScore = prefs.getInt("highScore", 0);
        soundEnabled = prefs.getBoolean("soundEnabled", true);

        setTitle("Floaty Squirrel");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        initializeSound();

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        MainMenuPanel menuPanel = new MainMenuPanel(this);
        GamePanel gamePanel = new GamePanel(this);
        HighScorePanel highScorePanel = new HighScorePanel(this);
        SettingsPanel settingsPanel = new SettingsPanel(this);

        cardPanel.add(menuPanel, "Menu");
        cardPanel.add(gamePanel, "Game");
        cardPanel.add(highScorePanel, "HighScore");
        cardPanel.add(settingsPanel, "Settings");

        add(cardPanel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initializeSound() {
        try {
            AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            // Create a softer, more pleasant chime sound
            for (int i = 0; i < 44100 * 0.15; i++) {
                double angle = i / (44100f / 880) * 2.0 * Math.PI;  // Higher frequency
                double fade = 1.0 - (i / (44100f * 0.15));  // Fade out
                short sample = (short) (Math.sin(angle) * 3000 * fade);  // Softer volume
                dos.writeShort(sample);
            }

            byte[] audioData = baos.toByteArray();
            AudioInputStream ais = new AudioInputStream(
                    new ByteArrayInputStream(audioData),
                    format,
                    audioData.length / format.getFrameSize()
            );

            scoreSound = AudioSystem.getClip();
            scoreSound.open(ais);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playScoreSound() {
        if (soundEnabled && scoreSound != null) {
            if (scoreSound.isRunning()) {
                scoreSound.stop();
            }
            scoreSound.setFramePosition(0);
            scoreSound.start();
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        prefs.putBoolean("soundEnabled", enabled);
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setResizable(boolean resizable) {
        super.setResizable(resizable);
        prefs.putBoolean("windowResizable", resizable);
    }

    public void switchToPanel(String panelName) {
        cardLayout.show(cardPanel, panelName);
        if (panelName.equals("Game")) {
            cardPanel.getComponent(1).requestFocusInWindow();
        }
    }

    public void updateHighScore(int score) {
        if (score > highScore) {
            highScore = score;
            prefs.putInt("highScore", highScore);
        }
    }

    public int getHighScore() {
        return highScore;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FloatySquirrel());
    }
}
class Background {
    private ArrayList<Cloud> clouds;
    private Random random;

    public Background() {
        clouds = new ArrayList<>();
        random = new Random();
        for (int i = 0; i < 8; i++) {  // More clouds for wider screen
            addCloud(random.nextInt(1200));
        }
    }

    private void addCloud(int x) {
        clouds.add(new Cloud(x, random.nextInt(200) + 50));
    }

    public void update() {
        for (int i = clouds.size() - 1; i >= 0; i--) {
            Cloud cloud = clouds.get(i);
            cloud.update();
            if (cloud.getX() + cloud.getWidth() < 0) {
                clouds.remove(i);
                addCloud(1200);  // Add new cloud at right edge
            }
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        // Forest-themed sky gradient
        GradientPaint skyGradient = new GradientPaint(
                0, 0, new Color(158, 203, 227),  // Light blue
                0, 400, new Color(209, 231, 240)  // Pale blue
        );
        g2d.setPaint(skyGradient);
        g2d.fillRect(0, 0, 1200, 600);

        for (Cloud cloud : clouds) {
            cloud.draw(g);
        }

        // Forest-themed ground gradient
        GradientPaint groundGradient = new GradientPaint(
                0, 500, new Color(76, 115, 61),    // Dark forest green
                0, 600, new Color(52, 78, 42)      // Deeper forest green
        );
        g2d.setPaint(groundGradient);
        g2d.fillRect(0, 500, 1200, 100);

        // Grass details
        g.setColor(new Color(60, 91, 48));
        for (int x = 0; x < 1200; x += 8) {  // More dense grass
            int grassHeight = random.nextInt(12) + 6;
            g.drawLine(x, 500, x, 500 - grassHeight);
        }
    }
}

class Cloud {
    private int x, y;
    private int width;
    private static final int SPEED = 1;

    public Cloud(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = 60 + new Random().nextInt(40);
    }

    public void update() {
        x -= SPEED;
    }

    public void draw(Graphics g) {
        g.setColor(new Color(255, 255, 255, 220));  // Slightly transparent clouds
        g.fillOval(x, y, width, 30);
        g.fillOval(x + 20, y - 10, width - 20, 30);
        g.fillOval(x + 10, y + 10, width - 10, 20);
    }

    public int getX() { return x; }
    public int getWidth() { return width; }
}

class Squirrel {
    private int x, y;
    private double velocity;
    private static final double GRAVITY = 0.4;
    private static final double GLIDE_FORCE = -8;
    private static final int SIZE = 40;
    private double glideAngle = 0;

    public Squirrel() {
        reset();
    }

    public void reset() {
        x = 100;
        y = 300;
        velocity = 0;
        glideAngle = 0;
    }

    public void update() {
        velocity += GRAVITY;
        y += velocity;
        glideAngle = Math.atan2(velocity, 5) * 0.7;  // Smooth rotation based on velocity
    }

    public void glide() {
        velocity = GLIDE_FORCE;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Save the current transform
        AffineTransform old = g2d.getTransform();

        // Rotate around the squirrel's center
        g2d.rotate(glideAngle, x + SIZE/2, y + SIZE/2);

        // Body (oval shape)
        g2d.setColor(new Color(139, 69, 19));  // Saddle brown
        g2d.fillOval(x, y, SIZE, SIZE/2);

        // Tail (fluffy, curved shape)
        g2d.setColor(new Color(160, 82, 45));  // Sienna
        int[] xPoints = {x - 5, x - 15, x - 10, x};
        int[] yPoints = {y + SIZE/4, y + SIZE/4 - 5, y + SIZE/4 + 10, y + SIZE/4 + 5};
        g2d.fillPolygon(xPoints, yPoints, 4);

        // Gliding membrane
        g2d.setColor(new Color(169, 169, 169, 180));  // Semi-transparent gray
        g2d.fillArc(x - 5, y - 5, SIZE + 10, SIZE + 10, 0, 180);

        // Head
        g2d.setColor(new Color(139, 69, 19));  // Saddle brown
        g2d.fillOval(x + SIZE - 15, y - 5, SIZE/2, SIZE/2);

        // Eye
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x + SIZE - 8, y + 2, 4, 4);

        // Nose
        g2d.fillOval(x + SIZE + 2, y + 5, 3, 3);

        // Restore the original transform
        g2d.setTransform(old);
    }

    public void drawAt(Graphics g, int drawX, int drawY) {
        int tempX = x;
        int tempY = y;
        x = drawX - SIZE/2;
        y = drawY - SIZE/2;
        draw(g);
        x = tempX;
        y = tempY;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, SIZE, SIZE/2);
    }

    public int getY() {
        return y;
    }
}

class TreeObstacle {
    private int x;
    private int height;
    private int width;
    private int gap;

    public TreeObstacle(int x, int height, int width, int gap) {
        this.x = x;
        this.height = height;
        this.width = width;
        this.gap = gap;
    }

    public void update() {
        x -= 3;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Tree trunk gradient
        GradientPaint trunkGradient = new GradientPaint(
                x, 0, new Color(101, 67, 33),  // Dark brown
                x + width, 0, new Color(139, 69, 19)  // Saddle brown
        );
        g2d.setPaint(trunkGradient);

        // Top tree section
        g2d.fillRect(x, 0, width, height);
        // Top tree crown
        int crownWidth = width + 20;
        g2d.setColor(new Color(47, 79, 47));  // Dark green
        g2d.fillOval(x - 10, height - 30, crownWidth, 40);

        // Bottom tree section
        int bottomStart = height + gap;
        g2d.setPaint(trunkGradient);
        g2d.fillRect(x, bottomStart, width, 600 - bottomStart);
        // Bottom tree crown
        g2d.setColor(new Color(47, 79, 47));
        g2d.fillOval(x - 10, bottomStart - 10, crownWidth, 40);
    }

    public boolean collidesWith(Squirrel squirrel) {
        Rectangle squirrelRect = squirrel.getBounds();
        Rectangle topTree = new Rectangle(x, 0, width, height);
        Rectangle bottomTree = new Rectangle(x, height + gap, width, 600 - (height + gap));

        return squirrelRect.intersects(topTree) || squirrelRect.intersects(bottomTree);
    }

    public int getX() {
        return x;
    }
}
class MainMenuPanel extends JPanel {
    private FloatySquirrel game;
    private Background background;
    private Squirrel mascot;
    private float mascotY = 250;
    private float mascotVelocity = 0;
    private Timer animationTimer;

    public MainMenuPanel(FloatySquirrel game) {
        this.game = game;
        this.background = new Background();
        this.mascot = new Squirrel();
        setLayout(new GridBagLayout());

        animationTimer = new Timer(16, e -> {
            background.update();
            mascotVelocity += 0.2;
            mascotY += mascotVelocity;
            if (mascotY > 270) {
                mascotY = 270;
                mascotVelocity = -4;
            }
            repaint();
        });
        animationTimer.start();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 50, 10, 50);

        JPanel titlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                setOpaque(false);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                String title = "Floaty Squirrel";
                g2d.setFont(new Font("Arial", Font.BOLD, 48));

                g2d.setColor(new Color(0, 0, 0, 60));
                g2d.drawString(title, 53, 53);

                g2d.setColor(new Color(139, 69, 19));  // Saddle brown
                g2d.drawString(title, 50, 50);
            }
        };
        titlePanel.setPreferredSize(new Dimension(400, 100));
        add(titlePanel, gbc);

        addStyledButton("Start Game", e -> game.switchToPanel("Game"), gbc);
        addStyledButton("High Score", e -> game.switchToPanel("HighScore"), gbc);
        addStyledButton("Settings", e -> game.switchToPanel("Settings"), gbc);
        addStyledButton("Exit", e -> System.exit(0), gbc);
    }

    private void addStyledButton(String text, ActionListener action, GridBagConstraints gbc) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(139, 69, 19),  // Saddle brown
                        0, getHeight(), new Color(101, 67, 33)  // Darker brown
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                g2d.setColor(new Color(0, 0, 0, 60));
                g2d.drawString(text, 51, 35);
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, 50, 34);
            }
        };
        button.setPreferredSize(new Dimension(200, 50));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.addActionListener(action);
        add(button, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        background.draw(g);
        mascot.drawAt(g, 600, (int)mascotY);  // Centered for 1200 width
    }
}

class GamePanel extends JPanel implements ActionListener, KeyListener {
    private FloatySquirrel game;
    private Squirrel squirrel;
    private ArrayList<TreeObstacle> obstacles;
    private Timer timer;
    private boolean isPlaying;
    private int score;
    private Random random;
    private Background background;

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 600;
    private static final int OBSTACLE_SPACING = 300;
    private static final int OBSTACLE_SPEED = 3;
    private static final int OBSTACLE_WIDTH = 80;
    private static final int GAP_HEIGHT = 200;

    public GamePanel(FloatySquirrel game) {
        this.game = game;
        setBackground(Color.cyan);
        setFocusable(true);
        addKeyListener(this);

        squirrel = new Squirrel();
        obstacles = new ArrayList<>();
        random = new Random();
        background = new Background();

        timer = new Timer(16, this);
        resetGame();
    }

    private void resetGame() {
        squirrel.reset();
        obstacles.clear();
        score = 0;
        isPlaying = false;

        for (int i = 0; i < 3; i++) {
            addObstacle(WIDTH + i * OBSTACLE_SPACING);
        }

        repaint();
    }

    private void addObstacle(int x) {
        int minHeight = 50;
        int maxHeight = HEIGHT - GAP_HEIGHT - 150;
        int height = random.nextInt(Math.max(1, maxHeight - minHeight)) + minHeight;
        obstacles.add(new TreeObstacle(x, height, OBSTACLE_WIDTH, GAP_HEIGHT));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        background.draw(g);
        background.update();

        for (TreeObstacle obstacle : obstacles) {
            obstacle.draw(g);
        }

        squirrel.draw(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        String scoreText = "Score: " + score;
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.drawString(scoreText, 21, 41);
        g2d.setColor(Color.WHITE);
        g2d.drawString(scoreText, 20, 40);

        if (!isPlaying) {
            drawCenteredText(g2d, "Press SPACE to start", 36, HEIGHT/2);
            drawCenteredText(g2d, "Press ESC for menu", 24, HEIGHT/2 + 50);
        }
    }

    private void drawCenteredText(Graphics2D g2d, String text, int fontSize, int y) {
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (WIDTH - fm.stringWidth(text)) / 2;

        g2d.setColor(new Color(0, 0, 0, 60));
        g2d.drawString(text, x + 2, y + 2);
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, x, y);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isPlaying) {
            squirrel.update();

            for (int i = obstacles.size() - 1; i >= 0; i--) {
                TreeObstacle obstacle = obstacles.get(i);
                obstacle.update();

                if (obstacle.getX() + OBSTACLE_WIDTH < 0) {
                    obstacles.remove(i);
                    addObstacle(obstacles.get(obstacles.size()-1).getX() + OBSTACLE_SPACING);
                    score++;
                    game.playScoreSound();
                }

                if (obstacle.collidesWith(squirrel)) {
                    gameOver();
                }
            }

            if (squirrel.getY() <= 0 || squirrel.getY() >= getHeight() - 100) {
                gameOver();
            }

            repaint();
        }
    }

    private void gameOver() {
        isPlaying = false;
        timer.stop();
        game.updateHighScore(score);
        JOptionPane.showMessageDialog(this, "Game Over! Score: " + score);
        resetGame();
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (!isPlaying) {
                isPlaying = true;
                timer.start();
            }
            squirrel.glide();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            timer.stop();
            isPlaying = false;
            resetGame();
            game.switchToPanel("Menu");
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}

class HighScorePanel extends JPanel {
    private FloatySquirrel game;
    private Background background;
    private Timer updateTimer;

    public HighScorePanel(FloatySquirrel game) {
        this.game = game;
        this.background = new Background();
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 50, 10, 50);

        JLabel titleLabel = new JLabel("High Score", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(new Color(139, 69, 19));
        add(titleLabel, gbc);

        JLabel scoreLabel = new JLabel("Best: " + game.getHighScore(), SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 36));
        scoreLabel.setForeground(new Color(101, 67, 33));
        add(scoreLabel, gbc);

        JButton backButton = new JButton("Back to Menu") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(139, 69, 19),
                        0, getHeight(), new Color(101, 67, 33)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                g2d.setColor(new Color(0, 0, 0, 60));
                String text = "Back to Menu";
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                g2d.drawString(text, x + 1, 35);
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, x, 34);
            }
        };
        backButton.setPreferredSize(new Dimension(200, 50));
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> game.switchToPanel("Menu"));
        add(backButton, gbc);

        updateTimer = new Timer(16, e -> {
            background.update();
            repaint();
            scoreLabel.setText("Best: " + game.getHighScore());
        });
        updateTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        background.draw(g);
    }
}

class SettingsPanel extends JPanel {
    private FloatySquirrel game;
    private Background background;
    private Timer updateTimer;

    public SettingsPanel(FloatySquirrel game) {
        this.game = game;
        this.background = new Background();
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 50, 10, 50);

        JLabel titleLabel = new JLabel("Settings", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(new Color(139, 69, 19));
        add(titleLabel, gbc);

        JCheckBox soundToggle = new JCheckBox("Enable Sound Effects", game.isSoundEnabled());
        soundToggle.setFont(new Font("Arial", Font.BOLD, 20));
        soundToggle.setForeground(new Color(101, 67, 33));
        soundToggle.setOpaque(false);
        soundToggle.addActionListener(e -> game.setSoundEnabled(soundToggle.isSelected()));
        add(soundToggle, gbc);

        JCheckBox resizeToggle = new JCheckBox("Allow Window Resizing", false);
        resizeToggle.setFont(new Font("Arial", Font.BOLD, 20));
        resizeToggle.setForeground(new Color(101, 67, 33));
        resizeToggle.setOpaque(false);
        resizeToggle.addActionListener(e -> game.setResizable(resizeToggle.isSelected()));
        add(resizeToggle, gbc);

        JButton backButton = new JButton("Back to Menu") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(139, 69, 19),
                        0, getHeight(), new Color(101, 67, 33)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                g2d.setColor(new Color(255, 255, 255, 100));
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                g2d.setColor(new Color(0, 0, 0, 60));
                String text = "Back to Menu";
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                g2d.drawString(text, x + 1, 35);
                g2d.setColor(Color.WHITE);
                g2d.drawString(text, x, 34);
            }
        };
        backButton.setPreferredSize(new Dimension(200, 50));
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> game.switchToPanel("Menu"));
        add(backButton, gbc);

        updateTimer = new Timer(16, e -> {
            background.update();
            repaint();
        });
        updateTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        background.draw(g);
    }
}
