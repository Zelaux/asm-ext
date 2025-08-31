package asmext.tools.graph.ui.opcode.groups;

import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.ui.elem.Element;
import asmext.tools.graph.ui.elem.ElementWithChild;
import asmext.tools.graph.ui.layout.ContainerLayout;
import asmext.tools.graph.ui.layout.ContainerLayoutProperties;
import asmext.tools.graph.ui.layout.LayoutPlacement;
import asmext.tools.graph.ui.layout.LayoutProperties;
import asmext.tools.graph.ui.opcode.layout.IfLayout;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

public class DoubleBranch extends ElementWithChild implements ContainerLayout {
    public final ContainerLayoutProperties containerProperties = (ContainerLayoutProperties) layoutProperties;
    public Element left, right;


    public DoubleBranch(Element left, Element right) {
        this.left = left;
        this.right = right;
        left.parent=this;
        right.parent=this;
    }

    @Override
    protected @NotNull LayoutProperties createLayoutProperties() {
        var properties = new ContainerLayoutProperties();
        properties.childGap = 10;
        return properties;
    }

    @Override
    public void update(UIContext context) {
        super.update(context);
        updateChild(left, context);
        updateChild(right, context);
    }

    @Override
    public void layout(UIContext context) {
        LayoutPlacement placement = context.useHorizontalMode ? IfLayout.horizontal : IfLayout.vertical;
        placement.layout(this, List.of(left, right), context);


    }

    @Override
    public void draw(Graphics2D g2d, UIContext context) {

        if (context.useHorizontalMode) {
            g2d.drawLine(x + width / 2, y, x + left.x + left.width / 2, y + 30);
            g2d.drawLine(x + width / 2, y, x + right.x + right.width / 2, y + 30);
        }

        drawChild(left, g2d, context);
        drawChild(right, g2d, context);
        if (context.useHorizontalMode) return;
        g2d.setColor(context.style.fontColor);
        int x1 = x + 20;
        int x2 = x + 30;

        int y1 = y;
        int y2 = y1 + left.height;
        int y3 = y2 + containerProperties.childGap;
        int y4 = y3 + right.height;
        g2d.drawLine(x1, y1, x2, y1);
        g2d.drawLine(x1, y1, x1, y2);
        g2d.drawLine(x1, y2, x2, y2);


        g2d.drawLine(x1, y3, x2, y3);
        g2d.drawLine(x1, y3, x1, y4);
        g2d.drawLine(x1, y4, x2, y4);
    }

    @Override
    public boolean removeChild(Element prevParent) {
        if (left == prevParent) {
            left = null;
            return true;
        }
        if (right == prevParent) {
            right = null;
            return true;
        }
        return false;
    }

    @Override
    public void eachChild(Consumer<Element> visitor) {
        visitor.accept(left);
        visitor.accept(right);
    }

    @Override
    public ContainerLayoutProperties containerProperties() {
        return containerProperties;
    }
}
