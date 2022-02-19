package dssp.hashidate;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;

import dssp.brailleLib.Util;
import dssp.hashidate.config.Config;
import dssp.hashidate.config.PageInfo;
import dssp.hashidate.io.ExportBase;
import dssp.hashidate.io.ImportSVG;
import dssp.hashidate.shape.DesignObject;
import dssp.hashidate.shape.SHAPE;
import dssp.hashidate.shape.ShapeGroup;
import dssp.hashidate.shape.helper.ObjectFactory;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class ObjectManager {
    private List<DesignObject> list = Util.newArrayList();
    private PageInfo pageInfo;
    private File file = null;
    private boolean isChanged = false;

    ObjectManager() {
    }

    public void changed() {
        this.isChanged = true;
    }

    void stored() {
        this.isChanged = false;
    }

    boolean isChanged() {
        return this.isChanged;
    }

    File getFile() {
        return this.file;
    }

    void setFile(File file) {
        this.file = file;
    }

    PageInfo getPageInfo() {
        if (null == this.pageInfo) {
            this.pageInfo = Config.getConfig(Config.PAGE.CURRENT);
        }
        return this.pageInfo;
    }

    public int addObject(DesignObject obj) {
        this.list.add(obj);
        //        ObjectUndoManager.getInstance().add(this.list.size()-1, obj);
        changed();

        return this.list.size() - 1;
    }

    public int delObject(DesignObject obj) {
        if (this.list.contains(obj)) {
            int index = this.list.indexOf(obj);
            //            ObjectUndoManager.getInstance().delete(this.list.indexOf(obj), obj);
            this.list.remove(obj);
            return index;
        }

        return -1;
    }

    public int insertObject(int index, DesignObject obj) {
        if (index > this.list.size()) {
            return -1;
        }

        this.list.add(index, obj);
        //        ObjectUndoManager.getInstance().add(index, obj);
        changed();

        return index;
    }

    public DesignObject delObject(int index) {
        if (index >= this.list.size()) {
            return null;
        }

        DesignObject obj = this.list.get(index);
        //        ObjectUndoManager.getInstance().delete(index, obj);
        this.list.remove(index);
        changed();

        return obj;
    }

    public DesignObject setObject(int index, DesignObject obj) {
        if (index >= this.list.size()) {
            return null;
        }

        DesignObject current = this.list.get(index);
        this.list.set(index, obj);
        changed();

        return current;
    }

    public int indexOf(DesignObject obj) {
        return this.list.indexOf(obj);
    }

    public enum COMMAND {
        COPY,
        CUT,
        PASTE,
        DELETE,
        UP,
        DOWN,
        TOP,
        BOTTOM;
    }

    void putObject(COMMAND cmd, DesignObject obj) {
        if (false == this.list.contains(obj)) {
            return;
        }

        int index = this.list.indexOf(obj);
        switch (cmd) {
        case UP:
            if (index == (this.list.size() - 1)) {
                return;
            }
            this.list.remove(obj);
            this.list.add(index + 1, obj);
            changed();
            break;
        case DOWN:
            if (0 == index) {
                return;
            }
            this.list.remove(obj);
            this.list.add(index - 1, obj);
            changed();
            break;
        case TOP:
            if (index == (this.list.size() - 1)) {
                return;
            }
            this.list.remove(obj);
            this.list.add(obj);
            changed();
            break;
        case BOTTOM:
            if (0 == index) {
                return;
            }
            this.list.remove(obj);
            this.list.add(0, obj);
            changed();
            break;
        default:
        }
    }

    void unselect() {
        for (int i = 0; i < this.list.size(); i++) {
            DesignObject obj = (DesignObject) this.list.get(i);
            obj.select(false);
        }
    }

    List<DesignObject> getList() {
        return this.list;
    }

    void draw(Graphics2D g, DesignObject.DRAW_MODE mode) {
        for (int i = 0; i < this.list.size(); i++) {
            DesignObject obj = (DesignObject) this.list.get(i);
            obj.draw(g, mode);
        }
    }

    List<DesignObject> hitTest(DesignPanel panel, Rectangle rect) {
        List<DesignObject> list = Util.newArrayList();

        for (int i = (this.list.size() - 1); i >= 0; i--) {
            DesignObject obj = (DesignObject) this.list.get(i);
            if (obj.hitTest(rect)) {
                list.add(obj);
            }
        }

        return list;
    }

    DesignObject hitTest(DesignObject.StatusHint hint, Point p) {
        for (int i = (this.list.size() - 1); i >= 0; i--) {
            DesignObject obj = (DesignObject) this.list.get(i);
            if (obj.hitTest(hint, p, true)) {
                return obj;
            }
        }
        return null;
    }

    Rectangle getArea() {
        Rectangle area = null;
        for (int i = 0; i < this.list.size(); i++) {
            DesignObject obj = (DesignObject) this.list.get(i);
            if (null == area) {
                area = new Rectangle(obj);
            } else {
                area.add(obj.getFrame());
            }
        }

        return area;
    }

    Rectangle getArea(List<DesignObject> selObjList) {
        Rectangle area = null;
        for (int i = 0; i < selObjList.size(); i++) {
            DesignObject obj = (DesignObject) selObjList.get(i);
            if (null == area) {
                area = new Rectangle(obj);
            } else {
                area.add(obj.getFrame());
            }
        }

        return area;
    }

    /**
    * 図形をグループ化する
    *
    * @param hint
    * @param selObjList グループ化する図形
    * @return グループ図形
    */
    DesignObject makeGroup(DesignObject.StatusHint hint, List<DesignObject> selObjList) {
        List<DesignObject> list = Util.newArrayList();
        for (int i = this.list.size() - 1; i >= 0; i--) {
            DesignObject obj = this.list.get(i);
            if (selObjList.contains(obj)) {
                list.add(0, obj);
                this.list.remove(obj);
            }
        }
        ObjectFactory factory = ObjectFactory.getInstance();
        ShapeGroup group = (ShapeGroup) factory.newObject(hint, SHAPE.GROUP, null);
        group.setObjects(list);

        return group;
    }

    /**
    * グループ解除する
    *
    * @param obj グループ図形
    * @return グループ化されていた図形のリスト
    */
    List<DesignObject> solveGroup(ShapeGroup obj) {
        int index = this.indexOf(obj);
        this.list.remove(obj);
        List<DesignObject> parts = Util.newArrayList();
        for (int i = 0; i < obj.count(); i++) {
            DesignObject part = obj.getObject(i);
            this.list.add(index + i, part);
            parts.add(part);
        }
        return parts;
    }

    boolean loadSVG(ImportSVG handler, String path) {
        List<DesignObject> list = handler.importFile(path);
        if (null != list) {
            this.list = list;
            this.file = handler.getFile();
            this.pageInfo = handler.getPageInfo();
            return true;
        }
        return false;
    }

    void importSVG(ImportSVG handler) {
        List<DesignObject> list = handler.importFile(null);
        if (null != list) {
            this.list.addAll(list);
            changed();
        }
    }

    void export(ExportBase export) {
        for (int i = 0; i < this.list.size(); i++) {
            DesignObject obj = (DesignObject) this.list.get(i);
            obj.export(export);
        }
    }

    void export(ExportBase export, List<DesignObject> selObjList) {
        for (int i = 0; i < this.list.size(); i++) {
            DesignObject obj = (DesignObject) this.list.get(i);
            if (selObjList.contains(obj)) {
                obj.export(export);
            }
        }
    }

    private static enum FORMAT {
        PNG,
        JPEG,
        BMP;

        public String toString() {
            return this.name().toLowerCase();
        }
    }

    /**
    * 画像ファイルに保存する
    *
    * @param objList
    * @param w 画像の幅
    * @param h 画像の高さ
    */
    void exportAsImage(Collection<DesignObject> objList, int w, int h) {
        FileNameExtensionFilter[] extList = new FileNameExtensionFilter[FORMAT.values().length];
        FORMAT[] fs = FORMAT.values();
        for (int i = 0; i < fs.length; i++) {
            FORMAT f = fs[i];
            String name = String.format("%sファイル(*.%s)", f.toString(), f.toString());
            extList[i] = new FileNameExtensionFilter(name, f.toString());
        }

        String name = null;
        if (null != this.file) {
            name = this.file.getName();
            String[] tokens = name.split("\\.");
            if (1 < tokens.length) {
                name = "";
                for (int i = 0; i < (tokens.length - 1); i++) {
                    if (false == name.isEmpty()) {
                        name += ".";
                    }
                    name += tokens[i];
                }
            }
        }
        File file = Util.selectFile(name, null, extList, true);
        String[] tokens = file.getName().split("\\.");
        String ext = tokens[tokens.length - 1];

        BufferedImage image = makeImage(FORMAT.valueOf(ext.toUpperCase()), objList, w, h);
        try {
            ImageIO.write(image, ext, file);
        } catch (IOException e) {
            Util.exception(e, "部品を画像ファイルに保存できませんでした");
        }
    }

    void copyAsImage(Collection<DesignObject> objList, int w, int h) {
        BufferedImage image = makeImage(FORMAT.PNG, objList, w, h);

        Util.setToClipboard(image);
    }

    private BufferedImage makeImage(FORMAT type, Collection<DesignObject> objList, int w, int h) {
        Point dp = null;
        if (null == objList) {
            if (0 >= w || 0 >= h) {
                throw new IllegalArgumentException("画像サイズが不正です");
            }
            objList = this.list;
        } else {
            Rectangle bound = null;
            for (DesignObject obj : objList) {
                Rectangle rect = obj.getBounds();
                int ext = Util.mmToPixel(0, Config.getConfig(obj.getDotSize()));
                rect.x -= ext / 2;
                rect.y -= ext / 2;
                rect.width += ext;
                rect.height += ext;

                if (null == bound) {
                    bound = rect;
                } else {
                    bound.add(rect);
                }
            }
            w = bound.width;
            h = bound.height;
            dp = new Point(-bound.x, -bound.y);
        }

        BufferedImage image;
        if (type == FORMAT.PNG) {
            image = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
        } else {
            image = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        }
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, w, h);
        for (DesignObject obj : objList) {
            DesignObject o = obj.clone();
            if (null != dp) {
                o.move(null, dp);
            }
            o.draw(g, DesignObject.DRAW_MODE.PRINT);
        }

        return image;
    }
}
