package dssp.hashidate.shape.property;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import dssp.brailleLib.BrailleBox;
import dssp.brailleLib.BrailleEditListener;
import dssp.brailleLib.BrailleEditPanel;
import dssp.brailleLib.BrailleInfo;
import dssp.brailleLib.BrailleRenderer;
import dssp.brailleLib.BrailleTranslater;
import dssp.brailleLib.Util;
import dssp.hashidate.config.Config;
import dssp.hashidate.misc.UndoFactory;
import dssp.hashidate.shape.SHAPE;
import dssp.hashidate.shape.helper.BrailleToolkit;
import dssp.hashidate.shape.helper.FormulaHandler;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class ShapeTextInput extends JDialog implements DocumentListener {
    SHAPE mode = SHAPE.TEXT;
    HashMap<SHAPE, JToggleButton> btnMap = new HashMap<SHAPE, JToggleButton>();
    boolean isOK = false;

    JEditorPane textArea = null;
    JPanel imageArea = null;
    Image panelImage = null;
    BufferedImage image = null;
    JEditorPane mathmlPane = null;

    JComboBox<String> fontNameBox = null;
    JSpinner fontSizeSpinner = null;
    JCheckBox boldBox = null;
    private JCheckBox italicBox;

    private String mathML = null;

    static ShapeTextInput instance = null;
    private JTextArea sumijiArea;
    private JTabbedPane tabbedPane;
    private BrailleEditPanel braillePane;

    private List<BrailleInfo> brailleList = Util.newArrayList();
    private DocumentListener docListener;
    private JCheckBox textBraille;

    public static ShapeTextInput getInstance() {
        if (null == instance) {
            instance = new ShapeTextInput();
        }

        return instance;
    }

    private ShapeTextInput() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent arg0) {
                close(false);
            }

            @Override
            public void windowActivated(WindowEvent e) {
                setChange(true);
                startWatch();
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                stopWatch();
            }
        });
        setSize(new Dimension(626, 523));
        setPreferredSize(new Dimension(300, 300));
        setTitle("テキスト/数式");

        JPanel panel = new JPanel();
        getContentPane().add(panel, BorderLayout.SOUTH);

        JButton btnOk = new JButton("OK");
        btnOk.setPreferredSize(new Dimension(100, 21));
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close(true);
            }
        });
        panel.add(btnOk);

        JButton button = new JButton("キャンセル");
        button.setPreferredSize(new Dimension(100, 21));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close(false);
            }
        });
        panel.add(button);

        JPanel panel_1 = new JPanel();
        getContentPane().add(panel_1, BorderLayout.NORTH);
        panel_1.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));

        JLabel label = new JLabel("フォント");
        panel_1.add(label);

        fontNameBox = new JComboBox<String>();
        panel_1.add(fontNameBox);
        fontNameBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent arg0) {
                setChange(true);
            }
        });

        fontSizeSpinner = new JSpinner();
        fontSizeSpinner.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                setChange(true);
            }
        });
        panel_1.add(fontSizeSpinner);
        fontSizeSpinner.setModel(new SpinnerNumberModel(12, 4, 99, 1));

        boldBox = new JCheckBox("太字");
        panel_1.add(boldBox);

        italicBox = new JCheckBox("イタリック");
        italicBox.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                setChange(true);
            }
        });
        panel_1.add(italicBox);
        boldBox.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent arg0) {
                setChange(true);
            }

        });
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        for (int i = 0; i < fontNames.length; i++) {
            fontNameBox.addItem(fontNames[i]);
        }
        Font font = Config.getConfig(Config.FONT.FORMULA);
        fontNameBox.setSelectedItem(font.getFamily());
        setModalityType(ModalityType.APPLICATION_MODAL);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent arg0) {
                switch (tabbedPane.getSelectedIndex()) {
                case 0:
                    setMode(SHAPE.TEXT);
                    break;
                case 1:
                    setMode(SHAPE.FORMULA);
                    break;
                }
            }
        });
        getContentPane().add(tabbedPane, BorderLayout.CENTER);

        braillePane = new BrailleEditPanel();

        textSplitPane = new JSplitPane();
        tabbedPane.addTab("テキスト", null, textSplitPane, null);
        textSplitPane.setResizeWeight(0.5);
        textSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

        textSumijiPane = new JPanel();
        textSplitPane.setLeftComponent(textSumijiPane);
        textSumijiPane.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane_2 = new JScrollPane();
        textSumijiPane.add(scrollPane_2);

        sumijiArea = new JTextArea();
        scrollPane_2.setViewportView(sumijiArea);

        JLabel label_1 = new JLabel("墨字");
        textSumijiPane.add(label_1, BorderLayout.NORTH);

        textBraillePane = new JPanel();
        textSplitPane.setRightComponent(textBraillePane);
        textBraillePane.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane_3 = new JScrollPane();
        textBraillePane.add(scrollPane_3);
        scrollPane_3.setViewportView(braillePane);

        textBraille = new JCheckBox("点字");
        textBraille.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent arg0) {
                setTextBrailleState(textBraille.isSelected());
            }
        });
        textBraillePane.add(textBraille, BorderLayout.NORTH);
        this.sumijiArea.getDocument().addDocumentListener(this.docListener);

        JSplitPane splitPane_1 = new JSplitPane();
        splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);

        imageArea = new JPanel() {
            protected void paintComponent(Graphics g) {
                showImageArea(g);
            }
        };
        imageArea.setPreferredSize(new Dimension(10, 100));
        imageArea.setBackground(Color.WHITE);
        splitPane_1.setLeftComponent(imageArea);

        JSplitPane splitPane = new JSplitPane();
        tabbedPane.addTab("数式", null, splitPane, null);

        JScrollPane scrollPane = new JScrollPane();
        splitPane.setLeftComponent(scrollPane);

        splitPane.setRightComponent(splitPane_1);

        JScrollPane scrollPane_1 = new JScrollPane();
        splitPane_1.setRightComponent(scrollPane_1);

        mathmlPane = new JEditorPane();
        mathmlPane.setEditable(false);
        scrollPane_1.setViewportView(mathmlPane);

        textArea = new JEditorPane();
        textArea.setPreferredSize(new Dimension(300, 19));
        scrollPane.setViewportView(textArea);

        this.textArea.getDocument().addDocumentListener(this);

        UndoFactory.add(this.textArea);

        this.docListener = new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                if (textBraille.isSelected()) {
                    insertBraille(e);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (textBraille.isSelected()) {
                    removeBraille(e);
                }
            }

        };

        this.braillePane.addBrailleEditListner(new BrailleEditListener() {

            @Override
            public void addingBox(List<BrailleBox> boxList) {
                setSumiji(boxList);
            }

            @Override
            public void addedBox(List<BrailleBox> boxList) {
                fixSumiji(boxList);
            }

            @Override
            public void deletedBox(List<BrailleBox> boxList) {
                fixSumiji(boxList);
            }

        });
    }

    private boolean isChanged = false;
    private ScheduledExecutorService timer = null;

    private synchronized void setChange(boolean flag) {
        isChanged = flag;
    }

    private void stopWatch() {
        try {
            timer.shutdown();
            timer.awaitTermination(5000, TimeUnit.MILLISECONDS);
            timer = null;
        } catch (InterruptedException e) {
            // 何もしない
        }
    }

    private void startWatch() {
        if (Objects.isNull(timer)) {
            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleWithFixedDelay(new Runnable() {

                @Override
                public void run() {
                    checkText();
                }

            }, 0, 500, TimeUnit.MILLISECONDS);
        }
    }

    private void checkText() {
        while (Objects.nonNull(timer) && !timer.isShutdown()) {
            if (isChanged) {
                switch (mode) {
                case TEXT:
                    break;
                case FORMULA:
                    updateFormula();
                    break;
                default:
                }
                setChange(false);
            }
        }

    }

    public void clear() {
        this.brailleList = Util.newArrayList();
        this.sumijiArea.setText("");
        this.braillePane.clear();
        this.textArea.setText("");
        this.mathmlPane.setText("");
        this.image = null;
        this.mathML = null;
    }

    void showImageArea() {
        Graphics g = this.imageArea.getGraphics();
        this.showImageArea(g);
    }

    void showImageArea(Graphics g) {
        if (null == this.image) {
            g.setColor(this.imageArea.getBackground());
            g.fillRect(0, 0, this.imageArea.getWidth(), this.imageArea.getHeight());
            return;
        }

        int x = (this.imageArea.getWidth() - image.getWidth()) / 2;
        int y = (this.imageArea.getHeight() - image.getHeight()) / 2;
        if (x < 0 || y < 0) {
            Dimension size = this.imageArea.getPreferredSize();
            if (x < 0) {
                x = 0;
                size.width = image.getWidth();
            }
            if (y < 0) {
                y = 0;
                size.height = image.getHeight();
            }
            this.imageArea.setPreferredSize(size);
        }

        panelImage = this.imageArea.createImage(this.imageArea.getWidth(), this.imageArea.getHeight());
        Graphics tg = panelImage.getGraphics();

        tg.setColor(this.imageArea.getBackground());
        tg.fillRect(0, 0, this.imageArea.getWidth(), this.imageArea.getHeight());

        tg.drawImage(image, x, y, null);

        g.drawImage(panelImage, 0, 0, null);

        //        this.textArea.requestFocusInWindow();
    }

    void close(boolean isOK) {
        this.isOK = isOK;
        setVisible(false);
    }

    public void setRenderer(BrailleRenderer renderer) {
        this.braillePane.setRenderer(renderer);
    }

    public void setMode(SHAPE mode) {
        this.mode = mode;

        Font font;
        switch (this.mode) {
        case TEXT:
            font = Config.getConfig(Config.FONT.TEXT);
            if (null != font) {
                this.fontSizeSpinner.setValue(font.getSize());
                this.italicBox.setSelected(0 != (font.getStyle() & Font.ITALIC));
                this.boldBox.setSelected(0 != (font.getStyle() & Font.BOLD));
            }
            this.tabbedPane.setSelectedIndex(0);
            break;
        case FORMULA:
            font = Config.getConfig(Config.FONT.FORMULA);
            if (null != font) {
                this.fontSizeSpinner.setValue(font.getSize());
                this.italicBox.setSelected(0 != (font.getStyle() & Font.ITALIC));
                this.boldBox.setSelected(0 != (font.getStyle() & Font.BOLD));
            }
            this.tabbedPane.setSelectedIndex(1);
            drawFormula(true);
            break;
        default:
            font = null;
        }
    }

    public SHAPE getMode() {
        return this.mode;
    }

    public boolean isOK() {
        return this.isOK;
    }

    public void setText(String text) {
        this.sumijiArea.getDocument().removeDocumentListener(this.docListener);
        this.sumijiArea.setText(text);
        this.sumijiArea.getDocument().addDocumentListener(this.docListener);
    }

    public String getText() {
        return this.sumijiArea.getText();
    }

    public void setTextBrailleInfo(List<BrailleInfo> brailleList) {
        this.brailleList = (null == brailleList ? Util.newArrayList() : Util.newArrayList(brailleList));

        BrailleTranslater translater = BrailleToolkit.getTextTranslater();
        String text = translater.getTextFromSumiji(this.brailleList);

        this.sumijiArea.getDocument().removeDocumentListener(this.docListener);
        this.sumijiArea.setText(text);
        this.sumijiArea.getDocument().addDocumentListener(this.docListener);
        this.braillePane.setBrailleList(this.brailleList);
    }

    public List<BrailleInfo> getTextBrailleInfo() {
        return this.brailleList;
    }

    public void setFormula(String text) {
        int pos = this.textArea.getCaretPosition();

        this.textArea.getDocument().removeDocumentListener(this);
        this.textArea.setText(text);
        this.textArea.getDocument().addDocumentListener(this);

        this.textArea.setCaretPosition(pos);
    }

    public String getFormula() {
        return this.textArea.getText();
    }

    public void setFont(Font font) {
        this.fontSizeSpinner.setValue(font.getSize());

        int style = font.getStyle();
        this.italicBox.setSelected(0 != (style & Font.ITALIC));
        this.boldBox.setSelected(0 != (style & Font.BOLD));

        int count = this.fontNameBox.getItemCount();
        String familyName = font.getFamily();
        for (int i = 0; i < count; i++) {
            String val = this.fontNameBox.getItemAt(i);
            if (0 == val.compareToIgnoreCase(familyName)) {
                this.fontNameBox.setSelectedIndex(i);
                break;
            }
        }
    }

    public String getFontFamily() {
        return (String) this.fontNameBox.getSelectedItem();
    }

    public int getFontSize() {
        return (int) this.fontSizeSpinner.getValue();
    }

    public int getFontStyle() {
        int style = (this.italicBox.isSelected() ? Font.ITALIC : Font.PLAIN);
        if (this.boldBox.isSelected()) {
            style |= Font.BOLD;
        }

        return style;
    }

    public void setTextBraille(boolean flag) {
        this.textBraille.setSelected(flag);
        this.setTextBrailleState(flag);
    }

    public boolean getTextBraille() {
        return this.textBraille.isSelected();
    }

    private JSplitPane textSplitPane;
    private JPanel textBraillePane;
    private JPanel textSumijiPane;
    private JPanel textPane = new JPanel(new BorderLayout());

    private void setTextBrailleState(boolean state) {
        if (state) {
            this.textPane.remove(this.textBraille);
            this.textSplitPane.setLeftComponent(this.textSumijiPane);
            this.textBraillePane.add(this.textBraille, BorderLayout.NORTH);
            this.tabbedPane.setComponentAt(0, this.textSplitPane);
            this.tabbedPane.repaint();

            BrailleTranslater translater = BrailleToolkit.getTextTranslater();
            this.brailleList = Util.newArrayList();
            translater.braileFromSumiji(this.sumijiArea.getText(), this.brailleList, false, false);
            this.braillePane.setBrailleList(this.brailleList);
        } else {
            this.textPane.add(this.textSumijiPane, BorderLayout.CENTER);
            this.textPane.add(this.textBraille, BorderLayout.SOUTH);
            this.tabbedPane.setComponentAt(0, this.textPane);
        }
    }

    void updateFormula() {
        if (false == this.isVisible()) {
            return;
        }
        if (SHAPE.FORMULA != this.mode) {
            return;
        }
        String text = this.getFormula();
        if (0 == text.length()) {
            this.panelImage = null;
            this.showImageArea();
            return;
        }
        FormulaHandler handler = FormulaHandler.getInstance();
        String tmp = handler.TeXtoMathML(text, true, this.getFontFamily(), this.getFontSize(), this.getFontStyle());
        if (null != tmp) {
            this.mathML = tmp;
            drawFormula(false);
            return;
        }

        //        try {
        //            Document doc = XmlUtil.parse(text);
        //
        //            Element math = doc.getDocumentElement();
        //            math.setAttribute("xmlns", "http://www.w3.org/1998/Math/MathML");
        //            math.setAttribute("mathsize", String.format("%dpt", this.getFontSize()));
        //            math.setAttribute("fontFamily", this.getFontFamily());
        //            math.setAttribute("mathvariant", handler.getFontStyleText(this.getFontStyle()));
        //
        //            this.mathML = XmlUtil.getXmlText(doc);
        //            this.setFormula(this.mathML);
        //            this.drawFormula(false);
        //        } catch (Exception ex) {
        //            Util.logException(ex);
        //        }
    }

    void drawFormula(boolean needConvert) {
        if (false == this.isVisible()) {
            return;
        }

        if (SHAPE.FORMULA != this.mode) {
            return;
        }
        String text = this.getFormula();
        if (0 == text.length()) {
            this.panelImage = null;
            this.showImageArea();
            return;
        }
        FormulaHandler handler = FormulaHandler.getInstance();
        if (needConvert) {
            this.mathML = handler.TeXtoMathML(text, true, this.getFontFamily(), this.getFontSize(),
                    this.getFontStyle());
            if (null == this.mathML) {
                this.mathML = text;
            }
        }
        this.mathmlPane.setText(this.mathML);
        this.mathmlPane.setCaretPosition(0);
        this.image = handler.getImageForMathML(this.mathML, this.getFontFamily());
        if (null == image) {
            return;
        }

        this.showImageArea();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        if (this.isVisible()) {
            setChange(true);
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        if (this.isVisible()) {
            setChange(true);
        }
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        if (this.isVisible()) {
            setChange(true);
        }
    }

    private void removeBraille(DocumentEvent ev) {
        int start = ev.getOffset();
        int len = ev.getLength();
        //        Util.logInfo("r %d %d %d", start, len, this.brailleList.size());
        if (0 == this.brailleList.size()) {
            return;
        }

        int index = 0;
        int offset = 0;
        boolean flag = true;
        int end = 0;
        int fdel = 0;
        int edel = 0;
        for (int i = 0; i < this.brailleList.size(); i++) {
            BrailleInfo info = this.brailleList.get(i);
            if (BrailleInfo.TYPE.VISIBLE == info.getType()) {
                index += info.getSumiji().length();
                if (flag && index > start) {
                    offset = i;
                    fdel = info.getSumiji().length() - (index - start);
                    flag = false;
                }
                if (index == (start + len)) {
                    end = i;
                    edel = info.getSumiji().length() - (index - start);
                    break;
                } else if (index > (start + len)) {
                    end = i;
                    edel = index - (start + len);
                    break;
                }
            }
        }
        BrailleInfo e = this.brailleList.get(end);
        String et = e.getSumiji();
        if (0 < edel) {
            String text = et.substring(0, et.length() - edel);
            BrailleInfo r = e.getDict().getBrailleInfo(text);
            this.brailleList.set(end, r);
        } else {
            this.brailleList.remove(end);
        }
        for (int i = (end - 1); i > offset; i--) {
            this.brailleList.remove(i);
        }
        if (end > offset) {
            BrailleInfo f = this.brailleList.get(offset);
            String ft = f.getSumiji();
            if (0 < fdel) {
                String text = ft.substring(0, ft.length() - fdel);
                BrailleInfo r = f.getDict().getBrailleInfo(text);
                this.brailleList.set(offset, r);
            } else {
                this.brailleList.remove(offset);
            }
        }
        this.braillePane.setBrailleList(this.brailleList);
    }

    private void insertBraille(DocumentEvent e) {

        int start = e.getOffset();
        int len = e.getLength();
        String text = null;
        try {
            text = e.getDocument().getText(start, len);
        } catch (BadLocationException e1) {
            Util.logException(e1, "start,len = %d,%d", start, len);
            ;
        }
        //        Util.logInfo("a %d %d %s %d", start, len, text, this.brailleList.size());

        int index = 0;
        int offset = 0;
        for (int i = 0; i < this.brailleList.size(); i++) {
            BrailleInfo info = this.brailleList.get(i);
            if (BrailleInfo.TYPE.VISIBLE == info.getType()) {
                index += info.getSumiji().length();
                if (index == start) {
                    offset = i + 1;
                } else if (index > start) {
                    offset = i;
                    break;
                }
            }
        }
        if (text.equals("\n")) {
            this.brailleList.add(offset, BrailleInfo.LINEBREAK);
        } else {
            List<BrailleInfo> list = Util.newArrayList();
            BrailleTranslater translater = BrailleToolkit.getTextTranslater();
            translater.braileFromSumiji(text, list, false, false);
            if (list.get(0).isPostChar()) {
                // 後置文字なので、前の文字と結合して点字に変換する
                try {
                    text = e.getDocument().getText(start - 1, len + 1);
                } catch (BadLocationException e1) {
                    Util.logException(e1, "start,len = %d,%d", start, len);
                    ;
                }
                list = Util.newArrayList();
                translater.braileFromSumiji(text, list, false, false);

                offset--;
                // 前の文字を削除
                this.brailleList.remove(offset);
            }

            this.brailleList.addAll(offset, list);
        }
        this.braillePane.setBrailleList(this.brailleList);
    }

    private void setSumiji(List<BrailleBox> boxList) {
        BrailleTranslater translater = BrailleToolkit.getTextTranslater();

        List<BrailleInfo> list = translater.sumijiFromBraille(boxList);
        String text = translater.getTextFromSumiji(list);

        this.sumijiArea.getDocument().removeDocumentListener(this.docListener);
        this.sumijiArea.setText(text);
        this.sumijiArea.getDocument().addDocumentListener(this.docListener);
    }

    private void fixSumiji(List<BrailleBox> boxList) {
        BrailleTranslater translater = BrailleToolkit.getTextTranslater();

        this.brailleList = translater.sumijiFromBraille(boxList);
        String text = translater.getTextFromSumiji(this.brailleList);

        this.sumijiArea.getDocument().removeDocumentListener(this.docListener);
        this.sumijiArea.setText(text);
        this.sumijiArea.getDocument().addDocumentListener(this.docListener);
        this.braillePane.setBrailleList(this.brailleList);
        //        this.setBraille();
    }
}
