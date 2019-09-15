package dssp.hashidate.config;

import javax.swing.JPanel;

import org.w3c.dom.Document;

import dssp.hashidate.config.Config.CONFIG_TYPE;
import dssp.hashidate.config.Config.ConfigBase;
import dssp.hashidate.config.gui.ConfigMiscPanel;

public class ConfigMisc extends ConfigBase
{
	private ConfigMiscPanel panel;

	private static ConfigMisc instance = new ConfigMisc();

	public static ConfigMisc getInstance()
	{
		return instance;
	}

	@Override
	void load(Document doc)
	{
		ConfigBase obj = ConfigMoveStep.getInstance();
		obj.load(doc);

		obj = ConfigSVG.getInstance();
		obj.load(doc);
	}

	@Override
	void save(Document doc)
	{
		ConfigBase obj = ConfigMoveStep.getInstance();
		obj.save(doc);

		obj = ConfigSVG.getInstance();
		obj.save(doc);

		this.isUpdated = false;
	}

	@SuppressWarnings("unchecked")
	@Override
	<T> T getConfig(CONFIG_TYPE key)
	{
		T obj = null;
		switch ((Config.MISC)key)
		{
		case MOVE_STEP:
			obj = (T) ConfigMoveStep.getInstance().getConfig(key);
			break;
		case PLAIN:
		case ITALIC:
			obj = (T) ConfigSVG.getInstance().getConfig(key);
			break;
		}
		return obj;
	}

	@Override
	void update()
	{
		ConfigBase obj = ConfigMoveStep.getInstance();
		obj.update();
		this.isUpdated = obj.isUpdated;

		obj = ConfigSVG.getInstance();
		obj.update();
		this.isUpdated |= obj.isUpdated;
	}

	@Override
	String getTitle()
	{
		return "その他";
	}

	@Override
	JPanel getPanel()
	{
		if (null == panel)
		{
			this.panel = new ConfigMiscPanel();
			ConfigMoveStep.getInstance().setPanel(this.panel);
			ConfigSVG.getInstance().setPanel(this.panel);
		}
		this.resetPanel();

		return this.panel;
	}

	@Override
	void resetPanel()
	{
		ConfigBase obj = ConfigMoveStep.getInstance();
		obj.resetPanel();

		obj = ConfigSVG.getInstance();
		obj.resetPanel();
	}

}
