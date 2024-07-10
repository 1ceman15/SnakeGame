import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {

    private static final int SCREEN_WIDTH = 1350;
    private static final int SCREEN_HEIGHT = 800;
    private static final int UNIT_SIZE = 50;
    private static final int GAME_WIDTH = SCREEN_WIDTH-UNIT_SIZE;
    private static final int GAME_HEIGHT = SCREEN_HEIGHT - UNIT_SIZE;
    private static final int GAME_UNITS = (GAME_WIDTH*GAME_HEIGHT)/(UNIT_SIZE*UNIT_SIZE);
    private static final int DELAY = 80;
    private final int x[] = new int[GAME_UNITS];
    private final int y[] = new int[GAME_UNITS];
    private int bodyParts = 6;
    private int applesEaten;
    private int appleX;
    private int appleY;
    private char direction = 'R';
    private boolean running = false;
    private Timer timer;
    private Random random;
    private Image appleImage = new ImageIcon(getClass().getResource("/Images/Apple.png")).getImage();
    private Image treeImage = new ImageIcon(getClass().getResource("/Images/Tree.png")).getImage();
    private Image backgroundImage = new ImageIcon(getClass().getResource("/Images/Background.png")).getImage();
    private AudioInputStream audio;
    private Clip clip;

    GamePanel(){
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH,SCREEN_HEIGHT));
        //this.setBackground(Color.black);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        x[0] = UNIT_SIZE*6;
        y[0] = UNIT_SIZE;
        try {
            InputStream audioStream = getClass().getResourceAsStream("/Sounds/AppleByte.wav");
            audio = AudioSystem.getAudioInputStream(audioStream);
            clip = AudioSystem.getClip();
        }catch (Exception e){
            System.out.printf(e.getMessage());
        }

        for (int i = 1; i < 6; i++) {
            y[i] = UNIT_SIZE;
            x[i] = x[i-1] - UNIT_SIZE;
        }

        startGame();
    }

    public void startGame() {
        newApple();
        running = true;
        timer = new Timer(DELAY,this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        draw(g);
    }

    public void draw(Graphics g) {
        if(running) {

			for(int i=0;i<SCREEN_WIDTH/UNIT_SIZE;i++) {
                //Рисование стен
                g.setColor(Color.black);
                //if(i!=12 & i != 13 & i != 14)
                //g.fillRect(i*UNIT_SIZE, 0, UNIT_SIZE, UNIT_SIZE);
                //g.fillRect(i*UNIT_SIZE, GAME_HEIGHT, UNIT_SIZE, UNIT_SIZE);
                //g.fillRect(0, i*UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
                //g.fillRect(GAME_WIDTH, i*UNIT_SIZE, UNIT_SIZE, UNIT_SIZE);
                if(i!=12 & i!=14 & i!=13)
                    g.drawImage(treeImage,i*UNIT_SIZE, 0, UNIT_SIZE, UNIT_SIZE, this);


                g.drawImage(treeImage,i*UNIT_SIZE, SCREEN_HEIGHT-UNIT_SIZE, UNIT_SIZE, UNIT_SIZE, this);
                g.drawImage(treeImage,0,i*UNIT_SIZE, UNIT_SIZE, UNIT_SIZE, this);
                g.drawImage(treeImage,SCREEN_WIDTH-UNIT_SIZE,i*UNIT_SIZE, UNIT_SIZE, UNIT_SIZE, this);



			}


            g.drawImage(appleImage,appleX, appleY, UNIT_SIZE, UNIT_SIZE, this);
            Graphics2D g2d = (Graphics2D) g;
            for(int i = 0; i< bodyParts;i++) {
                if(i == 0) {
                    g2d.setColor(new Color(255, 140, 0));
                    g2d.fillRoundRect(x[i], y[i], UNIT_SIZE-7, UNIT_SIZE-7,15,15);
                    g2d.setColor(new Color(136, 63, 1));
                    g2d.setStroke(new BasicStroke(4));
                    g2d.drawRoundRect(x[i], y[i], UNIT_SIZE-7, UNIT_SIZE-7,15,15);

                }
                else {
                    g2d.setColor(new Color(255, 140, 0));
                    g2d.fillRoundRect(x[i], y[i], UNIT_SIZE-7, UNIT_SIZE-7,15,15);
                    g2d.setColor(new Color(126, 55, 2));
                    g2d.drawRoundRect(x[i], y[i], UNIT_SIZE-7, UNIT_SIZE-7,15,15);

                }
            }

            g.setColor(new Color(0,0,0));
            g.setFont( new Font("Bodoni MT Black",Font.BOLD, 32));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: "+applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: "+applesEaten))/2, g.getFont().getSize());
        }
        else {
            gameOver(g);
        }

    }
    public void newApple(){
        boolean isAppleOnSnake;
        do {
            isAppleOnSnake = false;
            appleX = (1 + random.nextInt((GAME_WIDTH - UNIT_SIZE) / UNIT_SIZE)) * UNIT_SIZE;
            appleY = (1 + random.nextInt((GAME_HEIGHT - UNIT_SIZE) / UNIT_SIZE)) * UNIT_SIZE;
            for (int i = 0; i < bodyParts; i++) {
                if (appleX == x[i] && appleY == y[i]) {
                    isAppleOnSnake = true;
                    break;
                }
            }
        }while (isAppleOnSnake);

    }
    public void move(){
        for(int i = bodyParts;i>0;i--) {
            x[i] = x[i-1];
            y[i] = y[i-1];
        }

        switch(direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;
        }

    }
    public void checkApple() {
        if((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;
            applesEaten++;
            try {
                InputStream audioStream = new BufferedInputStream(getClass().getResourceAsStream("/Sounds/AppleByte.wav"));
                audio = AudioSystem.getAudioInputStream(audioStream);

                Clip clip = AudioSystem.getClip();

                clip.open(audio);

                clip.start();


            }catch (Exception e){
                System.out.printf(e.getMessage());
            }
            newApple();
        }
    }

    public void checkCollisions() {
        //checks if head collides with body
        for(int i = bodyParts;i>0;i--) {
            if((x[0] == x[i])&& (y[0] == y[i])) {
                running = false;
            }
        }
        //check if head touches left border
        if(x[0] < UNIT_SIZE) {
            running = false;
        }
        //check if head touches right border
        if(x[0] >= GAME_WIDTH) {
            running = false;
        }
        //check if head touches top border
        if(y[0] < UNIT_SIZE) {
            running = false;
        }
        //check if head touches bottom border
        if(y[0] >= GAME_HEIGHT) {
            running = false;
        }

        if(!running) {
            timer.stop();
        }
    }
    public void gameOver(Graphics g) {
        // Score
        g.setColor(new Color(0,0,0));
        g.setFont(new Font("Bodoni MT Black", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());

        // Game Over text

        g.setFont(new Font("Bodoni MT Black", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);

        try {
             InputStream audioStream = new BufferedInputStream(getClass().getResourceAsStream("/Sounds/GameOverSound.wav"));
             audio = AudioSystem.getAudioInputStream(audioStream);

            Clip clip = AudioSystem.getClip();

            clip.open(audio);

            clip.start();

        } catch (Exception e){
            System.out.printf(e.getMessage());
        }

        JButton restartButton = new JButton("Play Again");
        restartButton.setBackground(new Color(248, 248, 248));
        restartButton.setFocusable(false);
        restartButton.setFont(new Font("Bodoni MT Black", Font.BOLD, 30));
        restartButton.setBounds((SCREEN_WIDTH - 200) / 2, SCREEN_HEIGHT / 2 + 100, 200, 50);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Reset game state and start a new game
                bodyParts = 6;
                applesEaten = 0;
                direction = 'R';
                Arrays.fill(x, 0);
                Arrays.fill(y, 0);
                x[0] = UNIT_SIZE * 6;
                y[0] = UNIT_SIZE;
                for (int i = 1; i < 6; i++) {
                    y[i] = UNIT_SIZE;
                    x[i] = x[i - 1] - UNIT_SIZE;
                }
                remove(restartButton);
                startGame();
            }
        });
        this.add(restartButton);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch(e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    if(direction != 'R') {
                        direction = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    if(direction != 'L') {
                        direction = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                case KeyEvent.VK_W:
                    if(direction != 'D') {
                        direction = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_S:
                    if(direction != 'U') {
                        direction = 'D';
                    }
                    break;
            }
        }
    }
}

