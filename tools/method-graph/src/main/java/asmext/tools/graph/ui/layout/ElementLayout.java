package asmext.tools.graph.ui.layout;

import asmext.tools.graph.ui.UIContext;
import org.jetbrains.annotations.NotNull;

public interface ElementLayout {
    void setLayout(int x,int y,int width,int height);
    @NotNull
    LayoutProperties elementProperties();

    void setPosition(int x, int y);
    void setSize(int width, int height);
    void layout(UIContext context);

    int x();
    int y();
    int height();
    int width();

    void height(int height);
    void width(int width);
    void x(int x);
    void y(int y);
}
