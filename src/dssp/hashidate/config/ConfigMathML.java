package dssp.hashidate.config;

import java.util.Arrays;
import java.util.List;

import javax.swing.JPanel;

import org.w3c.dom.Document;

import dssp.hashidate.config.Config.CONFIG_TYPE;
import dssp.hashidate.config.Config.ConfigBase;
import dssp.hashidate.config.gui.ConfigMathMLPanel;
import dssp.hashidate.shape.helper.AliasResolver;
import dssp.brailleLib.Util;
import dssp.brailleLib.XmlUtil;

public class ConfigMathML extends ConfigBase
{
	private static final String PATH = "CONFIG/MATHML/UNICODE";
	private static final String DEFAULT_DICT = "mmlalias.xml";
	private static final String SEPARATOR = ",";
	private List<String> dictList = Util.newArrayList();
	private ConfigMathMLPanel panel = null;

	private static final ConfigMathML instance = new ConfigMathML();

	static ConfigMathML getInstance()
	{
		return instance;
	}

	@Override
	void load(Document doc)
	{
		String files = Config.loadString(doc, PATH, DEFAULT_DICT);
		String[] list = files.split(SEPARATOR);
		for (String file: list)
		{
			this.dictList.add(Util.exePath(file.trim()));
		}

		AliasResolver.getInstance().init();
	}

	@Override
	void save(Document doc)
	{
		StringBuilder files = new StringBuilder();
		for (String file: this.dictList)
		{
			if (0 < files.length())
			{
				files.append(SEPARATOR);
			}
			files.append(file);
		}

		try
		{
			XmlUtil.setString(doc, PATH, files.toString());
		}
		catch (Exception ex)
		{
			Util.logException(ex);
		}

		this.isUpdated = false;
	}

	@SuppressWarnings("unchecked")
	@Override
	<T> T getConfig(CONFIG_TYPE key)
	{
		T obj = null;
		switch((Config.MATHML)key)
		{
		case ALIAS:
			obj = (T) this.dictList;
		}
		return obj;
	}

//	@Override
//	<T> void setConfig(CONFIG_TYPE key, T val)
//	{
//		switch((Config.MATHML)key)
//		{
//		case ALIAS:
//			if (false == (val instanceof List))
//			{
//				throw new IllegalArgumentException(String.format("引数%sがパスのリストではない", val.getClass().getName()));
//			}
//			List<?> list = (List<?>)val;
//			for (Object obj: list)
//			{
//				if (false == (obj instanceof String))
//				{
//					throw new IllegalArgumentException(String.format("引数の要素%sがパスではない", obj.getClass().getName()));
//				}
//			}
//			this.dictList.clear();
//			for (Object obj: list)
//			{
//				String file = (String)obj;
//				this.dictList.add(file);
//			}
//		}
//	}

	@Override
	void update()
	{
		if (null == this.panel)
		{
			return;
		}
		List<String> list = this.panel.getFiles();
		if (false == Arrays.deepEquals(list.toArray(), this.dictList.toArray()))
		{
			this.dictList.clear();
			this.dictList.addAll(list);
			this.isUpdated = true;
		}

		if (this.isUpdated)
		{
			Util.logInfo("updated");
		}
	}

	@Override
	String getTitle()
	{
		return "MathML";
	}

	@Override
	JPanel getPanel()
	{
		if (null == this.panel)
		{
			this.panel = new ConfigMathMLPanel();
		}
		this.panel.setFiles(this.dictList);
		
		return this.panel;
	}

	@Override
	void resetPanel()
	{
		
	}
}
