package asmext.tools.graph.ui.opcode.groups;

import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.ui.elem.Element;
import asmext.tools.graph.ui.elem.ElementWithChild;
import asmext.tools.graph.util.BoundsRect;

import java.awt.*;
import java.util.function.Consumer;

public class DoubleBranch extends ElementWithChild {
    public Element left, right;

    public DoubleBranch(int x, int y, Element left, Element right) {
        super(x, y);
        this.left = left;
        this.right = right;
    }

    public DoubleBranch(Element left, Element right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void update(UIContext context) {
        super.update(context);
        updateChild(left, context);
        updateChild(right, context);
    }

    @Override
    public BoundsRect layout(UIContext context) {
        left.layout(context);
        right.layout(context);
        width = left.width + right.width;
        right.x = 10 + left.width;
        left.y = right.y = 30;
        height = Math.max(left.height, right.height) + 30;

        return BoundsRect.fromRect(width, height);
    }

    @Override
    public void draw(Graphics2D g2d, UIContext context) {
        g2d.drawLine(x + width / 2, y, x + left.x + left.width / 2, y + 30);
        g2d.drawLine(x + width / 2, y, x + right.x + right.width / 2, y + 30);
        drawChild(left, g2d, context);
        drawChild(right, g2d, context);
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
}
