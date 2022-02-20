package dssp.hashidate.config;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dssp.brailleLib.Util;
import dssp.brailleLib.XmlUtil;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class Config {
    private static final String FILENAME = "config.xml";
    private static final String PATH_ROOT = "CONFIG";
    private static final String NODE_UPDATE = "UPDATE";
    private static final String PATH_UPDATE = String.format("%s/%s", PATH_ROOT, NODE_UPDATE);
    private static final Config instance = new Config();
    private static boolean shouldUpdate = false;

    public static String HELP_FILE = "help/index.html";

    private List<ConfigBase> configlist = Arrays.asList(
            ConfigColor.getInstance(),
            ConfigFont.getInstance(),
            ConfigPage.getInstance(),
            ConfigBraille.getInstance(),
            ConfigBPLOT.getInstance(),
            ConfigMathML.getInstance(),
            ConfigMisc.getInstance());

    private static ConfigDlg dlg = null;

    public interface CONFIG_TYPE {
        ConfigBase getObj();
    }

    /**
    * 色の設定
    * ・getConfig()の返り値、setConfig()の値はColor
    *
    * @author yagi
    *
    */
    public enum COLOR implements CONFIG_TYPE {
        /**
        * 線の色<br>
        */
        LINE,
        /**
        * 内部の色<br>
        */
        PAINT,
        /**
        * 選択枠の色<br>
        */
        FRAME,
        /**
        * 点字枠の色<br>
        */
        BRAILLE;

        private final ConfigBase obj = ConfigColor.getInstance();

        @Override
        public ConfigBase getObj() {
            return obj;
        }
    }

    public enum LINE implements CONFIG_TYPE {
        /**
        * 線種<br>
        */
        TYPE,
        /**
        * 線幅<br>
        */
        WIDTH,
        /**
        * 端点の形<br>
        */
        EDGE;

        private final ConfigBase obj = ConfigColor.getInstance();

        @Override
        public ConfigBase getObj() {
            return obj;
        }
    }

    public enum FONT implements CONFIG_TYPE {
        /**
        * テキストのフォント<br>
        * ・getConfig()の返り値、setConfig()の値はFont
        */
        TEXT,
        /**
        * 数式のフォント<br>
        * ・getConfig()の返り値、setConfig()の値はFont
        */
        FORMULA;

        private final ConfigBase obj = ConfigFont.getInstance();

        @Override
        public ConfigBase getObj() {
            return obj;
        }
    }

    public enum PAGE implements CONFIG_TYPE {
        /**
        * 現在のページ情報<br>
        * ・getConfig()の返り値、setConfig()の値はPageInfo
        */
        CURRENT,
        /**
        * ページのリスト<br>
        * ・getConfig()の返り値、setConfig()の値はList<PageInfo>
        */
        LIST;

        private final ConfigBase obj = ConfigPage.getInstance();

        @Override
        public ConfigBase getObj() {
            return obj;
        }
    }

    //    public enum SVG implements CONFIG_TYPE
    //    {
    //        /**
    //         * 数式(フォントがPLAIN)のSVG出力時の縮小率<br>
    //         * ・getConfig()の返り値、setConfig()の値はPoint2D.Double
    //         */
    //        PLAIN,
    //        /**
    //         * 数式(フォントがITALIC)のSVG出力時の縮小率<br>
    //         * ・getConfig()の返り値、setConfig()の値はPoint2D.Double
    //         */
    //        ITALIC;
    //
    //        private final ConfigBase obj = ConfigMisc.getInstance();
    //
    //        @Override
    //        public ConfigBase getObj()
    //        {
    //            return obj;
    //        }
    //    }

    public enum BRAILLE implements CONFIG_TYPE {
        /**
        * 点字の辞書(テキスト用)<br>
        * ・getConfig()の返り値はBrailleDict<br>
        * ・setConfig()の値はString(辞書のファイル名)
        */
        DICT_TEXT,
        /**
        * 点字の辞書(数式用)<br>
        * ・getConfig()の返り値はBrailleDict<br>
        * ・setConfig()の値はString(辞書のファイル名)
        */
        DICT_FORMULA,
        /**
        * 点字の点のサイズ：小さい点<br>
        * ・getConfig()の返り値、setConfig()の値は実寸[0.1mm単位]
        */
        SMALL,
        /**
        * 点字の点のサイズ：普通の点<br>
        * ・getConfig()の返り値、setConfig()の値は実寸[0.1mm単位]
        */
        MIDDLE,
        /**
        * 点字の点のサイズ：大きい点<br>
        * ・getConfig()の返り値、setConfig()の値は実寸[0.1mm単位]
        */
        LARGE,
        /**
        * 点字の点の横間隔<br>
        * ・getConfig()の返り値、setConfig()の値は実寸[0.1mm単位]
        */
        SPACE_X,
        /**
        * 点字の点の縦間隔<br>
        * ・getConfig()の返り値、setConfig()の値は実寸[0.1mm単位]
        */
        SPACE_Y,
        /**
        * 行の間隔<br>
        * ・getConfig()の返り値、setConfig()の値は実寸[0.1mm単位]
        */
        LINE_SPACE,
        /**
        * マスの横間隔<br>
        * ・getConfig()の返り値、setConfig()の値は実寸[0.1mm単位]
        */
        BOX_SPACE,
        /**
        * 点図の点の間隔<br>
        * ・getConfig()の返り値、setConfig()の値は実寸[0.1mm単位]
        */
        DOT_SPAN,
        /**
        * 点字の点のサイズ<br>
        * ・getConfig()の返り値はSMALL, MIDDLE, LARGE
        */
        BRAILLE,
        /**
        * 点図の点のサイズ<br>
        * ・getConfig()の返り値はSMALL, MIDDLE, LARGE
        */
        FIGURE,
        /**
        * テキストを点字に変換するか<br>
        * ・getConfig()の返り値はBoolean
        */
        BRAILLE_TEXT;

        private final ConfigBase obj = ConfigBraille.getInstance();

        @Override
        public ConfigBase getObj() {
            return obj;
        }
    }

    public enum MATHML implements CONFIG_TYPE {
        /**
        * MathMLのエイリアス変換用辞書ファイル名
        */
        ALIAS;

        private final ConfigBase obj = ConfigMathML.getInstance();

        @Override
        public ConfigBase getObj() {
            return obj;
        }
    }

    public enum BPLOT implements CONFIG_TYPE {
        /**
        * A4用紙の大きさ[mm単位]<br>
        * ・getConfig()の返り値はDimension
        */
        PAGE_A4,
        /**
        * B5用紙の大きさ[mm単位]<br>
        * ・getConfig()の返り値はDimension
        */
        PAGE_B5,
        /**
        * 用紙枠を表示するか<br>
        * ・getConfig()の返り値はboolean
        */
        SHOW_PAGE_FRAME,
        /**
        * 用紙枠の色<br>
        * ・getConfig()の返り値はColor
        */
        PAGE_FRAME_COLOR,
        /**
        * 裏点の横位置補正
        * ・getConfig()の返り値はdouble
        */
        MIRROR,
        /**
        * BPLOT出力時に上下反転するか<br>
        * ・getConfig()の返り値はboolean
        */
        UPSIDEDOWN,
        /**
        * ファイルに出力するか
        * ・getConfig()の返り値はboolean
        */
        OUTPUT_FILE,
        /**
        * BPLOTコマンドのパス
        * ・getConfig()の返り値はString
        */
        EXEPATH,
        /**
        * プリンタ名
        */
        PRINTER,
        /**
        * 裏点を先に出力するか
        * ・getConfig()の返り値はboolean
        */
        DOWNSIDEFIRST,
        /**
        * テキスト、数式にNABCCを出力するか
        * ・getConfig()の返り値はboolean
        */
        USE_NABCC;

        private final ConfigBase obj = ConfigBPLOT.getInstance();

        @Override
        public ConfigBase getObj() {
            return obj;
        }
    }

    public enum MISC implements CONFIG_TYPE {
        /**
        * 矢印キーでの移動量<br>
        * ・getConfig()の返り値、setConfig()の値はInteger
        */
        MOVE_STEP(ConfigMisc.getInstance()),
        /**
        * 数式(フォントがPLAIN)のSVG出力時の縮小率<br>
        * ・getConfig()の返り値、setConfig()の値はPoint2D.Double
        */
        PLAIN(ConfigMisc.getInstance()),
        /**
        * 数式(フォントがITALIC)のSVG出力時の縮小率<br>
        * ・getConfig()の返り値、setConfig()の値はPoint2D.Double
        */
        ITALIC(ConfigMisc.getInstance());

        private final ConfigBase obj;

        private MISC(ConfigBase obj) {
            this.obj = obj;
        }

        @Override
        public ConfigBase getObj() {
            return obj;
        }
    }

    private Config() {
    }

    private static void initDlg() {
        if (null != Config.dlg) {
            for (ConfigBase obj : Config.instance.configlist) {
                obj.resetPanel();
            }
        } else {
            Config.dlg = new ConfigDlg();
            for (ConfigBase obj : Config.instance.configlist) {
                JPanel panel = obj.getPanel();

                try {
                    javax.swing.UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    javax.swing.SwingUtilities.updateComponentTreeUI(panel);
                } catch (Exception e) {
                    Util.logException(e);
                }

                Config.dlg.addPane(obj.getTitle(), panel);
            }
        }

        Util.setLocationUnderMouse(Config.dlg);
    }

    public static void showDialg() {
        initDlg();

        boolean isUpdated = false;
        if (Config.dlg.showDialog()) {
            for (ConfigBase obj : Config.instance.configlist) {
                obj.update();
                isUpdated = (isUpdated || obj.isUpdated());
            }
        }
        if (isUpdated) {
            Config.save();
        }
    }

    public static <T> T getConfig(CONFIG_TYPE key) {
        ConfigBase conf = key.getObj();
        if (null == conf) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T obj = (T) conf.getConfig(key);
        return obj;
    }

    //    public static <T> void setConfig(CONFIG_TYPE key, T val)
    //    {
    //        key.getObj().setConfig(key, val);
    //    }

    /**
    * 設定の基本クラス
    *
    * @author yagi
    *
    */
    public static abstract class ConfigBase {
        /**
        * 設定を読み込む
        *
        * @param doc Document
        */
        abstract void load(Document doc);

        /**
        * 設定を保存する<br>
        * ・保存したらisUpdatedをfaluseにする
        *
        * @param doc Document
        */
        abstract void save(Document doc);

        /**
        * 設定値を取得する
        *
        * @param key
        * @return
        */
        abstract <T> T getConfig(CONFIG_TYPE key);

        //        /**
        //         * 設定値を登録する
        //         *
        //         * @param key キー
        //         * @param val 値
        //         * @throws IllegalArgumentException 値のクラスが不適切
        //         */
        //        abstract <T> void setConfig(CONFIG_TYPE key, T val);
        /**
        * 設定値を更新する<br>
        * ・更新したらisUpdatedとtrueにする
        */
        abstract void update();

        /**
        * 設定タブの名前を取得する
        *
        * @return 名前
        */
        abstract String getTitle();

        /**
        * 設定画面のPanelを取得する
        *
        * @return JPanel
        */
        abstract JPanel getPanel();

        /**
        * 設定画面のPanelをリセットする
        */
        abstract void resetPanel();

        protected boolean isUpdated = false;

        public boolean isUpdated() {
            return this.isUpdated;
        }
    }

    public static void load() {
        Config.instance.loadConfig();
    }

    public static void save() {
        Config.instance.saveConfig();
    }

    static void requireUpdate() {
        Throwable obj = new Throwable();
        StackTraceElement[] trace = obj.getStackTrace();
        String line = String.format("required at %s:%s", trace[2].getFileName(), trace[2].getLineNumber());
        Util.logInfo(line);

        shouldUpdate = true;
    }

    private void loadConfig() {
        File file = new File(Util.exePath(Config.FILENAME));
        if (false == file.exists()) {
            Util.warning("設定ファイル " + FILENAME + " を読めませんでした。既定値で起動します。");
        }
        Util.logInfo("設定ファイル " + file.getAbsolutePath());

        try {
            Document document = XmlUtil.parse(file);

            for (ConfigBase config : this.configlist) {
                config.load(document);
            }

            if (false == file.exists() || Config.shouldUpdate) {
                saveConfig();
            }
        } catch (Exception e) {
            Util.logException(e);
        }
    }

    private void saveConfig() {
        try {
            File file = new File(Util.exePath(Config.FILENAME));

            Document document = XmlUtil.createDocument(null, PATH_ROOT);

            Element root = document.getDocumentElement();

            Element node = (Element) XmlUtil.getNode(document, PATH_UPDATE);
            if (null == node) {
                node = document.createElement(NODE_UPDATE);
                root.appendChild(node);
            }
            String comment = Util.now();
            node.setTextContent(comment);
            ;

            for (ConfigBase config : this.configlist) {
                config.save(document);
            }

            XmlUtil.write(document, file);

            Util.logInfo("Configuration is updated.");
            Config.shouldUpdate = false;
        } catch (Exception e) {
            Util.logException(e);
        }
    }

    static double loadDouble(Document doc, String path, double defVal) {
        double val = defVal;
        try {
            String text = XmlUtil.getString(doc, path);

            if (null == text || 0 == text.length()) {
                Config.requireUpdate();
            } else {
                val = Double.parseDouble(text);
            }
            Util.logInfo(path + "=" + val);
        } catch (Exception e) {
            Util.logException(e);
        }

        return val;
    }

    static int loadInt(Document doc, String path, int defVal) {
        int val = defVal;
        try {
            String text = XmlUtil.getString(doc, path);

            if (null == text || 0 == text.length()) {
                Config.requireUpdate();
            } else {
                val = Integer.parseInt(text);
            }
            Util.logInfo(path + "=" + val);
        } catch (Exception e) {
            Util.logException(e);
        }

        return val;
    }

    static String loadString(Document doc, String path, String defVal) {
        String val = defVal;
        try {
            String text = XmlUtil.getString(doc, path);

            if (null == text || 0 == text.length()) {
                Config.requireUpdate();
            } else {
                val = text;
            }
            Util.logInfo(path + "=" + val);
        } catch (Exception e) {
            Util.logException(e);
        }

        return val;
    }

    static boolean loadBoolean(Document doc, String path, boolean defVal) {
        boolean val = defVal;
        try {
            String text = XmlUtil.getString(doc, path);

            if (null == text || 0 == text.length()) {
                Config.requireUpdate();
            } else {
                val = Boolean.parseBoolean(text);
            }
            Util.logInfo(path + "=" + val);
        } catch (Exception e) {
            Util.logException(e);
        }

        return val;
    }

    static Color parseColor(String text) {
        if (6 > text.length()) {
            return new Color(0, 0, 0);
        }

        int[] pval = new int[3];
        for (int j = 0; j < text.length(); j += 2) {
            String pix = text.substring(j, j + 2);
            pval[j / 2] = Integer.parseInt(pix, 16);
        }
        Color color = new Color(pval[0], pval[1], pval[2]);

        return color;
    }

    static Color loadColor(Document doc, String path, String defVal) {
        Color color = parseColor(defVal);
        try {
            String val = XmlUtil.getString(doc, path);

            if (null == val || 0 == val.length()) {
                val = defVal;
                Config.requireUpdate();
            }
            color = parseColor(val);
            Util.logInfo(path + "=" + val);
        } catch (Exception e) {
            Util.logException(e);
        }
        return color;
    }
}
