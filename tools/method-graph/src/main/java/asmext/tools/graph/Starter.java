package asmext.tools.graph;

import asmext.tools.graph.layout.OldLayoutBuilder;
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
import javax.swing.plaf.FontUIResource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static asmext.tools.graph.Vars.mainGroup;

public class Starter {
    File lastFile;
    private MethodNode selectedMethod;
    private ArrayList<JMenuItem> methodItems;

    @SneakyThrows
    public static void main(String[] args) {
        setUIFont(new FontUIResource(Fonts.jetbrainsNoLigature));
        new Starter().start();
    }

    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    private void start() {
        var frame = new ClassfileGraphApp();

        frame.openItem.addActionListener(e -> {
            FIleChooserHook.chooseFile(frame, "", new FileNameExtensionFilter("Classfiles files (*.class)", "class"), file -> {
                ClassNode classNode = NodeUtil.classNode(Utils.readBytes(file), Opcodes.ASM9);
                initClass(frame, classNode);
                return true;
            }, () -> {});
        });
        frame.openItem.addActionListener(e -> {
            if (lastFile == null) return;
            ClassNode classNode = NodeUtil.classNode(Utils.readBytes(lastFile), Opcodes.ASM9);
            MethodNode selectedMethod = this.selectedMethod;

            initClass(frame, classNode);
            if (selectedMethod != null) {
                List<MethodNode> methods = classNode.methods;
                for (int i = 0; i < methods.size(); i++) {
                    MethodNode method = methods.get(i);
                    if (!method.name.equals(selectedMethod.name)) continue;
                    if (!method.desc.equals(selectedMethod.desc)) continue;
                    selectMethod(frame, classNode, method, methodItems.get(i));

                }
            }
        });

        frame.setSize(600, 600);
        frame.setVisible(true);


        {
            ClassNode classNode = NodeUtil.classNode(TestClass.class, Opcodes.ASM9);
            initClass(frame, classNode);
        }

        startLoop(frame::runUpdate);
    }

    private void initClass(ClassfileGraphApp frame, ClassNode classNode) {
        mainGroup.elements.clear();
        JMenu menu = frame.methodsMenu;
        menu.removeAll();
        this.methodItems = new ArrayList<>();

        boolean first = true;
        selectedMethod = null;
        for (MethodNode method : classNode.methods) {
            JMenuItem menuItem = new JMenuItem(method.name + method.desc);

            methodItems.add(menu.add(menuItem));
            if (method.instructions.getFirst() == null) {
                menuItem.setToolTipText("Empty method");
            }
            if (first) {
                first = false;
                selectMethod(frame, classNode, method, menuItem);

            }
            menuItem.addActionListener(e1 -> {
                selectMethod(frame, classNode, method, menuItem);
            });
        }
        menu.setEnabled(true);
        frame.setTitle(classNode.name);

    }

    private void selectMethod(ClassfileGraphApp frame, ClassNode classNode, MethodNode method, JMenuItem menuItem) {
        try {
            this.selectedMethod = method;
            mainGroup.elements.clear();
            frame.setTitle(classNode.name + ": " + menuItem.getText());
            OldLayoutBuilder.buildLayout(mainGroup, classNode, method, frame.context);
            mainGroup.layout(frame.context);
            for (var other : methodItems) {
                other.setEnabled(true);
            }
            menuItem.setEnabled(false);
        } catch (AnalyzerException ex) {
            throw Lombok.sneakyThrow(ex);
        }
    }

    private void startLoop(Runnable updater) {
        int delay = 1000 / 60; // ~60 fps
        new Timer(delay, e -> {
            if (updater != null) {
                updater.run();
            }
        }).start();
    }
}
