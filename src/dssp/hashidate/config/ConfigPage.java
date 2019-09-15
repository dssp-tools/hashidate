package dssp.hashidate.config;

import java.awt.Dimension;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dssp.hashidate.config.gui.ConfigPagePanel;
import dssp.brailleLib.Util;
import dssp.brailleLib.XmlUtil;

class ConfigPage extends Config.ConfigBase
{
	// ページ
	private static final String PATH = "CONFIG/PAGE/INFO";
	private static final String PATH_DEFAULT = "CONFIG/PAGE/DEFAULT";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_WIDTH = "width";
	private static final String ATTR_HEIGHT = "height";
	private enum PAGE_DEFAULT
	{
		A4(new PageInfo("A4", 210, 297)),
		B5(new PageInfo("B5", 192, 257));

		public PageInfo PAGEINFO;
		PAGE_DEFAULT(PageInfo info)
		{
			this.PAGEINFO = info;
		}
	}
	private final Map<String, PageInfo> map = Util.newHashMap();
	private PageInfo defPage = PAGE_DEFAULT.A4.PAGEINFO;

	private ConfigPagePanel panel;

	private static final ConfigPage instance = new ConfigPage();

	private ConfigPage()
	{
	}

	static ConfigPage getInstance()
	{
		return instance;
	}

	@Override
	void load(Document doc)
	{
		for (PAGE_DEFAULT page: PAGE_DEFAULT.values())
		{
			this.map.put(page.PAGEINFO.getName(), page.PAGEINFO);
		}
		try
		{
			NodeList nodeList = XmlUtil.getNodeList(doc, PATH);
			int nNode = nodeList.getLength();
			if (0 == nNode)
			{
				Config.requireUpdate();
			}
			else
			{
				for (int i = 0; i < nNode; i++)
				{
					Node node = nodeList.item(i);
					if (node instanceof Element)
					{
						Element elm = (Element)node;
						String name = elm.getAttribute(ATTR_NAME);
						String sw = elm.getAttribute(ATTR_WIDTH);
						int w;
						try
						{
							w = Integer.parseInt(sw);
						}
						catch (NumberFormatException ex)
						{
							Util.logException(ex);
							continue;
						}
						String sh = elm.getAttribute(ATTR_HEIGHT);
						int h;
						try
						{
							h = Integer.parseInt(sh);
						}
						catch (NumberFormatException ex)
						{
							Util.logException(ex);
							continue;
						}

						PageInfo info = new PageInfo(name, w, h);
						this.map.put(name, info);
					}
				}
			}

			String name = XmlUtil.getString(doc, PATH_DEFAULT);
			PageInfo info = this.map.get(name);
			if (null != info)
			{
				this.defPage = info;
			}
			if (name.isEmpty())
			{
				Config.requireUpdate();
			}
		}
		catch (Exception ex)
		{
			Util.logException(ex);
		}
	}

	@Override
	void save(Document doc)
	{
		try
		{
			for (PageInfo info: this.map.values())
			{
				Dimension size = info.getSize();

				Element elm = XmlUtil.addElement(doc, PATH, true);
				elm.setAttribute(ATTR_NAME, info.getName());
				elm.setAttribute(ATTR_WIDTH, Integer.toString(size.width));
				elm.setAttribute(ATTR_HEIGHT, Integer.toString(size.height));
			}
			XmlUtil.setString(doc, PATH_DEFAULT, this.defPage.getName());
		}
		catch (Exception ex)
		{
			Util.logException(ex);
		}

		this.isUpdated = false;
	}

	@Override
	@SuppressWarnings("unchecked")
	<T> T getConfig(Config.CONFIG_TYPE key)
	{
		T obj = null;
		switch((Config.PAGE)key)
		{
		case CURRENT:
			obj = (T) this.defPage;
			break;
		case LIST:
			List<PageInfo> list = Util.newArrayList();
			list.addAll(this.map.values());
			obj = (T) list;
		}
		return obj;
	}

//	@Override
//	<T> void setConfig(Config.CONFIG_TYPE key, T val)
//	{
//		switch((Config.PAGE)key)
//		{
//		case CURRENT:
//			if (false == (val instanceof PageInfo))
//			{
//				throw new IllegalArgumentException(String.format("引数%sがPageInfoではない", val.getClass().getName()));
//			}
//			this.defPage = (PageInfo)val;
//			break;
//		case LIST:
//			if (false == (val instanceof List))
//			{
//				throw new IllegalArgumentException(String.format("引数%sがListではない", val.getClass().getName()));
//			}
//			List<?> list = (List<?>)val;
//			for (Object obj: list)
//			{
//				if (false == (obj instanceof PageInfo))
//				{
//					throw new IllegalArgumentException(String.format("引数の要素%sがPageInfoではない", obj.getClass().getName()));
//				}
//			}
//			for (Object obj: list)
//			{
//				PageInfo info = (PageInfo)obj;
//				this.map.put(info.getName(), info);
//			}
//		}
//	}

	@Override
	void update()
	{
		List<PageInfo> list = this.panel.getPageInfo();
		if (list.size() != this.map.size())
		{
			this.isUpdated = true;
		}
		else
		{
			for (PageInfo info: list)
			{
				Dimension size = info.getSize();
				if (1 > size.width || 1 > size.height)
				{
					this.isUpdated = false;
					return;
				}
				if (false == this.map.containsValue(info))
				{
					this.isUpdated = true;
					break;
				}
			}
		}

		if (this.isUpdated)
		{
			this.map.clear();
			for (PageInfo info: list)
			{
				this.map.put(info.getName(), info);
			}
		}

		int index = this.panel.getDefault();
		PageInfo info = list.get(index);
		if (false == this.defPage.equals(info))
		{
			this.defPage = info;
			this.isUpdated = true;
		}
	}


	@Override
	String getTitle()
	{
		return "用紙";
	}

	@Override
	JPanel getPanel()
	{
		if (null == this.panel)
		{
			this.panel = new ConfigPagePanel();
		}
		this.resetPanel();

		return this.panel;
	}

	@Override
	void resetPanel()
	{
		if (null != this.panel)
		{
			this.panel.setPageInfo(this.map.values(), this.defPage);
		}
	}
}
