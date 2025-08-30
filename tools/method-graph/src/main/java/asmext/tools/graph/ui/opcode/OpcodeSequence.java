package asmext.tools.graph.ui.opcode;

import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.util.BoundingBox;

import java.awt.*;

public class OpcodeSequence extends OpcodePane {
    public int margin = 10;
    public OpcodePane next;

    public OpcodeSequence(int x, int y) {
        super(x, y);
    }

    @Override
    public void update(UIContext context) {
        super.update(context);
        updateChild(next, context);
    }

    @Override
    public void draw(Graphics2D g2d, UIContext context) {
        super.draw(g2d, context);
        drawChild(next, g2d, context);
    }


    @Override
    public BoundingBox layout(UIContext context) {
        super.layout(context);
        int myWidth = width;
        int myHeight = height;
        BoundingBox nextBB = next.layout(context);

        height += nextBB.height() + margin;

        width=Math.max(myWidth,nextBB.width());
        return BoundingBox.fromRect(width,height);
    }

}
