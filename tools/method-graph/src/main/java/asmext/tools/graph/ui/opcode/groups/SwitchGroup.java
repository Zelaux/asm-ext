package asmext.tools.graph.ui.opcode.groups;

import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.ui.elem.Element;
import asmext.tools.graph.ui.elem.ElementWithChild;
import asmext.tools.graph.ui.layout.ContainerLayout;
import asmext.tools.graph.ui.layout.ContainerLayoutProperties;
import asmext.tools.graph.ui.layout.LayoutPlacement;
import asmext.tools.graph.ui.layout.LayoutProperties;
import asmext.tools.graph.ui.opcode.OpcodePane;
import asmext.tools.graph.ui.opcode.layout.IfLayout;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Consumer;

public class SwitchGroup extends ElementWithChild implements ContainerLayout {
    public final ArrayList<SwitchEntry> entries = new ArrayList<>();

    public final ContainerLayoutProperties containerLayout = (ContainerLayoutProperties) layoutProperties;


    @Override
    public void update(UIContext context) {
        super.update(context);
    }

    @Override
    public void layout(UIContext context) {
        LayoutPlacement placement = context.useHorizontalMode ? IfLayout.horizontal : IfLayout.vertical;
        placement.layout(this, entries, context);
    }

    @Override
    public void draw(Graphics2D g2d, UIContext context) {
        super.draw(g2d, context);
    }

    @Override
    protected @NotNull LayoutProperties createLayoutProperties() {
        return new ContainerLayoutProperties();
    }

    @Override
    public boolean removeChild(Element prevParent) {
        ArrayList<SwitchEntry> panes1 = entries;
        for (int i = 0; i < panes1.size(); i++) {
            SwitchEntry switchEntry = panes1.get(i);
            if (switchEntry.inner() == prevParent) {
                panes1.remove(i);
                break;
            }
        }
        return false;
    }

    @Override
    public void eachChild(Consumer<Element> visitor) {
        for (SwitchEntry entry : entries) {
            visitor.accept(entry.inner());
        }
        ;
    }

    public void eachPaneChild(Consumer<OpcodePane> visitor) {
        for (var pane : entries)
            if (pane instanceof SwitchEntry.OpcodePaneSwitchEntry entry) {
                visitor.accept(entry.pane);
            }

    }

    @Override
    public ContainerLayoutProperties containerProperties() {
        return containerLayout;
    }
}
