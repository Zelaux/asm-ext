package asmext.tools.graph.ui.elem;

import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.util.BoundingBox;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

public class Group extends Element {
    public final ArrayList<Element> elements = new ArrayList<>();
    public LayoutDirection layoutDirection;

    public Group(int x, int y, int width, int height, LayoutDirection layoutDirection) {
        super(x, y, width, height);
        this.layoutDirection = layoutDirection;
    }

    protected void updateChild(Element child, UIContext context) {
        child.x += x;
        child.y += y;
        child.update(context);
        child.x -= x;
        child.y -= y;
    }

    protected void drawChild(Element child, Graphics2D g2d, UIContext context) {
        child.x += x;
        child.y += y;
        child.draw(g2d, context);
        child.x -= x;
        child.y -= y;
    }

    public Group add(Element... elements) {
        Collections.addAll(this.elements, elements);
        return this;
    }

    @Override
    public void draw(Graphics2D g2d, UIContext context) {
        if (layoutDirection != LayoutDirection.NoLayout && drawBounds) {
            g2d.setStroke(new BasicStroke(4));
            g2d.setColor(Color.green);
            g2d.drawRect(x, y, width, height);
        }
//        g2d.drawString(text, x, y);
        for (Element element : elements) {
            drawChild(element, g2d, context);
        }
    }

    @Override
    public void update(UIContext context) {
        super.update(context);
        for (Element element : elements) {
            updateChild(element, context);
        }
    }

    @Override
    public BoundingBox layout(UIContext context) {
        switch (layoutDirection) {
            case Horizontal -> {
                int height = 0;
                int x = 0;
                boolean hasFillY = false;
                for (Element element : elements) {
                    element.x = x;
                    element.y = 0;
                    element.layout(context);
                    hasFillY |= element.fillY;
                    height = Math.max(element.height, height);
                    x += element.width + 1;
                }
                if (hasFillY) {
                    for (Element element : elements) {
                        if (element.fillY) element.height = height;
                    }
                }
                this.width = x;
                this.height = height;
            }
            case Vertical -> {
                int width = 0;
                int y = 0;
                boolean hasFillX = false;
                for (Element element : elements) {
                    element.y = y;
                    element.x = 0;
                    element.layout(context);
                    if (element.fillX) hasFillX = true;
                    width = Math.max(element.width, width);
                    y += element.height + 1;
                }
                if (hasFillX) {
                    for (Element element : elements) {
                        if (element.fillX) element.width = width;
                    }
                }
                this.width = width;
                this.height = y;
            }
            case NoLayout -> {
                for (Element element : elements) {
                    element.layout(context);
                }
            }
        }
        return BoundingBox.fromRect(width, height);
    }

    public enum LayoutDirection {
        Horizontal,
        Vertical,
        NoLayout;
    }
}
