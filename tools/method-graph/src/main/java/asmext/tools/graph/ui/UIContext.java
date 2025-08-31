package asmext.tools.graph.ui;

import asmext.tools.graph.ZoomPanPanel;
import asmext.tools.graph.ui.opcode.OpcodeEntry;
import asmext.tools.graph.ui.style.Style;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.JumpInsnNode;

import java.util.function.Consumer;

@FieldDefaults(level = AccessLevel.PUBLIC)
@RequiredArgsConstructor
public class UIContext {

    final Consumer<Consumer<ZoomPanPanel>> viewUpdater;
    float mouseX, mouseY;
    boolean leftMouse;
    public boolean justClicked;
    @Nullable
    public OpcodeEntry hovered;
    public int maxIndex;
    public Style style = new Style();

    public OpcodeEntry[] everyEntry;

    @Setting
    public boolean useHorizontalMode;

    public void justClickedOn(OpcodeEntry opcodeEntry, boolean isUpdateOrDraw) {
        if (viewUpdater == null) return;
        if (!(opcodeEntry.insnNode instanceof JumpInsnNode jumpInsnNode)) return;
        int i = OpcodeEntry.MOCK_LIST.indexOf(jumpInsnNode.label);
        int[] targetCords = {0, 0};
        int[] justClickedCords = {0, 0};
        if (isUpdateOrDraw) {
            justClickedCords[0] += opcodeEntry.x;
            justClickedCords[1] += opcodeEntry.y;
        } else opcodeEntry.localToSceneCoordinate(justClickedCords);
        everyEntry[i].localToSceneCoordinate(targetCords);
        int dx = targetCords[0] - justClickedCords[0];
        int dy = targetCords[1] - justClickedCords[1];
        this.viewUpdater.accept(zoomPanPanel -> {
            zoomPanPanel.moveCamera(-dx,-dy);
        });

    }
}
