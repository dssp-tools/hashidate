package dssp.hashidate.misc;

import javax.swing.ImageIcon;

public class FigureType
{
	public static enum LINE_TYPE
	{
		/**
		 * 実線
		 */
		SOLID("img/solid.png"),
		/**
		 * 点線
		 */
		DOT("img/dot.png"),
		/**
		 * 破線
		 */
		DASHED("img/dashed.png");

		private ImageIcon icon;
		float[] dashArray;

		LINE_TYPE(String path)
		{
			ClassLoader cl = LINE_TYPE.class.getClassLoader();
			this.icon = new ImageIcon(cl.getResource(path));
		}

		public ImageIcon getIcon()
		{
			return this.icon;
		}

		public static LINE_TYPE getType(int index)
		{
			int ret = 0;
			for (LINE_TYPE type: LINE_TYPE.values())
			{
				if (ret == index)
				{
					return type;
				}
				ret++;
			}

			return null;
		}

		public static int getIndex(LINE_TYPE t)
		{
			int ret = 0;
			for (LINE_TYPE type: LINE_TYPE.values())
			{
				if (type == t)
				{
					return ret;
				}
				ret++;
			}
			return -1;
		}

		/**
		 * nameの値を取得する
		 *
		 * @param name
		 * @return 値が見つからない場合は既定値
		 */
		public static LINE_TYPE getValueOf(String name)
		{
			LINE_TYPE ret;
			try
			{
				ret = valueOf(name);
			}
			catch(IllegalArgumentException ex)
			{
				ret = LINE_TYPE.SOLID;
			}

			return ret;
		}
	}

	public static enum EDGE_TYPE
	{
		BUTT("img/solid.png"),
		RARROW("img/rarrow.png"),
		LARROW("img/larrow.png"),
		BARROW("img/barrow.png"),
		RARROW1("img/rarrow1.png"),
		LARROW1("img/larrow1.png"),
		BARROW1("img/barrow1.png");

		private ImageIcon icon;
		EDGE_TYPE(String path)
		{
			ClassLoader cl = LINE_TYPE.class.getClassLoader();
			this.icon = new ImageIcon(cl.getResource(path));
		}

		public ImageIcon getIcon()
		{
			return this.icon;
		}

		public static EDGE_TYPE getType(int index)
		{
			int ret = 0;
			for (EDGE_TYPE type: EDGE_TYPE.values())
			{
				if (ret == index)
				{
					return type;
				}
				ret++;
			}

			return null;
		}

		public static int getIndex(EDGE_TYPE t)
		{
			int ret = 0;
			for (EDGE_TYPE type: EDGE_TYPE.values())
			{
				if (type == t)
				{
					return ret;
				}
				ret++;
			}
			return -1;
		}

		/**
		 * nameの値を取得する
		 *
		 * @param name
		 * @return 値が見つからない場合は既定値
		 */
		public static EDGE_TYPE getValueOf(String name)
		{
			EDGE_TYPE ret;
			try
			{
				ret = valueOf(name);
			}
			catch(IllegalArgumentException ex)
			{
				ret = EDGE_TYPE.BUTT;
			}

			return ret;
		}
	}

	public static enum FILL_TYPE
	{
		/**
		 * 塗りつぶしなし
		 */
		TRANSPARENT("img/fillTransparent.png"),
		/**
		 * 単色
		 */
		SOLID("img/fillSolid.png"),
		/**
		 * ドット
		 */
		DOT("img/fillDot.png"),
		/**
		 * 斜線
		 */
		DIAGONAL("img/fillDiagonal.png");

		private ImageIcon icon;

		FILL_TYPE(String path)
		{
			ClassLoader cl = FILL_TYPE.class.getClassLoader();
			this.icon = new ImageIcon(cl.getResource(path));
		}

		public ImageIcon getIcon()
		{
			return this.icon;
		}

		public static FILL_TYPE getType(int index)
		{
			int ret = 0;
			for (FILL_TYPE type: FILL_TYPE.values())
			{
				if (ret == index)
				{
					return type;
				}
				ret++;
			}

			return null;
		}

		public static int getIndex(FILL_TYPE t)
		{
			int ret = 0;
			for (FILL_TYPE type: FILL_TYPE.values())
			{
				if (type == t)
				{
					return ret;
				}
				ret++;
			}
			return -1;
		}

		/**
		 * nameの値を取得する
		 *
		 * @param name
		 * @return 値が見つからない場合は既定値
		 */
		public static FILL_TYPE getValueOf(String name)
		{
			FILL_TYPE ret;
			try
			{
				ret = valueOf(name);
			}
			catch(IllegalArgumentException ex)
			{
				ret = FILL_TYPE.TRANSPARENT;
			}

			return ret;
		}
	}
}
