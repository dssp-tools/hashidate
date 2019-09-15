package dssp.hashidate.misc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import dssp.hashidate.misc.FigureType.FILL_TYPE;

public class FillPaint extends TexturePaint
{

	protected static final int FILL_SIZE = 5;
	public static Paint create(FILL_TYPE type, Color color, int size)
	{
		Paint obj = null;
		BufferedImage image;
		int area = FILL_SIZE * size;
		int offSet = size;
		switch(type)
		{
		case SOLID:
			obj = color;
			break;
		case DOT:
			image = new BufferedImage(area, area, BufferedImage.TYPE_3BYTE_BGR);
			{
				Graphics g = image.getGraphics();
				final int[] xs = {offSet , area/2 + offSet};
				final int[] ys = {offSet , area/2 + offSet};
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, area, area);
				g.setColor(color);
				g.fillOval(xs[0], ys[0], size, size);
				g.fillOval(xs[1], ys[1], size, size);
			}
			obj = new FillPaint(image, new Rectangle2D.Float(0, 0, image.getWidth(), image.getHeight()));
			break;
		case DIAGONAL:
			image = new BufferedImage(area, area, BufferedImage.TYPE_3BYTE_BGR);
			{
				Graphics2D g = (Graphics2D) image.getGraphics();
				final int[] xs = {-offSet, area - offSet, 2*area - offSet};
				final int[] ys = {0, area};
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, area, area);
				g.setColor(color);
				g.setStroke(new BasicStroke(size));
				g.drawLine(xs[1], ys[0], xs[0], ys[1]);
				g.drawLine(xs[2], ys[0], xs[1], ys[1]);
			}
			obj = new FillPaint(image, new Rectangle2D.Float(0, 0, image.getWidth(), image.getHeight()));
			break;
		default:
		}

		return obj;
	}

	private FillPaint(BufferedImage arg0, Rectangle2D arg1)
	{
		super(arg0, arg1);
	}
}
