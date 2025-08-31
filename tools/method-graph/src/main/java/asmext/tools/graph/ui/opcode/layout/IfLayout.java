package asmext.tools.graph.ui.opcode.layout;

import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.ui.layout.ContainerLayout;
import asmext.tools.graph.ui.layout.ElementLayout;
import asmext.tools.graph.ui.layout.LayoutPlacement;

import java.util.List;

public interface IfLayout {

    LayoutPlacement horizontal = new LayoutPlacement() {

        @Override
        public void layout(ContainerLayout container, List<? extends ElementLayout> elements, UIContext context) {
            LayoutPlacement.Horizontal.layout(container, elements, context);
            for (ElementLayout element : elements) {
                element.y(element.y() + 30);
            }
            container.height(container.height() + 30);
        }
    };
    LayoutPlacement vertical = new LayoutPlacement() {

        @Override
        public void layout(ContainerLayout container, List<? extends ElementLayout> elements, UIContext context) {
            LayoutPlacement.Vertical.layout(container, elements, context);
            for (ElementLayout element : elements) {
                element.x(element.x() + 30);
            }
            container.width(container.width() + 30);
        }
    };
}
