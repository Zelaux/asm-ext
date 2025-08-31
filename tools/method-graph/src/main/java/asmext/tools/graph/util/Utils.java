package asmext.tools.graph.util;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.awt.font.FontRenderContext;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.stream.Stream;

public class Utils {

    public static final Label[] tmpLabel1 = new Label[1];
    public static final Label[] tmpLabel2 = new Label[2];

    public static Stream<Label> extractLabel(AbstractInsnNode node) {
        if (node instanceof LabelNode labelNode) return Stream.of(labelNode.getLabel());
        if (node instanceof JumpInsnNode jumpInsnNode) return Stream.of(jumpInsnNode.label.getLabel());
        if (node instanceof LineNumberNode lineNumberNode) return Stream.of(lineNumberNode.start.getLabel());
        List<LabelNode> labels = getSwitchLabels(node);
        return labels == null ? null : labels.stream().map(LabelNode::getLabel);
    }

    public static boolean isSwitchNode(AbstractInsnNode node) {
        return node instanceof LookupSwitchInsnNode || node instanceof TableSwitchInsnNode;
    }

    public static @Nullable List<LabelNode> getSwitchLabels(AbstractInsnNode node) {
        if (node instanceof LookupSwitchInsnNode lookupSwitch)
            return lookupSwitch.labels;
        if (node instanceof TableSwitchInsnNode tableSwitch)
            return tableSwitch.labels;
//        if (node instanceof FrameNode frameNode) return frameNode.;
        return null;
    }

    public static String toString(AbstractInsnNode node) {
        Textifier textifier = new Textifier();
        return toString(node, textifier);
    }

    public static String toString(AbstractInsnNode node, Textifier textifier) {

        node.accept(new TraceMethodVisitor(textifier));
        String trim = textifier.text.get(0).toString().trim();
        textifier.text.clear();
        if (node instanceof FrameNode frameNode) {
            int i = trim.indexOf('[');
            if (i != -1) return trim.substring(0, i).trim();
        }
        return trim;
    }

    public static @NotNull FontRenderContext copyContext(FontRenderContext fontRenderContext) {
        return new FontRenderContext(
                fontRenderContext.getTransform(),
                fontRenderContext.getAntiAliasingHint(),
                fontRenderContext.getFractionalMetricsHint());
    }

    @SneakyThrows
    public static byte @NotNull [] readBytes(File file) {
        byte[] bytes;
        try (FileInputStream stream = new FileInputStream(file)) {
            bytes = stream.readAllBytes();
        }
        return bytes;
    }
}
