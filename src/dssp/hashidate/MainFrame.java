package dssp.hashidate;

import java.awt.AWTException;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import dssp.brailleLib.Util;
import dssp.hashidate.config.Config;
import dssp.hashidate.io.ExportBPLOT;
import dssp.hashidate.io.ExportSVG;
import dssp.hashidate.io.ImportSVG;
import dssp.hashidate.misc.About;
import dssp.hashidate.misc.CursorFactory;
import dssp.hashidate.misc.ObjectPopupMenu;
import dssp.hashidate.misc.ObjectUndoManager;
import dssp.hashidate.shape.DesignObject;
import dssp.hashidate.shape.DesignObject.STATUS;
import dssp.hashidate.shape.SHAPE;
import dssp.hashidate.shape.ShapeGroup;
import dssp.hashidate.shape.helper.ObjectFactory;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class MainFrame {
    public static String APP_NAME = "橋立";
    public static int[] VERSION = { 1, 8 };
    public static String VERSION_FORMAT = "%d.%02d";
    public static ImageIcon icon;

    private JFrame frmSample;
    private ObjectManager objMan = new ObjectManager();
    private SHAPE type = SHAPE.NONE;
    private HashMap<SHAPE, JToggleButton> toolBarMap = new HashMap<SHAPE, JToggleButton>();
    private HashMap<String, JMenuItem> menuMap = new HashMap<String, JMenuItem>();
    private DesignPanel designPanel = new DesignPanel();
    private List<DesignObject> selObjList = new Vector<DesignObject>();
    private DesignObject ctrlObject = null;
    private boolean isSelecting = false;
    private Point firstPoint = null;
    private Point prevPoint = null;
    private Color frameColor = null;
    private int arrowCount = 0;

    private final DesignObject.StatusHint hint = new DesignObject.StatusHint();

    public DesignObject.StatusHint getHint() {
        return this.hint;
    }

    public void setHint(DesignObject.StatusHint hint) {
    }

    private enum MOUSE_STATUS {
        RELEASED, PRESSED, DRAGGED
    };

    private MOUSE_STATUS mouseStatus = MOUSE_STATUS.RELEASED;

    /**
    * Launch the application.
    */
    public static void main(String[] args) {
        // 起動中のウィンドウを表示する
        icon = new ImageIcon(
                Toolkit.getDefaultToolkit().getImage(MainFrame.class.getClassLoader().getResource("img/neko1.png")));
        Util.waitBoard(String.format("橋立 " + VERSION_FORMAT, VERSION[0], VERSION[1]), icon, "辞書読み込み中…", true);

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainFrame window = new MainFrame(args);
                    Util.waitBoard(null, null, null, false);
                    window.frmSample.setVisible(true);

                    About.check();
                } catch (Exception e) {
                    Util.logException(e);
                }
            }
        });
    }

    /**
    * Create the application.
    */
    public MainFrame(String[] args) {
        if (0 < args.length) {
            for (String arg : args) {
                if (arg.equals("-debug")) {
                    Util.setDebug(true);
                } else {
                    this.initialPath = arg;
                }
            }
        }

        initialize();

        try {
            javax.swing.UIManager
                    .setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            javax.swing.SwingUtilities.updateComponentTreeUI(this.frmSample);
        } catch (Exception e) {
            Util.logException(e);
        }
    }

    /**
    * 変更されたかどうかを確認する
    */
    private void saveCheck() {
        if (this.objMan.isChanged()) {
            String text = String.format("変更されています。\n保存しますか？");
            switch (Util.select2(text, false)) {
            case JOptionPane.YES_OPTION:
                if (false == saveFile(this.objMan.getFile(), true)) {
                    return;
                }
                break;
            case JOptionPane.CANCEL_OPTION:
                return;
            }
        }
    }

    /**
    * 終了処理
    */
    void finish() {
        saveCheck();
        Util.logInfo("Finished.");
        Util.close();
        System.exit(0);
    }

    GridBagLayout toolBarV;
    GridBagLayout toolBarH;

    Robot robot;

    /**
    * ウィンドウを初期化する
    */
    private void initialize() {
        Util.logInfo("Start Ver." + VERSION_FORMAT, VERSION[0], VERSION[1]);
        ClassLoader cl = this.getClass().getClassLoader();
        Config.load();
        CursorFactory.init();
        ObjectUndoManager.init(this.objMan);

        if (null != this.initialPath && false == this.initialPath.isEmpty()) {
            File file = new File(this.initialPath);
            String parent = file.getParent();
            if (null != parent) {
                System.setProperty("user.dir", parent);
            }
        }

        this.frameColor = Config.getConfig(Config.COLOR.FRAME);

        frmSample = new JFrame();
        frmSample.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frmSample.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                finish();
            }

            @Override
            public void windowActivated(WindowEvent arg0) {
                initialFile();
            }
        });
        frmSample.setIconImage(Toolkit.getDefaultToolkit().getImage(
                cl.getResource("img/neko1.png")));
        // frmSample.setIconImage(Toolkit.getDefaultToolkit().getImage(cl.getResource("img/warai4.png")));
        // frmSample.setIconImage(Toolkit.getDefaultToolkit().getImage(cl.getResource("img/title.png")));
        frmSample.setTitle(APP_NAME);
        frmSample.setBounds(100, 100, 718, 538);
        frmSample.getContentPane().setLayout(new BorderLayout(0, 0));

        JToolBar toolBar = new JToolBar();
        toolBar.setOrientation(SwingConstants.VERTICAL);
        toolBar.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent arg0) {
                JToolBar toolBar = (JToolBar) arg0.getSource();
                if (0 == toolBar.getComponentCount()) {
                    return;
                }
                JPanel panel = (JPanel) toolBar.getComponent(0);
                switch (toolBar.getOrientation()) {
                case JToolBar.VERTICAL:
                    panel.setLayout(toolBarV);
                    break;
                case JToolBar.HORIZONTAL:
                    panel.setLayout(toolBarH);
                    break;
                }
            }
        });
        frmSample.getContentPane().add(toolBar, BorderLayout.WEST);

        JPanel panel = new JPanel();
        toolBar.add(panel);
        GridBagLayout gbl_panel = new GridBagLayout();
        gbl_panel.columnWidths = new int[] { 32, 32, 0 };
        gbl_panel.rowHeights = new int[] { 32, 32, 32, 32, 32, 32, 0, 0 };
        gbl_panel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
        gbl_panel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                Double.MIN_VALUE };
        panel.setLayout(gbl_panel);

        JToggleButton tglbtnX = new JToggleButton(new ImageIcon(
                cl.getResource("img/cursor.png")));
        tglbtnX.setBackground(UIManager.getColor("Button.background"));
        tglbtnX.setPreferredSize(new Dimension(36, 36));
        GridBagConstraints gbc_tglbtnX = new GridBagConstraints();
        gbc_tglbtnX.insets = new Insets(0, 0, 5, 5);
        gbc_tglbtnX.fill = GridBagConstraints.BOTH;
        gbc_tglbtnX.gridx = 0;
        gbc_tglbtnX.gridy = 0;
        panel.add(tglbtnX, gbc_tglbtnX);
        tglbtnX.setToolTipText("図形の選択");
        tglbtnX.setSelected(true);
        tglbtnX.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                selectDesignObject(btn, SHAPE.NONE);
            }
        });
        toolBarMap.put(SHAPE.NONE, tglbtnX);

        JToggleButton toggleButton_1 = new JToggleButton(new ImageIcon(
                cl.getResource("img/line.png")));
        toggleButton_1.setBackground(UIManager.getColor("Button.background"));
        toggleButton_1.setPreferredSize(new Dimension(36, 36));
        GridBagConstraints gbc_toggleButton_1 = new GridBagConstraints();
        gbc_toggleButton_1.insets = new Insets(0, 0, 5, 0);
        gbc_toggleButton_1.fill = GridBagConstraints.BOTH;
        gbc_toggleButton_1.gridx = 1;
        gbc_toggleButton_1.gridy = 0;
        panel.add(toggleButton_1, gbc_toggleButton_1);
        toggleButton_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                selectDesignObject(btn, SHAPE.LINE);
            }
        });
        toggleButton_1.setToolTipText("直線");
        toolBarMap.put(SHAPE.LINE, toggleButton_1);

        JToggleButton tglbtnA = new JToggleButton(new ImageIcon(
                cl.getResource("img/rectangle.png")));
        tglbtnA.setPreferredSize(new Dimension(36, 36));
        GridBagConstraints gbc_tglbtnA = new GridBagConstraints();
        gbc_tglbtnA.insets = new Insets(0, 0, 5, 5);
        gbc_tglbtnA.fill = GridBagConstraints.BOTH;
        gbc_tglbtnA.gridx = 0;
        gbc_tglbtnA.gridy = 1;
        panel.add(tglbtnA, gbc_tglbtnA);
        tglbtnA.setToolTipText("四角");
        tglbtnA.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                selectDesignObject(btn, SHAPE.RECTANGLE);
            }
        });
        toolBarMap.put(SHAPE.RECTANGLE, tglbtnA);

        JToggleButton toggleButton = new JToggleButton(new ImageIcon(
                cl.getResource("img/polyline.png")));
        toggleButton.setPreferredSize(new Dimension(36, 36));
        GridBagConstraints gbc_toggleButton = new GridBagConstraints();
        gbc_toggleButton.insets = new Insets(0, 0, 5, 0);
        gbc_toggleButton.fill = GridBagConstraints.BOTH;
        gbc_toggleButton.gridx = 1;
        gbc_toggleButton.gridy = 1;
        panel.add(toggleButton, gbc_toggleButton);
        toggleButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                selectDesignObject(btn, SHAPE.POLYLINE);
            }
        });
        toggleButton.setToolTipText("折れ線");
        toolBarMap.put(SHAPE.POLYLINE, toggleButton);

        JToggleButton toggleButton_7 = new JToggleButton(new ImageIcon(cl.getResource("img/polygon.png")));
        toggleButton_7.setPreferredSize(new Dimension(36, 36));
        GridBagConstraints gbc_toggleButton_7 = new GridBagConstraints();
        gbc_toggleButton_7.insets = new Insets(0, 0, 5, 5);
        gbc_toggleButton_7.fill = GridBagConstraints.BOTH;
        gbc_toggleButton_7.gridx = 0;
        gbc_toggleButton_7.gridy = 2;
        panel.add(toggleButton_7, gbc_toggleButton_7);
        toggleButton_7.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                selectDesignObject(btn, SHAPE.POLYGON);
            }
        });
        toggleButton_7.setToolTipText("多角形");
        toolBarMap.put(SHAPE.POLYGON, toggleButton_7);

        JToggleButton toggleButton_6 = new JToggleButton(new ImageIcon(
                cl.getResource("img/spline.png")));
        toggleButton_6.setPreferredSize(new Dimension(36, 36));
        GridBagConstraints gbc_toggleButton_6 = new GridBagConstraints();
        gbc_toggleButton_6.insets = new Insets(0, 0, 5, 0);
        gbc_toggleButton_6.fill = GridBagConstraints.BOTH;
        gbc_toggleButton_6.gridx = 1;
        gbc_toggleButton_6.gridy = 2;
        panel.add(toggleButton_6, gbc_toggleButton_6);
        toggleButton_6.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                selectDesignObject(btn, SHAPE.SPLINE);
            }
        });
        toggleButton_6.setToolTipText("スプライン曲線");
        toolBarMap.put(SHAPE.SPLINE, toggleButton_6);

        JToggleButton toggleButton_8 = new JToggleButton(new ImageIcon(
                cl.getResource("img/spline_loop.png")));
        toggleButton_8.setPreferredSize(new Dimension(36, 36));
        GridBagConstraints gbc_toggleButton_8 = new GridBagConstraints();
        gbc_toggleButton_8.insets = new Insets(0, 0, 5, 5);
        gbc_toggleButton_8.fill = GridBagConstraints.BOTH;
        gbc_toggleButton_8.gridx = 0;
        gbc_toggleButton_8.gridy = 3;
        panel.add(toggleButton_8, gbc_toggleButton_8);
        toggleButton_8.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                selectDesignObject(btn, SHAPE.SPLINE_LOOP);
            }
        });
        toggleButton_8.setToolTipText("スプライン閉曲線");
        toolBarMap.put(SHAPE.SPLINE_LOOP, toggleButton_8);

        JToggleButton toggleButton_3 = new JToggleButton(new ImageIcon(
                cl.getResource("img/circle.png")));
        toggleButton_3.setPreferredSize(new Dimension(36, 36));
        GridBagConstraints gbc_toggleButton_3 = new GridBagConstraints();
        gbc_toggleButton_3.insets = new Insets(0, 0, 5, 0);
        gbc_toggleButton_3.fill = GridBagConstraints.BOTH;
        gbc_toggleButton_3.gridx = 1;
        gbc_toggleButton_3.gridy = 3;
        panel.add(toggleButton_3, gbc_toggleButton_3);
        toggleButton_3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                selectDesignObject(btn, SHAPE.CIRCLE);
            }
        });
        toggleButton_3.setToolTipText("円");
        toolBarMap.put(SHAPE.CIRCLE, toggleButton_3);

        JToggleButton toggleButton_5 = new JToggleButton(new ImageIcon(
                cl.getResource("img/eclipse.png")));
        toggleButton_5.setPreferredSize(new Dimension(36, 36));
        GridBagConstraints gbc_toggleButton_5 = new GridBagConstraints();
        gbc_toggleButton_5.insets = new Insets(0, 0, 5, 5);
        gbc_toggleButton_5.fill = GridBagConstraints.BOTH;
        gbc_toggleButton_5.gridx = 0;
        gbc_toggleButton_5.gridy = 4;
        panel.add(toggleButton_5, gbc_toggleButton_5);
        toggleButton_5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                selectDesignObject(btn, SHAPE.ELLIPSE);
            }
        });
        toggleButton_5.setToolTipText("楕円");
        toolBarMap.put(SHAPE.ELLIPSE, toggleButton_5);

        JToggleButton toggleButton_2 = new JToggleButton(new ImageIcon(
                cl.getResource("img/spring.png")));
        toggleButton_2.setPreferredSize(new Dimension(36, 36));
        GridBagConstraints gbc_toggleButton_2 = new GridBagConstraints();
        gbc_toggleButton_2.insets = new Insets(0, 0, 5, 0);
        gbc_toggleButton_2.fill = GridBagConstraints.BOTH;
        gbc_toggleButton_2.gridx = 1;
        gbc_toggleButton_2.gridy = 4;
        panel.add(toggleButton_2, gbc_toggleButton_2);
        toggleButton_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                selectDesignObject(btn, SHAPE.SPRING);
            }
        });
        toggleButton_2.setToolTipText("バネ");
        toolBarMap.put(SHAPE.SPRING, toggleButton_2);

        JToggleButton toggleButton_4 = new JToggleButton(new ImageIcon(
                cl.getResource("img/text.png")));
        toggleButton_4.setPreferredSize(new Dimension(36, 36));
        GridBagConstraints gbc_toggleButton_4 = new GridBagConstraints();
        gbc_toggleButton_4.insets = new Insets(0, 0, 5, 5);
        gbc_toggleButton_4.fill = GridBagConstraints.BOTH;
        gbc_toggleButton_4.gridx = 0;
        gbc_toggleButton_4.gridy = 5;
        panel.add(toggleButton_4, gbc_toggleButton_4);
        toggleButton_4.setToolTipText("テキスト/数式");
        toggleButton_4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                selectDesignObject(btn, SHAPE.TEXT);
            }
        });
        toolBarMap.put(SHAPE.TEXT, toggleButton_4);

        JToggleButton toggleButton_9 = new JToggleButton(new ImageIcon(
                cl.getResource("img/graph.png")));
        toggleButton_9.setPreferredSize(new Dimension(36, 36));
        GridBagConstraints gbc_toggleButton_9 = new GridBagConstraints();
        gbc_toggleButton_9.insets = new Insets(0, 0, 5, 0);
        gbc_toggleButton_9.fill = GridBagConstraints.BOTH;
        gbc_toggleButton_9.gridx = 1;
        gbc_toggleButton_9.gridy = 5;
        panel.add(toggleButton_9, gbc_toggleButton_9);
        toggleButton_9.setToolTipText("グラフ");
        toggleButton_9.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                selectDesignObject(btn, SHAPE.GRAPH);
            }
        });
        toolBarMap.put(SHAPE.GRAPH, toggleButton_9);

        JToggleButton toggleButton_10 = new JToggleButton(new ImageIcon(
                cl.getResource("img/image.png")));
        toggleButton_10.setPreferredSize(new Dimension(36, 36));
        GridBagConstraints gbc_toggleButton_10 = new GridBagConstraints();
        gbc_toggleButton_10.insets = new Insets(0, 0, 0, 5);
        gbc_toggleButton_10.gridx = 0;
        gbc_toggleButton_10.gridy = 6;
        panel.add(toggleButton_10, gbc_toggleButton_10);
        toggleButton_10.setToolTipText("画像");
        toggleButton_10.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                selectDesignObject(btn, SHAPE.IMAGE);
            }
        });
        toolBarMap.put(SHAPE.IMAGE, toggleButton_10);

        this.designPanel.setFont(new Font("ＭＳ Ｐゴシック", Font.PLAIN, 12));
        this.designPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent arg0) {
                int key = arg0.getKeyCode();
                int mod = arg0.getModifiersEx();
                if (0 != (mod & KeyEvent.CTRL_DOWN_MASK)) {
                    switch (key) {
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_DOWN:
                        arrowPressed(key, mod);
                        arg0.consume();
                        break;
                    case KeyEvent.VK_Z:
                        undo();
                        arg0.consume();
                        break;
                    case KeyEvent.VK_Y:
                        redo();
                        arg0.consume();
                        break;
                    case KeyEvent.VK_A:
                        selectAll();
                        arg0.consume();
                        break;
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent arg0) {
                arrowReleased();
            }
        });

        this.designPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                dragged(e.getPoint(), e.getModifiers());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                moved(e.getPoint());
            }
        });
        this.designPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                switch (e.getButton()) {
                case MouseEvent.BUTTON1:
                    switch (e.getClickCount()) {
                    case 1:
                        if (MouseEvent.CTRL_DOWN_MASK == (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK)) {
                            clicked(e.getPoint(), false);
                        } else {
                            clicked(e.getPoint(), true);
                        }
                        break;
                    case 2:
                        doubleClicked(e.getPoint());
                    }
                    break;
                case MouseEvent.BUTTON3:
                    rightClicked(e.getPoint());
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                switch (e.getButton()) {
                case MouseEvent.BUTTON1:
                    pressed(e.getPoint());
                    break;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                switch (e.getButton()) {
                case MouseEvent.BUTTON1:
                    released(e.getPoint());
                    break;
                }
            }
        });
        this.designPanel.setBackground(Color.WHITE);
        this.designPanel.setObjectManager(this.objMan);
        this.designPanel.setLayout(null);
        Dimension size = this.designPanel.getPreferredSize();

        JScrollPane scrollPane = new JScrollPane();
        frmSample.getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel backPanel = new JPanel();

        backPanel.add(this.designPanel);
        backPanel.setMinimumSize(size);
        scrollPane.setViewportView(backPanel);

        JMenuBar menuBar = new JMenuBar();
        frmSample.setJMenuBar(menuBar);

        JMenu mnNewMenu = new JMenu("ファイル(F)");
        mnNewMenu.setMnemonic('F');
        mnNewMenu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(mnNewMenu);

        JMenuItem mntmo = new JMenuItem("開く(O)");
        mntmo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                openFile(null);
            }
        });

        JMenuItem mntmn = new JMenuItem("新規作成(N)");
        mntmn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                newFile();
            }
        });
        mntmn.setMnemonic('N');
        mntmn.setMnemonic(KeyEvent.VK_N);
        mntmn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
                InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        mnNewMenu.add(mntmn);
        mntmo.setMnemonic('O');
        mntmo.setMnemonic(KeyEvent.VK_O);
        mntmo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                InputEvent.CTRL_MASK));
        mnNewMenu.add(mntmo);

        JMenuItem mntmx = new JMenuItem("終了(X)");
        mntmx.setMnemonic('X');
        mntmx.setMnemonic(KeyEvent.VK_X);
        mntmx.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                finish();
            }
        });

        JMenuItem mntms = new JMenuItem("保存(S)");
        mntms.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                saveFile(objMan.getFile(), false);
            }
        });
        mntms.setMnemonic('S');
        mntms.setMnemonic(KeyEvent.VK_S);
        mntms.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                InputEvent.CTRL_MASK));
        mnNewMenu.add(mntms);

        JMenuItem mntmNewMenuItem_1 = new JMenuItem("別名で保存");
        mntmNewMenuItem_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveFile(objMan.getFile(), true);
            }
        });
        mntmNewMenuItem_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        mnNewMenu.add(mntmNewMenuItem_1);

        JSeparator separator_1 = new JSeparator();
        mnNewMenu.add(separator_1);

        JMenuItem mntmNewMenuItem = new JMenuItem("部品入力(SVG)(I)");
        mntmNewMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                importSVGFile();
            }
        });
        mntmNewMenuItem.setMnemonic('I');
        mntmNewMenuItem.setMnemonic(KeyEvent.VK_I);
        mntmNewMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
                InputEvent.CTRL_MASK));
        mnNewMenu.add(mntmNewMenuItem);

        JMenu mnNewMenu_1 = new JMenu("部品出力(SVG)(E)");
        mnNewMenu_1.setMnemonic('E');
        mnNewMenu_1.setMnemonic(KeyEvent.VK_E);
        mnNewMenu.add(mnNewMenu_1);

        JMenuItem mntme = new JMenuItem("ファイル(E)");
        mntme.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
                InputEvent.CTRL_MASK));
        mntme.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                exportSVGFile();
            }
        });
        mntme.setMnemonic('E');
        mntme.setMnemonic(KeyEvent.VK_E);
        mnNewMenu_1.add(mntme);

        JMenuItem mntmp_1 = new JMenuItem("クリップボード(P)");
        mntmp_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                InputEvent.CTRL_MASK));
        mntmp_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exportSVGClipboard();
            }
        });
        mntmp_1.setMnemonic('P');
        mntmp_1.setMnemonic(KeyEvent.VK_P);
        mnNewMenu_1.add(mntmp_1);

        JSeparator separator_7 = new JSeparator();
        mnNewMenu.add(separator_7);

        JMenuItem mntmp = new JMenuItem("印刷(P)");
        mntmp.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                print();
            }
        });
        mntmp.setMnemonic('P');
        mntmp.setMnemonic(KeyEvent.VK_P);
        mntmp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN, 0));
        mnNewMenu.add(mntmp);

        JMenuItem mntmBplot = new JMenuItem("部品出力(BPLOT)(B)");
        mntmBplot.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exportBPlot();
            }
        });
        mntmBplot.setMnemonic('B');
        mntmBplot.setMnemonic(KeyEvent.VK_B);
        mntmBplot.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
                InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        mnNewMenu.add(mntmBplot);

        JMenu mnp_1 = new JMenu("画像出力(D)");
        mnp_1.setMnemonic('D');
        mnp_1.setMnemonic(KeyEvent.VK_D);
        mnNewMenu.add(mnp_1);

        JMenuItem mntmi = new JMenuItem("ファイル(E)");
        mnp_1.add(mntmi);
        mntmi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                exportAsImage();
            }
        });
        mntmi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        mntmi.setMnemonic('E');
        mntmi.setMnemonic(KeyEvent.VK_E);

        JMenuItem mntmp_3 = new JMenuItem("クリップボード(P)");
        mntmp_3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                copyAsImage();
            }
        });
        mntmp_3.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK));
        mnp_1.add(mntmp_3);

        JSeparator separator_2 = new JSeparator();
        mnNewMenu.add(separator_2);
        mnNewMenu.add(mntmx);

        JMenu mne = new JMenu("編集(E)");
        mne.setMnemonic('E');
        mne.setMnemonic(KeyEvent.VK_E);
        menuBar.add(mne);

        JSeparator separator_4 = new JSeparator();
        mne.add(separator_4);

        JMenuItem mntmc = new JMenuItem("コピー(C)");
        mntmc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                copy();
            }
        });
        mntmc.setEnabled(false);
        mntmc.setMnemonic('C');
        mntmc.setMnemonic(KeyEvent.VK_C);
        mntmc.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                InputEvent.CTRL_MASK));
        mne.add(mntmc);
        menuMap.put("コピー", mntmc);

        JMenuItem mntmx_1 = new JMenuItem("切り取り(X)");
        mntmx_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cut();
            }
        });
        mntmx_1.setEnabled(false);
        mntmx_1.setMnemonic('X');
        mntmx_1.setMnemonic(KeyEvent.VK_X);
        mntmx_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                InputEvent.CTRL_MASK));
        mne.add(mntmx_1);
        menuMap.put("切り取り", mntmx_1);

        JMenuItem mntmv = new JMenuItem("貼り付け(V)");
        mntmv.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                paste();
            }
        });
        mntmv.setEnabled(false);
        mntmv.setMnemonic('V');
        mntmv.setMnemonic(KeyEvent.VK_V);
        mntmv.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                InputEvent.CTRL_MASK));
        mne.add(mntmv);
        menuMap.put("貼り付け", mntmv);

        JMenuItem mntmd = new JMenuItem("削除(D)");
        mntmd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                delete();
            }
        });
        mntmd.setEnabled(false);
        mntmd.setMnemonic('D');
        mntmd.setMnemonic(KeyEvent.VK_D);
        mntmd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        mne.add(mntmd);
        menuMap.put("削除", mntmd);

        JSeparator separator_5 = new JSeparator();
        mne.add(separator_5);

        JMenuItem mntmg = new JMenuItem("グループ化(G)");
        mntmg.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                makeGroup();
            }
        });

        JMenuItem menuItem_1 = new JMenuItem("プロパティ");
        menuItem_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                showProperty(null);
            }
        });
        menuItem_1.setEnabled(false);
        menuItem_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                InputEvent.CTRL_MASK));
        mne.add(menuItem_1);
        menuMap.put("プロパティ", menuItem_1);

        JSeparator separator_6 = new JSeparator();
        mne.add(separator_6);
        mntmg.setEnabled(false);
        mntmg.setMnemonic('G');
        mntmg.setMnemonic(KeyEvent.VK_G);
        mntmg.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
                InputEvent.CTRL_MASK));
        mne.add(mntmg);
        menuMap.put("グループ化", mntmg);

        JMenuItem mntmr_2 = new JMenuItem("グループ解除(R)");
        mntmr_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                solveGroup();
            }
        });
        mntmr_2.setEnabled(false);
        mntmr_2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R,
                InputEvent.CTRL_MASK));
        mntmr_2.setMnemonic('R');
        mntmr_2.setMnemonic(KeyEvent.VK_R);
        mne.add(mntmr_2);
        menuMap.put("グループ解除", mntmr_2);

        JSeparator separator_3 = new JSeparator();
        mne.add(separator_3);

        JMenuItem mntmu = new JMenuItem("元に戻す(U)");
        mntmu.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        });
        mntmu.setMnemonic('U');
        mntmu.setMnemonic(KeyEvent.VK_U);
        mntmu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                InputEvent.CTRL_MASK));
        mne.add(mntmu);

        JMenuItem mntmNewMenuItem_2 = new JMenuItem("やり直し(R)");
        mntmNewMenuItem_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        });
        mntmNewMenuItem_2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                InputEvent.CTRL_MASK));
        mntmNewMenuItem_2.setMnemonic('R');
        mntmNewMenuItem_2.setMnemonic(KeyEvent.VK_R);
        mne.add(mntmNewMenuItem_2);

        JMenu mnp = new JMenu("図形(P)");
        mnp.setMnemonic('P');
        mnp.setMnemonic(KeyEvent.VK_P);
        menuBar.add(mnp);

        JMenuItem mntml = new JMenuItem("直線(L)");
        mntml.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = toolBarMap.get(SHAPE.LINE);
                btn.setSelected(true);
                btn.requestFocus();
                selectDesignObject(btn, SHAPE.LINE);
            }
        });
        mntml.setMnemonic('L');
        mntml.setMnemonic(KeyEvent.VK_L);
        mntml.setIcon(new ImageIcon(cl.getResource("img/line1.png")));
        mnp.add(mntml);

        JMenuItem mntmr_1 = new JMenuItem("四角(R)");
        mntmr_1.setIcon(new ImageIcon(cl.getResource("img/rectangle1.png")));
        mntmr_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JToggleButton btn = toolBarMap.get(SHAPE.RECTANGLE);
                btn.setSelected(true);
                btn.requestFocus();
                selectDesignObject(btn, SHAPE.RECTANGLE);
            }
        });
        mntmr_1.setMnemonic('R');
        mntmr_1.setMnemonic(KeyEvent.VK_R);
        mnp.add(mntmr_1);

        JMenuItem mntmp_2 = new JMenuItem("折れ線(P)");
        mntmp_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                JToggleButton btn = toolBarMap.get(SHAPE.POLYLINE);
                btn.setSelected(true);
                btn.requestFocus();
                selectDesignObject(btn, SHAPE.POLYLINE);
            }
        });
        mntmp_2.setMnemonic('P');
        mntmp_2.setMnemonic(KeyEvent.VK_P);
        mntmp_2.setIcon(new ImageIcon(cl.getResource("img/polyline1.png")));
        mnp.add(mntmp_2);

        JMenuItem mntmg_1 = new JMenuItem("多角形(Q)");
        mntmg_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = toolBarMap.get(SHAPE.POLYGON);
                btn.setSelected(true);
                btn.requestFocus();
                selectDesignObject(btn, SHAPE.POLYGON);
            }
        });
        mntmg_1.setMnemonic('Q');
        mntmg_1.setMnemonic(KeyEvent.VK_Q);
        mntmg_1.setIcon(new ImageIcon(cl.getResource("img/polygon1.png")));
        mnp.add(mntmg_1);

        JMenuItem mntmu_1 = new JMenuItem("スプライン曲線(U)");
        mntmu_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = toolBarMap.get(SHAPE.SPLINE);
                btn.setSelected(true);
                btn.requestFocus();
                selectDesignObject(btn, SHAPE.CIRCLE);
            }
        });
        mntmu_1.setMnemonic('U');
        mntmu_1.setMnemonic(KeyEvent.VK_U);
        mntmu_1.setIcon(new ImageIcon(cl.getResource("img/spline1.png")));
        mnp.add(mntmu_1);

        JMenuItem mntml_1 = new JMenuItem("スプライン閉曲線(O)");
        mntml_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = toolBarMap.get(SHAPE.SPLINE_LOOP);
                btn.setSelected(true);
                btn.requestFocus();
                selectDesignObject(btn, SHAPE.SPLINE_LOOP);
            }
        });
        mntml_1.setMnemonic('O');
        mntml_1.setMnemonic(KeyEvent.VK_O);
        mntml_1.setIcon(new ImageIcon(cl.getResource("img/spline_loop1.png")));
        mnp.add(mntml_1);

        JMenuItem mntmc_3 = new JMenuItem("円(C)");
        mntmc_3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = toolBarMap.get(SHAPE.CIRCLE);
                btn.setSelected(true);
                btn.requestFocus();
                selectDesignObject(btn, SHAPE.CIRCLE);
            }
        });
        mntmc_3.setMnemonic('C');
        mntmc_3.setMnemonic(KeyEvent.VK_C);
        mntmc_3.setIcon(new ImageIcon(cl.getResource("img/circle1.png")));
        mnp.add(mntmc_3);

        JMenuItem mntme_2 = new JMenuItem("楕円(E)");
        mntme_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = toolBarMap.get(SHAPE.ELLIPSE);
                btn.setSelected(true);
                btn.requestFocus();
                selectDesignObject(btn, SHAPE.ELLIPSE);
            }
        });
        mntme_2.setMnemonic('E');
        mntme_2.setMnemonic(KeyEvent.VK_E);
        mntme_2.setIcon(new ImageIcon(cl.getResource("img/eclipse1.png")));
        mnp.add(mntme_2);

        JMenuItem mntms_1 = new JMenuItem("バネ(S)");
        mntms_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = toolBarMap.get(SHAPE.SPRING);
                btn.setSelected(true);
                btn.requestFocus();
                selectDesignObject(btn, SHAPE.SPRING);
            }
        });
        mntms_1.setMnemonic('S');
        mntms_1.setMnemonic(KeyEvent.VK_S);
        mntms_1.setIcon(new ImageIcon(cl.getResource("img/spring1.png")));
        mnp.add(mntms_1);

        JMenuItem mntmt = new JMenuItem("テキスト/数式(T)");
        mntmt.setMnemonic('T');
        mntmt.setMnemonic(KeyEvent.VK_T);
        mntmt.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = toolBarMap.get(SHAPE.TEXT);
                btn.setSelected(true);
                btn.requestFocus();
                selectDesignObject(btn, SHAPE.TEXT);
            }
        });
        mntmt.setIcon(new ImageIcon(cl.getResource("img/text1.png")));
        mnp.add(mntmt);

        JMenuItem menuItem_2 = new JMenuItem("グラフ(G)");
        menuItem_2.setMnemonic('G');
        menuItem_2.setMnemonic(KeyEvent.VK_G);
        menuItem_2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = toolBarMap.get(SHAPE.GRAPH);
                btn.setSelected(true);
                btn.requestFocus();
                selectDesignObject(btn, SHAPE.GRAPH);
            }
        });
        menuItem_2.setIcon(new ImageIcon(cl.getResource("img/graph1.png")));
        mnp.add(menuItem_2);

        JMenuItem menuItem = new JMenuItem("画像(I)");
        menuItem.setMnemonic('I');
        menuItem.setMnemonic(KeyEvent.VK_I);
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = toolBarMap.get(SHAPE.IMAGE);
                btn.setSelected(true);
                btn.requestFocus();
                selectDesignObject(btn, SHAPE.IMAGE);
            }
        });
        menuItem.setIcon(new ImageIcon(cl.getResource("img/image1.png")));
        mnp.add(menuItem);

        JMenuItem mntma = new JMenuItem("正多角形(A)");
        mntma.setMnemonic('A');
        mntma.setMnemonic(KeyEvent.VK_A);
        mntma.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = toolBarMap.get(SHAPE.REGULAR_POLYGON);
                btn.setSelected(true);
                btn.requestFocus();
                selectDesignObject(btn, SHAPE.REGULAR_POLYGON);
            }
        });
        mntma.setIcon(new ImageIcon(cl.getResource("img/regular_polygon1.png")));
        mnp.add(mntma);

        JMenu mnm = new JMenu("モード(M)");
        menuBar.add(mnm);

        JCheckBoxMenuItem chckbxmntmb = new JCheckBoxMenuItem("点字(B)");
        chckbxmntmb.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                switchViewMode();
            }
        });
        chckbxmntmb.setMnemonic('B');
        chckbxmntmb.setMnemonic(KeyEvent.VK_B);
        chckbxmntmb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B,
                InputEvent.CTRL_MASK));
        mnm.add(chckbxmntmb);

        JMenu mna = new JMenu("表示(V)");
        mna.setMnemonic('V');
        mna.setMnemonic(KeyEvent.VK_V);
        menuBar.add(mna);

        JMenuItem mntme_1 = new JMenuItem("拡大(I)");
        mntme_1.setEnabled(false);
        mntme_1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS,
                InputEvent.CTRL_MASK));
        mntme_1.setMnemonic('I');
        mntme_1.setMnemonic(KeyEvent.VK_I);
        mna.add(mntme_1);

        JMenuItem mntmc_2 = new JMenuItem("縮小(O)");
        mntmc_2.setEnabled(false);
        mntmc_2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
                InputEvent.CTRL_MASK));
        mntmc_2.setMnemonic('O');
        mntmc_2.setMnemonic(KeyEvent.VK_O);
        mna.add(mntmc_2);

        JMenuItem mntmr = new JMenuItem("リセット(R)");
        mntmr.setEnabled(false);
        mntmr.setMnemonic('R');
        mntmr.setMnemonic(KeyEvent.VK_R);
        mna.add(mntmr);

        JMenu mnh = new JMenu("ヘルプ(H)");
        mnh.setMnemonic('H');
        mnh.setMnemonic(KeyEvent.VK_H);
        menuBar.add(mnh);

        JMenuItem mntmc_1 = new JMenuItem("設定(C)");
        mntmc_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setConfig();
            }
        });
        mntmc_1.setMnemonic('C');
        mntmc_1.setMnemonic(KeyEvent.VK_C);
        mnh.add(mntmc_1);

        JMenuItem mntmh = new JMenuItem("ヘルプ(H)");
        mntmh.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showHelp();
            }
        });
        mntmh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,
                InputEvent.CTRL_MASK));
        mntmh.setMnemonic('H');
        mntmh.setMnemonic(KeyEvent.VK_H);
        mnh.add(mntmh);

        JMenuItem mntmNewMenuItem_3 = new JMenuItem("ログ表示");
        mntmNewMenuItem_3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                showLog();
            }
        });
        mnh.add(mntmNewMenuItem_3);

        JMenuItem mntmAbout = new JMenuItem("About");
        mntmAbout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showAbout();
            }
        });
        mntmAbout.setMnemonic('A');
        mntmAbout.setMnemonic(KeyEvent.VK_A);
        mnh.add(mntmAbout);

        ObjectPopupMenu.setMainFrame(this);

        this.toolBarV = gbl_panel;
        this.toolBarH = new GridBagLayout();
        this.toolBarH.rowHeights = this.toolBarV.columnWidths;
        this.toolBarH.rowWeights = this.toolBarV.columnWeights;
        this.toolBarH.columnWidths = this.toolBarV.rowHeights;
        this.toolBarH.columnWeights = this.toolBarV.rowWeights;

        JToggleButton toggleButton_11 = new JToggleButton(new ImageIcon(
                cl.getResource("img/regular_polygon.png")));
        toggleButton_11.setToolTipText("正多角形");
        toggleButton_11.setPreferredSize(new Dimension(36, 36));
        GridBagConstraints gbc_toggleButton_11 = new GridBagConstraints();
        gbc_toggleButton_11.gridx = 1;
        gbc_toggleButton_11.gridy = 6;
        panel.add(toggleButton_11, gbc_toggleButton_11);
        toggleButton_11.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JToggleButton btn = (JToggleButton) e.getSource();
                selectDesignObject(btn, SHAPE.REGULAR_POLYGON);
            }
        });
        toolBarMap.put(SHAPE.REGULAR_POLYGON, toggleButton_11);

        // 図形ツールバーを2列に設定する
        int nButton = panel.getComponentCount();
        for (int i = 0; i < nButton; i++) {
            Component comp = panel.getComponent(i);
            if (comp instanceof JToggleButton) {
                GridBagConstraints c = new GridBagConstraints();
                c.gridx = i / 2;
                c.gridy = i % 2;
                toolBarH.addLayoutComponent(comp, c);
            }
        }

        try {
            this.robot = new Robot();
        } catch (AWTException e1) {
            Util.logException(e1);
        }
    }

    private String initialPath = null;

    private void initialFile() {
        if (null != this.initialPath) {
            this.openFile(this.initialPath);
            this.initialPath = null;
        }
    }

    /**
    * 右クリックメニューの共通処理
    *
    * @param name 選択された名前
    * @param target 対象の図形オブジェクト
    * @param location メニューの位置
    */
    public void menuCalled(String name, DesignObject target, Point location) {
        switch (name) {
        case "プロパティ":
            showProperty(location);
            break;
        case "消しゴム":
            target.initMask();
            this.hint.status = STATUS.ERASE;
            notifyHint(this.hint);
            break;
        case "コピー":
            editObject(ObjectManager.COMMAND.COPY);
            break;
        case "切り取り":
            editObject(ObjectManager.COMMAND.CUT);
            break;
        case "削除":
            editObject(ObjectManager.COMMAND.DELETE);
            break;
        case "一つ上へ移動":
            editObject(ObjectManager.COMMAND.UP);
            break;
        case "一つ下へ移動":
            editObject(ObjectManager.COMMAND.DOWN);
            break;
        case "一番上へ移動":
            editObject(ObjectManager.COMMAND.TOP);
            break;
        case "一番下へ移動":
            editObject(ObjectManager.COMMAND.BOTTOM);
            break;
        default:
            target.menuCalled(name, location, this.hint);
        }
        this.redraw();
    }

    /**
    * 設定ウィンドウを表示する
    */
    private void setConfig() {
        Config.showDialg();
    }

    /**
    * Aboutウィンドウを表示する
    */
    private void showAbout() {
        About.show();
    }

    /**
    * ヘルプを表示する
    */
    private void showHelp() {
        File file = new File(Util.exePath(Config.HELP_FILE));
        String urlText = file.getAbsolutePath().replace("\\", "/");
        Util.showWeb(String.format("file:%s", urlText));
    }

    /**
    * 図形を編集する
    *
    * @param cmd 編集のコマンド
    */
    private void editObject(ObjectManager.COMMAND cmd) {
        switch (cmd) {
        case COPY:
            this.copy();
            break;
        case CUT:
            this.cut();
            break;
        case DELETE:
            this.delete();
            break;
        case UP:
        case DOWN:
        case TOP:
        case BOTTOM:
            if (1 != this.selObjList.size()) {
                return;
            }

            DesignObject obj = this.selObjList.get(0);
            this.objMan.putObject(cmd, obj);

            int index = this.objMan.indexOf(obj);
            ObjectUndoManager.getInstance().changeZorder(index, obj.clone());
        default:
        }
    }

    /**
    * 描画する図形を選ぶ
    *
    * @param btn 図形のボタン
    * @param type 図形の種類
    */
    private void selectDesignObject(JToggleButton btn, SHAPE type) {
        unselect(null);
        if (btn.isSelected()) {
            this.type = type;
            Collection<JToggleButton> btns = (Collection<JToggleButton>) toolBarMap
                    .values();
            Iterator<JToggleButton> it = btns.iterator();
            while (it.hasNext()) {
                JToggleButton tbtn = it.next();
                if (tbtn != btn) {
                    tbtn.setSelected(false);
                }
            }
            this.designPanel.setReshapeCursor();
        } else if (this.type == type) {
            this.type = SHAPE.NONE;
            JToggleButton tbtn = toolBarMap.get(SHAPE.NONE);
            tbtn.setSelected(true);
            tbtn.requestFocus();
        }
    }

    /**
    * ウィンドウタイトルをファイル名に設定する
    */
    void setTitle() {
        try {
            File file = this.objMan.getFile();
            if (null == file) {
                frmSample.setTitle(APP_NAME);
            } else {
                frmSample.setTitle(APP_NAME + ":" + file.getCanonicalPath());
            }
        } catch (Exception e) {
            Util.logException(e);
        }
    }

    /**
    * 新しい紙面を作成する
    */
    private void newFile() {
        saveCheck();
        this.objMan = new ObjectManager();
        this.designPanel.setObjectManager(this.objMan);
        setTitle();
        frmSample.validate();
        redraw();
    }

    /**
    * カーソルを待ちに変える
    */
    void setWaitCursor() {
        this.frmSample
                .setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    }

    /**
    * カーソルを既定に戻す
    */
    public void resetCursor() {
        this.frmSample.setCursor(Cursor.getDefaultCursor());
    }

    /**
    * ファイルを開く
    */
    private void openFile(String path) {
        saveCheck();
        this.objMan = new ObjectManager();
        ObjectFactory.getInstance().set(this.objMan, designPanel);
        ObjectUndoManager.init(this.objMan);
        // ImportSVG handler = new ImportSVG(this.designPanel);
        ImportSVG handler = new ImportSVG();
        this.setWaitCursor();
        this.objMan.loadSVG(handler, path);
        this.resetCursor();
        this.objMan.stored();
        this.designPanel.setObjectManager(this.objMan);
        setTitle();
        frmSample.validate();
        redraw();
    }

    /**
    * ファイルに保存する
    *
    * @param file
    *            ファイル
    * @param newFile
    *            ファイルが指定されていない場合に新しくファイルを作るか
    * @return
    */
    protected boolean saveFile(File file, boolean newFile) {
        ExportSVG export = new ExportSVG();
        Rectangle rect = new Rectangle(this.designPanel.getWidth(),
                this.designPanel.getHeight());
        if (export.init(rect, file, newFile)) {
            this.setWaitCursor();
            this.objMan.export(export);
            ;
            this.resetCursor();
            this.objMan.setFile(export.end());
            this.objMan.stored();
            setTitle();
            return true;
        }
        return false;
    }

    /**
    * 印刷する
    */
    private void print() {
        this.selObjList.clear();
        this.objMan.unselect();

        // PrintaerJobの取得
        PrinterJob job = PrinterJob.getPrinterJob();

        // Printable, Pageableの設定
        job.setPrintable(this.designPanel);
        job.setPageable(this.designPanel);

        // 印刷ダイアログの表示と印刷
        PrintRequestAttributeSet pset = new HashPrintRequestAttributeSet();
        if (job.printDialog(pset)) {
            this.designPanel.setPageFormat(job.getPageFormat(pset));
            try {
                job.print();
            } catch (Exception e) {
                Util.logException(e);
            }
        }
    }

    /**
    * 再描画する
    */
    private void redraw() {
        Graphics2D g = (Graphics2D) this.designPanel.getBufferGraphics();
        this.designPanel.clear(g);
        this.objMan.draw(g, DesignObject.DRAW_MODE.DISPLAY);
        this.designPanel.flush(g);
    }

    /**
    * 選択枠を表示する
    *
    * @param rect
    *            選択枠の矩形
    */
    private void drawSelectFrame(Rectangle rect) {
        Graphics2D g = (Graphics2D) this.designPanel.getBufferGraphics();
        this.designPanel.clear(g);
        this.objMan.draw(g, DesignObject.DRAW_MODE.DISPLAY);

        BasicStroke st0 = (BasicStroke) g.getStroke();
        float[] dash = { 10.0f, 3.0f };
        BasicStroke st = new BasicStroke(st0.getLineWidth(),
                BasicStroke.CAP_BUTT, st0.getLineJoin(), st0.getMiterLimit(),
                dash, 0.0f);
        g.setStroke(st);
        g.setColor(this.frameColor);
        g.drawRect(rect.x, rect.y, rect.width, rect.height);

        this.designPanel.flush(g);
    }

    /**
    * マウスボタンを押した時のイベント処理
    *
    * @param p
    *            マウスの位置
    */
    private void pressed(Point p) {
        this.mouseStatus = MOUSE_STATUS.PRESSED;
        this.designPanel.requestFocusInWindow();
        if (this.type == SHAPE.NONE) {
            this.ctrlObject = this.objMan.hitTest(this.hint, p);
            if (null == this.ctrlObject) {
                unselect(null);

                this.firstPoint = p;
                this.isSelecting = true;
            } else {
                this.ctrlObject.putStatus(this.hint);
                this.designPanel.changeCursor(this.hint);
            }
            if (0 < this.selObjList.size()) {
                for (DesignObject obj : this.selObjList) {
                    int index = this.objMan.indexOf(obj);
                    ObjectUndoManager.getInstance().readyEdit(index, obj.clone());
                }
            }
        }
        this.prevPoint = p;
    }

    /**
    * マウスボタンを離した時のイベント処理
    *
    * @param p
    *            マウスの位置
    */
    private void released(Point p) {
        switch (this.mouseStatus) {
        case PRESSED:
            if (STATUS.EDIT != this.hint.status) {
                ObjectUndoManager.getInstance().cancelEdit();
            }
            break;
        case DRAGGED:
            ObjectUndoManager.getInstance().fixEdit();
            break;
        default:
        }
        this.mouseStatus = MOUSE_STATUS.RELEASED;
        if (this.isSelecting) {
            this.isSelecting = false;
            redraw();
            this.notifyMenu();
        }
    }

    /**
    * 編集メニューの有効/無効を制御する
    */
    private void notifyMenu() {
        menuMap.get("グループ化").setEnabled(1 < this.selObjList.size());
        menuMap.get("グループ解除").setEnabled(
                1 == this.selObjList.size()
                        && this.ctrlObject instanceof ShapeGroup);
        menuMap.get("コピー").setEnabled(0 < this.selObjList.size());
        menuMap.get("切り取り").setEnabled(0 < this.selObjList.size());
        menuMap.get("削除").setEnabled(0 < this.selObjList.size());
        menuMap.get("貼り付け").setEnabled(0 < this.editList.size());
        menuMap.get("プロパティ").setEnabled(0 < this.selObjList.size());
    }

    /**
    * マウスの左ボタンをクリックした時のイベント処理
    *
    * @param panel 紙面パネル
    * @param p マウスの位置
    */
    private void clicked(Point p, boolean toReplace) {
        if (this.type == SHAPE.NONE) // 図形の変更中
        {
            // マウスの位置の図形を取得する
            DesignObject obj = this.objMan.hitTest(this.hint, p);
            if (toReplace && STATUS.EDIT != this.hint.status && (null != obj && false == obj.isStatus(STATUS.ERASE))) {
                unselect(obj);
            }
            if (null == obj) {
                // 図形がない
                this.hint.status = null;
            } else if (obj.isStatus(STATUS.ERASE)) {
                // 消しゴム中
                obj.expandMask(p);
            } else {
                // 図形の変更
                obj.putStatus(this.hint);
                this.designPanel.changeCursor(this.hint);
                boolean flag = true;
                if (DesignObject.STATUS.EDIT == this.hint.status) {
                    if (obj.edit(this.hint, p)) {
                        ObjectUndoManager um = ObjectUndoManager.getInstance();
                        um.change();
                        um.fixEdit();
                        int index = this.objMan.indexOf(obj);
                        um.readyEdit(index, obj.clone());
                        flag = false;
                    }
                }
                if (flag) {
                    if (obj.isSelected()) {
                        obj.select(false);
                        this.selObjList.remove(obj);
                        int index = this.objMan.indexOf(obj);
                        ObjectUndoManager.getInstance().relaxEdit(index);
                    } else {
                        obj.select(true);
                        this.selObjList.add(obj);
                        int index = this.objMan.indexOf(obj);
                        ObjectUndoManager.getInstance().readyEdit(index, obj.clone());
                    }
                }
            }
            this.notifyMenu();
        } else // 図形の追加中
        {
            if (0 < this.selObjList.size()) {
                DesignObject obj = this.selObjList.get(0);
                if (obj.isStatus(DesignObject.STATUS.INITIAL)) {
                    obj.clicked(this.designPanel, p);
                    if (false == obj.isStatus(DesignObject.STATUS.INITIAL)) {
                        this.selObjList.clear();
                        this.hint.status = null;
                    }
                }
            } else {
                ObjectFactory factory = ObjectFactory.getInstance();
                factory.set(this.objMan, this.designPanel);
                DesignObject obj = factory.newObject(this.hint, this.type, p);
                this.selObjList.clear();
                if (null != obj) {
                    obj.putStatus(this.hint);
                    if (obj.isStatus(DesignObject.STATUS.INITIAL)) {
                        this.selObjList.add(obj);
                    }
                }
            }
        }
        redraw();
    }

    /**
    * マウスの左ボタンをダブルクリックした時のイベント処理
    *
    * @param p マウスの位置
    */
    private void doubleClicked(Point p) {
        if (0 < this.selObjList.size()) {
            DesignObject obj = this.selObjList.get(0);
            if (obj.isSelected()) {
                if (obj.isStatus(STATUS.ERASE)) {
                    obj.endMask();
                    if (false == obj.isStatus(STATUS.ERASE)) {
                        this.designPanel.resetCursor();
                        ;
                    }
                } else {
                    obj.doubleClicked(this.designPanel, p);
                    ;
                    if (false == obj.isStatus(DesignObject.STATUS.INITIAL)) {
                        this.selObjList.clear();
                        this.hint.status = null;
                        redraw();
                    }
                }
            }
        }
    }

    /**
    * マウスの右ボタンをクリックした時のイベント処理
    *
    * @param p マウスの位置
    */
    private void rightClicked(Point p) {
        if (0 < this.selObjList.size()) {
            DesignObject obj = this.selObjList.get(0);

            Point origin = this.designPanel.getLocationOnScreen();
            p.x += origin.x;
            p.y += origin.y;
            obj.showMenu(p);
        }
    }

    /**
    * 操作に応じてカーソルを変える
    *
    * @param hint 操作のヒント
    */
    public void notifyHint(DesignObject.StatusHint hint) {
        if (0 < this.selObjList.size()) {
            DesignObject obj = this.selObjList.get(0);
            if (null == obj) {
                this.designPanel.resetCursor();
            } else if (obj.isStatus(STATUS.ERASE)) {
                this.designPanel.setEraseCursor();
            } else {
                obj.putStatus(this.hint);
                this.designPanel.changeCursor(this.hint);
            }
        }
    }

    /**
    * マウスを移動した時のイベント処理
    *
    * @param p マウスの位置
    */
    private void moved(Point p) {
        if (this.type == SHAPE.NONE) {
            DesignObject obj = this.objMan.hitTest(this.hint, p);
            if (null == obj) {
                this.designPanel.resetCursor();
            } else if (obj.isStatus(STATUS.ERASE)) {
                obj.moveMask(p);
                this.redraw();
            } else {
                obj.putStatus(this.hint);
                this.designPanel.changeCursor(this.hint);
                if (false == this.selObjList.contains(obj)) {
                    this.designPanel.setHandCursor();
                }
            }
        } else {
            if (0 < this.selObjList.size()) {
                int count = this.selObjList.size();
                for (int i = 0; i < count; i++) {
                    DesignObject obj = this.selObjList.get(i);
                    if (obj.hitTest(this.hint, p, true)) {
                        obj.putStatus(this.hint);
                        this.designPanel.changeCursor(this.hint);
                    }
                    if (obj.isStatus(DesignObject.STATUS.INITIAL)) {
                        Point dp = new Point(p.x - this.prevPoint.x, p.y - this.prevPoint.y);
                        if (obj.reshape(this.hint, dp)) {
                            obj.putStatus(this.hint);
                            this.designPanel.changeCursor(this.hint);
                        }
                        redraw();
                    }
                }
            }
        }
        this.prevPoint = p;
    }

    /**
    * マウスをドラッグした時のイベント処理
    *
    * @param p マウスの位置
    * @param modifiers 修飾キー
    */
    private void dragged(Point p, int modifiers) {
        this.mouseStatus = MOUSE_STATUS.DRAGGED;
        this.hint.modifiers = modifiers;
        if (this.isSelecting) {
            int left = Math.min(this.firstPoint.x, p.x);
            int right = Math.max(this.firstPoint.x, p.x);
            int top = Math.min(this.firstPoint.y, p.y);
            int bottom = Math.max(this.firstPoint.y, p.y);
            Rectangle rect = new Rectangle(left, top, right - left, bottom - top);

            List<DesignObject> list = this.objMan.hitTest(this.designPanel, rect);
            this.select(list);

            drawSelectFrame(rect);
        } else {
            if (null != this.ctrlObject) {
                Point dp = new Point(p.x - this.prevPoint.x, p.y - this.prevPoint.y);
                if (this.ctrlObject.isStatus(DesignObject.STATUS.MOVE)) {
                    if (0 < this.selObjList.size()) {
                        for (Iterator<DesignObject> it = this.selObjList.iterator(); it.hasNext();) {
                            DesignObject obj = it.next();
                            if (obj.move(this.hint, dp)) {
                                obj.putStatus(this.hint);
                            }
                        }
                        this.prevPoint = p;
                        ObjectUndoManager.getInstance().change();
                    }
                } else {
                    if (this.ctrlObject.reshape(this.hint, dp)) {
                        this.ctrlObject.putStatus(this.hint);
                        ObjectUndoManager.getInstance().change();
                    }
                }
                redraw();
            }
        }
        this.prevPoint = p;
    }

    /**
    * Ctrl+矢印キーを押した時のイベント処理
    *
    * @param key キー
    * @param mod 修飾キー
    */
    private void arrowPressed(int key, int mod) {
        if (this.type == SHAPE.NONE) {
            if (0 < this.selObjList.size()) {
                int step = (this.arrowCount < 4 ? 1 : (this.arrowCount % 2) + 1);
                int defStep = Config.getConfig(Config.MISC.MOVE_STEP);
                if (step > defStep) {
                    step = defStep;
                } else {
                    this.arrowCount++;
                }
                if (0 != (mod & KeyEvent.SHIFT_DOWN_MASK)) {
                    step *= 2;
                }
                Point dp = new Point();
                switch (key) {
                case KeyEvent.VK_LEFT:
                    dp.x -= step;
                    break;
                case KeyEvent.VK_RIGHT:
                    dp.x += step;
                    break;
                case KeyEvent.VK_UP:
                    dp.y -= step;
                    break;
                case KeyEvent.VK_DOWN:
                    dp.y += step;
                    break;
                }
                Point p = this.designPanel.getMousePosition();
                p.x += dp.x;
                p.y += dp.y;
                Point base = this.designPanel.getLocationOnScreen();
                base.x += p.x;
                base.y += p.y;
                this.robot.mouseMove(base.x, base.y);
                switch (this.hint.status) {
                case RESIZE:
                    for (Iterator<DesignObject> it = this.selObjList.iterator(); it.hasNext();) {
                        DesignObject obj = it.next();
                        if (obj.resize(this.hint, dp)) {
                            obj.putStatus(this.hint);
                        }
                    }
                    break;
                case RESHAPE:
                    for (Iterator<DesignObject> it = this.selObjList.iterator(); it.hasNext();) {
                        DesignObject obj = it.next();
                        if (obj.reshape(this.hint, dp)) {
                            obj.putStatus(this.hint);
                        }
                    }
                    break;
                default:
                    for (Iterator<DesignObject> it = this.selObjList.iterator(); it.hasNext();) {
                        DesignObject obj = it.next();
                        if (obj.move(this.hint, dp)) {
                            obj.putStatus(this.hint);
                        }
                    }
                }
                redraw();
            }
        }
    }

    /**
    * 矢印キーを離した時のイベント処理
    */
    private void arrowReleased() {
        if (0 == this.arrowCount) {
            return;
        }
        this.arrowCount = 0;
        ObjectUndoManager.getInstance().fixEdit();
        if (0 < this.selObjList.size()) {
            for (DesignObject obj : this.selObjList) {
                int index = this.objMan.indexOf(obj);
                ObjectUndoManager.getInstance().readyEdit(index, obj.clone());
            }
        }
    }

    /**
    * 全図形を選択する
    */
    private void selectAll() {
        if (this.type == SHAPE.NONE) {
            this.selObjList.clear();
            ObjectUndoManager.getInstance().relaxAll();
            this.select(this.objMan.getList());
        }
    }

    /**
    * 図形を選択状態にする
    *
    * @param objList 図形のリスト
    */
    private void select(Collection<DesignObject> objList) {
        if (SHAPE.NONE == this.type) {
            for (DesignObject obj : objList) {
                if (false == this.selObjList.contains(obj)) {
                    obj.select(true);
                    this.selObjList.add(obj);
                    int index = this.objMan.indexOf(obj);
                    ObjectUndoManager.getInstance().readyEdit(index, obj.clone());
                }
            }
            redraw();
            this.notifyMenu();
        }
    }

    /**
    * 図形の選択を解除する
    *
    * @param obj 解除しない図形
    */
    void unselect(DesignObject obj) {
        if (0 < this.selObjList.size()) {
            int count = this.selObjList.size();
            for (int i = 0; i < count; i++) {
                DesignObject tmp = this.selObjList.get(i);
                if (tmp != obj) {
                    tmp.showMenu(null);
                    tmp.select(false);
                    int index = this.objMan.indexOf(tmp);
                    ObjectUndoManager.getInstance().relaxEdit(index);
                }
            }
            this.selObjList.clear();
            this.notifyMenu();
        }
    }

    /**
    * 全図形の選択を解除する
    */
    void unselectAll() {
        for (DesignObject obj : this.selObjList) {
            obj.select(false);
        }
        ObjectUndoManager.getInstance().relaxAll();
        this.selObjList.clear();
        this.notifyMenu();
    }

    /**
    * グループ化する
    */
    private void makeGroup() {
        DesignObject group = this.objMan.makeGroup(this.hint, this.selObjList);

        int index = this.objMan.indexOf(group);
        ObjectUndoManager.EditInfo info = new ObjectUndoManager.EditInfo(index, group.clone());
        ObjectUndoManager.getInstance().group(info);
        ;

        unselect(null);
        this.ctrlObject = null;
        this.notifyMenu();
        redraw();
    }

    /**
    * グループを解除する
    */
    private void solveGroup() {
        List<DesignObject> parts = this.objMan.solveGroup((ShapeGroup) this.ctrlObject);

        ObjectUndoManager.EditInfo info = new ObjectUndoManager.EditInfo();
        for (DesignObject part : parts) {
            int index = this.objMan.indexOf(part.clone());
            info.add(index, part);
        }
        ObjectUndoManager.getInstance().ungroup(info);
        ;

        this.selObjList.clear();
        this.ctrlObject = null;
        this.notifyMenu();
        redraw();
    }

    /**
    * 編集対象のリスト
    */
    private java.util.List<DesignObject> editList = Util.newArrayList();

    /**
    * 元に戻す
    */
    private void undo() {
        ObjectUndoManager.getInstance().undo();
        this.designPanel.repaint();
    }

    /**
    * やり直し
    */
    private void redo() {
        ObjectUndoManager.getInstance().redo();
        ;
        this.designPanel.repaint();
    }

    /**
    * コピー
    */
    private void copy() {
        if (SHAPE.NONE == this.type) {
            this.editList.clear();
            for (DesignObject obj : this.selObjList) {
                this.editList.add(obj);
            }
            this.notifyMenu();
        }
    }

    /**
    * 切り取り
    */
    private void cut() {
        if (SHAPE.NONE == this.type) {
            ObjectUndoManager.getInstance().delete();

            this.editList.clear();
            for (DesignObject obj : this.selObjList) {
                this.editList.add(obj);
                this.objMan.delObject(obj);
            }

            this.unselectAll();

            this.notifyMenu();
            this.redraw();
        }
    }

    /**
    * 貼り付け
    *
    * @param p 貼り付ける位置
    */
    private void paste() {
        if (SHAPE.NONE == this.type) {
            Point p = this.designPanel.getMousePosition();
            Point dp;
            if (null == p) {
                dp = new Point(10, 10);
            } else {
                Rectangle bound = new Rectangle(this.editList.get(0));
                for (DesignObject obj : this.editList) {
                    bound.add(obj);
                }
                dp = new Point(p.x - bound.x, p.y - bound.y);
            }

            this.unselectAll();

            java.util.List<DesignObject> copied = Util.newArrayList();
            ObjectUndoManager.EditInfo info = new ObjectUndoManager.EditInfo();
            for (DesignObject obj : this.editList) {
                DesignObject copy = obj.clone();
                copy.move(null, dp);
                copied.add(copy);
                int index = this.objMan.addObject(copy);
                info.add(index, copy);
            }
            ObjectUndoManager.getInstance().add(info);
            this.select(copied);
        }
    }

    /**
    * 削除
    */
    private void delete() {
        if (SHAPE.NONE == this.type) {
            for (DesignObject obj : this.selObjList) {
                this.objMan.delObject(obj);
            }
            this.selObjList.clear();
            ObjectUndoManager.getInstance().delete();
            this.ctrlObject = null;
            this.redraw();
        }
    }

    /**
    * SVGファイルを読み込む
    */
    private void importSVGFile() {
        this.setWaitCursor();
        ObjectFactory factory = ObjectFactory.getInstance();
        factory.set(this.objMan, this.designPanel);
        ImportSVG handler = new ImportSVG();
        this.objMan.importSVG(handler);
        this.resetCursor();
        redraw();
    }

    /**
    * SVGでファイルに出力する
    */
    private void exportSVGFile() {
        this.setWaitCursor();
        ExportSVG export = new ExportSVG();
        if (0 < this.selObjList.size()) {
            Rectangle rect = this.objMan.getArea(this.selObjList);
            if (export.init(rect, null, true)) {
                this.objMan.export(export, this.selObjList);
                export.end();
            }
        } else {
            Rectangle rect = this.objMan.getArea();
            if (export.init(rect, null, true)) {
                this.objMan.export(export);
                export.end();
            }
        }
        this.resetCursor();
    }

    /**
    * SVGでクリップボードに出力する
    */
    private void exportSVGClipboard() {
        ExportSVG export = new ExportSVG();
        if (0 < this.selObjList.size()) {
            Rectangle rect = this.objMan.getArea(this.selObjList);
            if (export.initClipboard(rect)) {
                this.objMan.export(export, this.selObjList);
                export.clipboardEnd();
            }
        } else {
            Rectangle rect = this.objMan.getArea();
            if (export.initClipboard(rect)) {
                this.objMan.export(export);
                export.clipboardEnd();
            }
        }
    }

    /**
    * BPLOTコマンドで出力する
    */
    private void exportBPlot() {
        ExportBPLOT export = new ExportBPLOT();
        if (export.init(new Rectangle(this.designPanel.getWidth(),
                this.designPanel.getHeight()), null, true)) {
            if (0 < this.selObjList.size()) {
                this.objMan.export(export, this.selObjList);
                export.end();
            } else {
                this.objMan.export(export);
                export.end();
            }
        }
    }

    /**
    * 画像ファイルに保存する
    */
    private void exportAsImage() {
        if (0 < this.selObjList.size()) {
            this.objMan.exportAsImage(this.selObjList, 0, 0);
        } else {
            this.objMan.exportAsImage(null, this.designPanel.getWidth(), this.designPanel.getHeight());
        }
    }

    /**
    * 画像としてクリップボードにコピーする
    */
    private void copyAsImage() {
        if (0 < this.selObjList.size()) {
            this.objMan.copyAsImage(this.selObjList, 0, 0);
        } else {
            this.objMan.copyAsImage(null, this.designPanel.getWidth(), this.designPanel.getHeight());
        }
    }

    /**
    * 墨字モード、点字モードを切り替える
    */
    private void switchViewMode() {
        ViewMode.MODE mode = (ViewMode.getMode() == ViewMode.MODE.SUMIJI ? ViewMode.MODE.BRAILLE
                : ViewMode.MODE.SUMIJI);
        ViewMode.setMode(mode);
        this.redraw();
    }

    /**
    * 図形のプロパティを表示する
    *
    * @param p マウスの位置
    */
    private void showProperty(Point p) {
        if (0 < this.selObjList.size()) {
            DesignObject obj = this.selObjList.get(0);
            int index = this.objMan.indexOf(obj);
            ObjectUndoManager um = ObjectUndoManager.getInstance();
            um.readyEdit(index, obj.clone());
            if (obj.showProperty(this.designPanel, p)) {
                if (obj.isChanged()) {
                    um.change();
                    um.fixEdit();
                    um.readyEdit(index, obj.clone());
                } else {
                    um.cancelEdit();
                    ;
                }
            } else {
                this.selObjList.remove(obj);
                um.delete();
                ;
            }
            this.designPanel.repaint();
        }
    }

    /**
    * ログファイルを表示する
    */
    private void showLog() {
        String log = Util.exePath(Util.LOG_FILE);
        Util.exec("explorer " + log, false);
    }
}
