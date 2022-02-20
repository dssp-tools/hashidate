package dssp.hashidate.shape.helper;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import dssp.brailleLib.Util;
import dssp.brailleLib.XmlUtil;
import net.sourceforge.jeuclid.MutableLayoutContext;
import net.sourceforge.jeuclid.context.LayoutContextImpl;
import net.sourceforge.jeuclid.context.Parameter;
import net.sourceforge.jeuclid.layout.JEuclidView;
import uk.ac.ed.ph.snuggletex.SerializationMethod;
import uk.ac.ed.ph.snuggletex.SessionConfiguration;
import uk.ac.ed.ph.snuggletex.SnuggleEngine;
import uk.ac.ed.ph.snuggletex.SnuggleInput;
import uk.ac.ed.ph.snuggletex.SnuggleSession;
import uk.ac.ed.ph.snuggletex.XMLStringOutputOptions;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public class FormulaHandler {
    private static final FormulaHandler instance = new FormulaHandler();

    private final Map<String, String> codeMap = Util.newHashMap();
    private final StringBuffer buf = new StringBuffer();
    private final Pattern pattern = Pattern.compile("&.+?;");

    private FormulaHandler() {
    }

    public static FormulaHandler getInstance() {
        return instance;
    }

    /**
    * Fontのスタイル値に応じた文字列を取得する
    *
    * @param fontStyle Fontのスタイル値
    * @return 文字列
    */
    public String getFontStyleText(int fontStyle) {
        String style = "normal";
        if (0 != (fontStyle & Font.ITALIC)) {
            style = "italic";
            if (0 != (fontStyle & Font.BOLD)) {
                style = "bold-italic";
            }
        } else {
            if (0 != (fontStyle & Font.BOLD)) {
                style = "bold";
            }
        }

        return style;
    }

    /**
    * TeXをMathMLに変換する
    *
    * @param text TeX
    * @param indent true=インデントする
    * @param fontFamily フォント名
    * @param fontSize フォントサイズ
    * @param fontStyle フォントスタイル
    * @return MathMLの文字列 null=入力がTeXではないなどで変換できない場合
    */
    public String TeXtoMathML(String text, boolean indent, String fontFamily, int fontSize, int fontStyle) {
        text = text.trim();
        if (text.startsWith("<math")) {
            return text;
        }
        text = String.format("$%s$", text);

        String xmlString = null;
        //        // fMathで変換
        //        try
        //        {
        //            xmlString = ConvertFromLatexToMathML.convertToMathML(text);
        //        }
        //        catch (Exception ex)
        //        {
        //            Util.logException(ex);
        //            return null;
        //        }
        //
        //        try
        //        {
        //            xmlString = this.translatetAlias(xmlString);
        //
        //            Document doc = XmlUtil.parse(xmlString);
        //
        //            Element math = doc.getDocumentElement();
        //            math.setAttribute("xmlns", "http://www.w3.org/1998/Math/MathML");
        //            math.setAttribute("mathsize", String.format("%dpt", fontSize));
        //            math.setAttribute("fontFamily", fontFamily);
        //            math.setAttribute("mathvariant", this.getFontStyleText(fontStyle));
        //
        //            xmlString = XmlUtil.getXmlText(doc);
        //        }
        //        catch (Exception ex)
        //        {
        //            Util.logException(ex);
        //        }
        // SnuggleTeXで変換
        SnuggleEngine engine = new SnuggleEngine();
        SnuggleSession session = engine.createSession();
        SessionConfiguration conf = session.getConfiguration();
        conf.setFailingFast(true);

        /* Parse some LaTeX input */
        //        SnuggleInput input = new SnuggleInput("\\section*{The quadratic formula}"+ "$$ \\frac{-b \\pm \\sqrt{b^2-4ac}}{2a} $$");
        SnuggleInput input = new SnuggleInput(text);
        try {
            if (false == session.parseInput(input)) {
                // fMathで変換
                //                try {
                //                    xmlString = ConvertFromLatexToMathML.convertToMathML(text);
                //                } catch (Exception ex) {
                //                    Util.logException(ex);
                //                    return null;
                //                }
                //
                //                try {
                //                    xmlString = this.translatetAlias(xmlString);
                //
                //                    Document doc = XmlUtil.parse(xmlString);
                //
                //                    Element math = doc.getDocumentElement();
                //                    math.setAttribute("xmlns", "http://www.w3.org/1998/Math/MathML");
                //                    math.setAttribute("mathsize", String.format("%dpt", fontSize));
                //                    math.setAttribute("fontFamily", fontFamily);
                //                    math.setAttribute("mathvariant", this.getFontStyleText(fontStyle));
                //
                //                    xmlString = XmlUtil.getXmlText(doc);
                //
                //                    return xmlString;
                //                } catch (Exception ex) {
                //                    Util.logException(ex);
                //                }
                return null;
            }

            /* Specify how we want the resulting XML */
            XMLStringOutputOptions options = new XMLStringOutputOptions();
            options.setSerializationMethod(SerializationMethod.XHTML);
            options.setIndenting(true);
            options.setEncoding("UTF-8");
            options.setAddingMathSourceAnnotations(true);
            options.setUsingNamedEntities(true); /* (Only used if caller has an XSLT 2.0 processor) */

            /* Convert the results to an XML String, which in this case will
            * be a single MathML <math>...</math> element. */
            xmlString = session.buildXMLString(options);

            Document doc = XmlUtil.parse(xmlString);

            Element math = doc.getDocumentElement();
            math.setAttribute("xmlns", "http://www.w3.org/1998/Math/MathML");
            math.setAttribute("mathsize", String.format("%dpt", fontSize));
            math.setAttribute("fontFamily", fontFamily);
            math.setAttribute("mathvariant", this.getFontStyleText(fontStyle));

            xmlString = XmlUtil.getXmlText(doc);
        } catch (SAXException e) {
            return null;
        } catch (IOException | TransformerException | ParserConfigurationException e) {
            Util.logException(e);
            return null;
        }

        return xmlString;
    }

    String translatetAlias(String text) {
        int end = 0;
        AliasResolver resolver = AliasResolver.getInstance();

        this.codeMap.clear();
        Matcher matcher = this.pattern.matcher(text);
        while (matcher.find(end)) {
            end = matcher.end();
            String alias = matcher.group();
            if (false == this.codeMap.containsKey(alias)) {
                String aliasText = alias.replaceAll("[&;]", "");
                String[] list = resolver.getUnicode(aliasText);
                if (null != list) {

                    this.buf.delete(0, this.buf.length());
                    for (String unicode : list) {
                        if (null != unicode && false == unicode.isEmpty()) {
                            this.buf.append(String.format("&#x%s;", unicode));
                        }
                    }

                    this.codeMap.put(alias, this.buf.toString());
                }
            }
        }
        for (String alias : this.codeMap.keySet()) {
            text = text.replace(alias, this.codeMap.get(alias));
        }

        return text;
    }

    /**
    * MathMLの数式い画像を取得する<br>
    *
    * getImageForMathML(String text, String fontFamily, Point2D.Double scale)で拡大率に縦、横1をしてした場合と同じ
    *
    * @param text MathML
    * @param fontFamily フォント名
    * @return 数式の画像
    * @see getImageForMathML(String text, String fontFamily, Point2D.Double scale)
    */
    public BufferedImage getImageForMathML(String text, String fontFamily) {
        return this.getImageForMathML(text, fontFamily, new Point2D.Double(1, 1));
    }

    /**
    * MathMLの数式い画像を取得する
    *
    * @param text MathML
    * @param fontFamily フォント名
    * @param scale 拡大率(横、縦)
    * @return 数式の画像
    */
    public BufferedImage getImageForMathML(String text, String fontFamily, Point2D.Double scale) {
        BufferedImage image = null;
        try {
            //            Document doc = XmlUtil.parse(text);
            //
            //            MutableLayoutContext context = new LayoutContextImpl(LayoutContextImpl.getDefaultLayoutContext());
            //            List<String> list = new Vector<String>();
            //            list.add(fontFamily);
            //            context.setParameter(Parameter.FONTS_SERIF, list);
            //
            //            Converter converter = Converter.getInstance();
            //            image = converter.render(doc, context);

            text = this.translatetAlias(text);

            Document node = XmlUtil.parse(text);
            int imageType = BufferedImage.TYPE_INT_ARGB;
            int MAX_RGB_VALUE = 255;

            MutableLayoutContext context = new LayoutContextImpl(LayoutContextImpl.getDefaultLayoutContext());
            List<String> list = new ArrayList<>();
            list.add(fontFamily);
            context.setParameter(Parameter.FONTS_SERIF, list);

            final Image tempimage = new BufferedImage(1, 1, imageType);
            final Graphics2D tempg = (Graphics2D) tempimage.getGraphics();

            JEuclidView view = new JEuclidView(node, context, tempg);

            final int width = Math.max(1, (int) Math.ceil(view.getWidth() * scale.x));
            final int ascent = (int) Math.ceil(view.getAscentHeight());
            double dh = (Math.ceil(view.getDescentHeight()) + ascent) * scale.y;
            final int height = Math.max(1, (int) dh);

            image = new BufferedImage(width, height, imageType);
            final Graphics2D g3 = image.createGraphics();

            final Color background;
            if (image.getColorModel().hasAlpha()) {
                background = new Color(MAX_RGB_VALUE,
                        MAX_RGB_VALUE, MAX_RGB_VALUE, 0);
            } else {
                background = Color.WHITE;
            }
            g3.setColor(background);
            g3.fillRect(0, 0, width, height);
            g3.setColor(Color.black);

            AffineTransform at = AffineTransform.getScaleInstance(scale.x, scale.y);
            g3.setTransform(at);
            view.draw(g3, 0, ascent);
        } catch (Exception ex) {
            Util.logException(ex);
            Util.logInfo(text);
        }

        return image;
    }

}
