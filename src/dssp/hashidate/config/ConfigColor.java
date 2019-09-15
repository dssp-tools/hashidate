package dssp.hashidate.config;

import java.awt.Color;
import java.util.Map;

import javax.swing.JPanel;

import org.w3c.dom.Document;

import dssp.hashidate.config.gui.ConfigColorPanel;
import dssp.brailleLib.Util;
import dssp.brailleLib.XmlUtil;

/**
 * 色（線、枠）の設定
 *
 * @author yagi
 *
 */
class ConfigColor extends Config.ConfigBase
{
	private enum COLOR
	{
		LINE("CONFIG/COLOR/LINE", "000000"),
		PAINT("CONFIG/COLOR/PAINT", "FFFFFF"),
		FRAME("CONFIG/COLOR/FRAME", "888888"),
		BRAILLE("CONFIG/COLOR/BRAILLE", "00FF00");

		public final String PATH;
		public final String DEFAULT;

		COLOR(String path, String defVal)
		{
			this.PATH = path;
			this.DEFAULT = defVal;
		}
	}

	private final Map<Config.COLOR, Color> map = Util.newHashMap();
	private static ConfigColor instance;
	static
	{
		instance = new ConfigColor();
	}

	private ConfigColorPanel panel = null;

	private ConfigColor()
	{
	}

	static ConfigColor getInstance()
	{
		return instance;
	}

	@Override
	<T> T getConfig(Config.CONFIG_TYPE key)
	{
		@SuppressWarnings("unchecked")
		T obj =  (T) this.map.get(key);
		return obj;
	}

//	@Override
//	<T> void setConfig(Config.CONFIG_TYPE key, T val)
//	{
//		if (false == (val instanceof Color))
//		{
//			throw new IllegalArgumentException(String.format("引数%sが色の文字表現ではない", val.getClass().getName()));
//		}
//		map.put((Config.COLOR)key, (Color)val);
//	}

	@Override
	void load(Document doc)
	{
		COLOR[] types = COLOR.values();
		Config.COLOR[] keys = Config.COLOR.values();
		for (int i = 0; i < types.length; i++)
		{
			COLOR type = types[i];
			Config.COLOR key = keys[i];

			Color color = Config.loadColor(doc, type.PATH, type.DEFAULT);
			this.map.put(key, color);
		}
	}

	@Override
	void save(Document doc)
	{
		try
		{
			COLOR[] types = COLOR.values();
			Config.COLOR[] keys = Config.COLOR.values();
			for (int i = 0; i < types.length; i++)
			{
				COLOR type = types[i];
				Config.COLOR key = keys[i];

				Color color = this.map.get(key);
				XmlUtil.setString(doc, type.PATH, Util.colorString(color));
			}
		}
		catch (Exception ex)
		{
			Util.logException(ex);
		}

		this.isUpdated = false;
	}

	@Override
	void update()
	{
		Map<Config.COLOR, Color> ret = this.panel.getColor();
		for (Config.COLOR type: Config.COLOR.values())
		{
			Color color = this.map.get(type);
			Color rcolor = ret.get(type);
			if (false == color.equals(rcolor))
			{
				this.map.put(type, rcolor);
				this.isUpdated = true;
			}
		}
	}

	@Override
	String getTitle()
	{
		return "描画色";
	}

	@Override
	JPanel getPanel()
	{
		if (null == this.panel)
		{
			this.panel = new ConfigColorPanel();
		}
		this.resetPanel();

		return this.panel;
	}

	@Override
	void resetPanel()
	{
		this.panel.setColor(this.map);
	}
}
