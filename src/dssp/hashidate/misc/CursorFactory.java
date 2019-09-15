package dssp.hashidate.misc;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.EnumMap;

import javax.imageio.ImageIO;

import dssp.brailleLib.Util;

public class CursorFactory
{
	public static enum TYPE
	{
		ADD("img/cursor_add.png", 1, 1),
		DEL("img/cursor_del.png", 1, 1),
		ERASE("img/cursor_erase.png", 5, 14);

		private final String fileName;
		private final Point hotSpot;

		/**
		 *
		 * @param fileName カーソルの画像ファイル名
		 * @param x ホットスポット
		 * @param y ホットスポット
		 */
		TYPE(String fileName, int x, int y)
		{
			this.fileName = fileName;
			this.hotSpot = new Point(x,y);
		}
	}

	private static EnumMap<TYPE, Cursor> cursorList = new EnumMap<TYPE, Cursor>(TYPE.class);

	public static void init()
	{
		ClassLoader cl = CursorFactory.class.getClassLoader();
		try
		{
			for (TYPE type: TYPE.values())
			{
				Image img = ImageIO.read(cl.getResource(type.fileName));

				Toolkit kit = Toolkit.getDefaultToolkit();

				// ホットスポットをカーソルの大きさに合わせる
				int w = img.getWidth(null);
				int h = img.getHeight(null);
				Dimension size = kit.getBestCursorSize(w, h);
				Point p = new Point(type.hotSpot.x * size.width/w, type.hotSpot.y * size.height/h);

				Cursor cursor = kit.createCustomCursor(img, p, type.name());
				cursorList.put(type, cursor);
			}
		}
		catch (IOException e)
		{
			Util.logException(e);
		}
	}

	public static Cursor getCursor(TYPE type)
	{
		return cursorList.get(type);
	}
}
