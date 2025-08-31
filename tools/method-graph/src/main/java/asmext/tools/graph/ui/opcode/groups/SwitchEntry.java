package asmext.tools.graph.ui.opcode.groups;

import asmext.tools.graph.Fonts;
import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.ui.elem.Element;
import asmext.tools.graph.ui.layout.ElementLayout;
import asmext.tools.graph.ui.opcode.OpcodePane;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import java.awt.geom.Rectangle2D;

public abstract class SwitchEntry implements ElementLayout {
    public abstract Element inner();

    @RequiredArgsConstructor
    public static class OpcodePaneSwitchEntry extends SwitchEntry implements ElementLayout {
        @Delegate
        public final OpcodePane pane;

        @Override
        public Element inner() {
            return pane;
        }
    }

    public static class LoopSwitchEntry extends SwitchEntry implements ElementLayout {
        @Delegate
        public final Element inner = new Element() {
            {
                var properties = layoutProperties;
                properties.fillX = properties.fillY = true;
            }

            @Override
            public void layout(UIContext context) {
                Rectangle2D bounds = context.style.font.getStringBounds("SYM", Fonts.defaultContext[0]);
                width = (int) (bounds.getWidth() + 0.5f);
                height = (int) (bounds.getHeight() + 0.5f);
                super.layout(context);
            }
        };

        @Override
        public Element inner() {
            return inner;
        }
    }

}
