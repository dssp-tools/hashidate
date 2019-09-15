package dssp.hashidate.shape;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.List;

import dssp.brailleLib.Util;

public class MaskLine
{
	private static BasicStroke pathStroke = new BasicStroke(1.0f);
	private static AlphaComposite maskAreaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f);
	private static AlphaComposite maskComp = AlphaComposite.getInstance(AlphaComposite.CLEAR, 1.0f);
	private static int R = 8;
	public static enum MASK_SIZE
	{
		NARROW(5),
		MIDDLE(10),
		WIDE(20);

		private int size;

		MASK_SIZE(int size)
		{
			this.size = size;
		}

		int getSize()
		{
			return this.size;
		}
	}

	private MASK_SIZE size = MASK_SIZE.MIDDLE;
	private List<Point2D.Float> points = Util.newArrayList();
	private BasicStroke boundStroke = new BasicStroke(this.size.getSize(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);

	private static final String POINT_SEPARATOR = ";";
	private static final String COORD_SEPARATOR = ",";

	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		for (Point2D.Float p: this.points)
		{
			if (0 < buf.length())
			{
				buf.append(POINT_SEPARATOR);
			}
			buf.append(String.format("%f%s%f", p.x, COORD_SEPARATOR, p.y));
		}

		return buf.toString();
	}

	public static MaskLine fromString(String text)
	{
		MaskLine ml = new MaskLine();

		String[] ps = text.split(POINT_SEPARATOR);
		if (0 == ps.length)
		{
			return null;
		}
		for (String pt: ps)
		{
			String[] cs = pt.split(COORD_SEPARATOR);
			if (2 > cs.length)
			{
				return null;
			}
			float x = Float.parseFloat(cs[0]);
			float y = Float.parseFloat(cs[1]);
			ml.expand(x, y, false);
		}

		return ml;
	}

	public void setSize(MASK_SIZE size)
	{
		this.size = size;
//		this.boundStroke = new BasicStroke(this.size.getSize(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
		this.boundStroke = new BasicStroke(this.size.getSize(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	}

	public boolean hitTest(int x, int y)
	{
		return !(0 > this.getControl(x, y));
	}

	public int getControl(int x, int y)
	{
		int index = -1;
		for (int i = 0; i < this.points.size(); ++i)
		{
			Point2D.Float lp = this.points.get(i);
			Rectangle rect = new Rectangle((int)lp.x - R/2, (int)lp.y + R/2, R, R);
			if (rect.contains(x, y))
			{
				index = i;
				break;
			}
		}

		return index;
	}

	public void removePoint(int index)
	{
		this.points.remove(index);
	}

	public Point2D.Float getPoint(int index)
	{
		return this.points.get(index);
	}

	public int countPoint()
	{
		return this.points.size();
	}

	public void move(float dx, float dy)
	{
		for (Point2D.Float p: this.points)
		{
			p.x += dx;
			p.y += dy;
		}
	}

	public void expand(float x, float y, boolean forced)
	{
		if (false == forced && 0 < this.points.size())
		{
			Point2D.Float lp = this.points.get(this.points.size()-1);
			float dx = lp.x - x;
			float dy = lp.y - y;
			float dist = (float) Math.sqrt(dx*dx + dy*dy);
			if (dist < this.size.getSize())
			{
				return;
			}
		}
		this.points.add(new Point2D.Float(x, y));
	}

	public void move(int index, float x, float y)
	{
		if (0 > index)
		{
			index = this.points.size()-1;
		}
		Point2D.Float p = this.points.get(index);
		p.setLocation(x, y);
	}

	public void shrink(int index)
	{
		if (0 > index || index >= this.points.size())
		{
			return;
		}
		this.points.remove(index);
	}

	public void reshape(int index, int dx, int dy)
	{
		if (0 > index || index >= this.points.size())
		{
			return;
		}
		Point2D.Float p = this.points.get(index);
		p.x += dx;
		p.y += dy;
	}

	public void putMask(Graphics2D g, DesignObject.DRAW_MODE mode, boolean selected)
	{
		Path2D.Float path = null;
		for (Point2D.Float p: points)
		{
			if (null == path)
			{
				path = new Path2D.Float();
				path.moveTo(p.x, p.y);
			}
			else
			{
				path.lineTo(p.x, p.y);
			}
		}

		GeneralPath mask = new GeneralPath(boundStroke.createStrokedShape(path));

		switch(mode)
		{
		case DISPLAY:
		case PRINT:
			if (selected)
			{
				Color c = g.getColor();
				g.setColor(Color.BLACK);

				Composite cp = g.getComposite();
				g.setComposite(maskAreaComp);

				g.fill(mask);

				g.setComposite(cp);

				Stroke b = g.getStroke();
				g.setStroke(pathStroke);
				g.draw(path);

				for (Point2D.Float p: this.points)
				{
					g.drawRect((int)(p.x - R/2), (int)(p.y - R/2), R, R);
				}

				g.setStroke(b);

				g.setColor(c);
			}
			else
			{
				Color c = g.getColor();
				g.setColor(Color.WHITE);
				Composite cp = g.getComposite();
				g.setComposite(maskComp);
				g.fill(mask);
				g.setComposite(cp);
				g.setColor(c);
			}
			break;
		case EXPORT:
			Color c = g.getColor();
			g.setColor(Color.BLACK);
			g.fill(mask);
			g.setColor(c);
		}
	}
}
