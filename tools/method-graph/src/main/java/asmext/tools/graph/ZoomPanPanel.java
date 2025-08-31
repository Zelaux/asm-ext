package asmext.tools.graph;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class ZoomPanPanel extends JPanel {
    private AffineTransform transform = new AffineTransform();
    private Point lastDrag;
    private Point2D.Float cameraPosition = new Point2D.Float(0, 0);
    @Getter
    private float scale = 1.0F;
    private Painter painter;

    public ZoomPanPanel(Painter painter) {
        this.painter = painter;
        updateTransform();
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateTransform();
            }
        });
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lastDrag = e.getPoint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastDrag.x;
                int dy = e.getY() - lastDrag.y;
                cameraPosition.x += dx / scale;
                cameraPosition.y += dy / scale;

                updateTransform();

                lastDrag = e.getPoint();
            }
        });

        addMouseWheelListener(e -> {
            float factor = (e.getWheelRotation() < 0) ? 1.1f : 0.9f;
            Point2D.Float beforeM = componentToWorld(e.getPoint(), new Point2D.Float());
            scale *= factor;
            updateTransform();
            Point2D.Float afterM = componentToWorld(e.getPoint(), new Point2D.Float());
            this.cameraPosition.x += afterM.x - beforeM.x;
            this.cameraPosition.y += afterM.y - beforeM.y;
            updateTransform();
            repaint();
        });
    }

    public void updateTransform() {
        transform.setToIdentity(); // сброс трансформации
        int w = getWidth();
        int h = getHeight();

        // Переносим камеру в центр панели
        transform.translate(w / 2.0f, h / 2.0f);

        // Масштаб
        transform.scale(scale, scale);

        // Сдвигаем мир так, чтобы cameraPosition оказался в центре
        transform.translate(cameraPosition.x, cameraPosition.y);

    }

    public void componentToWorld(Point p) {
        try {
            transform.inverseTransform(p, p);
        } catch (Exception ignored) {
        }
    }

    public <T extends Point2D> T componentToWorld(Point p, T point) {
        try {
            transform.inverseTransform(p, point);
        } catch (Exception ignored) {
        }
        return point;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        if (painter != null) {
            painter.paint(g2, transform);
        }

        g2.dispose();
    }

    public AffineTransform getTransform() {
        return new AffineTransform(transform);
    }

    public void moveCamera(float dx, float dy) {
        cameraPosition.x += dx;
        cameraPosition.y += dy;
        updateTransform();
    }

    public interface Painter {
        void paint(Graphics2D g2, AffineTransform transform);
    }

}
