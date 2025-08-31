package asmext.tools.graph.ui.elem;

import asmext.tools.graph.ui.UIContext;

import java.awt.*;
import java.util.function.Consumer;

public abstract class ElementWithChild extends Element {
    public ElementWithChild(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public ElementWithChild(int x, int y) {
        super(x, y);
    }

    public ElementWithChild() {
    }

    public ElementWithChild(String text) {
        super(text);
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

    public abstract boolean removeChild(Element prevParent);
    public abstract void eachChild(Consumer<Element> visitor);
}
