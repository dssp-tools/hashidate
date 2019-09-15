package dssp.hashidate.io;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.svggen.DOMTreeManager;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dssp.hashidate.config.Config;
import dssp.hashidate.misc.FigureType.FILL_TYPE;
import dssp.hashidate.shape.DesignObject;
import dssp.hashidate.shape.ShapeCircle;
import dssp.hashidate.shape.ShapeGraph;
import dssp.hashidate.shape.ShapePolyline;
import dssp.hashidate.shape.ShapeSpline;
import dssp.hashidate.shape.ShapeSpring;
import dssp.hashidate.shape.ShapeText;
import dssp.brailleLib.Util;
import dssp.brailleLib.XmlUtil;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class ExportSVG extends ExportBase
{
	private Document document;
	private SVGGraphics2D svgGenerator;
	private static class SVGSet
	{
		final Document objDoc;
		final SVGGraphics2D objSVG;

		SVGSet(Document doc, SVGGraphics2D g)
		{
			this.objDoc = doc;
			this.objSVG = g;
		}
	}
	private Stack<SVGSet> stack = new Stack<SVGSet>();
	private SVGSet set;
	private Point2D.Double svgScale;
	private Point2D.Double svgScaleItalic;

	final static String CLASS_ATTR = "class";
	final static String CLASS_ATTR_VAL = "className";

	public ExportSVG()
	{
		svgScale = Config.getConfig(Config.MISC.PLAIN);
		svgScaleItalic = Config.getConfig(Config.MISC.ITALIC);
	}

	public boolean clipboardEnd()
	{
		StringWriter sWriter = new StringWriter();
		try (BufferedWriter writer = new BufferedWriter(sWriter))
		{
			try
			{
				svgGenerator.stream(writer);
			}
			catch (Exception e)
			{
				Util.logException(e);
				return false;
			}

			String text = sWriter.toString();
			int index = text.indexOf("<svg");
			text = text.substring(index);

			Util.setToClipboard(text);
		}
		catch (IOException e)
		{
			Util.logException(e);
			return false;
		}
		return true;
	}

	public boolean initClipboard(Rectangle area)
	{
		if (null == area)
		{
			return false;
		}
		this.area = area;
		try
		{
			document = XmlUtil.createDocument(SVGDOMImplementation.SVG_NAMESPACE_URI, "svg");

			SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
//			GraphicContextDefaults gcd = new GraphicContextDefaults();
//			gcd.setFont(new Font("Mathjax_Math", Font.ITALIC, 12));
//			ctx.setGraphicContextDefaults(gcd);
			String dt = Util.now();
			ctx.setComment(String.format(" Created on %s ", dt));

			svgGenerator = new SVGGraphics2D(ctx, false);
			svgGenerator.setSVGCanvasSize(new Dimension(area.width, area.height));
		}
		catch (Exception e)
		{
			Util.logException(e);
			return false;
		}

		return true;
	}

	@Override
	public boolean init(Rectangle area, File file, boolean newFile)
	{
		this.file = file;
		if (null == this.file || newFile)
		{
			FileNameExtensionFilter filter = new FileNameExtensionFilter("SVG ファイル(*.svg)", "svg");
			FileNameExtensionFilter[] list = {filter};
			if (null == this.file)
			{
				this.file = Util.selectFile(null, "sample", list, true);
			}
			else
			{
				this.file = Util.selectFile(this.file.getName(), "sample", list, true);
			}
			if(null == this.file)
			{
				return false;
			}
		}

		return initClipboard(area);
	}

	@Override
	public File end()
	{
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.file), "UTF-8")))
		{
			svgGenerator.stream(writer);
			Util.logInfo(String.format("Written in %s", this.file.getCanonicalPath()));
		}
		catch (Exception e)
		{
			Util.logException(e);
			return null;
		}

		return this.file;
	}

	@Override
	public void writeStart(ShapeInfo info)
	{
		try
		{
			if (null == this.set)
			{
				this.stack.push(new SVGSet(this.document, this.svgGenerator));
				SVGGraphics2D g = new SVGGraphics2D(this.document);
				this.set = new SVGSet(this.document, g);
			}
			else
			{
				this.stack.push(this.set);
				SVGGraphics2D g = new SVGGraphics2D(this.document);
				this.set = new SVGSet(this.document, g);
			}
		}
		catch (Exception e)
		{
			Util.logException(e);
		}
	}

	private Element extractObj(Node group)
	{
		if (group.hasChildNodes())
		{
			NodeList children = group.getChildNodes();
			int count = children.getLength();
			for (int i = 0; i < count; i++)
			{
				Node child = children.item(i);
				if (0 == child.getNodeName().compareToIgnoreCase("g"))
				{
					return extractObj(child);
				}
				else if (child instanceof Element && 0 != child.getNodeName().compareToIgnoreCase("defs"))
				{
					return (Element)child;
				}
			}
		}

		return null;
	}

	@Override
	public void writeEnd(ShapeInfo info)
	{
		Element group = this.set.objSVG.getTopLevelGroup();
		if (null == group)
		{
			return;
		}

		this.addMask(group, info.obj);

		if (this.stack.empty())
		{
			Element clone = (Element)this.document.importNode(group, true);
			clone.setAttribute("fill", "#" + Util.colorString(info.obj.getLineColor()));

			DOMTreeManager dtm = svgGenerator.getDOMTreeManager();
			dtm.appendGroup(clone, null);

			this.set = null;
		}
		else
		{
			SVGSet set = this.stack.pop();
			Element clone = (Element)this.document.importNode(group, true);
			clone.setAttribute("fill", "#" + Util.colorString(info.obj.getLineColor()));

			DOMTreeManager dtm = set.objSVG.getDOMTreeManager();
			dtm.appendGroup(clone, null);
			this.set = set;
		}
		return;
	}

	private int maskCount;
	private void addMask(Element elm, DesignObject obj)
	{
		Document doc = this.set.objDoc;
		SVGGraphics2D g = new SVGGraphics2D(doc);

		g.setColor(Color.WHITE);
		g.fillRect(obj.x, obj.y, obj.width, obj.height);
		g.setColor(Color.BLACK);
		obj.drawMask(g, DesignObject.DRAW_MODE.EXPORT, false);
		Element group = g.getTopLevelGroup();

		Element mask = doc.createElement("mask");
		String id = String.format("mask%d", ++maskCount);
		mask.setAttribute("id", id);
		mask.appendChild(group);

		elm.appendChild(mask);
		elm.setAttribute("mask", String.format("url(#%s)", id));
	}

	@Override
	public void openGroup(ShapeInfo info)
	{
		writeStart(info);

		Element obj = this.set.objSVG.getTopLevelGroup();

		Element desc = this.document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI,"desc");
		desc.setAttribute(CLASS_ATTR, CLASS_ATTR_VAL);
		desc.setTextContent(info.getDesc(""));
		obj.appendChild(desc);
		this.set.objSVG.setTopLevelGroup(obj);
	}

	@Override
	public void closeGroup(ShapeInfo info)
	{
		Element obj = this.set.objSVG.getTopLevelGroup();


		if (this.stack.empty())
		{
			Element clone = (Element)this.document.importNode(obj, true);
			DOMTreeManager dtm = svgGenerator.getDOMTreeManager();
			dtm.appendGroup(clone, null);
		}
		else
		{
			SVGSet set = this.stack.pop();
			Element clone = (Element)this.document.importNode(obj, true);
			DOMTreeManager dtm = set.objSVG.getDOMTreeManager();
			dtm.appendGroup(clone, null);
			this.set = set;
		}
		return;
	}

	private void addDesc(ShapeInfo info)
	{
		Element group = this.set.objSVG.getTopLevelGroup();
		Element obj = extractObj(group);
		if (null == obj)
		{
			return;
		}

		Element desc = this.document.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI,"desc");
		desc.setAttribute(CLASS_ATTR, CLASS_ATTR_VAL);
		desc.setTextContent(info.getDesc(""));
		obj.appendChild(desc);

		this.set.objSVG.setTopLevelGroup(group);
	}

	@Override
	public boolean write(ShapeInfo info)
	{
		switch(info.getType())
		{
		case SHAPE:
			return writeShape(info);
		case CIRCLE:
		case ELLIPSE:
			return writeArc(info);
		case LINE:
		case POLYLINE:
		case SPRING:
			return writePolyline(info);
		case POLYGON:
			return writePolygon(info);
		case SPLINE:
		case SPLINE_LOOP:
			return writePathList(info);
		case TEXT:
			return writeText(info);
		case FORMULA:
			return writeFormula(info);
		case GRAPH:
			return this.writeGraph(info);
		default:
			return writeShape(info);
		}
	}

	private boolean writeShape(ShapeInfo info)
	{
		DesignObject obj = info.obj;

		obj.draw(this.set.objSVG, DesignObject.DRAW_MODE.EXPORT);
//		if (FILL_TYPE.TRANSPARENT != obj.getFillType())
//		{
//			obj.setFillProperty(this.set.objSVG, false, true);
//			this.set.objSVG.fill(obj);
//			obj.setFillProperty(this.set.objSVG, false, false);
//		}
//
//		obj.setDrawProperty(this.set.objSVG, false, true);
//		this.set.objSVG.draw(obj);
//		obj.setDrawProperty(this.set.objSVG, false, false);

		this.addDesc(info);

		return true;
	}

	private boolean writePathList(ShapeInfo info)
	{
		ShapeSpline obj = (ShapeSpline) info.obj;

		Path2D.Double path = new Path2D.Double();
		for (Path2D.Double seg: obj.getPathList())
		{
			path.append(seg, true);
		}

		obj.setDrawProperty(this.set.objSVG, false, true);
		this.set.objSVG.draw(path);
		obj.setDrawProperty(this.set.objSVG, false, false);

		this.addDesc(info);

		return true;
	}

	private boolean writePolyline(ShapeInfo info)
	{
		if (info.obj instanceof ShapePolyline)
		{
			ShapePolyline obj = (ShapePolyline) info.obj;
			obj.setDrawProperty(this.set.objSVG, false, true);
			this.set.objSVG.drawPolyline(obj.getxPoints(), obj.getyPoints(), obj.getxPoints().length);
			obj.setDrawProperty(this.set.objSVG, false, false);
		}
		else if (info.obj instanceof ShapeSpring)
		{
			ShapeSpring obj = (ShapeSpring) info.obj;
			obj.setDrawProperty(this.set.objSVG, false, true);
			this.set.objSVG.drawPolyline(obj.getxPoints(), obj.getyPoints(), obj.getxPoints().length);
			obj.setDrawProperty(this.set.objSVG, false, false);
		}

		this.addDesc(info);

		return true;
	}

	private boolean writePolygon(ShapeInfo info)
	{
		ShapePolyline obj = (ShapePolyline) info.obj;

		if (FILL_TYPE.TRANSPARENT != obj.getFillType())
		{
			obj.setFillProperty(this.set.objSVG, false, true);
			this.set.objSVG.fillPolygon(obj.getxPoints(), obj.getyPoints(), obj.getxPoints().length);
			obj.setFillProperty(this.set.objSVG, false, false);
		}

		obj.setDrawProperty(this.set.objSVG, false, true);
		this.set.objSVG.drawPolygon(obj.getxPoints(), obj.getyPoints(), obj.getxPoints().length);
		info.obj.setDrawProperty(this.set.objSVG, false, false);

		this.addDesc(info);

		return true;
	}

	private boolean writeArc(ShapeInfo info)
	{
		ShapeCircle obj = (ShapeCircle) info.obj;
		if (FILL_TYPE.TRANSPARENT != obj.getFillType())
		{
			obj.setFillProperty(this.set.objSVG, false, true);
			this.set.objSVG.fill(obj.getArc());
			obj.setFillProperty(this.set.objSVG, false, false);
		}

		obj.setDrawProperty(this.set.objSVG, false, true);
		this.set.objSVG.draw(obj.getArc());
		obj.setDrawProperty(this.set.objSVG, false, false);

		this.addDesc(info);

		return true;
	}

	private boolean writeText(ShapeInfo info)
	{
		ShapeText obj = (ShapeText) info.obj;

		Element eText = this.set.objDoc.createElement("text");

		eText.setAttribute("stroke", "none");
		eText.setAttribute("x", Integer.toString(obj.x));
		eText.setAttribute("y", Integer.toString(obj.y));
		eText.setAttribute("xml:space", "preserve");
		Font font = obj.getFont();
		eText.setAttribute("font-family", font.getFamily());
		eText.setAttribute("font-size", Integer.toString(font.getSize()));

		eText.setAttribute("transform", String.format("translate(%d %d) scale(%f %f) translate(%d %d)", obj.x, obj.y, obj.getScale().x, obj.getScale().y, -obj.x, -obj.y));
		int style = font.getStyle();
		if (0 != (style & Font.ITALIC))
		{
			eText.setAttribute("font-style", "italic");
		}
		else
		{
			eText.setAttribute("font-style", "normal");
		}

		if (0 != (style & Font.BOLD))
		{
			eText.setAttribute("font-weight", "bold");
		}
		else
		{
			eText.setAttribute("font-weight", "normal");
		}

		StringTokenizer st = new StringTokenizer(obj.getText(),"\n");
		if (1 == st.countTokens())
		{
			eText.setTextContent(obj.getText());
		}
		else
		{
			String dy = Integer.toString(font.getSize());
			for (int i = 0; st.hasMoreTokens(); i++)
			{
				String line = st.nextToken();
				Element tspan = this.set.objDoc.createElement("tspan");
				if (0 < i)
				{
					tspan.setAttribute("x", Integer.toString((int) Math.round(obj.getX())));
					tspan.setAttribute("dy", dy);
				}
				tspan.setTextContent(line);;
				eText.appendChild(tspan);
			}
		}
		Element group = this.set.objSVG.getTopLevelGroup();
		group.appendChild(eText);
		this.set.objSVG.setTopLevelGroup(group);

		this.addDesc(info);
		return true;
	}

	private boolean writeFormula(ShapeInfo info)
	{
		ShapeText obj = (ShapeText) info.obj;

		Element tag = this.set.objDoc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI,"foreignObject");
		tag.setAttribute("x", Integer.toString(obj.x));
		tag.setAttribute("y", Integer.toString(obj.y));
		tag.setAttribute("transform", String.format("translate(%d %d) scale(%f %f) translate(%d %d)", obj.x, obj.y, svgScale.x * obj.getScale().x, svgScale.y * obj.getScale().y, -obj.x, -obj.y));

		try
		{
			Document doc = XmlUtil.parse(obj.getMathML());
			NodeList nodeList = doc.getElementsByTagName("math");

			for (int i = 0; i < nodeList.getLength(); i++)
			{
				Element elm = (Element)nodeList.item(i);

				String style = elm.getAttribute("mathvariant");
				if (style.equals("italic") || style.equals("bold-italic"))
				{
					int w = (int)Math.round(obj.getFrame().getWidth()/this.svgScaleItalic.x);
					int h = (int)Math.round(obj.getFrame().getHeight()/this.svgScaleItalic.y);
					tag.setAttribute("width", Integer.toString(w));
					tag.setAttribute("height", Integer.toString(h));
				}
				else
				{
					int w = (int)Math.round(obj.getFrame().getWidth()/this.svgScale.x);
					int h = (int)Math.round(obj.getFrame().getHeight()/this.svgScale.y);
					tag.setAttribute("width", Integer.toString(w));
					tag.setAttribute("height", Integer.toString(h));
				}

				Node clone = this.set.objDoc.importNode(elm, true);
				tag.appendChild(clone);
			}
		}
		catch (Exception ex)
		{
			Util.logException(ex);
			return false;
		}

		Element group = this.set.objSVG.getTopLevelGroup();
		group.appendChild(tag);
		this.set.objSVG.setTopLevelGroup(group);

		this.addDesc(info);

		return true;
	}

	private boolean writeGraph(ShapeInfo info)
	{
		Element obj = this.set.objSVG.getTopLevelGroup();

		Element desc = this.set.objDoc.createElementNS(SVGDOMImplementation.SVG_NAMESPACE_URI,"desc");
		desc.setAttribute(CLASS_ATTR, CLASS_ATTR_VAL);
		desc.setTextContent(info.getDesc(""));
		obj.appendChild(desc);
		this.set.objSVG.setTopLevelGroup(obj);

		ShapeGraph graph = (ShapeGraph) info.obj;
		graph.draw(this.set.objSVG, DesignObject.DRAW_MODE.EXPORT);

		Element clone = (Element)document.importNode(obj, true);
		DOMTreeManager dtm = svgGenerator.getDOMTreeManager();
		dtm.appendGroup(clone, null);

		return true;
	}
}
