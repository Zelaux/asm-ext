package asmext.tools.graph.ui.opcode;

import asmext.analytics.controlflow.ControlFlowNode;
import asmext.tools.graph.Fonts;
import asmext.tools.graph.ui.HighlightHandler;
import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.ui.elem.Element;
import asmext.tools.graph.ui.style.Style;
import asmext.tools.graph.util.Utils;
import lombok.Getter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.util.Textifier;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OpcodeEntry extends Element {
    public static final InsnList MOCK_LIST = new InsnList();
    public final AbstractInsnNode insnNode;
    public final ControlFlowNode controlFlowNode;
    public final Set<Label> labelSet;
    public int margin = 2;
    int textWidth;
    @Getter
    Textifier textifier;
    int textHeight;
    boolean hover = false;
    boolean highlight = false;
    private int lineNumPadding;

    public OpcodeEntry(AbstractInsnNode insnNode, ControlFlowNode controlFlowNode, Textifier textifier) {

        this.insnNode = insnNode;
        this.controlFlowNode = controlFlowNode;
        this.textifier = textifier;
        Stream<Label> stream = Utils.extractLabel(insnNode);
        labelSet = stream == null ? Set.of() : stream.collect(Collectors.toSet());
    }

    public static OpcodeEntry opcodeEntry(ControlFlowNode node, Textifier textifier) {
        return new OpcodeEntry(node.node, node, textifier).fillX(true);
    }

    @Override
    public OpcodeEntry fillX(boolean fillX) {
        super.fillX(fillX);
        return this;
    }

    @Override
    public OpcodeEntry fillY(boolean fillY) {
        super.fillY(fillY);
        return this;
    }

    @Override
    public OpcodeEntry text(String text) {
        super.text(text);
        return this;
    }

    public void layout(UIContext context) {
        context.everyEntry[controlFlowNode.myIndex] = this;
        text = Utils.toString(insnNode, textifier);
        text = text.split("\n")[0];
        super.layout(context);
        var style = context.style;
        var bounds = style.font.getStringBounds("" + (context.maxIndex * 10), Fonts.defaultContext[0]);
        lineNumPadding = (int) (bounds.getWidth() + 0.5f);
        width += margin + lineNumPadding;
        textWidth = width;
        textHeight = height;
        width += margin * 2;
        height += margin * 2;

    }

    @Override
    public void update(UIContext context) {
        super.update(context);
        hover = x <= context.mouseX && context.mouseX <= x + width && y <= context.mouseY && context.mouseY <= y + height;
        if (hover) {
            context.hovered = this;
            if (context.justClicked) {
                context.justClickedOn(this, true);
            }
        } else if (context.hovered == this) {
            context.hovered = null;
        }

        OpcodeEntry hovered = context.hovered;

        if (!hover && hovered != null) {
            highlight = HighlightHandler.shouldHighlight(context, this, hovered);

        } else highlight = false;
    }


    @Override
    public void draw(Graphics2D g2d, UIContext context) {
        if (drawBounds) {
            g2d.setStroke(new BasicStroke(1));
            g2d.setColor(Color.red);
            g2d.drawRect(x, y, width, height);
            g2d.setColor(Color.green);
            g2d.drawRect(x + margin, y + margin, width - margin * 2, height - margin * 2);
        }
//        g2d.setColor(new Color(0x2B2D30));
        Style style = context.style;
        if (hover) {
            g2d.setColor(style.hoverBackgroundColor);
            g2d.fillRect(x, y, width, height);
            g2d.setColor(style.hoverIndexColor);
        } else if (highlight) {
            g2d.setColor(style.highlighBackgroundColor);
            g2d.fillRect(x, y, width, height);
            g2d.setColor(style.highlighIndexColor);
        } else {
            g2d.setColor(style.indexColor);
        }
        {
            String str = MOCK_LIST.indexOf(insnNode) + "";
            Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(str, g2d);
            int i = (int) (bounds.getWidth() + 0.5);
            g2d.drawString(str, x + lineNumPadding - i, y + textHeight);

        }

        g2d.setColor(style.fontColor);
        g2d.drawString(text, x + margin + lineNumPadding + margin, (int) (y + textHeight));
    }
}
