package asmext.tools.graph.ui.opcode;

import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.ui.elem.Group;
import asmext.tools.graph.ui.layout.LayoutDirection;

import java.awt.*;

public class OpcodePane extends Group {

    public OpcodePane(int x,int y) {
        super(x, y, 0,0, LayoutDirection.Vertical);
    }

    @Override
    public void draw(Graphics2D g2d, UIContext context) {
        g2d.setColor(context.style.paneBackgroundColor);
//        g2d.fill(new Rectangle2D.Float(x,y,width,height));
        g2d.fillRect(x, y, width, height);
        super.draw(g2d, context);
    }

}
