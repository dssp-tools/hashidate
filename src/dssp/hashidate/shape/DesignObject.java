package dssp.hashidate.shape;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import javax.swing.JMenuItem;

import dssp.brailleLib.Util;
import dssp.hashidate.DesignPanel;
import dssp.hashidate.ObjectManager;
import dssp.hashidate.ViewMode;
import dssp.hashidate.config.Config;
import dssp.hashidate.config.Config.BRAILLE;
import dssp.hashidate.io.ExportBase;
import dssp.hashidate.misc.BraillePaint;
import dssp.hashidate.misc.BrailleStroke;
import dssp.hashidate.misc.FigureType.EDGE_TYPE;
import dssp.hashidate.misc.FigureType.FILL_TYPE;
import dssp.hashidate.misc.FigureType.LINE_TYPE;
import dssp.hashidate.misc.FillPaint;
import dssp.hashidate.misc.LineStroke;
import dssp.hashidate.misc.ObjectPopupMenu;
import dssp.hashidate.misc.PairList;
import dssp.hashidate.shape.property.ShapeProperty;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public abstract class DesignObject extends Rectangle implements Cloneable {
    protected SHAPE shape;

    public SHAPE getShape() {
        return shape;
    }

    protected ObjectManager objMan = null;
    protected static final int DEFAULT_WIDTH = 50;
    protected static final int DEFAULT_HEIGHT = 50;
    public static final int SEP = 4;
    public static final int TOGGLE = 8;

    protected Config.BRAILLE dotSize;

    public Config.BRAILLE getDotSize() {
        return dotSize;
    }

    protected float dotSpan;

    public float getDotSpan() {
        float ret = this.dotSpan;
        switch (this.dotSize) {
        case SMALL:
            ret = this.dotSpan * 0.5f;
            break;
        case LARGE:
            ret = this.dotSpan * 1.5f;
            break;
        default:
        }
        switch (this.lineType) {
        case DOT:
            return ret * 0.5f;
        case DASHED:
            return ret * 0.5f;
        default:
            return ret;
        }
    }

    public static enum STATUS {
        SELECTED, MOVE, RESHAPE, INITIAL, RESIZE, EDIT, ERASE, ERASE_RESHAPE, CHANGE
    };

    static final Set<STATUS> ALL = EnumSet.of(STATUS.MOVE, STATUS.RESHAPE, STATUS.INITIAL, STATUS.RESIZE, STATUS.EDIT);
    protected EnumSet<STATUS> status = EnumSet.noneOf(STATUS.class);

    public static interface HINT_OPTION {
    }

    public static enum EDIT_TYPE implements HINT_OPTION {
        ADD,
        DEL;
    }

    public static enum CONTROL {
        LEFTTOP,
        TOP,
        RIGHTTOP,
        RIGHT,
        RIGHTBOTTOM,
        BOTTOM,
        LEFTBOTTOM,
        LEFT,
        OTHER;
    };

    private CONTROL resizeIndex = null;

    public static class StatusHint {
        public STATUS status;
        public CONTROL resizeIndex;
        public Point point;
        public HINT_OPTION option = null;
        public int modifiers;

        public void clear() {
            this.status = null;
            this.resizeIndex = null;
            this.point = null;
        }
    }

    List<MaskLine> maskLines = Util.newArrayList();

    public List<MaskLine> getMaskLines() {
        return maskLines;
    }

    protected DesignObject() {
        this.lineColor = Config.getConfig(Config.COLOR.LINE);
        this.frameColor = Config.getConfig(Config.COLOR.FRAME);

        //        int size = Config.getConfig(Config.BRAILLE.FIGURE);
        //        this.dotSize = Util.mmToPixel(0, size);
        this.dotSize = Config.getConfig(Config.BRAILLE.FIGURE);

        int size = Config.getConfig(Config.BRAILLE.DOT_SPAN);
        this.dotSpan = Util.mmToPixel(0, size);

        this.setStroke();
    }

    protected DesignObject(Point p) {
        this();
        this.x = p.x;
        this.y = p.y;
    }

    protected DesignObject(DesignObject src) {
        this.lineColor = src.lineColor;
        this.lineStroke = src.lineStroke;
        this.fillPaint = src.fillPaint;
        this.frameColor = src.frameColor;
        this.dotSize = src.dotSize;
        this.dotSpan = src.dotSpan;
    }

    protected boolean changed = false;

    public boolean isChanged() {
        return this.changed;
    }

    public final void notifyChanged() {
        if (null == this.objMan) {
            Util.logInfo("No object manager %s", this.getClass().getSimpleName());
            return;
        }
        this.objMan.changed();
    }

    protected final void drawToggle(Graphics g, Point p) {
        g.setColor(this.frameColor);
        g.drawRect(p.x - TOGGLE / 2, p.y - TOGGLE / 2, TOGGLE, TOGGLE);
    }

    protected final void drawToggle(Graphics g, int x, int y) {
        g.setColor(this.frameColor);
        g.drawRect(x - TOGGLE / 2, y - TOGGLE / 2, TOGGLE, TOGGLE);
    }

    protected final void drawToggles(Graphics g, int[] x, int[] y) {
        g.setColor(this.frameColor);
        for (int i = 0; i < x.length; i++) {
            g.drawRect(x[i] - TOGGLE / 2, y[i] - TOGGLE / 2, TOGGLE, TOGGLE);
        }
    }

    protected final void drawFrame(Graphics g, Rectangle frame) {
        Graphics2D g2 = (Graphics2D) g;
        BasicStroke st0 = (BasicStroke) g2.getStroke();
        float[] dash = { 10.0f, 3.0f };
        BasicStroke st = new BasicStroke(st0.getLineWidth(), BasicStroke.CAP_BUTT, st0.getLineJoin(),
                st0.getMiterLimit(), dash, 0.0f);
        g2.setStroke(st);
        g2.setColor(this.frameColor);
        g2.drawRect(frame.x, frame.y, frame.width, frame.height);

        g2.setStroke(st0);
        drawToggle(g, frame.x, frame.y);
        drawToggle(g, frame.x + frame.width, frame.y);
        drawToggle(g, frame.x + frame.width, frame.y + frame.height);
        drawToggle(g, frame.x, frame.y + frame.height);
    }

    public final void select(boolean flag) {
        if (flag) {
            this.status.add(STATUS.SELECTED);
        } else {
            this.status.clear();
        }
    }

    public final boolean isSelected() {
        return this.status.contains(STATUS.SELECTED);
    }

    protected final void setStatus(STATUS status) {
        this.status.add(status);
    }

    protected final void unsetStatus(STATUS status) {
        this.status.remove(status);
    }

    protected final void unsetStatus(Set<STATUS> status) {
        this.status.removeAll(status);
    }

    public final boolean isStatus(STATUS status) {
        return this.status.contains(status);
    }

    protected final boolean isResize(CONTROL resizeIndex) {
        return (this.resizeIndex == resizeIndex);
    }

    public final void putStatus(StatusHint hint) {
        if (null == hint.status) {
            return;
        }
        switch (hint.status) {
        case INITIAL:
            unsetStatus(ALL);
            setStatus(STATUS.INITIAL);
            break;
        case RESHAPE:
            unsetStatus(ALL);
            setStatus(STATUS.RESHAPE);
            break;
        case MOVE:
            unsetStatus(ALL);
            setStatus(STATUS.MOVE);
            break;
        case RESIZE:
            unsetStatus(ALL);
            setStatus(STATUS.RESIZE);
            this.resizeIndex = hint.resizeIndex;
            break;
        case EDIT:
            unsetStatus(ALL);
            setStatus(STATUS.EDIT);
        default:
        }
    }

    protected final boolean hitTestFrame(StatusHint hint, Point p, boolean reshapable, Rectangle rect) {
        if (hint.status == STATUS.ERASE && this.isStatus(STATUS.ERASE)) {
            return true;
        }

        int left = rect.x;
        int right = rect.x + rect.width;
        int top = rect.y;
        int bottom = rect.y + rect.height;

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
                } else if (p.y > top && p.y < bottom) // 左辺
                {
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
                } else if (p.y > top && p.y < bottom) // 右辺
                {
                    hint.status = STATUS.MOVE;
                    hint.point = p;
                    return true;
                }
            } else {
                if (p.x > left && p.x < right) {
                    if (td < SEP) // 上辺
                    {
                        hint.status = STATUS.MOVE;
                        hint.point = p;
                        return true;
                    } else if (bd < SEP) // 下辺
                    {
                        hint.status = STATUS.MOVE;
                        hint.point = p;
                        return true;
                    }
                }
            }

        } else {
            if (ld < SEP || rd < SEP) {
                if (p.y < bottom && p.y > top) // 左辺か右辺
                {
                    return true;
                }
            } else if (td < SEP || bd < SEP) {
                if (p.x > left && p.x < right) // 上辺か下辺
                {
                    return true;
                }
            }
        }
        return false;
    }

    protected final double transform(double val, double origin, double scale) {
        double tval = val - origin;
        return (tval * scale + origin);
    }

    protected final void resetOrigin(Point origin) {
        Point dp = new Point(-origin.x, -origin.y);
        this.move(null, dp);
    }

    protected final CONTROL getResize() {
        return this.resizeIndex;
    }

    public boolean init(DesignPanel panel) {
        return true;
    }

    public void setObjMan(ObjectManager objMan) {
        this.objMan = objMan;
    }

    protected void drawFrame(Graphics g) {
        drawFrame(g, this);
    }

    public void clicked(DesignPanel panel, Point p) {
    }

    protected boolean hitTestFrame(StatusHint hint, Point p, boolean reshapable) {
        return hitTestFrame(hint, p, reshapable, this);
    }

    public void doubleClicked(DesignPanel panel, Point p) {
    }

    @Override
    public DesignObject clone() {
        DesignObject obj = (DesignObject) super.clone();

        obj.shape = this.shape;

        obj.lineColor = this.lineColor;
        obj.lineType = this.lineType;
        obj.lineStroke = this.lineStroke;

        obj.fillType = this.fillType;
        obj.fillColor = this.fillColor;
        obj.fillSize = this.fillSize;
        obj.fillPaint = this.fillPaint;

        obj.frameColor = this.frameColor;
        obj.dotSize = this.dotSize;
        obj.dotSpan = this.dotSpan;
        obj.resizeIndex = this.resizeIndex;
        obj.setBounds(this);
        obj.status = this.status.clone();
        //        obj.menuList = null;

        obj.select(false);

        return obj;
    }

    public static enum DRAW_MODE {
        DISPLAY,
        PRINT,
        EXPORT;
    }

    public class BufInfo {
        private BufferedImage buf = null;
        private Graphics2D tg = null;

        public BufferedImage getBuf() {
            return buf;
        }

        public void setBuf(BufferedImage buf) {
            this.buf = buf;
        }

        public Graphics2D getTg() {
            return tg;
        }

        public void setTg(Graphics2D tg) {
            this.tg = tg;
        }
    }

    protected BufInfo getBuffer(Graphics2D g) {
        BufInfo info = new BufInfo();
        info.buf = new BufferedImage(this.width + 4 * TOGGLE, this.height + 4 * TOGGLE, BufferedImage.TYPE_4BYTE_ABGR);

        info.tg = (Graphics2D) info.buf.getGraphics();
        info.tg.translate(-this.x + 2 * TOGGLE, -this.y + 2 * TOGGLE);

        return info;
    }

    protected void drawBuf(Graphics2D g, BufferedImage buf) {
        g.drawImage(buf, this.x - 2 * TOGGLE, this.y - 2 * TOGGLE, null);
    }

    /**
    * 描画
    *
    * @param g Graphics
    * @param printing true=印刷用 false=画面用
    */
    public void draw(Graphics2D g, DRAW_MODE mode) {
        switch (mode) {
        case DISPLAY:
        case PRINT:
            if (this.isSelected()) {
                switch (ViewMode.getMode()) {
                case SUMIJI:
                    this.drawSumiji(g, (DesignObject.DRAW_MODE.PRINT == mode));
                    break;
                case BRAILLE:
                    this.drawBraille(g, (DesignObject.DRAW_MODE.PRINT == mode));
                }

                this.drawMask(g, mode, true);
            } else {
                BufInfo info = this.getBuffer(g);
                Graphics2D tg = info.tg;

                switch (ViewMode.getMode()) {
                case SUMIJI:
                    this.drawSumiji(tg, (DesignObject.DRAW_MODE.PRINT == mode));
                    break;
                case BRAILLE:
                    this.drawBraille(tg, (DesignObject.DRAW_MODE.PRINT == mode));
                }

                this.drawMask(tg, mode, false);
                this.drawBuf(g, info.buf);
            }
            break;
        case EXPORT:
            switch (ViewMode.getMode()) {
            case SUMIJI:
                this.drawSumiji(g, true);
                break;
            case BRAILLE:
                this.drawBraille(g, true);
            }
        }
    }

    protected abstract void drawSumiji(Graphics2D g, boolean printing);

    protected abstract void drawBraille(Graphics2D g, boolean printing);

    public void drawMask(Graphics2D g, DesignObject.DRAW_MODE mode, boolean selected) {
        for (MaskLine ml : this.maskLines) {
            ml.putMask(g, mode, selected);
        }
    }

    /**
    * ヒットテスト
    *
    * @param hint 図形の状態(返り値)
    * @param p マウスの位置(DesignPanelを基準)
    * @param reshapable true=変形用 false=選択、移動用
    * @return true=マウスが図形上にある
    */
    public abstract boolean hitTest(StatusHint hint, Point p, boolean reshapable);

    /**
    * ヒットテスト
    *
    * @param rect テストする領域
    * @return true=マウスが領域上にある
    */
    public abstract boolean hitTest(Rectangle rect);

    /**
    * 枠の適応
    */
    public abstract void adaptFrame();

    /**
    * 変形
    *
    * @param hint 図形の状態(返り値)
    * @param dp マウスの移動ベクトル
    * @return
    */
    public abstract boolean reshape(StatusHint hint, Point dp);

    /**
    * 移動
    *
    * @param hint 図形の状態(返り値)
    * @param dp マウスの移動ベクトル
    */
    public boolean move(StatusHint hint, Point dp) {
        for (MaskLine ml : this.maskLines) {
            ml.move(dp.x, dp.y);
        }
        return true;
    };

    /**
    * リサイズ
    *
    * @param hint 図形の状態(返り値)
    * @param dp マウスの移動ベクトル
    */
    public abstract boolean resize(StatusHint hint, Point dp);

    /**
    * グループ化時のリサイズ
    *
    * @param origin 原点
    * @param dxscale 倍率(X方向)
    * @param dyscale 倍率(Y方向)
    */
    public abstract void resize(Point origin, double dxscale, double dyscale);

    public boolean edit(StatusHint hint, Point p) {
        return false;
    }

    public static class TAG_BASE {
        public static final String CLASS = "class";
        static final String SHAPE = "shape";
        public static final String X = "x";
        public static final String Y = "y";
        public static final String WIDTH = "width";
        public static final String HEIGHT = "height";
        static final String DOT_SIZE = "dotSize";
        static final String DOT_SPAN = "dotSpan";

        static final String LINE_COLOR = "lineColor";
        static final String LINE_TYPE = "lineType";
        static final String EDGE_TYPE = "edgeType";
        static final String LINE_BACK = "lineBack";

        static final String FILL_TYPE = "fillType";
        static final String FILL_COLOR = "fillColor";
        static final String FILL_SIZE = "fillSize";
        static final String FILL_BACK = "fillBack";

        static final String MASK_LINE = "maskLine";
    }

    /**
    * 出力
    *
    * @param export 出力用クラス
    */
    public abstract void export(ExportBase export);

    public PairList makeDesc(DesignObject obj) {
        PairList list = new PairList();
        list.add(TAG_BASE.CLASS, obj.getClass().getSimpleName());
        list.add(TAG_BASE.SHAPE, obj.shape);
        list.add(TAG_BASE.X, obj.x);
        list.add(TAG_BASE.Y, obj.y);
        list.add(TAG_BASE.WIDTH, obj.width);
        list.add(TAG_BASE.HEIGHT, obj.height);
        list.add(TAG_BASE.DOT_SIZE, obj.dotSize.name());
        list.add(TAG_BASE.DOT_SPAN, obj.dotSpan);
        list.add(TAG_BASE.LINE_TYPE, obj.lineType.name());
        list.add(TAG_BASE.EDGE_TYPE, obj.edgeType.name());
        list.add(TAG_BASE.LINE_COLOR, Util.colorString(obj.lineColor));
        list.add(TAG_BASE.LINE_BACK, obj.lineBack);
        list.add(TAG_BASE.FILL_TYPE, obj.fillType.name());
        list.add(TAG_BASE.FILL_COLOR, Util.colorString(obj.fillColor));
        list.add(TAG_BASE.FILL_SIZE, obj.fillSize.name());
        list.add(TAG_BASE.FILL_BACK, obj.fillBack);

        for (MaskLine ml : this.maskLines) {
            list.add(TAG_BASE.MASK_LINE, ml.toString());
        }

        return list;
    }

    public void parseDesc(PairList list) {
        List<Object> vals = list.getValues(TAG_BASE.CLASS);

        vals = list.getValues(TAG_BASE.SHAPE);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            this.shape = SHAPE.valueOf((String) vals.get(0));
        }
        vals = list.getValues(TAG_BASE.X);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            this.x = java.lang.Integer.parseInt((String) vals.get(0));
        }
        vals = list.getValues(TAG_BASE.Y);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            this.y = java.lang.Integer.parseInt((String) vals.get(0));
        }
        vals = list.getValues(TAG_BASE.WIDTH);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            this.width = java.lang.Integer.parseInt((String) vals.get(0));
        }
        vals = list.getValues(TAG_BASE.HEIGHT);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            this.height = java.lang.Integer.parseInt((String) vals.get(0));
        }
        vals = list.getValues(TAG_BASE.DOT_SIZE);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            this.dotSize = Config.BRAILLE.valueOf((String) vals.get(0));
        }
        vals = list.getValues(TAG_BASE.DOT_SPAN);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            this.dotSpan = java.lang.Float.parseFloat((String) vals.get(0));
        }
        vals = list.getValues(TAG_BASE.LINE_TYPE);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            this.lineType = LINE_TYPE.getValueOf((String) vals.get(0));
        }
        vals = list.getValues(TAG_BASE.EDGE_TYPE);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            this.edgeType = EDGE_TYPE.getValueOf((String) vals.get(0));
        }
        vals = list.getValues(TAG_BASE.LINE_COLOR);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            this.lineColor = Util.getColor((String) vals.get(0));
        }
        vals = list.getValues(TAG_BASE.LINE_BACK);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            this.lineBack = Boolean.parseBoolean((String) vals.get(0));
        }
        vals = list.getValues(TAG_BASE.FILL_TYPE);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            this.fillType = FILL_TYPE.getValueOf((String) vals.get(0));
        }
        vals = list.getValues(TAG_BASE.FILL_COLOR);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            this.fillColor = Util.getColor((String) vals.get(0));
        }
        vals = list.getValues(TAG_BASE.FILL_SIZE);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            this.fillSize = Config.BRAILLE.valueOf((String) vals.get(0));
        }
        vals = list.getValues(TAG_BASE.FILL_BACK);
        if (0 < vals.size() && vals.get(0) instanceof String) {
            this.fillBack = Boolean.parseBoolean((String) vals.get(0));
        }
        vals = list.getValues(TAG_BASE.MASK_LINE);
        for (Object val : vals) {
            if (val instanceof String) {
                MaskLine ml = MaskLine.fromString((String) val);
                if (null != ml) {
                    this.maskLines.add(ml);
                }
            }
        }
    }

    protected Color frameColor;

    /**
    * 線
    */
    protected LINE_TYPE lineType = LINE_TYPE.SOLID;

    public LINE_TYPE getLineType() {
        return lineType;
    }

    protected Color lineColor;

    public Color getLineColor() {
        return lineColor;
    }

    protected EDGE_TYPE edgeType = EDGE_TYPE.BUTT;

    public EDGE_TYPE getEdgeType() {
        return edgeType;
    }

    public void setEdgeType(EDGE_TYPE edgeType) {
        this.edgeType = edgeType;
    }

    protected LineStroke lineStroke;
    protected BrailleStroke brailleStroke;

    public Stroke getLineStroke() {
        return lineStroke;
    }

    protected void setStroke() {
        float psize;
        switch (this.dotSize) {
        case SMALL:
            psize = 1;
            break;
        case LARGE:
            psize = 4;
            break;
        default:
            psize = 2;
        }

        this.lineStroke = new LineStroke(psize, this.lineType, this.edgeType);

        float dSize = Util.mmToPixel(0, Config.getConfig(this.dotSize));
        this.brailleStroke = new BrailleStroke(dSize, this.getDotSpan(), this.lineType, this.edgeType, !this.lineBack);
    }

    /**
    * 裏線
    */
    protected boolean lineBack = false;

    public boolean isBackLine() {
        return lineBack;
    }

    /**
    * 塗りつぶし
    */
    protected FILL_TYPE fillType = FILL_TYPE.TRANSPARENT;

    public FILL_TYPE getFillType() {
        return fillType;
    }

    protected Color fillColor = Color.BLACK;

    public Color getFillColor() {
        return fillColor;
    }

    protected Config.BRAILLE fillSize = Config.BRAILLE.MIDDLE;

    public Config.BRAILLE getFillSize() {
        return fillSize;
    }

    protected Paint fillPaint;
    protected Paint braillePaint;

    public void setFillPaint() {
        int size;
        switch (this.fillSize) {
        case SMALL:
            size = 1;
            break;
        case LARGE:
            size = 4;
            break;
        default:
            size = 2;
        }

        this.fillPaint = FillPaint.create(this.fillType, this.fillColor, size);
        int dSize = Util.mmToPixel(0, Config.getConfig(this.fillSize));
        this.braillePaint = BraillePaint.create(this.fillType, this.fillColor, dSize, this.getDotSpan(),
                !this.fillBack);
    }

    /**
    * 裏点（塗りつぶし）
    */
    protected boolean fillBack = false;

    public boolean isBackPoint() {
        return fillBack;
    }

    protected Stroke bStroke;
    protected Color bColor;

    /**
    * 描画属性の設定ON/OFF
    * @param g
    * @param braille true = 点字モード
    * @param on
    */
    public void setDrawProperty(Graphics2D g, boolean braille, boolean on) {
        if (on) {
            this.bStroke = g.getStroke();
            if (braille && null != this.brailleStroke) {
                g.setStroke(this.brailleStroke);
            } else if (null != this.lineStroke) {
                g.setStroke(this.lineStroke);
            }
            this.bColor = g.getColor();
            g.setColor(this.lineColor);
        } else {
            g.setStroke(this.bStroke);
            g.setColor(this.bColor);
        }
    }

    protected Paint bPaint;

    public void setFillProperty(Graphics2D g, boolean braille, boolean on) {
        if (on) {
            bPaint = g.getPaint();
            if (braille && null != this.braillePaint) {
                g.setPaint(this.braillePaint);
            } else if (null != this.fillPaint) {
                g.setPaint(this.fillPaint);
            }
        } else {
            g.setPaint(bPaint);
        }
    }

    /**
    * 描画属性の変更
    *
    * @param panel DesignPanel(null可)
    * @param p マウスの位置
    */
    public boolean showProperty(DesignPanel panel, Point p) {
        ShapeProperty dlg = ShapeProperty.getInstance();
        this.setCommonProperty(dlg);

        this.changed = false;
        dlg.setVisible(true);
        if (dlg.isOk()) {
            this.getCommonProperty(dlg);
        }

        return true;
    }

    protected void setCommonProperty(ShapeProperty dlg) {
        dlg.setLineSize(this.dotSize);
        dlg.setLineType(this.lineType);
        dlg.setEdgeType(this.edgeType);
        dlg.setLineColor(this.lineColor);
        dlg.setLineBack(this.lineBack);

        dlg.setFillType(this.fillType);
        dlg.setFillColor(this.fillColor);
        dlg.setFillSize(this.fillSize);
        dlg.setFillBack(this.fillBack);
    }

    protected void getCommonProperty(ShapeProperty dlg) {
        Color lc = dlg.getLineColor();
        if (false == this.lineColor.equals(lc)) {
            this.lineColor = lc;
            this.changed = true;
        }
        BRAILLE ls = dlg.getLineSize();
        if (false == this.dotSize.equals(ls)) {
            this.dotSize = ls;
            this.changed = true;
        }
        LINE_TYPE lt = dlg.getLineType();
        if (false == this.lineType.equals(lt)) {
            this.lineType = lt;
            this.changed = true;
        }
        EDGE_TYPE et = dlg.getEdgeType();
        if (false == this.edgeType.equals(et)) {
            this.edgeType = et;
            this.changed = true;
        }
        boolean lb = dlg.isLineBack();
        if (this.lineBack != lb) {
            this.lineBack = lb;
            this.changed = true;
        }
        FILL_TYPE ft = dlg.getFillType();
        if (null != ft && false == this.fillType.equals(ft)) {
            this.fillType = ft;
            this.changed = true;
        }
        Color fc = dlg.getFillColor();
        if (null != fc && false == this.fillColor.equals(fc)) {
            this.fillColor = fc;
            this.changed = true;
        }
        BRAILLE fs = dlg.getFillSize();
        if (null != fs && false == this.fillSize.equals(fs)) {
            this.fillSize = fs;
            this.changed = true;
        }
        boolean fb = dlg.isFillBack();
        if (this.fillBack != fb) {
            this.fillBack = fb;
            this.changed = true;
        }
        if (this.isChanged()) {
            this.notifyChanged();
            this.setStroke();
            this.setFillPaint();
        }
    }

    public void showMenu(Point location) {

    }

    public void menuCalled(String name, Point location, StatusHint hint) {
    }

    protected void initCommonMenu(ObjectPopupMenu menu) {
        JMenuItem menuItem = new JMenuItem("プロパティ");
        menu.add(menuItem);

        menuItem = new JMenuItem("消しゴム");
        menu.add(menuItem);

        menuItem = new JMenuItem("コピー");
        menu.add(menuItem);

        menuItem = new JMenuItem("切り取り");
        menu.add(menuItem);

        menuItem = new JMenuItem("削除");
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("一つ上へ移動");
        menu.add(menuItem);

        menuItem = new JMenuItem("一つ下へ移動");
        menu.add(menuItem);

        menuItem = new JMenuItem("一番上へ移動");
        menu.add(menuItem);

        menuItem = new JMenuItem("一番下へ移動");
        menu.add(menuItem);
    }

    public void initMask() {
        this.setStatus(STATUS.ERASE);
    }

    public void endMask() {
        this.unsetStatus(STATUS.ERASE);

        int np = this.maskLine.countPoint();
        if (1 < np) {
            Point2D.Float p1 = this.maskLine.getPoint(np - 2);
            Point2D.Float p2 = this.maskLine.getPoint(np - 1);
            if (p1.equals(p2)) {
                this.maskLine.removePoint(np - 1);
            }
        }
        if (2 > this.maskLine.countPoint()) {
            this.maskLines.remove(this.maskLine);
        }
        this.maskLine = null;
    }

    protected MaskLine maskLine;
    protected int maskLineControl = -1;

    public void expandMask(Point p) {
        if (null == this.maskLine) {
            this.maskLine = new MaskLine();
            this.maskLines.add(this.maskLine);
            this.maskLine.expand(p.x, p.y, true);
            this.maskLine.expand(p.x, p.y, true);
        } else {
            this.maskLine.expand(p.x, p.y, true);
        }
    }

    public void moveMask(Point dp) {
        if (null == this.maskLine) {
            return;
        }

        if (0 > this.maskLineControl) {
            this.maskLine.move(-1, dp.x, dp.y);
            return;
        }

        Point2D.Float p = this.maskLine.getPoint(this.maskLineControl);
        this.maskLine.move(this.maskLineControl, p.x + dp.x, p.y + dp.y);
    }

    public void startMoveMask(Point p) {
        this.setStatus(STATUS.ERASE_RESHAPE);
        for (MaskLine ml : this.maskLines) {
            if (ml.hitTest(p.x, p.y)) {
                this.maskLine = ml;
                ;
                this.maskLineControl = ml.getControl(p.x, p.y);
                break;
            }
        }
    }

    public void endMoveMask() {
        this.unsetStatus(STATUS.ERASE_RESHAPE);
        this.maskLine = null;
        this.maskLineControl = -1;
    }

    public boolean hitTestMask(Point p) {
        for (MaskLine ml : this.maskLines) {
            if (ml.hitTest(p.x, p.y)) {
                return true;
            }
        }

        return false;
    }

    StatusHint hint;

    public StatusHint getHint() {
        if (null == hint) {
            hint = new StatusHint();
        }
        return hint;
    }

    protected Rectangle2D getShapeBound() {
        return this;
    }

    protected Rectangle2D getBrailleBound() {
        return this;
    }
}
