package asmext.tools.graph.ui.elem;

import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.ui.layout.ContainerLayoutProperties;
import asmext.tools.graph.ui.layout.LayoutDirection;
import asmext.tools.graph.util.BoundsRect;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

public class Group extends ElementWithChild {

    @Override
    protected @NotNull ContainerLayoutProperties createLayoutProperties() {
        return new ContainerLayoutProperties();
    }
    public final ContainerLayoutProperties containerLayout = (ContainerLayoutProperties) layoutProperties;
    @Override
    public void eachChild(Consumer<Element> visitor) {
        elements.forEach(visitor);
    }

    public final ArrayList<Element> elements = new ArrayList<>();
    public LayoutDirection layoutDirection;

    public Group(int x, int y, int width, int height, LayoutDirection layoutDirection) {
        super(x, y, width, height);
        this.layoutDirection = layoutDirection;
    }

    public Group(LayoutDirection layoutDirection) {
        this.layoutDirection = layoutDirection;
    }

    public Group add(Element... elements) {
        Collections.addAll(this.elements, elements);
        for (Element element : elements) {
            var prevParent = element.parent;
            if (prevParent != null)
                prevParent.removeChild(element);
            element.parent = this;
        }
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
    public BoundsRect layout(UIContext context) {
        BoundsRect bb = layoutDirection.layout(containerLayout, elements, context);
        width=bb.width();
        height=bb.height();
        return bb;
    }

    @Override
    public boolean removeChild(Element prevParent) {
        return elements.remove(prevParent);
    }

}
