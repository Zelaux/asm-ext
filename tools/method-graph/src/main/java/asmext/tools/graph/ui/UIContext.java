package asmext.tools.graph.ui;

import asmext.tools.graph.ui.opcode.OpcodeEntry;
import asmext.tools.graph.ui.style.Style;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;

@FieldDefaults(level = AccessLevel.PUBLIC)
public class UIContext {
    float mouseX, mouseY;
    boolean leftMouse;
    public boolean justClicked;
    @Nullable
    public OpcodeEntry hovered;
    public int maxIndex;
    public Style style = new Style();
}
