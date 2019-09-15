package dssp.hashidate.config;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import dssp.hashidate.config.gui.ConfigBraillePanel;
import dssp.brailleLib.BrailleDict;
import dssp.brailleLib.Util;
import dssp.brailleLib.XmlUtil;

public class ConfigBraille extends Config.ConfigBase
{
	// 点字辞書
	private ConfigBraillePanel panel = null;

	public static enum BRAILLE
	{
		TEXT("CONFIG/BRAILLE/BRAILLE/TEXT", true);

		final private String path;
		private Object defVal;

		BRAILLE(String path, Object defVal)
		{
			this.path = path;
			this.defVal = defVal;
		}

		String getPath()
		{
			return this.path;
		}

		Object getDefValue()
		{
			return this.defVal;
		}
	}

	private boolean textBraille;

	public static enum DICT
	{
		TEXT("CONFIG/BRAILLE/DICT/TEXT", "braille.xml"),
		FORMULA("CONFIG/BRAILLE/DICT/FORMULA", "braille.xml", "equation.xml");

		public final String PATH;
		private final List<String> fileList = Util.newArrayList();
		private BrailleDict dict = new BrailleDict();

		DICT(String path, String... files)
		{
			this.PATH = path;
			for (String file: files)
			{
				this.fileList.add(file);
			}
		}

		public String[] getFiles()
		{
			String[] files = new String[this.fileList.size()];
			this.fileList.toArray(files);
			return files;
		}

		public void setFiles(Collection<String> files)
		{
			this.fileList.clear();
			this.fileList.addAll(files);
		}

		private static String SEPARATOR = ",";
		public String getFileList()
		{
			StringBuilder b = new StringBuilder();
			for (String file: this.fileList)
			{
				if (0 < b.length())
				{
					b.append(SEPARATOR);
				}
				b.append(file);
			}

			return b.toString();
		}

		public void setFileList(String files)
		{
			String[] list = files.split(SEPARATOR);
			this.setFiles(Arrays.asList(list));
		}

		public BrailleDict getDict()
		{
			return this.dict;
		}
	}

	/**
	 * 点のサイズの設定
	 *
	 * @author yagi
	 *
	 */
	public static enum DOT
	{
		/**
		 * 小さい点
		 */
		SMALL("CONFIG/BRAILLE/DOT/SMALL", "小点", 5, Config.BRAILLE.SMALL),
		/**
		 * 普通の点
		 */
		MIDDLE("CONFIG/BRAILLE/DOT/MIDDLE", "中点", 14, Config.BRAILLE.MIDDLE),
		/**
		 * 大きい点
		 */
		LARGE("CONFIG/BRAILLE/DOT/LARGE", "大点", 16, Config.BRAILLE.LARGE),
		/**
		 * 点字：点の横方向の間隔
		 */
		SPACE_X("CONFIG/BRAILLE/DOT/SPACE_X", "横点間", 21, Config.BRAILLE.SPACE_X),
		/**
		 * 点字：点の縦方向の間隔
		 */
		SPACE_Y("CONFIG/BRAILLE/DOT/SPACE_Y", "縦点間", 24, Config.BRAILLE.SPACE_Y),
		/**
		 * 点字：行の間隔
		 */
		LINE_SPACE("CONFIG/BRAILLE/LINE_SPACE", "行間", 92, Config.BRAILLE.LINE_SPACE),
		/**
		 * 点字：マスの間隔
		 */
		BOX_SPACE("CONFIG/BRAILLE/BOX_SPACE", "マス間", 30, Config.BRAILLE.BOX_SPACE),
		/**
		 * 点図：点の間隔
		 */
		DOT_SPAN("CONFIG/BRAILLE/DOT/SPAN", "点間隔", 28, Config.BRAILLE.DOT_SPAN);

		final String path;
		// 点のサイズ(0.1mm単位)
		final int defSize;
		final String name;
		private int size;

		DOT(String path, String name, int size, Config.BRAILLE type)
		{
			this.path = path;
			this.name = name;
			this.defSize = size;
			this.size = size;
			map.put(type, this);
		}

		public boolean setSize(int size)
		{
			if (0 > size)
			{
				return false;
			}

			this.size = size;
			return true;
		}

		public int getSize()
		{
			return this.size;
		}

		public String getEnum()
		{
			return super.toString();
		}

		@Override
		public String toString()
		{
			return this.name;
		}
	}

	public static enum DOT_TYPE
	{
		BRAILLE("CONFIG/BRAILLE/DOT_TYPE/BRAILLE", DOT.MIDDLE, Config.BRAILLE.BRAILLE),
		FIGURE("CONFIG/BRAILLE/DOT_TYPE/FIGURE", DOT.MIDDLE, Config.BRAILLE.FIGURE);

		final String path;
		final DOT defSize;
		DOT size;

		DOT_TYPE(String path, DOT defSize, Config.BRAILLE type)
		{
			this.path = path;
			this.defSize = defSize;
			typeMap.put(type, this);
		}

		public void setSize(DOT size)
		{
			if (null == size)
			{
				throw new IllegalArgumentException("sizeがnull");
			}
			this.size = size;
		}

		public DOT getDot()
		{
			return this.size;
		}

		public int getSize()
		{
			return this.size.getSize();
		}
	}

	private static final Map<Config.BRAILLE, DOT> map = Util.newHashMap();
	private static final Map<Config.BRAILLE, DOT_TYPE> typeMap = Util.newHashMap();

	private static final ConfigBraille instance = new ConfigBraille();
//	private BrailleDict dict = null;

	private ConfigBraille()
	{
	}

	static ConfigBraille getInstance()
	{
		return instance;
	}

	@Override
	void load(Document doc)
	{
		this.textBraille = Config.loadBoolean(doc, BRAILLE.TEXT.getPath(), (boolean) BRAILLE.TEXT.getDefValue());

		// 辞書
		for (DICT type: DICT.values())
		{
			String list = Config.loadString(doc, type.PATH, type.getFileList());
			type.setFileList(list);

			BrailleDict dict = type.getDict();
			String[] files = type.getFiles();
			for (String name: files)
			{
				File file = new File(Util.exePath(name));
				if (file.exists())
				{
					dict.load(file);
				}
				else
				{
					String path = Util.exePath(name);
					file = new File(path);
					if (false == file.exists())
					{
						Util.error(String.format("辞書ファイル%sは見つかりません", path));
					}
				}
			}
		}

//		this.dict = new BrailleDict();
//		try
//		{
//			String fileNames = Config.loadString(doc, PATH_DICT, DEFAULT_DICT);
//			if (fileNames.isEmpty())
//			{
//				fileNames = DEFAULT_DICT;
//				Config.requireUpdate();
//			}
//
//			String[] fileNameList = fileNames.split(SEPARATOR);
//			for (String fileName: fileNameList)
//			{
//				File file = Util.getFile(fileName);
//				if (this.dict.load(file))
//				{
//					brailleDictList.add(fileName);
//				}
//			}
//		}
//		catch (Exception ex)
//		{
//			Util.logException(ex);
//		}

		// 点のサイズ
		for (DOT dot: DOT.values())
		{
			dot.setSize(Config.loadInt(doc, dot.path, dot.defSize));
		}

		// 点の種類
		for (DOT_TYPE type: DOT_TYPE.values())
		{
			DOT dot = DOT.valueOf(Config.loadString(doc, type.path, type.defSize.getEnum()));
			type.setSize(dot);
		}
	}

	@Override
	void save(Document doc)
	{
		try
		{
			XmlUtil.setString(doc, BRAILLE.TEXT.getPath(), Boolean.toString(this.textBraille));
		}
		catch (XPathExpressionException e1)
		{
			Util.logException(e1);
		}

		// 辞書
		for (DICT type: DICT.values())
		{
			String files = type.getFileList();
			try
			{
				XmlUtil.setString(doc, type.PATH, files);
			}
			catch (XPathExpressionException e)
			{
				Util.logException(e);
			}
		}
//		StringBuilder names = new StringBuilder();
//		for(String name: this.brailleDictList)
//		{
//			if (0 < names.length())
//			{
//				names.append(SEPARATOR);
//			}
//			names.append(name);
//		}
//		try
//		{
//			XmlUtil.setString(doc, PATH_DICT, names.toString());
//		}
//		catch (Exception ex)
//		{
//			Util.logException(ex);
//		}

		// 点のサイズ
		for (DOT dot: DOT.values())
		{
			try
			{
				XmlUtil.setString(doc, dot.path, Integer.toString(dot.size));
			}
			catch (Exception ex)
			{
				Util.logException(ex);
			}
		}

		// 点の種類
		for (DOT_TYPE type: DOT_TYPE.values())
		{
			try
			{
				XmlUtil.setString(doc, type.path, type.getDot().getEnum());
			}
			catch (Exception ex)
			{
				Util.logException(ex);
			}
		}

		this.isUpdated = false;
	}

	@SuppressWarnings("unchecked")
	@Override
	<T> T getConfig(Config.CONFIG_TYPE key)
	{
		T obj = null;
		switch((Config.BRAILLE)key)
		{
		case DICT_TEXT:
			obj = (T) DICT.TEXT.getDict();
			break;
		case DICT_FORMULA:
			obj = (T) DICT.FORMULA.getDict();
			break;
		case BRAILLE:
		case FIGURE:
			DOT_TYPE type = typeMap.get(key);
			{
				DOT dot = type.getDot();
				switch(dot)
				{
				case SMALL:
					obj = (T) Config.BRAILLE.SMALL;
					break;
				case MIDDLE:
					obj = (T) Config.BRAILLE.MIDDLE;
					break;
				case LARGE:
					obj = (T) Config.BRAILLE.LARGE;
					break;
				default:
				}
			}
			break;
		case BRAILLE_TEXT:
			obj = (T) Boolean.valueOf(this.textBraille);
			break;
		default:
			DOT dot = map.get(key);
			if (null != dot)
			{
				obj = (T) new Integer(dot.size);
			}
		}
		return obj;
	}

//	@Override
//	<T> void setConfig(Config.CONFIG_TYPE key, T val)
//	{
//		switch((Config.BRAILLE)key)
//		{
//		case DICT:
//			break;
//		case LIST:
//			if (false == (val instanceof List))
//			{
//				throw new IllegalArgumentException(String.format("引数%sがファイル名のリストではない", val.getClass().getName()));
//			}
//			List<?> list = (List<?>) val;
//			if (0 == list.size())
//			{
//				this.brailleDictList.clear();
//			}
//
//			String name = (String)val;
//			File file = new File(name);
//			if (file.exists())
//			{
//				this.dict.load(file);
//			}
//			this.brailleDictList.add(name);
//			break;
//		case TEXT:
//		case FIGURE:
//			break;
//		default:
//		}
//	}

	@Override
	void update()
	{
		if (null == this.panel)
		{
			return;
		}

		boolean flag = this.panel.getTextBraille();
		if (flag != this.textBraille)
		{
			this.textBraille = flag;
			this.isUpdated = true;
		}

		for (DICT type: DICT.values())
		{
			List<String> list = this.panel.getFiles(type);
			if (false == Arrays.deepEquals(list.toArray(), type.getFiles()))
			{
				type.setFiles(list);
				this.isUpdated = true;
			}
		}

		for (DOT dot: DOT.values())
		{
			int size = this.panel.getDotSize(dot);
			if (size != dot.getSize())
			{
				dot.setSize(size);
				this.isUpdated = true;
			}
		}

		for (DOT_TYPE type: DOT_TYPE.values())
		{
			DOT dot = this.panel.getDotType(type);
			if (dot != type.getDot())
			{
				type.setSize(dot);
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
		return "点字";
	}

	@Override
	JPanel getPanel()
	{
		if (null == this.panel)
		{
			this.panel = new ConfigBraillePanel();
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
		this.panel.setTextBraille(this.textBraille);
		for (DICT type: DICT.values())
		{
			this.panel.setFiles(type);
		}
		this.panel.setDotSize(DOT.values());
		this.panel.setDotType(DOT_TYPE.values());
	}
}
