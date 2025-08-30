package asmext.tools.graph;


import asmext.tools.graph.ui.UIContext;
import asmext.tools.graph.ui.style.Style;
import asmext.tools.graph.util.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;

import static asmext.tools.graph.Vars.mainGroup;

public class ClassfileGraphApp extends JFrame {

    public static final String APP_TITLE = "Classfile graph app";

    public final JMenuBar menuBar;
    public final ZoomPanPanel panel;
    public final UIContext context;
    public final JMenu fileMenu;
    public final JMenuItem openItem;
    public final JMenuItem reloadItem;
    public final JMenuItem exitItem;
    public final JMenu methodsMenu;

    public ClassfileGraphApp() throws HeadlessException {
        super(APP_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        menuBar = new JMenuBar();

        fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        openItem = new JMenuItem("Open");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));

        reloadItem = new JMenuItem("Reload");
        reloadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));

        exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));

        fileMenu.add(openItem);
//        fileMenu.add(recent);
        fileMenu.add(reloadItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);


        methodsMenu = new JMenu("Methods");
        menuBar.add(methodsMenu);
        methodsMenu.setEnabled(false);
        createMethodsMenu();

        setJMenuBar(menuBar);

        panel = new ZoomPanPanel(this::draw);
        add(panel, BorderLayout.CENTER);


        context = new UIContext();
        MousePositionTaker positionTaker = new MousePositionTaker(context, this.panel);
        panel.addMouseListener(positionTaker);
        panel.addMouseMotionListener(positionTaker);
    }

    private void createMethodsMenu() {

    }


    public void runUpdate() {

        mainGroup.update(context);
        context.justClicked = false;
        panel.repaint();

    }

    @Override
    protected void frameInit() {
        super.frameInit();
    }

    private void draw(Graphics2D g2, AffineTransform transform) {
        Style style = context.style;
        g2.setFont(style.font);
        g2.setColor(style.sceneBackgroundColor);
        g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());
        g2.setTransform(transform);
        FontRenderContext fontRenderContext = Fonts.defaultContext[0];
        if (!fontRenderContext.equals(g2.getFontRenderContext())) {
            Fonts.defaultContext[0] = Utils.copyContext(g2.getFontRenderContext());
            mainGroup.layout(context);
        }
        mainGroup.draw(g2, context);
    }
}
