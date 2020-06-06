package dssp.hashidate.io;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGOMForeignObjectElement;
import org.apache.batik.dom.svg.SVGOMImageElement;
import org.apache.batik.dom.svg.SVGOMTextElement;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.svg.SVGCircleElement;
import org.w3c.dom.svg.SVGDescElement;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGEllipseElement;
import org.w3c.dom.svg.SVGGElement;
import org.w3c.dom.svg.SVGLineElement;
import org.w3c.dom.svg.SVGPathElement;
import org.w3c.dom.svg.SVGPolygonElement;
import org.w3c.dom.svg.SVGPolylineElement;
import org.w3c.dom.svg.SVGRectElement;
import org.w3c.dom.svg.SVGSVGElement;

import dssp.brailleLib.Util;
import dssp.brailleLib.XmlUtil;
import dssp.hashidate.config.Config;
import dssp.hashidate.config.PageInfo;
import dssp.hashidate.misc.Pair;
import dssp.hashidate.misc.PairList;
import dssp.hashidate.shape.DesignObject;
import dssp.hashidate.shape.SHAPE;
import dssp.hashidate.shape.ShapeCircle;
import dssp.hashidate.shape.ShapeGraph;
import dssp.hashidate.shape.ShapeGroup;
import dssp.hashidate.shape.ShapeImage;
import dssp.hashidate.shape.ShapePolyline;
import dssp.hashidate.shape.ShapeRectangle;
import dssp.hashidate.shape.ShapeRegularPolygon;
import dssp.hashidate.shape.ShapeSpline;
import dssp.hashidate.shape.ShapeSpring;
import dssp.hashidate.shape.helper.ObjectFactory;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class ImportSVG {
    private List<DesignObject> objList = Util.newArrayList();
    private PageInfo pageInfo = null;
    private File file = null;

    private SVGDocument document;

    public List<DesignObject> importFile(String path) {
        if (null == path || false == Files.exists(FileSystems.getDefault().getPath(path))) {
            FileNameExtensionFilter filter = new FileNameExtensionFilter("SVG ファイル(*.svg)", "svg");
            FileNameExtensionFilter[] list = { filter };
            this.file = Util.selectFile(null, "*", list, false);
            if (null == this.file) {
                return null;
            }
        } else {
            this.file = new File(path);
        }

        try (FileInputStream is = new FileInputStream(this.file)) {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
            document = f.createSVGDocument(null, is);
            SVGSVGElement top = document.getRootElement();

            int w = (int) Math.round(Util.pixelToMM(0, (int) top.getWidth().getBaseVal().getValue()) / 10.0);
            int h = (int) Math.round(Util.pixelToMM(0, (int) top.getHeight().getBaseVal().getValue()) / 10.0);
            this.pageInfo = new PageInfo(null, w, h);

            parse(top, null);
        } catch (Exception e) {
            Util.logException(e);
            return null;
        }

        return this.objList;
    }

    public File getFile() {
        return this.file;
    }

    public PageInfo getPageInfo() {
        return this.pageInfo;
    }

    private String getClassName(Node node) {
        NodeList children = node.getChildNodes();
        int count = children.getLength();
        for (int i = 0; i < count; i++) {
            Node child = children.item(i);
            if (child instanceof SVGDescElement) {
                String val = ((SVGDescElement) child).getAttribute(ExportSVG.CLASS_ATTR);
                if (0 == val.compareToIgnoreCase(ExportSVG.CLASS_ATTR_VAL)) {
                    String text = child.getTextContent();
                    if (text.startsWith("class")) {
                        String[] pairs = text.split("\\|");
                        Pair pair = Pair.fromString(pairs[0]);
                        return (String) pair.getValue();
                    }
                    return text;
                }
            }
        }

        return null;
    }

    private String getDescText(Node node) {
        NodeList children = node.getChildNodes();
        int count = children.getLength();
        for (int i = 0; i < count; i++) {
            Node child = children.item(i);
            if (child instanceof SVGDescElement) {
                String val = ((SVGDescElement) child).getAttribute(ExportSVG.CLASS_ATTR);
                if (0 == val.compareToIgnoreCase(ExportSVG.CLASS_ATTR_VAL)) {
                    return child.getTextContent();
                }
            }
        }

        return null;
    }

    private DesignObject parseRectangle(SVGRectElement node) {
        ShapeInfo info = new ShapeInfo(SHAPE.RECTANGLE);
        info.desc = getDescText(node);
        info.x = (int) node.getX().getBaseVal().getValue();
        info.y = (int) node.getY().getBaseVal().getValue();
        info.width = (int) node.getWidth().getBaseVal().getValue();
        info.height = (int) node.getHeight().getBaseVal().getValue();
        ObjectFactory factory = ObjectFactory.getInstance();
        DesignObject obj = factory.buildObject(SHAPE.RECTANGLE, info);

        return obj;
    }

    private DesignObject parseCircle(SVGCircleElement node) {
        ShapeInfo info = new ShapeInfo(SHAPE.CIRCLE);
        info.desc = getDescText(node);
        int cx = (int) node.getCx().getBaseVal().getValue();
        int cy = (int) node.getCy().getBaseVal().getValue();
        int r = (int) node.getR().getBaseVal().getValue();
        info.x = cx;
        info.y = cy;
        info.width = r * 2;
        info.height = r * 2;
        ObjectFactory factory = ObjectFactory.getInstance();
        DesignObject obj = factory.buildObject(SHAPE.CIRCLE, info);
        return obj;
    }

    private DesignObject parseEllipse(SVGEllipseElement node) {
        ShapeInfo info = new ShapeInfo(SHAPE.ELLIPSE);
        info.desc = getDescText(node);
        int cx = (int) node.getCx().getBaseVal().getValue();
        int cy = (int) node.getCy().getBaseVal().getValue();
        int rx = (int) node.getRx().getBaseVal().getValue();
        int ry = (int) node.getRy().getBaseVal().getValue();
        info.x = cx;
        info.y = cy;
        info.width = rx * 2;
        info.height = ry * 2;
        ObjectFactory factory = ObjectFactory.getInstance();
        DesignObject obj = factory.buildObject(SHAPE.ELLIPSE, info);
        return obj;
    }

    private DesignObject parsePathSVG(SVGElement node) {
        //        SVGPathSegList segList = node.getPathSegList();
        //        int count = segList.getNumberOfItems();
        //        ShapePolyline obj = null;
        //        obj = new ShapePolyline();
        //        if (2 > count)
        //        {
        //            return null;
        //        }
        //        for (int i = 0; i < count; i++)
        //        {
        //            SVGPathSeg seg = segList.getItem(i);
        //            switch(seg.getPathSegType())
        //            {
        //            case SVGPathSeg.PATHSEG_MOVETO_ABS:
        //                SVGPathSegMovetoAbs moveTo = (SVGPathSegMovetoAbs)seg;
        //                obj.add((int)moveTo.getX(), (int)moveTo.getY());
        //                break;
        //            case SVGPathSeg.PATHSEG_LINETO_ABS:
        //                SVGPathSegLinetoAbs lineTo = (SVGPathSegLinetoAbs)seg;
        //                obj.add((int)lineTo.getX(), (int)lineTo.getY());
        //            }
        //        }
        //        return obj;

        //        ShapeCircle obj = new ShapeCircle();
        //        SVGPathSegList segList = node.getPathSegList();
        //        int count = segList.getNumberOfItems();
        //        for (int i = 0; i < count; i++)
        //        {
        //            SVGPathSeg seg = segList.getItem(i);
        //            switch(seg.getPathSegType())
        //            {
        //            case SVGPathSeg.PATHSEG_MOVETO_ABS:
        //                SVGPathSegMovetoAbs moveTo = (SVGPathSegMovetoAbs)seg;
        //                obj.frame.x = (int)moveTo.getX();
        //                obj.frame.y = (int)moveTo.getY();
        //                break;
        //            case SVGPathSeg.PATHSEG_ARC_ABS:
        //                SVGPathSegArcAbs arc = (SVGPathSegArcAbs)seg;
        //                int r1 = (int)arc.getR1();
        //                int r2 = (int)arc.getR2();
        //                obj.frame.width = r1*2;
        //                obj.frame.height = r2*2;
        //            }
        //        }
        //        return obj;

        return null;
    }

    private DesignObject parsePath(SVGPathElement node) {
        String desc = getDescText(node);
        if (null == desc || desc.isEmpty()) {
            return null;
        }
        String className;
        if (desc.startsWith("class=")) {
            PairList list = PairList.fromString(desc);
            List<Object> vals = list.getValues("class");
            className = (String) vals.get(0);
        } else {
            className = desc;
        }

        DesignObject obj = null;
        ShapeInfo info = null;
        if (null == className) {
            info = new ShapeInfo(SHAPE.POLYLINE);
            info.desc = desc;
            obj = parsePathSVG(node);
        } else if (className.startsWith(ShapeRectangle.class.getSimpleName())) {
            info = new ShapeInfo(SHAPE.RECTANGLE);
            info.desc = desc;
            ObjectFactory factory = ObjectFactory.getInstance();
            obj = factory.buildObject(SHAPE.RECTANGLE, info);
        } else if (className.startsWith(ShapePolyline.class.getSimpleName())) {
            info = new ShapeInfo(SHAPE.POLYLINE);
            info.desc = desc;
            ObjectFactory factory = ObjectFactory.getInstance();
            obj = factory.buildObject(SHAPE.POLYLINE, info);
        } else if (className.startsWith(ShapeRegularPolygon.class.getSimpleName())) {
            info = new ShapeInfo(SHAPE.REGULAR_POLYGON);
            info.desc = desc;
            ObjectFactory factory = ObjectFactory.getInstance();
            obj = factory.buildObject(SHAPE.REGULAR_POLYGON, info);
        } else if (className.startsWith(ShapeSpline.class.getSimpleName())) {
            info = new ShapeInfo(SHAPE.SPLINE);
            info.desc = desc;
            ObjectFactory factory = ObjectFactory.getInstance();
            obj = factory.buildObject(SHAPE.SPLINE, info);
        } else if (className.startsWith(ShapeSpring.class.getSimpleName())) {
            info = new ShapeInfo(SHAPE.SPRING);
            info.desc = desc;
            ObjectFactory factory = ObjectFactory.getInstance();
            obj = factory.buildObject(SHAPE.SPRING, info);
        } else if (className.startsWith(ShapeCircle.class.getSimpleName())) {
            info = new ShapeInfo(SHAPE.ELLIPSE);
            info.desc = desc;
            ObjectFactory factory = ObjectFactory.getInstance();
            obj = factory.buildObject(SHAPE.ELLIPSE, info);
        }
        if (null == info) {
            return null;
        }

        return obj;
    }

    private DesignObject parsePolygon(SVGPolygonElement node) {
        DesignObject obj = null;
        ShapeInfo info = new ShapeInfo(SHAPE.POLYGON);
        info.desc = getDescText(node);

        if (null == info.desc) {
            obj = this.parsePathSVG(node);
        } else {
            ObjectFactory factory = ObjectFactory.getInstance();
            obj = factory.buildObject(SHAPE.POLYGON, info);
        }

        return obj;
    }

    private DesignObject parseText(SVGOMTextElement node) {
        ShapeInfo info = new ShapeInfo(SHAPE.TEXT);
        info.desc = this.getDescText(node);
        info.setDesc(info.desc);

        info.x = Integer.parseInt(node.getAttribute("x"));
        info.y = Integer.parseInt(node.getAttribute("y"));
        Font font = Config.getConfig(Config.FONT.TEXT);
        String fontFamily = font.getFamily();
        String attr = node.getAttribute("font-family");
        if (null != attr && 0 < attr.length()) {
            fontFamily = attr;
        }

        int fontSize = font.getSize();
        attr = node.getAttribute("font-size");
        if (null != attr && 0 < attr.length()) {
            fontSize = Integer.parseInt(attr);
        }

        String fontStyleText = node.getAttribute("font-style");
        int fontStyle = font.getStyle();
        if (null != fontStyleText) {
            switch (fontStyleText.toLowerCase()) {
            case "italic":
                fontStyle = Font.ITALIC;
                break;
            default:
                fontStyle = Font.PLAIN;
            }
        }

        String fontWeightText = node.getAttribute("font-weight");
        if (null != fontWeightText && 0 == fontStyleText.compareToIgnoreCase("bold")) {
            fontStyle |= Font.BOLD;
        }
        info.font = new Font(fontFamily, fontStyle, fontSize);

        String text = "";
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node child = list.item(i);
            if (child instanceof Text) {
                text += child.getNodeValue();
            } else if (child instanceof Element && child.getNodeName().equals("tspan")) {
                Element elm = (Element) child;
                if (0 < text.length()) {
                    text += "\n";
                }
                text += elm.getTextContent();
            }
        }
        info.text = text;

        ObjectFactory factory = ObjectFactory.getInstance();
        DesignObject obj = factory.buildObject(SHAPE.TEXT, info);

        return obj;
    }

    private DesignObject parseForeinObject(SVGOMForeignObjectElement node) {
        DesignObject obj = null;
        ShapeInfo info = new ShapeInfo(SHAPE.FORMULA);
        info.desc = getDescText(node);
        if (null == info.desc) {
            Util.logInfo("未知のforeObject");
            return null;
        }

        info.x = Integer.parseInt(node.getAttribute("x"));
        info.y = Integer.parseInt(node.getAttribute("y"));

        String text = "";
        Font font = null;
        NodeList list = node.getElementsByTagName("math");
        Element math = (Element) list.item(0);

        try {
            int fontSize = 12;
            font = Config.getConfig(Config.FONT.FORMULA);
            String fontFamily = font.getFamily();
            int fontStyle = Font.PLAIN;
            String attr = math.getAttribute("mathsize").trim();
            if (0 < attr.length()) {
                String[] vals = attr.split("\\D");
                fontSize = Integer.parseInt(vals[0]);
            }
            attr = math.getAttribute("fontfamily");
            if (0 < attr.length()) {
                fontFamily = attr;
            }
            attr = math.getAttribute("mathvariant");
            switch (attr) {
            case "italic":
                fontStyle = Font.ITALIC;
                break;
            case "bold":
                fontStyle = Font.BOLD;
                break;
            case "bold-italic":
                fontStyle = Font.BOLD | Font.ITALIC;
            }
            info.font = new Font(fontFamily, fontStyle, fontSize);

            text = XmlUtil.getXmlText(math);
            int index = text.indexOf("<math");
            info.mathML = text.substring(index);

            Node texNode = searchAnnotation(math);
            if (Objects.nonNull(texNode)) {
                info.text = XmlUtil.getNodeText(texNode).replaceAll("\\$", "");
            } else {
                info.text = info.mathML;
            }
        } catch (Exception ex) {
            Util.logException(ex);
            return null;
        }

        ObjectFactory factory = ObjectFactory.getInstance();
        obj = factory.buildObject(SHAPE.TEXT, info);

        return obj;
    }

    private Node searchAnnotation(Node parent) {
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeName().equals("annotation")) {
                return child;
            }
            child = searchAnnotation(child);
            if (Objects.nonNull(child)) {
                return child;
            }
        }
        return null;
    }

    private DesignObject parseGraph(SVGElement node) {
        ObjectFactory factory = ObjectFactory.getInstance();
        ShapeInfo info = new ShapeInfo(SHAPE.GRAPH);
        info.setDesc(this.getDescText(node));
        ShapeGraph obj = (ShapeGraph) factory.buildObject(SHAPE.GRAPH, info);

        return obj;
    }

    private DesignObject parseImage(SVGOMImageElement node) {
        ObjectFactory factory = ObjectFactory.getInstance();
        ShapeInfo info = new ShapeInfo(SHAPE.IMAGE);
        String desc = this.getDescText(node);
        if (null != desc) {
            info.setDesc(this.getDescText(node));
        }
        PairList list = new PairList();
        String val = node.getHref().getBaseVal();
        list.add("href", val.replace(" ", ""));
        if (null != desc) {
            info.setDesc(this.getDescText(node) + PairList.SEPARATOR + list.toString());
        } else {
            list.add(DesignObject.TAG_BASE.CLASS, ShapeImage.class.getSimpleName());

            int dval = (int) node.getX().getBaseVal().getValue();
            list.add(DesignObject.TAG_BASE.X, dval);

            dval = (int) node.getY().getBaseVal().getValue();
            list.add(DesignObject.TAG_BASE.Y, dval);

            dval = (int) node.getWidth().getBaseVal().getValue();
            list.add(DesignObject.TAG_BASE.WIDTH, dval);

            dval = (int) node.getHeight().getBaseVal().getValue();
            list.add(DesignObject.TAG_BASE.HEIGHT, dval);

            info.setDesc(list.toString());
        }

        ShapeImage obj = (ShapeImage) factory.buildObject(SHAPE.IMAGE, info);

        return obj;
    }

    private void parse(Element group, ShapeGroup parent) {
        NodeList nl = group.getChildNodes();
        int count = nl.getLength();

        //        int objCount = 0;
        //        if (group instanceof SVGSVGElement)
        //        {
        //            for (int i = 0; i < count ; i++)
        //            {
        //                Node node = nl.item(i);
        //                if (node instanceof SVGElement && !(node instanceof SVGDefsElement))
        //                {
        //                    objCount++;
        //                }
        //            }
        //        }

        for (int i = 0; i < count; i++) {
            Node node = nl.item(i);
            DesignObject obj = null;
            if (node instanceof SVGGElement) {
                if (false == (group instanceof SVGSVGElement)) {
                    String desc = this.getClassName(node);
                    if (null != desc) {
                        String className;
                        if (desc.startsWith("class=")) {
                            PairList list = PairList.fromString(desc);
                            List<Object> vals = list.getValues("class");
                            className = (String) vals.get(0);
                        } else {
                            className = desc;
                        }
                        if (className.equals(ShapeGraph.class.getSimpleName())) {
                            obj = parseGraph((SVGElement) node);
                        } else if (className.equals(ShapeGroup.class.getSimpleName())) {
                            ObjectFactory factory = ObjectFactory.getInstance();
                            obj = factory.buildObject(SHAPE.GROUP, null);
                            parse((Element) node, (ShapeGroup) obj);
                        }
                    } else {
                        parse((Element) node, parent);
                    }
                } else {
                    parse((Element) node, (ShapeGroup) obj);
                }
            } else if (node instanceof SVGRectElement) {
                obj = parseRectangle((SVGRectElement) node);
            } else if (node instanceof SVGCircleElement) {
                obj = parseCircle((SVGCircleElement) node);
            } else if (node instanceof SVGEllipseElement) {
                obj = parseEllipse((SVGEllipseElement) node);
            } else if (node instanceof SVGLineElement) {
            } else if (node instanceof SVGPolylineElement) {
            } else if (node instanceof SVGPolygonElement) {
                obj = this.parsePolygon((SVGPolygonElement) node);
            } else if (node instanceof SVGPathElement) {
                obj = parsePath((SVGPathElement) node);
            } else if (node instanceof SVGOMTextElement) {
                obj = parseText((SVGOMTextElement) node);
            } else if (node instanceof SVGOMForeignObjectElement) {
                obj = parseForeinObject((SVGOMForeignObjectElement) node);
            } else if (node instanceof SVGOMImageElement) {
                obj = parseImage((SVGOMImageElement) node);
            }
            if (null != obj) {
                if (null == parent) {
                    if (false == this.objList.contains(obj)) {
                        this.objList.add(obj);
                    }
                } else {
                    //                    if (false == parent.contains(obj))
                    //                    {
                    //                        parent.addObject(obj);
                    //                    }
                    if (false == parent.hasObject(obj)) {
                        parent.addObject(obj);
                    }
                }
            }
        }
    }
}
