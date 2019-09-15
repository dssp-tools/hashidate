package dssp.hashidate.misc;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import dssp.hashidate.misc.FigureType.FILL_TYPE;

public class BraillePaint extends TexturePaint
{
	protected static final int FILL_SIZE = 5;
	public static Paint create(FILL_TYPE type, Color color, int size, float dotSpan, boolean front)
	{
		Paint obj = null;
		BufferedImage image;
		int area = FILL_SIZE * size;
		int offSet = size;
		switch(type)
		{
		case SOLID:
			image = new BufferedImage(area, area, BufferedImage.TYPE_3BYTE_BGR);
			{
				Graphics2D g = (Graphics2D) image.getGraphics();
				final int[] xs = {offSet , area/2 + offSet};
				final int[] ys = {offSet , area/2 + offSet};
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, area, area);
				g.setColor(color);
				drawDot(g, xs[0], ys[0], size, front);
				drawDot(g, xs[0], ys[1], size, front);
				drawDot(g, xs[1], ys[0], size, front);
				drawDot(g, xs[1], ys[1], size, front);
			}
			obj = new BraillePaint(image, new Rectangle2D.Float(0, 0, image.getWidth(), image.getHeight()));
			break;
		case DOT:
			image = new BufferedImage(area, area, BufferedImage.TYPE_3BYTE_BGR);
			{
				Graphics2D g = (Graphics2D) image.getGraphics();
				final int[] xs = {offSet , area/2 + offSet};
				final int[] ys = {offSet , area/2 + offSet};
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, area, area);
				g.setColor(color);
				drawDot(g, xs[0], ys[0], size, front);
				drawDot(g, xs[1], ys[1], size, front);
			}
			obj = new BraillePaint(image, new Rectangle2D.Float(0, 0, image.getWidth(), image.getHeight()));
			break;
		case DIAGONAL:
			image = new BufferedImage(area, area, BufferedImage.TYPE_3BYTE_BGR);
			{
				Graphics2D g = (Graphics2D) image.getGraphics();
				final int[] xs = {-offSet, area + offSet, area-offSet, 2*area + offSet};
				final int[] ys = {-offSet, area + offSet};
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, area, area);
				g.setColor(color);
				g.setStroke(new BasicStroke(size));
				drawLine(g, xs[1], ys[0], xs[0], ys[1], size, dotSpan, front);
				drawLine(g, xs[3], ys[0], xs[2], ys[1], size, dotSpan, front);
			}
			obj = new BraillePaint(image, new Rectangle2D.Float(0, 0, image.getWidth(), image.getHeight()));
			break;
		default:
		}

		return obj;
	}

	private static Stroke bound = new BasicStroke(1.0f);
	private static void drawDot(Graphics2D g, int x, int y, int size, boolean front)
	{
		if (front)
		{
			g.fillOval(x - size/2, y - size/2, size, size);
		}
		else
		{
			Stroke b = g.getStroke();
			g.setStroke(bound);
			g.drawOval(x - size/2, y - size/2, size, size);
			g.setStroke(b);
		}
	}

	private static void drawLine(Graphics2D g, int sx, int sy, int ex, int ey, int size, float dotSpan, boolean front)
	{
		Point2D.Float prev = new Point2D.Float(sx, sy);
		Point2D.Float p = new Point2D.Float(ex, ey);
		Point2D.Float dp = new Point2D.Float(p.x - prev.x, p.y - prev.y);
		double dist = (float) Math.sqrt(dp.x*dp.x + dp.y*dp.y);
		float dd = dotSpan;
		while (dist > dd)
		{
			prev.setLocation(dd*dp.x/dist + prev.x, dd*dp.y/dist + prev.y);
			drawDot(g, (int)Math.round(prev.x), (int)Math.round(prev.y), size, front);
			dp.setLocation(p.x - prev.x, p.y - prev.y);
			dist = (float) Math.sqrt(dp.x*dp.x + dp.y*dp.y);
		}
	}

	private BraillePaint(BufferedImage arg0, Rectangle2D arg1)
	{
		super(arg0, arg1);
	}

}
