import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.Random;

public class HypocycloidGraph extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final double A = 3;
    private static final double a = 2;
    private static final String AUTHOR = "Humeniuk";
    private static final int VARIANT = 5;

    private Color graphColor = Color.BLUE;
    private float lineThickness = 2.0f;
    private float[] dashPattern = new float[]{10, 5, 2, 5};

    private final Random random = new Random();

    public HypocycloidGraph() {
        setTitle("Hypocycloid Graph");
        setSize(WIDTH, HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGraph(g);
            }
        };

        graphPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                changeGraphStyle();
                repaint();
            }
        });

        add(graphPanel);
    }

    private void drawGraph(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        double scale = Math.min(getWidth(), getHeight()) / (2 * A);

        // Draw coordinate axes
        g2.setColor(Color.BLACK);
        g2.drawLine(0, centerY, getWidth(), centerY);
        g2.drawLine(centerX, 0, centerX, getHeight());

        // Draw hypocycloid
        g2.setColor(graphColor);
        g2.setStroke(new BasicStroke(lineThickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dashPattern, 0));

        Path2D path = new Path2D.Double();
        int prevX = centerX, prevY = centerY;

        for (double t = 0; t <= 2 * Math.PI * 2; t += 0.1) {
            double x = (A - a) * Math.cos(t) + a * Math.cos((A / a - 1) * t);
            double y = (A - a) * Math.sin(t) - a * Math.sin((A / a - 1) * t);

            int plotX = centerX + (int) (x * scale);
            int plotY = centerY - (int) (y * scale);  // Invert y for proper orientation

            if (t > 0) {
                g2.drawLine(prevX, prevY, plotX, plotY);
            }

            prevX = plotX;
            prevY = plotY;
        }
        g2.draw(path);

        // Draw author and variant
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        g2.drawString(AUTHOR + ", Variant " + VARIANT, 10, 20);
    }

    private void changeGraphStyle() {
        graphColor = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat());
        lineThickness = random.nextFloat() * 3 + 1;
        int dashStyle = random.nextInt(4);
        switch (dashStyle) {
            case 0:
                dashPattern = null;
                break;
            case 1:
                dashPattern = new float[]{5, 5};
                break;
            case 2:
                dashPattern = new float[]{10, 5, 2, 5};
                break;
            case 3:
                dashPattern = new float[]{15, 5, 5, 5};
                break;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HypocycloidGraph().setVisible(true));
    }
}
