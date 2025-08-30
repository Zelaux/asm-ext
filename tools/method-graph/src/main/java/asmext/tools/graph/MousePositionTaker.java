package asmext.tools.graph;

import asmext.tools.graph.ui.UIContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

class MousePositionTaker extends MouseAdapter {
    private final UIContext context;
    private final ZoomPanPanel panel;

    public MousePositionTaker(UIContext context, ZoomPanPanel panel) {
        this.context = context;
        this.panel = panel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            context.justClicked = true;
        }
        Point point = e.getPoint();
        panel.componentToWorld(point);
        context.mouseX = point.x;
        context.mouseY = point.y;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        super.mouseMoved(e);
        Point point = e.getPoint();
        panel.componentToWorld(point);
        context.mouseX = point.x;
        context.mouseY = point.y;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            context.leftMouse = true;
        }
        Point point = e.getPoint();
        panel.componentToWorld(point);
        context.mouseX = point.x;
        context.mouseY = point.y;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            context.leftMouse = true;
        }
        Point point = e.getPoint();
        panel.componentToWorld(point);
        context.mouseX = point.x;
        context.mouseY = point.y;
    }
}
