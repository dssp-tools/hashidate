package dssp.hashidate.shape;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import dssp.brailleLib.BrailleInfo;
import dssp.brailleLib.BrailleRenderer;
import dssp.brailleLib.Util;
import dssp.hashidate.DesignPanel;
import dssp.hashidate.ViewMode;
import dssp.hashidate.config.Config;
import dssp.hashidate.io.ExportBase;
import dssp.hashidate.io.ShapeInfo;
import dssp.hashidate.misc.ObjectPopupMenu;
import dssp.hashidate.misc.PairList;
import dssp.hashidate.shape.helper.BrailleToolkit;
import dssp.hashidate.shape.helper.FormulaHandler;
import dssp.hashidate.shape.property.ShapeTextInput;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public class ShapeText extends DesignObject {
    private SHAPE shape = SHAPE.TEXT;
    private Rectangle2D.Double base = new Rectangle2D.Double();
    private Rectangle2D.Double frame = new Rectangle2D.Double();
    private Point2D.Double scale = new Point2D.Double(1, 1);
    private FontMetrics fontMetrix = null;
    private BufferedImage image = null;

    private boolean braille = true;

    private String text = null;
    private String mathML = null;

    private Rectangle brailleBound = null;
    private List<BrailleInfo> brailleList = null;

    public ShapeText() {
        this.dotSize = Config.getConfig(Config.BRAILLE.BRAILLE);

        this.braille = Config.getConfig(Config.BRAILLE.BRAILLE_TEXT);
        this.initPopupMenu();
    }

    public ShapeText(Point p) {
        this();
        this.base.x = p.x;
        this.base.y = p.y;

        //        int size = Config.getConfig(Config.BRAILLE.BRAILLE);
        //        this.dotSize = Util.mmToPixel(0, size);
    }

    private static ObjectPopupMenu popupMenu;

    private void initPopupMenu() {
        if (null != popupMenu) {
            return;
        }
        popupMenu = new ObjectPopupMenu();
        this.initCommonMenu(popupMenu);
    }

    @Override
    public void showMenu(Point location) {
        if (null == location || popupMenu.isVisible()) {
            popupMenu.setVisible(false);
            return;
        }
        popupMenu.show(this, location);
        ;
    }

    /**
    * 点字変換を行うか
    *
    * @return true=行う(既定値)
    */
    public boolean isBraille() {
        return braille;
    }

    /**
    * 点字変換を行うかを設定する
    *
    * @param braille true=行う(既定値)
    */
    public void setBraille(boolean braille) {
        this.braille = braille;
    }

    @Override
    public Rectangle getBounds() {
        switch (ViewMode.getMode()) {
        case SUMIJI:
            return this;
        case BRAILLE:
            return this.brailleBound;
        }

        return null;
    }

    @Override
    public ShapeText clone() {
        ShapeText obj = (ShapeText) super.clone();

        obj.braille = this.braille;

        obj.shape = this.shape;
        obj.text = this.text;
        obj.mathML = this.mathML;
        obj.base = (Rectangle2D.Double) this.base.clone();
        obj.frame = (Rectangle2D.Double) this.frame.clone();
        obj.scale = (Point2D.Double) this.scale.clone();
        obj.fontMetrix = this.fontMetrix;

        obj.image = null;
        obj.brailleBound = null;
        obj.brailleList = null;

        obj.initText();
        obj.adaptFrame();

        return obj;
    }

    public Font getFont() {
        return this.fontMetrix.getFont();
    }

    public Point2D.Double getScale() {
        return this.scale;
    }

    public String getText() {
        return this.text;
    }

    public String getMathML() {
        return mathML;
    }

    public List<BrailleInfo> getBrailleList() {
        return this.brailleList;
    }

    static final int DOT_WIDTH = 4;
    static final int DOT_HEIGHT = 4;
    static final int BOX_WIDTH = DOT_WIDTH * 4;
    static final int BOX_HEIGHT = DOT_HEIGHT * 6;

    @Override
    public boolean init(DesignPanel panel) {
        ShapeTextInput dlg = ShapeTextInput.getInstance();
        dlg.clear();
        Util.setLocationUnderMouse(dlg);

        dlg.setRenderer(BrailleToolkit.getRenderer());
        dlg.setTextBraille(this.braille);

        Font font = null;
        switch (this.shape) {
        case TEXT:
            font = Config.getConfig(Config.FONT.TEXT);
            if (this.braille) {
                dlg.setTextBrailleInfo(this.brailleList);
            } else {
                dlg.setText(this.text);
            }
            break;
        case FORMULA:
            font = Config.getConfig(Config.FONT.FORMULA);
            break;
        default:
        }
        dlg.setFont(font);
        dlg.setMode(this.shape);

        dlg.setVisible(true);
        if (dlg.isOK()) {
            this.shape = dlg.getMode();
            switch (this.shape) {
            case TEXT:
                this.braille = dlg.getTextBraille();
                if (this.braille) {
                    this.brailleList = dlg.getTextBrailleInfo();
                    this.text = BrailleToolkit.getTextTranslater().getTextFromSumiji(this.brailleList);
                } else {
                    this.text = dlg.getText();
                }
                if (null == this.text || this.text.isEmpty()) {
                    return false;
                }
                break;
            case FORMULA:
                this.braille = true;
                this.text = dlg.getFormula().replace("\t", "    ");
                if (Objects.isNull(this.text) || this.text.isEmpty()) {
                    return false;
                }
                break;
            default:
            }

            String fontFamily = dlg.getFontFamily();
            int fontSize = dlg.getFontSize();
            int fontStyle = dlg.getFontStyle();
            font = new Font(fontFamily, fontStyle, fontSize);

            this.fontMetrix = panel.getFontMetrics(font);
            return initText();
        }
        return false;
    }

    private boolean initText() {
        switch (this.shape) {
        case FORMULA:
            this.braille = true;
            Font font = this.fontMetrix.getFont();
            FormulaHandler handler = FormulaHandler.getInstance();
            this.mathML = handler.TeXtoMathML(this.text, false, font.getFamily(), font.getSize(), font.getStyle());
            if (null == this.mathML) {
                this.image = null;
                if (Objects.nonNull(this.brailleList)) {
                    this.brailleList = new ArrayList<>();
                }
                return false;
            }
            this.image = handler.getImageForMathML(mathML, this.fontMetrix.getFont().getFamily(),
                    new Point2D.Double(1, 1));
            if (null == this.image) {
                return false;
            }

            this.base.width = this.image.getWidth();
            this.base.height = this.image.getHeight();

            this.frame.x = this.base.x;
            this.frame.y = this.base.y;
            this.frame.width = this.base.width * this.scale.x;
            this.frame.height = this.base.height * this.scale.y;

            if (1 != this.scale.x || 1 != this.scale.y) {
                this.image = handler.getImageForMathML(mathML, this.fontMetrix.getFont().getFamily(), this.scale);
            }
            break;
        case TEXT:
            Rectangle2D rect = null;
            if (this.text.isEmpty()) {
                Rectangle2D trect = this.fontMetrix.getFont().getStringBounds(" ",
                        this.fontMetrix.getFontRenderContext());
                rect = new Rectangle2D.Double(trect.getX(), trect.getY() + trect.getHeight(), 0, 0);
            } else {
                StringTokenizer st = new StringTokenizer(this.text, "\n");
                while (st.hasMoreTokens()) {
                    String line = st.nextToken();
                    Rectangle2D trect = this.fontMetrix.getFont().getStringBounds(line,
                            this.fontMetrix.getFontRenderContext());
                    if (null == rect) {
                        rect = trect;
                    } else {
                        trect = new Rectangle2D.Double(trect.getX(), trect.getY() + rect.getHeight(), trect.getWidth(),
                                trect.getHeight());
                        rect.add(trect);
                    }
                }
            }
            this.base.width = rect.getWidth();
            this.base.height = rect.getHeight();

            this.frame.x = this.base.x + rect.getX();
            this.frame.y = this.base.y + rect.getY();
            this.frame.width = this.base.width * this.scale.x;
            this.frame.height = this.base.height * this.scale.y;
            break;
        default:
        }

        this.adaptFrame();
        this.calcBrailleBound();

        return true;
    }

    @Override
    protected Rectangle2D getShapeBound() {
        Rectangle2D bound = new Rectangle2D.Double();

        bound.setRect(this.x, this.y,
                Math.max(this.frame.width * this.scale.x, this.brailleBound.getWidth()),
                Math.max(this.frame.height * this.scale.y, this.brailleBound.getHeight()));

        return bound;
    }

    @Override
    protected Rectangle2D getBrailleBound() {
        Rectangle2D bound = new Rectangle2D.Double();
        bound.setRect(this.brailleBound);

        return bound;
    }

    @Override
    public BufInfo getBuffer(Graphics2D g) {
        BufInfo info = super.getBuffer(g);
        if (false == this.braille) {
            return info;
        }
        BufferedImage buf = info.getBuf();
        if (Objects.nonNull(this.brailleBound)) {
            int w = Math.max(this.brailleBound.width + 4 * TOGGLE, buf.getWidth());
            int h = Math.max(this.brailleBound.height + 4 * TOGGLE, buf.getHeight());
            if (this.brailleBound.width < this.width && this.brailleBound.height < this.height) {
                return info;
            }
            info.setBuf(new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR));
            info.setTg((Graphics2D) info.getBuf().getGraphics());

            info.getTg().translate(-this.x + 2 * TOGGLE, -this.y + 2 * TOGGLE);
        }

        return info;
    }

    @Override
    protected void drawBraille(Graphics2D g, boolean printing) {
        if (false == this.braille) {
            return;
        }
        BrailleRenderer renderer = BrailleToolkit.getRenderer();
        if (printing) {
            renderer.setMode(BrailleRenderer.MODE.PRINT);
        }
        int dSize = Util.mmToPixel(0, (int) Config.getConfig(this.dotSize));
        renderer.setDotSize(dSize);
        switch (this.shape) {
        case FORMULA:
            renderer.drawBraille(g, this.brailleList, this.x, this.y, false);
            break;
        case TEXT:
            renderer.drawBraille(g, this.brailleList, this.x, this.y, true);
            break;
        default:
        }
        renderer.setMode(BrailleRenderer.MODE.DISPLAY);

        if (isSelected() && false == printing) {
            g.setColor(this.frameColor);
            g.drawRect(this.brailleBound.x, this.brailleBound.y, this.brailleBound.width, this.brailleBound.height);
        }
    }

    private void calcBrailleBound() {
        BrailleRenderer renderer = BrailleToolkit.getRenderer();
        int dSize = Util.mmToPixel(0, (int) Config.getConfig(this.dotSize));
        renderer.setDotSize(dSize);

        switch (this.shape) {
        case FORMULA:
            this.brailleList = new ArrayList<>();
            BrailleToolkit.getFormula().fromMathML(this.mathML, false, this.brailleList);
            this.brailleBound = BrailleToolkit.getRenderer().getBound(this.brailleList, this.x, this.y, false);
            break;
        case TEXT:
            if (false == this.braille) {
                return;
            }
            if (null == this.brailleList) {
                this.brailleList = new ArrayList<>();
                BrailleToolkit.getTextTranslater().braileFromSumiji(this.text, this.brailleList, false, true);
            }
            this.brailleBound = BrailleToolkit.getRenderer().getBound(this.brailleList, this.x, this.y, true);
            break;
        default:
        }

        //        List<BrailleBox> boxList = BrailleToolkit.getRenderer().getBoxList(this.brailleList, this.x, this.y, false);
        //
        //        if (0 < boxList.size())
        //        {
        //            this.brailleBound = (Rectangle) boxList.get(0).clone();
        //        }
        //        for (Rectangle rect: boxList)
        //        {
        //            this.brailleBound.add(rect);
        //        }
        int size = renderer.getDotSize();
        this.brailleBound.width += size;
        this.brailleBound.height += size;
    }

    @Override
    protected void drawSumiji(Graphics2D g, boolean printing) {
        if (null == this.text || 0 == this.text.length()) {
            return;
        }
        if (0 == this.scale.x || 0 == this.scale.y) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g;
        switch (this.shape) {
        case FORMULA:
            if (null != image) {
                this.drawSumijiFormula(g2);
            }
            break;
        case TEXT:
            this.drawSumijiText(g2);
            break;
        default:
        }

        if (false == printing) {
            if (isSelected()) {
                g2.setColor(this.lineColor);
                g2.drawRect(this.x, this.y, this.width, this.height);

                drawToggle(g, this.x, this.y);
                drawToggle(g, this.x + this.width, this.y);
                drawToggle(g, this.x + this.width, this.y + this.height);
                drawToggle(g, this.x, this.y + this.height);
            }

            if (this.braille && Objects.nonNull(this.brailleBound)) {
                Color color = Config.getConfig(Config.COLOR.BRAILLE);
                g.setColor(color);
                g.drawRect(this.brailleBound.x, this.brailleBound.y, this.brailleBound.width, this.brailleBound.height);
            }
        }
    }

    private void drawSumijiFormula(Graphics2D g) {
        //            AffineTransformOp at = new AffineTransformOp(AffineTransform.getScaleInstance(this.scale.x,  this.scale.y), AffineTransformOp.TYPE_BICUBIC);
        //            g.drawImage(this.image, at, this.x, this.y);

        g.drawImage(image, this.x, this.y, null);
    }

    private void drawSumijiText(Graphics2D g) {
        AffineTransform atBack = g.getTransform();

        g.translate(this.frame.x, this.frame.y);
        g.scale(this.scale.x, this.scale.y);
        g.translate(-this.frame.x, -this.frame.y);

        int bx = (int) Math.round(this.base.x);
        int by = (int) Math.round(this.base.y);
        g.setColor(this.lineColor);
        g.setFont(this.fontMetrix.getFont());
        StringTokenizer st = new StringTokenizer(this.text, "\n");
        int th = this.fontMetrix.getHeight();
        int ly = by;
        while (st.hasMoreTokens()) {
            String line = st.nextToken();
            g.drawString(line, bx, ly);
            ly += th;
        }

        g.setTransform(atBack);
    }

    @Override
    public boolean hitTest(StatusHint hint, Point p, boolean reshapable) {
        if (this.isSelected()) {
            if (null != hint.status) {
                switch (hint.status) {
                case ERASE:
                    if (this.isStatus(STATUS.ERASE)) {
                        return true;
                    }
                    break;
                case MOVE:
                    if (this.hitTestMask(p)) {
                        hint.status = STATUS.ERASE_RESHAPE;
                        return true;
                    }
                    break;
                case ERASE_RESHAPE:
                    if (this.hitTestMask(p)) {
                        this.startMoveMask(p);
                        return true;
                    } else {
                        this.endMoveMask();
                    }
                    break;
                default:
                }
            }
        }

        boolean ret = false;
        switch (ViewMode.getMode()) {
        case BRAILLE:
            ret = this.hitTestBraille(hint, p, reshapable);
            break;
        case SUMIJI:
            ret = this.hitTestSumiji(hint, p, reshapable);
        }

        return ret;
    }

    private boolean hitTestBraille(StatusHint hint, Point p, boolean reshapable) {
        if (this.brailleBound.contains(p)) {
            hint.status = STATUS.MOVE;
            hint.point = p;

            return true;
        }

        return false;
    }

    private boolean hitTestSumiji(StatusHint hint, Point p, boolean reshapable) {
        int left = this.x;
        int right = this.x + this.width;
        int top = this.y;
        int bottom = this.y + this.height;

        int ld = Math.abs(left - p.x);
        int rd = Math.abs(right - p.x);
        int td = Math.abs(top - p.y);
        int bd = Math.abs(bottom - p.y);
        if (isSelected()) {
            if (ld < SEP) {
                if (reshapable && td < SEP) // 左上角
                {
                    hint.status = STATUS.RESIZE;
                    hint.resizeIndex = CONTROL.LEFTTOP;
                    hint.point = p;
                    return true;
                } else if (reshapable && bd < SEP) // 左下角
                {
                    hint.status = STATUS.RESIZE;
                    hint.resizeIndex = CONTROL.LEFTBOTTOM;
                    hint.point = p;
                    return true;
                } else if (this.contains(p)) // 内部
                {
                    //                    putStatus(panel, STATUS.MOVE, null, p);
                    hint.status = STATUS.MOVE;
                    hint.point = p;
                    return true;
                }
            } else if (rd < SEP) {
                if (reshapable && td < SEP) // 右上角
                {
                    hint.status = STATUS.RESIZE;
                    hint.resizeIndex = CONTROL.RIGHTTOP;
                    hint.point = p;
                    return true;
                } else if (reshapable && bd < SEP) // 右下角
                {
                    hint.status = STATUS.RESIZE;
                    hint.resizeIndex = CONTROL.RIGHTBOTTOM;
                    hint.point = p;
                    return true;
                } else if (this.contains(p)) // 内部
                {
                    hint.status = STATUS.MOVE;
                    hint.point = p;
                    return true;
                }
            } else {
                if (this.contains(p)) {
                    hint.status = STATUS.MOVE;
                    hint.point = p;
                    return true;
                }
            }
        } else {
            if (this.contains(p)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hitTest(Rectangle rect) {
        return this.intersects(rect);
    }

    @Override
    public void adaptFrame() {
        if (null == this.text || 0 == this.text.length()) {
            return;
        }
        this.width = (int) Math.round(this.frame.width);
        this.height = (int) Math.round(this.frame.height);
        this.x = (int) Math.round(this.frame.x);
        this.y = (int) Math.round(this.frame.y);

        if (null != this.brailleBound) {
            this.brailleBound.x = this.x;
            this.brailleBound.y = this.y;
        }
    }

    @Override
    public boolean reshape(StatusHint hint, Point dp) {
        // 移動
        if (isStatus(STATUS.MOVE)) {
            return move(hint, dp);
        }

        // 変形
        if (isStatus(STATUS.RESIZE)) {
            return resize(hint, dp);
        }

        return false;
    }

    @Override
    public boolean move(StatusHint hint, Point dp) {
        if (this.isStatus(STATUS.ERASE_RESHAPE)) {
            this.moveMask(dp);
            return true;
        }

        super.move(hint, dp);

        this.base.x += dp.x;
        this.base.y += dp.y;
        this.frame.x += dp.x;
        this.frame.y += dp.y;
        adaptFrame();

        if (null != hint) {
            hint.status = STATUS.MOVE;
            hint.point = dp;
            notifyChanged();
        }
        return true;
    }

    @Override
    public boolean resize(StatusHint hint, Point dp) {
        double left = this.frame.x;
        double right = this.frame.x + this.frame.width;
        double top = this.frame.y;
        double bottom = this.frame.y + this.frame.height;
        hint.status = STATUS.RESIZE;
        hint.resizeIndex = getResize();
        switch (hint.resizeIndex) {
        case LEFTTOP:
            left += dp.x;
            top += dp.y;
            break;
        case LEFTBOTTOM:
            left += dp.x;
            bottom += dp.y;
            break;
        case RIGHTTOP:
            right += dp.x;
            top += dp.y;
            break;
        case RIGHTBOTTOM:
            right += dp.x;
            bottom += dp.y;
            break;
        default:
        }
        hint.point.translate(dp.x, dp.y);
        if (left > right) {
            double tmp = left;
            left = right;
            right = tmp;
            switch (hint.resizeIndex) {
            case LEFTTOP:
                hint.resizeIndex = CONTROL.RIGHTTOP;
                break;
            case LEFTBOTTOM:
                hint.resizeIndex = CONTROL.RIGHTBOTTOM;
                break;
            case RIGHTTOP:
                hint.resizeIndex = CONTROL.LEFTTOP;
                break;
            case RIGHTBOTTOM:
                hint.resizeIndex = CONTROL.LEFTBOTTOM;
                break;
            default:
            }
        }
        if (top > bottom) {
            double tmp = top;
            top = bottom;
            bottom = tmp;
            switch (hint.resizeIndex) {
            case LEFTTOP:
                hint.resizeIndex = CONTROL.LEFTBOTTOM;
                break;
            case LEFTBOTTOM:
                hint.resizeIndex = CONTROL.LEFTTOP;
                break;
            case RIGHTTOP:
                hint.resizeIndex = CONTROL.RIGHTBOTTOM;
                break;
            case RIGHTBOTTOM:
                hint.resizeIndex = CONTROL.RIGHTTOP;
                break;
            default:
            }
        }
        Point2D.Double df = new Point2D.Double(this.base.x - this.frame.x, this.base.y - this.frame.y);

        this.frame.x = left;
        this.frame.y = top;
        this.frame.width = right - left;
        this.frame.height = bottom - top;

        this.base.x = this.frame.x + df.x;
        this.base.y = this.frame.y + df.y;

        this.scale.x = this.frame.width / this.base.width;
        this.scale.y = this.frame.height / this.base.height;

        if (SHAPE.FORMULA == this.shape) {
            this.image = FormulaHandler.getInstance().getImageForMathML(mathML, this.fontMetrix.getFont().getFamily(),
                    this.scale);
        }
        adaptFrame();
        notifyChanged();

        return true;
    }

    @Override
    public void resize(Point origin, double dxscale, double dyscale) {
        double left = this.frame.x;
        double right = this.frame.x + this.frame.width;
        double top = this.frame.y;
        double bottom = this.frame.y + this.frame.height;

        left = transform(left, origin.x, dxscale);
        right = transform(right, origin.x, dxscale);
        top = transform(top, origin.y, dyscale);
        bottom = transform(bottom, origin.y, dyscale);

        if (left > right) {
            double tmp = left;
            left = right;
            right = tmp;
        }
        if (top > bottom) {
            double tmp = top;
            top = bottom;
            bottom = tmp;
        }

        Point2D.Double df = new Point2D.Double(this.base.x - this.frame.x, this.base.y - this.frame.y);

        this.frame.x = left;
        this.frame.y = top;
        this.frame.width = right - left;
        this.frame.height = bottom - top;

        this.base.x = this.frame.x + df.x;
        this.base.y = this.frame.y + df.y;

        this.scale.x = this.frame.width / this.base.width;
        this.scale.y = this.frame.height / this.base.height;

        this.image = FormulaHandler.getInstance().getImageForMathML(mathML, this.fontMetrix.getFont().getFamily(),
                this.scale);

        adaptFrame();
        notifyChanged();
    }

    private static class TAG extends TAG_BASE {
        static final String SCALE_X = "scaleX";
        static final String SCALE_Y = "scaleY";
        static final String BRAILLE = "braille";
        static final String NABCC = "nabcc";
    }

    @Override
    public void export(ExportBase export) {
        ShapeText copy = this.clone();
        copy.resetOrigin(export.getOrigin());

        if (SHAPE.FORMULA == shape) {
            ShapeInfo info = new ShapeInfo(SHAPE.FORMULA);
            info.obj = copy;

            PairList list = new PairList();
            list.add(TAG.CLASS, this.getClass().getSimpleName());
            list.add(TAG.SHAPE, copy.shape);
            list.add(TAG.X, (int) Math.round(copy.base.x));
            list.add(TAG.Y, (int) Math.round(copy.base.y));
            list.add(TAG.SCALE_X, copy.scale.x);
            list.add(TAG.SCALE_Y, copy.scale.y);
            list.add(TAG.DOT_SIZE, copy.dotSize.name());
            list.add(TAG.DOT_SPAN, copy.dotSpan);
            list.add(TAG.BRAILLE, Boolean.toString(this.braille));
            info.setDesc(list);

            //            info.x = copy.x;
            //            info.y = copy.y;
            //            info.base.x = copy.x;
            //            info.base.y = copy.y;
            //            info.width = (int)Math.round(copy.frame.width);
            //            info.height = (int)Math.round(copy.frame.height);
            //            info.text = copy.text;
            //            info.mathML = copy.mathML;
            //            info.scale = copy.scale;
            //            info.desc = String.format("%s %d %d %f %f", this.getClass().getSimpleName(), info.base.x, info.base.y, copy.scale.x, copy.scale.y);
            //            info.dotSize = copy.dotSize;
            //            info.dotSpan = copy.dotSpan;
            //            info.brailleList = copy.brailleList;
            //            info.dict = formulaTranslater.getDict();

            export.writeStart(info);
            export.write(info);
            export.writeEnd(info);
        } else {
            ShapeInfo info = new ShapeInfo(SHAPE.TEXT);
            info.obj = copy;

            PairList list = new PairList();
            list.add(TAG.CLASS, this.getClass().getSimpleName());
            list.add(TAG.SHAPE, copy.shape);
            list.add(TAG.X, (int) Math.round(copy.base.x));
            list.add(TAG.Y, (int) Math.round(copy.base.y));
            list.add(TAG.SCALE_X, copy.scale.x);
            list.add(TAG.SCALE_Y, copy.scale.y);
            list.add(TAG.DOT_SIZE, copy.dotSize.name());
            list.add(TAG.DOT_SPAN, copy.dotSpan);
            list.add(TAG.BRAILLE, Boolean.toString(this.braille));
            if (this.braille) {
                StringBuilder buf = new StringBuilder();
                for (BrailleInfo b : this.brailleList) {
                    if (b.isLineBreak()) {
                        list.add(TAG.NABCC, buf.toString());
                        buf.delete(0, buf.length());
                    } else {
                        buf.append(b.getNABCC(true));
                    }
                }
                if (0 < buf.length()) {
                    list.add(TAG.NABCC, buf.toString());
                    buf.delete(0, buf.length());
                }
            }
            info.setDesc(list);

            //            info.x = copy.x;
            //            info.y = copy.y;
            //            info.base.x = (int)Math.round(copy.base.x);
            //            info.base.y = (int)Math.round(copy.base.y);
            //            info.font = copy.fontMetrix.getFont();
            //            info.scale = copy.scale;
            //            info.text = copy.text;
            //            info.desc = String.format("%s %d %d %f %f", this.getClass().getSimpleName(), info.base.x, info.base.y, copy.scale.x, copy.scale.y);
            //            info.brailleList = copy.brailleList;
            //            info.dotSize = copy.dotSize;
            //            info.dotSpan = copy.dotSpan;
            //            info.dict = BrailleToolkit.getTextTranslater().getDict();

            export.writeStart(info);
            export.write(info);
            export.writeEnd(info);
        }
    }

    @Override
    public boolean showProperty(DesignPanel panel, Point p) {
        ShapeTextInput dlg = ShapeTextInput.getInstance();
        dlg.clear();
        Util.setLocationUnderMouse(dlg);
        dlg.setRenderer(BrailleToolkit.getRenderer());

        dlg.setMode(this.shape);
        dlg.setFont(this.fontMetrix.getFont());
        dlg.setTextBraille(this.braille);
        switch (this.shape) {
        case TEXT:
            if (this.braille) {
                dlg.setTextBrailleInfo(this.brailleList);
            } else {
                dlg.setText(this.text);
            }
            break;
        case FORMULA:
            dlg.setFormula(this.text);
            break;
        default:
        }

        this.changed = false;
        dlg.setVisible(true);
        if (dlg.isOK()) {
            SHAPE shape = dlg.getMode();
            if (shape != this.shape) {
                this.shape = shape;
                this.changed = true;
            }

            String text = null;
            switch (this.shape) {
            case TEXT:
                boolean flag = dlg.getTextBraille();
                if (flag != this.braille) {
                    this.braille = flag;
                    this.changed = true;
                }
                if (this.braille) {
                    List<BrailleInfo> list = dlg.getTextBrailleInfo();
                    if (false == list.equals(this.brailleList)) {
                        this.brailleList = list;
                        this.text = BrailleToolkit.getTextTranslater().getTextFromSumiji(this.brailleList);
                        this.changed = true;
                    }
                } else {
                    text = dlg.getText();
                    if (false == text.equals(this.text)) {
                        this.text = text;
                        this.changed = true;
                    }
                }
                break;
            case FORMULA:
                text = dlg.getFormula().replace("\t", "    ");
                if (false == text.equals(this.text)) {
                    this.text = text;
                    this.changed = true;
                }
                break;
            default:
            }

            String fontFamily = dlg.getFontFamily();
            int fontSize = dlg.getFontSize();
            int fontStyle = dlg.getFontStyle();
            Font font = new Font(fontFamily, fontStyle, fontSize);

            FontMetrics fontMetrix = panel.getFontMetrics(font);
            if (false == font.equals(this.fontMetrix.getFont())) {
                this.fontMetrix = fontMetrix;
                this.changed = true;
            }
            initText();
        }

        if (this.changed) {
            this.notifyChanged();
            return true;
        }
        return false;
    }

    /**
    * descにクラス情報がある場合
    *
    * @param className
    * @param text
    * @param shape
    * @param fontMetrix
    * @return
    */
    public static ShapeText parse(ShapeInfo info, FontMetrics fontMetrix) {
        ShapeText obj = new ShapeText();
        obj.shape = info.getType();

        if (null == info.desc) {
            obj.base.x = info.x;
            obj.base.y = info.y;
            obj.text = info.text;

            obj.fontMetrix = fontMetrix;
            obj.initText();

            return obj;
        }

        PairList list = PairList.fromString(info.desc);
        List<Object> vals = null;
        vals = list.getValues(TAG.CLASS);
        if (0 == vals.size()) {
            obj.base.x = info.x;
            obj.base.y = info.y;
            obj.text = info.text;

            obj.fontMetrix = fontMetrix;
            obj.initText();

            return obj;
        }

        obj.parseDesc(list);
        vals = list.getValues(TAG.X);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            obj.base.x = java.lang.Double.parseDouble((String) vals.get(0));
        }
        vals = list.getValues(TAG.Y);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            obj.base.y = java.lang.Double.parseDouble((String) vals.get(0));
        }
        vals = list.getValues(TAG.SCALE_X);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            obj.scale.x = java.lang.Double.parseDouble((String) vals.get(0));
        }
        vals = list.getValues(TAG.SCALE_Y);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            obj.scale.x = java.lang.Double.parseDouble((String) vals.get(0));
        }
        vals = list.getValues(TAG.BRAILLE);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            obj.braille = Boolean.valueOf((String) vals.get(0));
        }
        vals = list.getValues(TAG.NABCC);
        if (obj.braille && 0 < vals.size()) {
            obj.brailleList = new ArrayList<>();
            for (Object val : vals) {
                String nabcc = (String) val;

                // NABCCからBrailleInfoを取得する
                List<BrailleInfo> l = BrailleToolkit.getTextTranslater().getBrailleFromNABCC(nabcc);
                if (0 < obj.brailleList.size()) {
                    obj.brailleList.add(BrailleInfo.LINEBREAK);
                }
                obj.brailleList.addAll(l);
            }
            obj.text = BrailleToolkit.getTextTranslater().getTextFromSumiji(obj.brailleList);
        } else {
            obj.text = info.text;
            obj.mathML = info.text;
        }

        obj.fontMetrix = fontMetrix;
        obj.initText();

        return obj;
    }
}
