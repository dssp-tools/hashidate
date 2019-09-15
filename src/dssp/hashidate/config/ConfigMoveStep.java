package dssp.hashidate.config;

import javax.swing.JPanel;

import org.w3c.dom.Document;

import dssp.hashidate.config.gui.ConfigMiscPanel;
import dssp.brailleLib.Util;
import dssp.brailleLib.XmlUtil;

class ConfigMoveStep extends Config.ConfigBase
{
	// 矢印キーでの移動量
	private static final String PATH = "CONFIG/MOVE_STEP";
	private static final int DEFAULT = 5;
	private int moveStep = 0;

	private ConfigMiscPanel panel;

	private static final ConfigMoveStep instance = new ConfigMoveStep();

	private ConfigMoveStep()
	{
	}

	static ConfigMoveStep getInstance()
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
		this.moveStep = Config.loadInt(doc, PATH, DEFAULT);
	}

	@Override
	void save(Document doc)
	{
		try
		{
			XmlUtil.setString(doc, PATH, Integer.toString(this.moveStep));
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
		T obj = (T) new Integer(this.moveStep);
		return obj;
	}

//	@Override
//	<T> void setConfig(Config.CONFIG_TYPE key, T val)
//	{
//		if (false == (val instanceof Integer))
//		{
//			throw new IllegalArgumentException(String.format("引数%sがIntegerではない", val.getClass().getName()));
//		}
//		this.moveStep = (Integer)val;
//	}

	@Override
	void update()
	{
		if (null == this.panel)
		{
			return;
		}
		int val = this.panel.getMoveStep();
		if (val != this.moveStep)
		{
			this.moveStep = val;
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
		this.panel.setMoveStep(this.moveStep);
	}
}
