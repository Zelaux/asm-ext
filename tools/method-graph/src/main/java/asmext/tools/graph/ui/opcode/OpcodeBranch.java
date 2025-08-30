package asmext.tools.graph.ui.opcode;

import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.util.BoundingBox;

import java.awt.*;

public class OpcodeBranch extends OpcodePane {
    public int margin = 10;
    public OpcodePane left, right;

    public OpcodeBranch(int x, int y) {
        super(x, y);
    }

    @Override
    public void update(UIContext context) {
        super.update(context);
        updateChild(left, context);
        updateChild(right, context);
    }

    @Override
    public void draw(Graphics2D g2d, UIContext context) {
        super.draw(g2d, context);
        drawChild(left, g2d, context);
        drawChild(right, g2d, context);
    }


    @Override
    public BoundingBox layout(UIContext context) {
        super.layout(context);
        int myWidth = width;
        int myHeight = height;
        BoundingBox leftBB = left.layout(context);
        BoundingBox rightBB = right.layout(context);

        height += Math.max(leftBB.height(), rightBB.height()) + margin;

        left.y = myHeight + margin;
        right.y = myHeight + margin;

        int sumWidth = margin * 2 + leftBB.width() + rightBB.width();
        int midW = sumWidth / 2;

        int leftleft = -leftBB.left;
        int middle = leftBB.right + margin * 2 + (-rightBB.left);
        int rightright = rightBB.right;

        if (leftleft >= midW) {
            left.x = midW;
        } else {
            left.x = -(midW - leftleft);
        }
        right.x = left.x + middle;

        width = Math.max(sumWidth, width);
        return BoundingBox.centerXRect(width,height);
    }

}
