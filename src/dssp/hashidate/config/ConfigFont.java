package dssp.hashidate.config;

import java.awt.Font;
import java.util.Map;

import javax.swing.JPanel;

import org.w3c.dom.Document;

import dssp.hashidate.config.gui.ConfigFontPanel;
import dssp.brailleLib.Util;
import dssp.brailleLib.XmlUtil;

public class ConfigFont extends Config.ConfigBase
{
	// フォント
	private static class Item
	{
		private String path;
		private Object val;

		Item(String path, Object val)
		{
			this.path = path;
			this.val = val;
		}

		String getPath()
		{
			return this.path;
		}

		public <T> T getVal()
		{
			@SuppressWarnings("unchecked")
			T ret = (T) this.val;
			return ret;
		}
	}

	public enum FONT
	{
//		TEXT(new Item("CONFIG/FONT/NAME", "Times New Roman"), new Item("CONFIG/FONT/STYLE", Font.PLAIN), new Item("CONFIG/FONT/SIZE", 12)),
		TEXT(new Item("CONFIG/FONT/NAME", Font.SERIF), new Item("CONFIG/FONT/STYLE", Font.PLAIN), new Item("CONFIG/FONT/SIZE", 12)),
		FORMULA(new Item("CONFIG/FONT_EQ/NAME", Font.SERIF), new Item("CONFIG/FONT_EQ/STYLE", Font.ITALIC), new Item("CONFIG/FONT_EQ/SIZE", 12));

		final Item NAME;
		final Item STYLE;
		final Item SIZE;

		FONT(Item name, Item style, Item size)
		{
			this.NAME = name;
			this.STYLE = style;
			this.SIZE = size;
		}
	}

	private static final ConfigFont instance = new ConfigFont();
	final Map<Config.FONT, Font> map = Util.newHashMap();

	private ConfigFontPanel panel = null;

	private ConfigFont()
	{
	}

	static ConfigFont getInstance()
	{
		return instance;
	}

	@Override
	void load(Document doc)
	{
		String name = Config.loadString(doc, FONT.TEXT.NAME.getPath(), (String)FONT.TEXT.NAME.getVal());
		int style = Config.loadInt(doc, FONT.TEXT.STYLE.getPath(), (int)FONT.TEXT.STYLE.getVal());
		int size = Config.loadInt(doc, FONT.TEXT.SIZE.getPath(), (int)FONT.TEXT.SIZE.getVal());
		Font font = new Font(name, style, size);
		this.map.put(Config.FONT.TEXT, font);

		name = Config.loadString(doc, FONT.FORMULA.NAME.getPath(), (String)FONT.FORMULA.NAME.getVal());
		style = Config.loadInt(doc, FONT.FORMULA.STYLE.getPath(), (int)FONT.FORMULA.STYLE.getVal());
		size = Config.loadInt(doc, FONT.FORMULA.SIZE.getPath(), (int)FONT.FORMULA.SIZE.getVal());
		font = new Font(name, style, size);
		this.map.put(Config.FONT.FORMULA, font);
	}

	@Override
	void save(Document doc)
	{
		Font font = this.map.get(Config.FONT.TEXT);
		try
		{
			XmlUtil.setString(doc, FONT.TEXT.NAME.getPath(), font.getFamily());
			XmlUtil.setString(doc, FONT.TEXT.STYLE.getPath(), Integer.toString(font.getStyle()));
			XmlUtil.setString(doc, FONT.TEXT.SIZE.getPath(), Integer.toString(font.getSize()));
		}
		catch (Exception ex)
		{
			Util.logException(ex);
		}

		font = this.map.get(Config.FONT.FORMULA);
		try
		{
			XmlUtil.setString(doc, FONT.FORMULA.NAME.getPath(), font.getFamily());
			XmlUtil.setString(doc, FONT.FORMULA.STYLE.getPath(), Integer.toString(font.getStyle()));
			XmlUtil.setString(doc, FONT.FORMULA.SIZE.getPath(), Integer.toString(font.getSize()));
		}
		catch (Exception ex)
		{
			Util.logException(ex);
		}
		
		this.isUpdated = false;
	}

	@Override
	<T> T getConfig(Config.CONFIG_TYPE key)
	{
		@SuppressWarnings("unchecked")
		T obj = (T) this.map.get(key);
		return obj;
	}

//	@Override
//	<T> void setConfig(Config.CONFIG_TYPE key, T val)
//	{
//		if (false == (val instanceof Font))
//		{
//			throw new IllegalArgumentException(String.format("引数%sがフォントではない", val.getClass().getName()));
//		}
//		this.map.put((Config.FONT)key, (Font)val);
//	}

	@Override
	void update()
	{
		if (null == this.panel)
		{
			return;
		}
		for (Config.FONT type: Config.FONT.values())
		{
			Font tmp = this.panel.getFont(type);
			Font font = this.getConfig(type);
			
			if (false == tmp.getFamily().equals(font.getFamily()) || tmp.getSize() != font.getSize() || tmp.getStyle() != font.getStyle())
			{
				this.map.put(type, tmp);
				this.isUpdated = true;
			}
		}

		if (this.isUpdated)
		{
			Util.logInfo("updated");
		}
	}

	@Override
	String getTitle()
	{
		return "フォント";
	}

	@Override
	JPanel getPanel()
	{
		if (null == this.panel)
		{
			this.panel = new ConfigFontPanel();
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
		for (Config.FONT type: Config.FONT.values())
		{
			this.panel.setFont(type, this.map.get(type));
		}
	}
}
