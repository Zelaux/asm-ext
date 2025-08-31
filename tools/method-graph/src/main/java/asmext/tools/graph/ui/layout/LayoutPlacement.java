package asmext.tools.graph.ui.layout;

import asmext.tools.graph.ui.UIContext;

import java.util.List;

public abstract class LayoutPlacement {
    public static final LayoutPlacement Horizontal = new LayoutPlacement() {
        @Override
        public void layout(ContainerLayout container, List<? extends ElementLayout> elements, UIContext context) {
            int height = 0;
            int x = 0;
            boolean hasFillY = false;
            boolean hasCenter = false;
            var layoutProperties = container.containerProperties();
            for (var element : elements) {
                var properties = element.elementProperties();
                element.setPosition(x, 0);
                element.layout(context);
                if (properties.fillY) hasFillY = true;
                if (properties.centered) hasCenter = true;
                height = Math.max(element.height(), height);
                x += element.width() + 1 + layoutProperties.childGap;
            }
            if (hasFillY || hasCenter) {
                for (var element : elements) {
                    var properties = element.elementProperties();
                    if (properties.fillY) element.height(height);
                    if (properties.centered) element.y((height - element.height()) / 2);
                }
            }
            container.setSize(x-layoutProperties.childGap, height);
        }
    };
    public static final LayoutPlacement Vertical = new LayoutPlacement() {
        @Override
        public void layout(ContainerLayout container, List<? extends ElementLayout> elements, UIContext context) {
            int width = 0;
            int y = 0;
            int totalHeight = 0;
            boolean hasFillX = false;
            boolean hasCenter = false;
            var layoutProperties = container.containerProperties();
            for (var element : elements) {
                var properties = element.elementProperties();
                element.setPosition(0, y);
                element.layout(context);
                if (properties.fillX) hasFillX = true;
                if (properties.centered) hasCenter = true;
                width = Math.max(element.width(), width);
                totalHeight += element.height() + 1;
                y += element.height() + 1 + layoutProperties.childGap;
            }
            if (hasFillX || hasCenter) {
                for (var element : elements) {
                    var properties = element.elementProperties();
                    if (properties.fillX) element.width(width);
                    if (properties.centered) element.x((width - element.width()) / 2);
                }
            }
            container.setSize(width, y-layoutProperties.childGap);
        }
    };
    public static final LayoutPlacement NoLayout = new LayoutPlacement() {
        @Override
        public void layout(ContainerLayout container, List<? extends ElementLayout> elements, UIContext context) {
            for (var element : elements) {
                element.layout(context);
            }
        }
    };

    public abstract void layout(ContainerLayout container, List<? extends ElementLayout> elements, UIContext context);
}
