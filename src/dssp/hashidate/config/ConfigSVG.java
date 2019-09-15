package dssp.hashidate.config;

import java.awt.geom.Point2D;

import javax.swing.JPanel;

import org.w3c.dom.Document;

import dssp.hashidate.config.gui.ConfigMiscPanel;
import dssp.brailleLib.Util;
import dssp.brailleLib.XmlUtil;

class ConfigSVG extends Config.ConfigBase
{
	// SVG出力変換スケール

	private static class Item
	{
		public final String PATH;
		public final double VAL;

		Item(String path, double val)
		{
			this.PATH = path;
			this.VAL = val;
		}
	}

	private enum SVGSCALE
	{
//		PLAIN(new Item("CONFIG/SVGSCALE/PLAIN/WIDTH", 0.67), new Item("CONFIG/SVGSCALE/PLAIN/HEIGHT", 0.80)),
//		ITALIC(new Item("CONFIG/SVGSCALE/ITALIC/WIDTH", 0.66), new Item("CONFIG/SVGSCALE/ITALIC/HEIGHT", 0.75));
		PLAIN(new Item("CONFIG/SVGSCALE/PLAIN/WIDTH", 0.8), new Item("CONFIG/SVGSCALE/PLAIN/HEIGHT", 0.5)),
		ITALIC(new Item("CONFIG/SVGSCALE/ITALIC/WIDTH", 0.8), new Item("CONFIG/SVGSCALE/ITALIC/HEIGHT", 0.5));

		public final Item WIDTH;
		public final Item HEIGHT;

		SVGSCALE(Item width, Item height)
		{
			this.WIDTH = width;
			this.HEIGHT = height;
		}

		public Point2D.Double getScale()
		{
			return new Point2D.Double(this.WIDTH.VAL, this.HEIGHT.VAL);
		}
	}

	private Point2D.Double scale_plain = SVGSCALE.PLAIN.getScale();
	private Point2D.Double scale_italic = SVGSCALE.ITALIC.getScale();

	private ConfigMiscPanel panel;

	private static ConfigSVG instance = new ConfigSVG();

	private ConfigSVG()
	{
	}

	static ConfigSVG getInstance()
	{
		return instance;
	}

	public void setPanel(ConfigMiscPanel panel)
	{
		this.panel = panel;
	}

	@Override
	void load(Document doc)
	{
		this.scale_plain.x = Config.loadDouble(doc, SVGSCALE.PLAIN.WIDTH.PATH, SVGSCALE.PLAIN.WIDTH.VAL);
		this.scale_plain.y = Config.loadDouble(doc, SVGSCALE.PLAIN.HEIGHT.PATH, SVGSCALE.PLAIN.HEIGHT.VAL);

		this.scale_italic.x = Config.loadDouble(doc, SVGSCALE.ITALIC.WIDTH.PATH, SVGSCALE.ITALIC.WIDTH.VAL);
		this.scale_italic.y = Config.loadDouble(doc, SVGSCALE.ITALIC.HEIGHT.PATH, SVGSCALE.ITALIC.HEIGHT.VAL);
	}

	@Override
	void save(Document doc)
	{
		try
		{
			XmlUtil.setString(doc, SVGSCALE.PLAIN.WIDTH.PATH, Double.toString(this.scale_plain.x));
			XmlUtil.setString(doc, SVGSCALE.PLAIN.HEIGHT.PATH, Double.toString(this.scale_plain.y));

			XmlUtil.setString(doc, SVGSCALE.ITALIC.WIDTH.PATH, Double.toString(this.scale_italic.x));
			XmlUtil.setString(doc, SVGSCALE.ITALIC.HEIGHT.PATH, Double.toString(this.scale_italic.y));
		}
		catch (Exception ex)
		{
			Util.logException(ex);
		}

		this.isUpdated = false;
	}

	@SuppressWarnings("unchecked")
	@Override
	<T> T getConfig(Config.CONFIG_TYPE key)
	{
		T obj = null;
		switch((Config.MISC)key)
		{
		case PLAIN:
			obj = (T) this.scale_plain;
			break;
		case ITALIC:
			obj = (T) this.scale_italic;
		default:
		}
		return obj;
	}

//	@Override
//	<T> void setConfig(Config.CONFIG_TYPE key, T val)
//	{
//		if (false == (val instanceof Point2D.Double))
//		{
//			throw new IllegalArgumentException(String.format("引数%sがPoint2D.Doubleではない", val.getClass().getName()));
//		}
//		switch((Config.SVG)key)
//		{
//		case PLAIN:
//			this.scale_plain = (Point2D.Double)val;
//			break;
//		case ITALIC:
//			this.scale_italic = (Point2D.Double)val;
//			break;
//		}
//	}

	@Override
	void update()
	{
		if (null == this.panel)
		{
			return;
		}
		Point2D.Double val = this.panel.getScale(Config.MISC.PLAIN);
		if (null != val && false == (val.equals(this.scale_plain)))
		{
			this.scale_plain = (Point2D.Double) val.clone();
			this.isUpdated = true;
		}

		val = this.panel.getScale(Config.MISC.ITALIC);
		if (null != val && false == (val.equals(this.scale_italic)))
		{
			this.scale_italic = (Point2D.Double) val.clone();
			this.isUpdated = true;
		}
	}

	@Override
	String getTitle()
	{
		return null;
	}

	@Override
	JPanel getPanel()
	{
		return null;
	}

	@Override
	void resetPanel()
	{
		if (null == this.panel)
		{
			return;
		}
		this.panel.setScale(Config.MISC.PLAIN, this.scale_plain);
		this.panel.setScale(Config.MISC.ITALIC, this.scale_italic);
	}
}
