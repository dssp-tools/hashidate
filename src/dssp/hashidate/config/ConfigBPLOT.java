package dssp.hashidate.config;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import org.w3c.dom.Document;

import dssp.hashidate.config.Config.CONFIG_TYPE;
import dssp.hashidate.config.Config.ConfigBase;
import dssp.hashidate.config.gui.ConfigBPLOTPanel;
import dssp.brailleLib.Util;
import dssp.brailleLib.XmlUtil;

public class ConfigBPLOT extends ConfigBase
{
	public static enum PRINTER_NAME
	{
		ESA721,
		ESA600G;
	}

	private static enum BPLOT
	{
		PAGE_A4_WIDTH("CONFIG/BPLOT/PAGE/A4/WIDTH", 206),
		PAGE_A4_HEIGHT("CONFIG/BPLOT/PAGE/A4/HEIGHT", 250),
		PAGE_B5_WIDTH("CONFIG/BPLOT/PAGE/B5/WIDTH", 165),
		PAGE_B5_HEIGHT("CONFIG/BPLOT/PAGE/B5/HEIGHT", 229),
		SHOW_PAGE_FRAME("CONFIG/BPLOT/PAGE_SHOW", true),
		PAGE_FRAME_COLOR("CONFIG/BPLOT/PAGE_FRAME_COLOR", Color.LIGHT_GRAY),
		MIRROR("CONFIG/BPLOT/MIRROR", 2.5),
		UPSIDEDOWN("CONFIG/BPLOT/UPSIDEDOWN", true),
		OUTPUT_FILE("CONFIG/BPLOT/OUTPUT_FILE", true),
		EXEPATH("CONFIG/BPLOT/EXEPATH", "BPlot.exe"),
		PRINTER("CONFIG/BPLOT/PRINTER", PRINTER_NAME.ESA721.name()),
		DOWNSIDEFIRST("CONFIG/BPLOT/DONSIDEFIRST", false),
		USE_NABCC("CONFIG/BPLOT/USE_NABCC", true);


		public final String PATH;
		private final Object defVal;

		BPLOT(String path, Object val)
		{
			this.PATH = path;
			this.defVal = val;
		}

		@SuppressWarnings("unchecked")
		<T> T getDefValue()
		{
			return (T) this.defVal;
		}
	}

	private Map<Config.BPLOT, Dimension> pages = Util.newHashMap();
	private double mirror;
	private boolean showPageFrame;
	private Color pageFrameColor;
	private boolean upsideDown;
	private boolean outputFile;
	private String exePath;
	private String printer;
	private boolean downsideFirst;
	private boolean useNABCC;

	private ConfigBPLOTPanel panel;

	private static ConfigBPLOT instance = new ConfigBPLOT();

	public static ConfigBPLOT getInstance()
	{
		return instance;
	}

	@Override
	void load(Document doc)
	{
		{
			int w = Config.loadInt(doc, BPLOT.PAGE_A4_WIDTH.PATH, (int)BPLOT.PAGE_A4_WIDTH.getDefValue());
			int h = Config.loadInt(doc, BPLOT.PAGE_A4_HEIGHT.PATH, (int)BPLOT.PAGE_A4_HEIGHT.getDefValue());
			this.pages.put(Config.BPLOT.PAGE_A4, new Dimension(w,h));
		}

		{
			int w = Config.loadInt(doc, BPLOT.PAGE_B5_WIDTH.PATH, (int)BPLOT.PAGE_B5_WIDTH.getDefValue());
			int h = Config.loadInt(doc, BPLOT.PAGE_B5_HEIGHT.PATH, (int)BPLOT.PAGE_B5_HEIGHT.getDefValue());
			this.pages.put(Config.BPLOT.PAGE_B5, new Dimension(w,h));
		}

		this.showPageFrame = Config.loadBoolean(doc, BPLOT.SHOW_PAGE_FRAME.PATH, (boolean) BPLOT.SHOW_PAGE_FRAME.getDefValue());
		this.pageFrameColor = Config.loadColor(doc, BPLOT.PAGE_FRAME_COLOR.PATH, Util.colorString((Color) BPLOT.PAGE_FRAME_COLOR.getDefValue()));

		this.mirror = Config.loadDouble(doc, BPLOT.MIRROR.PATH, (double) BPLOT.MIRROR.getDefValue());

		this.outputFile = Config.loadBoolean(doc, BPLOT.OUTPUT_FILE.PATH, (boolean) BPLOT.OUTPUT_FILE.getDefValue());

		this.exePath = Config.loadString(doc, BPLOT.EXEPATH.PATH, (String) BPLOT.EXEPATH.getDefValue());

		this.printer = Config.loadString(doc, BPLOT.PRINTER.PATH, (String) BPLOT.PRINTER.getDefValue());

		this.downsideFirst = Config.loadBoolean(doc, BPLOT.DOWNSIDEFIRST.PATH, (boolean) BPLOT.DOWNSIDEFIRST.getDefValue());

		this.upsideDown = Config.loadBoolean(doc, BPLOT.UPSIDEDOWN.PATH, (boolean) BPLOT.UPSIDEDOWN.getDefValue());

		this.useNABCC = Config.loadBoolean(doc, BPLOT.USE_NABCC.PATH, (boolean) BPLOT.USE_NABCC.getDefValue());
	}

	@Override
	void save(Document doc)
	{
		try
		{
			{
				Dimension size = this.pages.get(Config.BPLOT.PAGE_A4);
				XmlUtil.setString(doc, BPLOT.PAGE_A4_WIDTH.PATH, Integer.toString(size.width));
				XmlUtil.setString(doc, BPLOT.PAGE_A4_HEIGHT.PATH, Integer.toString(size.height));
			}

			{
				Dimension size = this.pages.get(Config.BPLOT.PAGE_B5);
				XmlUtil.setString(doc, BPLOT.PAGE_B5_WIDTH.PATH, Integer.toString(size.width));
				XmlUtil.setString(doc, BPLOT.PAGE_B5_HEIGHT.PATH, Integer.toString(size.height));
			}

			XmlUtil.setString(doc, BPLOT.SHOW_PAGE_FRAME.PATH, Boolean.toString(this.showPageFrame));
			XmlUtil.setString(doc, BPLOT.PAGE_FRAME_COLOR.PATH, Util.colorString(this.pageFrameColor));

			XmlUtil.setString(doc, BPLOT.MIRROR.PATH, Double.toString(this.mirror));

			XmlUtil.setString(doc, BPLOT.OUTPUT_FILE.PATH, Boolean.toString(this.outputFile));

			XmlUtil.setString(doc, BPLOT.EXEPATH.PATH, this.exePath);

			XmlUtil.setString(doc, BPLOT.PRINTER.PATH, this.printer);

			XmlUtil.setString(doc, BPLOT.DOWNSIDEFIRST.PATH, Boolean.toString(this.downsideFirst));

			XmlUtil.setString(doc, BPLOT.UPSIDEDOWN.PATH, Boolean.toString(this.upsideDown));

			XmlUtil.setString(doc, BPLOT.USE_NABCC.PATH, Boolean.toString(this.useNABCC));
		}
		catch (Exception ex)
		{
			Util.logException(ex);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	<T> T getConfig(CONFIG_TYPE key)
	{
		T obj = null;
		switch((Config.BPLOT)key)
		{
		case PAGE_A4:
		case PAGE_B5:
			obj = (T) this.pages.get(key);
			break;
		case SHOW_PAGE_FRAME:
			obj = (T) Boolean.valueOf(this.showPageFrame);
			break;
		case PAGE_FRAME_COLOR:
			obj = (T) this.pageFrameColor;
			break;
		case MIRROR:
			obj = (T) Double.valueOf(this.mirror);
			break;
		case OUTPUT_FILE:
			obj = (T) Boolean.valueOf(this.outputFile);
			break;
		case EXEPATH:
			obj = (T) this.exePath;
			break;
		case PRINTER:
			obj = (T) this.printer;
			break;
		case DOWNSIDEFIRST:
			obj = (T) Boolean.valueOf(this.downsideFirst);
			break;
		case UPSIDEDOWN:
			obj = (T) Boolean.valueOf(this.upsideDown);
			break;
		case USE_NABCC:
			obj = (T) Boolean.valueOf(this.useNABCC);
			break;
		}

		return obj;
	}

	@Override
	void update()
	{
		@SuppressWarnings("unchecked")
		Map<Config.BPLOT, Dimension> p = (Map<Config.BPLOT, Dimension>) ((HashMap<Config.BPLOT, Dimension>)this.pages).clone();
		this.panel.getSize(this.pages);
		for (Config.BPLOT key: this.pages.keySet())
		{
			Dimension cs = this.pages.get(key);
			Dimension ns = p.get(key);
			if (false == cs.equals(ns))
			{
				this.pages = p;
				this.isUpdated = true;
				break;
			}
		}

		if (this.showPageFrame != this.panel.getShowPageFrame())
		{
			this.showPageFrame = !this.showPageFrame;
			this.isUpdated = true;
		}

		Color color = this.panel.getPageFrameColor();
		if (color != this.pageFrameColor)
		{
			this.pageFrameColor = color;
			this.isUpdated = true;
		}

		double val = this.panel.getMirror();
		if (val != this.mirror)
		{
			this.mirror = val;
			this.isUpdated = true;
		}

		boolean flag = this.panel.getUpsideDown();
		if (flag != this.upsideDown)
		{
			this.upsideDown = flag;
			this.isUpdated = true;
		}

		flag = this.panel.isOutputFile();
		if (flag != this.outputFile)
		{
			this.outputFile = flag;
			this.isUpdated = true;
		}

		String text = this.panel.getExePath();
		if (false == text.equals(this.exePath))
		{
			this.exePath = text;
			this.isUpdated = true;
		}

		text = this.panel.getPrinter();
		if (false == text.equals(this.printer))
		{
			this.printer = text;
			this.isUpdated = true;
		}

		flag = this.panel.isDownsideFirst();
		if (flag != this.downsideFirst)
		{
			this.downsideFirst = flag;
			this.isUpdated = true;
		}

		flag = this.panel.isUseNABCC();
		if (flag != this.useNABCC)
		{
			this.useNABCC = flag;
			this.isUpdated = true;
		}
	}

	@Override
	String getTitle()
	{
		return "BPLOT";
	}

	@Override
	JPanel getPanel()
	{
		if (null == this.panel)
		{
			this.panel = new ConfigBPLOTPanel();
		}

		this.resetPanel();
		return this.panel;
	}

	@Override
	void resetPanel()
	{
		if (null == this.panel)
		{
			return;
		}
		this.panel.setSize(this.pages);
		this.panel.setShowPageFrame(this.showPageFrame);
		this.panel.setPageFrameColor(this.pageFrameColor);
		this.panel.setMirror(this.mirror);
		this.panel.setUpsideDown(this.upsideDown);
		this.panel.setOutputFile(this.outputFile);
		this.panel.setExePath(this.exePath);
		this.panel.setPrinter(this.printer);
		this.panel.setDownsideFirst(this.downsideFirst);
		this.panel.setUseNABCC(this.useNABCC);
	}

}
