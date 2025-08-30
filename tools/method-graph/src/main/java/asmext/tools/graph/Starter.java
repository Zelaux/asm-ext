package asmext.tools.graph;

import asmext.tools.graph.layout.LayoutBuilder;
import asmext.tools.graph.util.FIleChooserHook;
import asmext.tools.graph.util.Utils;
import asmlib.util.NodeUtil;
import lombok.Lombok;
import lombok.SneakyThrows;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.ArrayList;

import static asmext.tools.graph.Vars.mainGroup;

public class Starter {
    @SneakyThrows
    public static void main(String[] args) {
        var frame = new ClassfileGraphApp();

        frame.openItem.addActionListener(e -> {
            FIleChooserHook.chooseFile(frame, "", new FileNameExtensionFilter("Classfiles files (*.class)", "class"), file -> {
                ClassNode classNode = NodeUtil.classNode(Utils.readBytes(file), Opcodes.ASM9);
                initClass(frame, classNode);
                return true;
            }, () -> {});

        });

        frame.setSize(600, 600);
        frame.setVisible(true);


        {
            ClassNode classNode = NodeUtil.classNode(TestClass.class, Opcodes.ASM9);
            initClass(frame, classNode);
        }

        startLoop(frame::runUpdate);
    }

    private static void initClass(ClassfileGraphApp frame, ClassNode classNode) {
        mainGroup.elements.clear();
        JMenu menu = frame.methodsMenu;
        menu.removeAll();
        ArrayList<JMenuItem> items = new ArrayList<>();
        boolean first=true;
        for (MethodNode method : classNode.methods) {
            JMenuItem menuItem = new JMenuItem(method.name + method.desc);

            items.add(menu.add(menuItem));
            if (method.instructions.getFirst() == null) {
                menuItem.setToolTipText("Empty method");
            }
            if(first){
                first=false;
                selectMethod(frame, classNode, method, menuItem, items);

            }
            menuItem.addActionListener(e1 -> {
                selectMethod(frame, classNode, method, menuItem, items);
            });
        }
        menu.setEnabled(true);
        frame.setTitle(classNode.name);
    }

    private static void selectMethod(ClassfileGraphApp frame, ClassNode classNode, MethodNode method, JMenuItem menuItem, ArrayList<JMenuItem> items) {
        try {
            mainGroup.elements.clear();
            frame.setTitle(classNode.name + ": " + menuItem.getText());
            LayoutBuilder.buildLayout(mainGroup, classNode, method, frame.context);
            mainGroup.layout(frame.context);
            for (var other : items) {
                other.setEnabled(true);
            }
            menuItem.setEnabled(false);
        } catch (AnalyzerException ex) {
            throw Lombok.sneakyThrow(ex);
        }
    }

    public static void startLoop(Runnable updater) {
        int delay = 1000 / 60; // ~60 fps
        new Timer(delay, e -> {
            if (updater != null) {
                updater.run();
            }
        }).start();
    }
}
