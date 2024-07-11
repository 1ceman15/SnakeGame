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

    //Размеры экрана
    private static final int SCREEN_WIDTH = 1350;
    private static final int SCREEN_HEIGHT = 800;
    //Размер объектов
    private static final int UNIT_SIZE = 50;
    //Размеры игрового поля
    private static final int GAME_WIDTH = SCREEN_WIDTH-UNIT_SIZE;
    private static final int GAME_HEIGHT = SCREEN_HEIGHT - UNIT_SIZE;

    private static final int GAME_UNITS = (GAME_WIDTH*GAME_HEIGHT)/(UNIT_SIZE*UNIT_SIZE);//Максимальное количество объектов на экране

    private static final int DELAY = 80;

    private final int x[] = new int[GAME_UNITS];//Хранит Х координаты сегментов змеи
    private final int y[] = new int[GAME_UNITS];//Хранит Y координаты сегментов змеи
    private int bodyParts = 6;//Начальное количесто сегментов
    private int applesEaten;//Сколько яблок съедено
    private int appleX;//X координаты яблока
    private int appleY;//Y координаты яблока
    private char direction = 'R';//Направление движения
    private boolean running = false;//Запущена ли игра
    private Timer timer = new Timer(DELAY,this);//Выполняет код в actionPerformed раз в DELAY милисекунд
    private Random random;
    private Image appleImage = new ImageIcon(getClass().getResource("/Images/Apple.png")).getImage();
    private Image treeImage = new ImageIcon(getClass().getResource("/Images/Tree.png")).getImage();
    private Image backgroundImage = new ImageIcon(getClass().getResource("/Images/Background.png")).getImage();
    private AudioInputStream audio;
    private Clip clip;
    private JButton restartButton = new JButton("Play Again");//Кнопка restart
    private boolean isGameOver = false;//Вспомогательная переменная, чтобы звук окончания игры проигрывался всего 1 раз


    GamePanel(){
        random = new Random();
        this.setPreferredSize(new Dimension(SCREEN_WIDTH,SCREEN_HEIGHT));
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        startGame();
    }

    //Метод для запуска игры
    public void startGame() {
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

        newApple();//Спавн первого яблока

        running = true;
        isGameOver = false;
        timer.start();
    }

    //Переопределение метода для перерисовки
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
        if(isGameOver){
            //drawGameOver(g);
        }
    }


    //Метод рисующий экран
    public void draw(Graphics g) {
        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);//Рисуем фон
        if(running) {//Проверяем запущена ли игра

            //Рисование стен
            for(int i=0;i<SCREEN_WIDTH/UNIT_SIZE;i++) {
                if(i!=12 & i!=14 & i!=13)//Место под счет
                    g.drawImage(treeImage,i*UNIT_SIZE, 0, UNIT_SIZE, UNIT_SIZE, this);

                g.drawImage(treeImage,i*UNIT_SIZE, SCREEN_HEIGHT-UNIT_SIZE, UNIT_SIZE, UNIT_SIZE, this);
                g.drawImage(treeImage,0,i*UNIT_SIZE, UNIT_SIZE, UNIT_SIZE, this);
                g.drawImage(treeImage,SCREEN_WIDTH-UNIT_SIZE,i*UNIT_SIZE, UNIT_SIZE, UNIT_SIZE, this);
			}


            g.drawImage(appleImage,appleX, appleY, UNIT_SIZE, UNIT_SIZE, this);//Рисуем яблоко
            Graphics2D g2d = (Graphics2D) g;

            //Рисуем Змею
            for(int i = 0; i< bodyParts;i++) {
                g2d.setColor(new Color(255, 140, 0));//Основной цвет
                g2d.fillRoundRect(x[i], y[i], UNIT_SIZE-7, UNIT_SIZE-7,15,15);
                g2d.setColor(new Color(136, 63, 1));//Цвет оконтовки
                g2d.setStroke(new BasicStroke(4));//Толщина оконтовки
                g2d.drawRoundRect(x[i], y[i], UNIT_SIZE-7, UNIT_SIZE-7,15,15);
            }

            //Рисуем счет
            g.setColor(new Color(0,0,0));
            g.setFont( new Font("Bodoni MT Black",Font.BOLD, 28));
            FontMetrics metrics = getFontMetrics(g.getFont());
            g.drawString("Score: "+applesEaten, (SCREEN_WIDTH - metrics.stringWidth("Score: "+applesEaten))/2, g.getFont().getSize());
        }
        else {
            if(!isGameOver) {
                gameOver(g);
            }
        }

    }

    //Справн нового яблока
    public void newApple(){
        boolean isAppleOnSnake;
        do {
            isAppleOnSnake = false;
            appleX = (1 + random.nextInt((GAME_WIDTH - UNIT_SIZE) / UNIT_SIZE)) * UNIT_SIZE;
            appleY = (1 + random.nextInt((GAME_HEIGHT - UNIT_SIZE) / UNIT_SIZE)) * UNIT_SIZE;
            //Проверка не заспавнилось ли яблоко в змее
            for (int i = 0; i < bodyParts; i++) {
                if (appleX == x[i] && appleY == y[i]) {
                    isAppleOnSnake = true;
                    break;
                }
            }
        }while (isAppleOnSnake);

    }

    //Движение (по умолчанию напарво)
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

    //Проверка на съеденое яблоко
    public void checkApple() {
        if((x[0] == appleX) && (y[0] == appleY)) {
            bodyParts++;//Увеличиваем количество сегметов змеи
            applesEaten++;//Увеличиваем счет
            //Проигрываем звук съедения яблока
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

    //Метод проверки столкновений
    public void checkCollisions() {
        //Столкновение с телом
        for(int i = bodyParts;i>0;i--) {
            if((x[0] == x[i])&& (y[0] == y[i])) {
                running = false;
            }
        }
        //Столкновение с левой стеной
        if(x[0] < UNIT_SIZE) {
            running = false;
        }
        //Столкновение с правой стеной
        if(x[0] >= GAME_WIDTH) {
            running = false;
        }
        //Столкновение с верхней стеной
        if(y[0] < UNIT_SIZE) {
            running = false;
        }
        //Столкновение с нижней стеной
        if(y[0] >= GAME_HEIGHT) {
            running = false;
        }

        if(!running) {//Если столкнулись остаенавливем перерисовку
            timer.stop();
        }
    }


    //Метод окончания игры
    public void gameOver(Graphics g) {
        isGameOver = true;

        drawGameOver(g);//Рисуем окно окончания

        //Проигрываем звук окончания
        try {
            InputStream audioStream = new BufferedInputStream(getClass().getResourceAsStream("/Sounds/GameOverSound.wav"));
            audio = AudioSystem.getAudioInputStream(audioStream);

            Clip clip = AudioSystem.getClip();

            clip.open(audio);

            clip.start();


        } catch (Exception e) {
            System.out.printf(e.getMessage());
        }



    }

    //Метод рисующий окно GameOver
    public void drawGameOver(Graphics g) {
        g.setColor(new Color(0,0,0));
        g.setFont(new Font("Bodoni MT Black", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (SCREEN_WIDTH - metrics1.stringWidth("Score: " + applesEaten)) / 2, g.getFont().getSize());

        // Game Over text

        g.setFont(new Font("Bodoni MT Black", Font.BOLD, 75));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (SCREEN_WIDTH - metrics2.stringWidth("Game Over")) / 2, SCREEN_HEIGHT / 2);


        //Добовляем кнопку restart
        restartButton.setBackground(new Color(248, 248, 248));
        restartButton.setFocusable(false);
        restartButton.setFont(new Font("Bodoni MT Black", Font.BOLD, 20));
        restartButton.setBounds((SCREEN_WIDTH - 200) / 2, SCREEN_HEIGHT / 2 + 100, 200, 50);
        restartButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Reset game state and start a new game
                remove(restartButton);
                startGame();
            }
        });
        this.add(restartButton);
    }

    //Переопределяем метод, который будет вызываться в Timer
    @Override
    public void actionPerformed(ActionEvent e) {

        if(running) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }


    //Отклик клавищ
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

