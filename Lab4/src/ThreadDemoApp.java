import javax.swing.*;
import java.awt.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.text.DecimalFormat;

public class ThreadDemoApp extends JFrame {
    private AnimationPanel animationPanel;
    private JTextArea calculationArea;
    private MarqueePanel marqueePanel;
    private JPanel controlPanel;

    private AnimationThread animationThread;
    private CalculationThread calculationThread;
    private MarqueeThread marqueeThread;

    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public ThreadDemoApp() {
        setTitle("Thread Demo Application");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initializeComponents();
        initializeThreads();
        createControlPanel();

        setVisible(true);
    }

    private void initializeComponents() {
        // Панель анімації
        animationPanel = new AnimationPanel();
        animationPanel.setPreferredSize(new Dimension(400, 300));

        // Панель для виведення розрахунків
        calculationArea = new JTextArea();
        calculationArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(calculationArea);
        scrollPane.setPreferredSize(new Dimension(400, 150));

        // Панель біжучого рядка
        marqueePanel = new MarqueePanel();
        marqueePanel.setPreferredSize(new Dimension(800, 50));

        // Додавання компонентів
        add(animationPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.EAST);
        add(marqueePanel, BorderLayout.SOUTH);
    }

    private void initializeThreads() {
        animationThread = new AnimationThread(animationPanel);
        calculationThread = new CalculationThread(calculationArea, lock, condition);
        marqueeThread = new MarqueeThread(marqueePanel);
    }

    private void createControlPanel() {
        controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(3, 1, 10, 10)); // 3 рядки, 1 стовпець

        // Панель для елементів керування анімацією
        JPanel animationControls = new JPanel();
        animationControls.setBorder(BorderFactory.createTitledBorder("Animation Controls"));
        animationControls.setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton startAnimBtn = new JButton("Start Animation");
        JButton pauseAnimBtn = new JButton("Pause Animation");
        JSlider animSpeedSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 50);

        startAnimBtn.addActionListener(e -> animationThread.resumeThread());
        pauseAnimBtn.addActionListener(e -> animationThread.pauseThread());
        animSpeedSlider.addChangeListener(e ->
                animationThread.setSpeed(animSpeedSlider.getValue()));

        animationControls.add(startAnimBtn);
        animationControls.add(pauseAnimBtn);
        animationControls.add(new JLabel("Speed: "));
        animationControls.add(animSpeedSlider);

        // Панель для елементів керування розрахунками
        JPanel calculationControls = new JPanel();
        calculationControls.setBorder(BorderFactory.createTitledBorder("Calculation Controls"));
        calculationControls.setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton startCalcBtn = new JButton("Start Calculation");
        JButton pauseCalcBtn = new JButton("Pause Calculation");
        JSlider calcPrioritySlider = new JSlider(JSlider.HORIZONTAL,
                Thread.MIN_PRIORITY, Thread.MAX_PRIORITY, Thread.NORM_PRIORITY);

        startCalcBtn.addActionListener(e -> calculationThread.resumeThread());
        pauseCalcBtn.addActionListener(e -> calculationThread.pauseThread());
        calcPrioritySlider.addChangeListener(e ->
                calculationThread.setPriority(calcPrioritySlider.getValue()));

        calculationControls.add(startCalcBtn);
        calculationControls.add(pauseCalcBtn);
        calculationControls.add(new JLabel("Priority: "));
        calculationControls.add(calcPrioritySlider);

        // Панель для елементів керування біжучим рядком
        JPanel marqueeControls = new JPanel();
        marqueeControls.setBorder(BorderFactory.createTitledBorder("Marquee Controls"));
        marqueeControls.setLayout(new FlowLayout(FlowLayout.LEFT));

        JButton startMarqueeBtn = new JButton("Start Marquee");
        JButton pauseMarqueeBtn = new JButton("Pause Marquee");
        JSlider marqueeSpeedSlider = new JSlider(JSlider.HORIZONTAL, 1, 50, 25);

        startMarqueeBtn.addActionListener(e -> marqueeThread.resumeThread());
        pauseMarqueeBtn.addActionListener(e -> marqueeThread.pauseThread());
        marqueeSpeedSlider.addChangeListener(e ->
                marqueeThread.setSpeed(marqueeSpeedSlider.getValue()));

        marqueeControls.add(startMarqueeBtn);
        marqueeControls.add(pauseMarqueeBtn);
        marqueeControls.add(new JLabel("Speed: "));
        marqueeControls.add(marqueeSpeedSlider);

        // Додавання всіх панелей керування до головної панелі
        controlPanel.add(animationControls);
        controlPanel.add(calculationControls);
        controlPanel.add(marqueeControls);

        add(controlPanel, BorderLayout.NORTH);
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ThreadDemoApp());
    }
}

class AnimationPanel extends JPanel {
    private int x = 0;
    private int y = 0;
    public double angle = 0;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Малювання складної фігури
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        g2d.setColor(Color.BLUE);
        g2d.rotate(angle, centerX, centerY);

        int[] xPoints = {centerX - 50, centerX + 50, centerX + 25,
                centerX - 25};
        int[] yPoints = {centerY - 50, centerY - 50, centerY + 50,
                centerY + 50};

        g2d.fillPolygon(xPoints, yPoints, 4);

        // Додаткові елементи
        g2d.setColor(Color.RED);
        g2d.fillOval(centerX - 10, centerY - 10, 20, 20);
    }
}

class MarqueePanel extends JPanel {
    private String text = "This is a scrolling text demonstration... ";
    int xPos = 0;

    public MarqueePanel() {
        setBackground(Color.BLACK);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString(text, xPos, 30);
    }
}

// Розширений клас для керування анімацією
class AnimationThread extends Thread {
    private AnimationPanel panel;
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private int speed = 50;
    private long sleepTime = 100;  // Час блокування потоку в мс

    public AnimationThread(AnimationPanel panel) {
        this.panel = panel;
        setName("AnimationThread");  // Встановлюємо ім'я потоку
        start();
    }

    @Override
    public void run() {
        while (running) {
            if (!paused) {
                panel.angle += 0.1;
                panel.repaint();

                try {
                    Thread.sleep(sleepTime - speed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                // Якщо потік призупинено, чекаємо на notify
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    public void setSpeed(int speed) {
        this.speed = Math.min(Math.max(speed, 1), 95); // Обмежуємо швидкість
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = Math.max(sleepTime, 5); // Мінімальний час сну 5мс
    }

    public synchronized void pauseThread() {
        paused = true;
    }

    public synchronized void resumeThread() {
        paused = false;
        notify(); // Пробуджуємо потік
    }

    public void stopThread() {
        running = false;
        resumeThread(); // Пробуджуємо потік, якщо він очікує
    }

    public boolean isPaused() {
        return paused;
    }
}

// Розширений клас для керування обчисленнями
class CalculationThread extends Thread {
    private JTextArea output;
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private ReentrantLock lock;
    private Condition condition;
    private long calculationInterval = 500;
    private int iterationsBeforePause = 10;
    private int calculationType = 0; // Тип обчислень (0-3)

    public CalculationThread(JTextArea output, ReentrantLock lock, Condition condition) {
        this.output = output;
        this.lock = lock;
        this.condition = condition;
        setName("CalculationThread");
        start();
    }

    @Override
    public void run() {
        DecimalFormat df = new DecimalFormat("#.####");
        double x = 0;
        int iterations = 0;

        while (running) {
            if (!paused) {
                lock.lock();
                try {
                    // Різні типи обчислень
                    double result = 0;
                    String formula = "";

                    switch (calculationType) {
                        case 0: // Комплексна тригонометрична функція
                            result = Math.sin(x) * Math.cos(2*x) + Math.tan(x/2) * Math.exp(-x/10);
                            formula = "sin(x) * cos(2x) + tan(x/2) * e^(-x/10)";
                            break;

                        case 1: // Поліноміальна функція
                            result = Math.pow(x, 3) - 2*Math.pow(x, 2) + 3*x - 1;
                            formula = "x³ - 2x² + 3x - 1";
                            break;

                        case 2: // Гіперболічні функції
                            result = Math.sinh(x) * Math.cosh(x) + Math.tanh(x);
                            formula = "sinh(x) * cosh(x) + tanh(x)";
                            break;

                        case 3: // Комбінована функція з логарифмом
                            if (x > 0) {
                                result = Math.log(x) * Math.sqrt(x) + Math.pow(Math.E, -x/5);
                                formula = "ln(x) * √x + e^(-x/5)";
                            } else {
                                result = 0;
                                formula = "undefined for x ≤ 0";
                            }
                            break;
                    }

                    double finalResult = result;
                    double finalX = x;
                    String finalFormula = formula;

                    SwingUtilities.invokeLater(() -> {
                        output.append(String.format("[Priority: %d] Formula: %s%n" +
                                        "x = %s, result = %s%n%n",
                                getPriority(),
                                finalFormula,
                                df.format(finalX),
                                df.format(finalResult)));
                    });

                    x += 0.2; // Збільшуємо крок для більшого діапазону значень
                    iterations++;

                    // Зміна типу обчислень після певної кількості ітерацій
                    if (x > 10) {
                        x = 0;
                        calculationType = (calculationType + 1) % 4;
                        SwingUtilities.invokeLater(() -> {
                            output.append("------- Changing calculation type -------\n\n");
                        });
                    }

                    if (iterations >= iterationsBeforePause) {
                        iterations = 0;
                        Thread.sleep(calculationInterval);
                    }

                    condition.signalAll();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } finally {
                    lock.unlock();
                }
            } else {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    public void setCalculationType(int type) {
        if (type >= 0 && type <= 3) {
            this.calculationType = type;
        }
    }

    public void setCalculationInterval(long interval) {
        this.calculationInterval = Math.max(interval, 100);
    }

    public void setIterationsBeforePause(int iterations) {
        this.iterationsBeforePause = Math.max(iterations, 1);
    }

    public synchronized void pauseThread() {
        paused = true;
    }

    public synchronized void resumeThread() {
        paused = false;
        notify();
    }

    public void stopThread() {
        running = false;
        resumeThread();
    }
}

// Розширений клас для керування біжучим рядком
class MarqueeThread extends Thread {
    private MarqueePanel panel;
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private int speed = 25;
    private int stepSize = 1; // Розмір кроку переміщення тексту

    public MarqueeThread(MarqueePanel panel) {
        this.panel = panel;
        setName("MarqueeThread");
        start();
    }

    @Override
    public void run() {
        while (running) {
            if (!paused) {
                panel.xPos -= stepSize;
                if (panel.xPos < -300) {
                    panel.xPos = panel.getWidth();
                }
                panel.repaint();

                try {
                    Thread.sleep(50 - speed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } else {
                synchronized (this) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
    }

    public void setSpeed(int speed) {
        this.speed = Math.min(Math.max(speed, 1), 45); // Обмежуємо швидкість
    }

    public void setStepSize(int size) {
        this.stepSize = Math.max(size, 1); // Мінімальний розмір кроку 1
    }

    public synchronized void pauseThread() {
        paused = true;
    }

    public synchronized void resumeThread() {
        paused = false;
        notify();
    }

    public void stopThread() {
        running = false;
        resumeThread();
    }
}
