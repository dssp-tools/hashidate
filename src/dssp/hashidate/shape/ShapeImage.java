package dssp.hashidate.shape;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.filechooser.FileNameExtensionFilter;

import dssp.brailleLib.Util;
import dssp.hashidate.DesignPanel;
import dssp.hashidate.io.ExportBase;
import dssp.hashidate.io.ShapeInfo;
import dssp.hashidate.misc.FigureType;
import dssp.hashidate.misc.PairList;

public class ShapeImage extends ShapeRectangle
{
	private BufferedImage image = null;
	private String fileName = null;

	public ShapeImage()
	{
		super();
		this.shape = SHAPE.IMAGE;
		this.fillType = FigureType.FILL_TYPE.SOLID;
	}

	public ShapeImage(Point p)
	{
		super(p);
		this.shape = SHAPE.IMAGE;
		this.fillType = FigureType.FILL_TYPE.SOLID;
	}

	@Override
	public ShapeImage clone()
	{
		ShapeImage obj = (ShapeImage) super.clone();

		obj.base = (Rectangle2D.Double) this.base.clone();
		obj.fileName = this.fileName;
		obj.image = new BufferedImage(this.image.getWidth(), this.image.getHeight(), this.image.getType());
		Graphics g = obj.image.getGraphics();
		g.drawImage(this.image, 0, 0, null);
		g.dispose();

		return obj;
	}

	@Override
	public boolean init(DesignPanel panel)
	{
		FileNameExtensionFilter[] extList = {
				new FileNameExtensionFilter("画像ファイル", "png", "jpg", "jpeg", "bmp", "gif")
				};
		File file = Util.selectFile(this.fileName, null, extList, false);
		if (null == file)
		{
			return false;
		}

		try
		{
			this.image = ImageIO.read(file);
			this.base.width = this.image.getWidth(null);
			this.base.height = this.image.getHeight(null);
			this.adaptFrame();
		}
		catch (IOException e)
		{
			Util.logException(e);
			return false;
		}

		return true;
	}

	@Override
	protected void drawSumiji(Graphics2D g, boolean printing)
	{
		g.drawImage(this.image, this.x, this.y, this.width, this.height, null);
		boolean braille = false;
		if (isSelected() && false == printing)
		{
			this.setDrawProperty(g, braille, true);
			g.drawRect(this.x, this.y, this.width, this.height);
			this.setDrawProperty(g, braille, false);

			drawToggle(g, this.x, this.y);
			drawToggle(g, this.x + this.width, this.y);
			drawToggle(g, this.x + this.width, this.y + this.height);
			drawToggle(g, this.x, this.y + this.height);
		}
//		if (isSelected())
//		{
//			super.drawSumiji(g, printing);
//		}
	}

	@Override
	protected void drawBraille(Graphics2D g, boolean printing)
	{
		if (printing)
		{
			return;
		}
		Color c = g.getColor();
		g.setColor(Color.LIGHT_GRAY);
		g.drawRect(this.x, this.y, this.width, this.height);
		g.setColor(c);
	}

	@Override
	public boolean showProperty(DesignPanel panel, Point p)
	{
		this.changed = this.init(panel);
		return true;
	}

	private static class TAG extends TAG_BASE
	{
		public static final String HREF = "href";
	}

	@Override
	public void export(ExportBase export)
	{
		ShapeImage copy = this.clone();
		copy.resetOrigin(export.getOrigin());

		ShapeInfo info = new ShapeInfo(SHAPE.IMAGE);
		info.obj = copy;

		PairList list = this.makeDesc(copy);

		info.setDesc(list);

		export.writeStart(info);
		export.write(info);
		export.writeEnd(info);
	}

	public static ShapeImage parse(ShapeInfo info)
	{
		ShapeImage obj = new ShapeImage();
		obj.shape = info.getType();

		PairList list = info.getDesc(null);
		List<Object> vals = list.getValues(TAG.CLASS);

		Object val = null;
		obj.parseDesc(list);
		vals = list.getValues(TAG.X);
		if (0 < vals.size() && (val = vals.get(0)) instanceof String)
		{
			obj.base.x = java.lang.Double.parseDouble((String) val);
		}
		vals = list.getValues(TAG.Y);
		if (0 < vals.size() && (val = vals.get(0)) instanceof String)
		{
			obj.base.y = java.lang.Double.parseDouble((String) val);
		}
		vals = list.getValues(TAG.WIDTH);
		if (0 < vals.size() && (val = vals.get(0)) instanceof String)
		{
			obj.base.width = java.lang.Double.parseDouble((String) val);
		}
		vals = list.getValues(TAG.HEIGHT);
		if (0 < vals.size() && (val = vals.get(0)) instanceof String)
		{
			obj.base.height = java.lang.Double.parseDouble((String) val);
		}
		vals = list.getValues(TAG.HREF);
		if (0 < vals.size() && (val = vals.get(0)) instanceof String)
		{
			String[] tokens = ((String)val).split("[:;,]");
			if (4 > tokens.length)
			{
				return null;
			}
			String mime = tokens[1];
			String data = tokens[3];
			obj.image = Util.decodeImage(mime, data);
		}

		return obj;
	}
}
