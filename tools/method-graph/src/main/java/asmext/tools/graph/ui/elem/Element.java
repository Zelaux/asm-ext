package asmext.tools.graph.ui.elem;

import asmext.tools.graph.Fonts;
import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.ui.layout.ElementLayout;
import asmext.tools.graph.ui.layout.LayoutProperties;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.Rectangle2D;

@FieldDefaults(level = AccessLevel.PUBLIC)
public class Element implements ElementLayout {
    int x, y;
    int width, height;
    LayoutProperties layoutProperties = createLayoutProperties();
    String text;
    ElementWithChild parent;
    public boolean drawBounds;

    public Element(int x, int y) {
        this.x = x;
        this.y = y;
    }

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

    protected @NotNull LayoutProperties createLayoutProperties() {
        return new LayoutProperties();
    }

    public Element fillX(boolean fillX) {
        layoutProperties.fillX = fillX;
        return this;
    }

    public Element fillY(boolean fillY) {
        layoutProperties.fillY = fillY;
        return this;
    }

    public Element text(String text) {
        this.text = text;
        return this;
    }


    public void update(UIContext context) {

    }

    public void layout(UIContext context) {
        Rectangle2D bb = Fonts.jetbrainsLigature.getStringBounds(text, Fonts.defaultContext[0]);
        width = (int) (bb.getWidth() + 0.5f);
        height = (int) (bb.getHeight() + 0.5f);

    }


    public void draw(Graphics2D g2d, UIContext context) {
        if (drawBounds) {
            g2d.setStroke(new BasicStroke(4));
            g2d.setColor(Color.red);
            g2d.drawRect((int) x, (int) y, (int) width, (int) height);
        }
        g2d.drawString(text, x, y + height);
    }

    public @NotNull Element centered(boolean centered) {
        layoutProperties.centered = centered;
        return this;
    }


    //region layout
    @Override
    public void setLayout(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public @NotNull LayoutProperties elementProperties() {
        return layoutProperties;
    }

    @Override
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public int x() {
        return x;
    }

    @Override
    public int y() {
        return y;
    }

    @Override
    public int height() {
        return height;
    }

    @Override
    public int width() {
        return width;
    }

    @Override
    public void height(int height) {
        this.height = height;
    }

    @Override
    public void width(int width) {
        this.width = width;
    }

    @Override
    public void x(int x) {
        this.x = x;
    }

    @Override
    public void y(int y) {
        this.y = y;
    }
    //endregion

}
