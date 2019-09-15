package dssp.hashidate.shape.helper;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dssp.hashidate.config.Config;
import dssp.brailleLib.Util;
import dssp.brailleLib.XmlUtil;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class AliasResolver
{
	private static final String ENTITY = "entity";
	private static final String UNICODE = "unicode";
	private static final String NAME = "name";
	private static final String ALIAS = "alias";

	private static final Map<String,String> aliasMap = Util.newHashMap();

	private static final AliasResolver instance = new AliasResolver();

	private AliasResolver()
	{
	}

	public static AliasResolver getInstance()
	{
		return AliasResolver.instance;
	}

	public String[] getUnicode(String alias)
	{
		String code = aliasMap.get(alias);
		if (null == code)
		{
			return null;
		}
		StringTokenizer st = new StringTokenizer(code, ",");
		String[] list = new String[st.countTokens()];
		for (int i = 0; st.hasMoreTokens(); i++)
		{
			list[i] = st.nextToken();
		}

		return list;
	}

	public boolean init()
	{
		List<String> fileList = Config.getConfig(Config.MATHML.ALIAS);
		try
		{
			for (String fileName: fileList)
			{
				Util.logInfo("loading %s", fileName);
				File file = new File(fileName);
				if (file.exists())
				{
					Document doc = XmlUtil.parse(file);
					createHashMap(doc);
				}
			}
		}
		catch (Exception ex)
		{
			Util.logException(ex);
		}

		return true;
	}

	private void createHashMap(Document doc)
	{
		NodeList entityList = doc.getElementsByTagName(ENTITY);
		for (int i = 0; i < entityList.getLength(); i++)
		{
			Element entity = (Element)entityList.item(i);

			String unicode = entity.getAttribute(UNICODE);

			String name = entity.getAttribute(NAME);
			aliasMap.put(name, unicode);

			NodeList aliasList = entity.getElementsByTagName(ALIAS);
			for (int j = 0; j < aliasList.getLength(); j++)
			{
				Element alias = (Element)aliasList.item(j);
				String aliasText = alias.getTextContent();
				if (0 < aliasText.length())
				{
					aliasMap.put(aliasText, unicode);
				}
			}
		}
	}
}
