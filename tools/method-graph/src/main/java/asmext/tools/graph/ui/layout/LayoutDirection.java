package asmext.tools.graph.ui.layout;

import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.ui.elem.Element;
import asmext.tools.graph.util.BoundsRect;

import java.util.List;

public enum LayoutDirection {
    Horizontal {
        @Override
        public BoundsRect layout(ContainerLayoutProperties containerLayoutProperties, List<Element> elements, UIContext context) {
            int height = 0;
            int x = 0;
            boolean hasFillY = false;
            boolean hasCenter = false;
            for (Element element : elements) {
                var properties = element.layoutProperties;
                element.x = x;
                element.y = 0;
                element.layout(context);
                if (properties.fillY) hasFillY = true;
                if (properties.centered) hasCenter = true;
                height = Math.max(element.height, height);
                x += element.width + 1 + containerLayoutProperties.childGap;
            }
            if (hasFillY || hasCenter) {
                for (Element element : elements) {
                    LayoutProperties properties = element.layoutProperties;
                    if (properties.fillY) element.height = height;
                    if (properties.centered) element.y = (height - element.height) / 2;
                }
            }
            return BoundsRect.fromRect(x, height);
        }
    },
    Vertical {
        @Override
        public BoundsRect layout(ContainerLayoutProperties containerLayoutProperties, List<Element> elements, UIContext context) {
            int width = 0;
            int y = 0;
            boolean hasFillX = false;
            boolean hasCenter = false;
            for (Element element : elements) {
                var properties = element.layoutProperties;
                element.y = y;
                element.x = 0;
                element.layout(context);
                if (properties.fillX) hasFillX = true;
                if (properties.centered) hasCenter = true;
                width = Math.max(element.width, width);
                y += element.height + 1 + containerLayoutProperties.childGap;
            }
            if (hasFillX || hasCenter) {
                for (Element element : elements) {
                    LayoutProperties properties = element.layoutProperties;
                    if (properties.fillX) element.width = width;
                    if (properties.centered) element.x = (width - element.width) / 2;
                }
            }
            return BoundsRect.fromRect(width, y);
        }
    },
    NoLayout {
        @Override
        public BoundsRect layout(ContainerLayoutProperties containerLayoutProperties, List<Element> elements, UIContext context) {
            for (Element element : elements) {
                element.layout(context);
            }
            return BoundsRect.fromRect(0, 0);
        }
    };

    public abstract BoundsRect layout(ContainerLayoutProperties containerLayoutProperties, List<Element> elements, UIContext context);
}
