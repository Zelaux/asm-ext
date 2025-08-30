package asmext.tools.graph.ui.elem;

import asmext.tools.graph.Fonts;
import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.util.BoundingBox;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.awt.*;
import java.awt.geom.Rectangle2D;

@FieldDefaults(level = AccessLevel.PUBLIC)
public class Element {
    int x, y;
    int width, height;
    boolean fillX, fillY;
    String text;
    public boolean drawBounds;

    public Element(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Element() {

    }

    public Element(String text) {
        this.text = text;
    }

    public Element fillX(boolean fillX) {
        this.fillX = fillX;
        return this;
    }

    public Element fillY(boolean fillY) {
        this.fillY = fillY;
        return this;
    }

    public Element text(String text) {
        this.text = text;
        return this;
    }


    public void update(UIContext context) {

    }

    public BoundingBox layout(UIContext context) {
        Rectangle2D bb = Fonts.jetbrainsLigature.getStringBounds(text, Fonts.defaultContext[0]);
        width = (int) (bb.getWidth() + 0.5f);
        height = (int) (bb.getHeight() + 0.5f);
        return BoundingBox.fromRect(width,height);
    }

    public void draw(Graphics2D g2d, UIContext context) {
        if (drawBounds) {
            g2d.setStroke(new BasicStroke(4));
            g2d.setColor(Color.red);
            g2d.drawRect((int) x, (int) y, (int) width, (int) height);
        }
        g2d.drawString(text, x, y + height);
    }

}
