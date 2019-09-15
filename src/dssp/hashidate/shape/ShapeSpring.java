package dssp.hashidate.shape;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.StringTokenizer;

import dssp.hashidate.DesignPanel;
import dssp.hashidate.io.ExportBase;
import dssp.hashidate.io.ShapeInfo;
import dssp.hashidate.misc.FigureType.EDGE_TYPE;
import dssp.hashidate.misc.ObjectPopupMenu;
import dssp.hashidate.misc.PairList;
import dssp.hashidate.shape.property.ShapeSpringProperty;
import dssp.brailleLib.Util;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public class ShapeSpring extends DesignObject
{
	private int pointIndex = -1;
	private Point2D.Double[] bar = new Point2D.Double[2];
	private int barLen = 40;
	private int radius = 40;
	private int nHelix = 5;
	private int[] xPoints;
	private int[] yPoints;
	private boolean useBackPoint = false;

	public int getRadius()
	{
		return this.radius;
	}

	public int getNHelix()
	{
		return this.nHelix;
	}

	public int[] getxPoints()
	{
		return xPoints;
	}

	public int[] getyPoints()
	{
		return yPoints;
	}

	public boolean useBackPoint()
	{
		return this.useBackPoint;
	}

	private static final int NANGLE = 50;

	public ShapeSpring()
	{
		this.shape = SHAPE.SPRING;
		this.bar[0] = new Point2D.Double();
		this.bar[1] = new Point2D.Double();
		this.initPopupMenu();
	}

	@Override
	public ShapeSpring clone()
	{
		ShapeSpring obj = (ShapeSpring) super.clone();

		obj.bar = new Point2D.Double[this.bar.length];
		Util.deepCopyByClone(this.bar, obj.bar);
		obj.barLen = this.barLen;
		obj.radius = this.radius;
		obj.nHelix = this.nHelix;
		obj.xPoints = new int[this.xPoints.length];
		System.arraycopy(this.xPoints, 0, obj.xPoints, 0, obj.xPoints.length);
		obj.yPoints = new int[this.yPoints.length];
		System.arraycopy(this.yPoints, 0, obj.yPoints, 0, obj.yPoints.length);
		obj.adaptFrame();
		obj.useBackPoint = this.useBackPoint;

		return obj;
	}

	public ShapeSpring(StatusHint hint, Point p)
	{
		this();

		hint.status = STATUS.INITIAL;
		hint.point = p;
		this.bar[0].setLocation(p.x, p.y);
		this.bar[1].setLocation(p.x, p.y);

		initPoints();
	}

	private static ObjectPopupMenu popupMenu;
	private void initPopupMenu()
	{
		if (null != popupMenu)
		{
			return;
		}
		popupMenu = new ObjectPopupMenu();
		this.initCommonMenu(popupMenu);
	}

	@Override
	public void showMenu(Point location)
	{
		if (null == location || popupMenu.isVisible())
		{
			popupMenu.setVisible(false);
			return;
		}
		popupMenu.show(this, location);;
	}

	void initPoints()
	{
		int nPoint = NANGLE * this.nHelix + 3;
		this.xPoints = new int[nPoint];
		this.yPoints = new int[nPoint];
		pointIndex = 1;
		calcPath();
	}

	void calcPath()
	{
		Point2D.Double dp = new Point2D.Double(this.bar[1].x - this.bar[0].x, this.bar[1].y - this.bar[0].y);
		double len = Math.sqrt(dp.x*dp.x + dp.y*dp.y);
		Point2D.Double angle = new Point2D.Double(dp.x/len, dp.y/len);

		Point2D.Double[] edge = new Point2D.Double[2];
		edge[0] = new Point2D.Double(this.bar[0].x + this.barLen*angle.x, this.bar[0].y + this.barLen*angle.y);
		edge[1] = new Point2D.Double(this.bar[1].x - this.barLen*angle.x, this.bar[1].y - this.barLen*angle.y);

		dp.x = edge[1].x - edge[0].x;
		dp.y = edge[1].y - edge[0].y;
		len = Math.sqrt(dp.x*dp.x + dp.y*dp.y);

		Point2D.Double pitch = new Point2D.Double(dp.x/this.nHelix, dp.y/this.nHelix);

		double dt = Math.PI/(NANGLE / 2);
		Point2D.Double[] r = new Point2D.Double[NANGLE];
		for (int i = 0; i < NANGLE; i++)
		{
			double c = Math.cos((i+1)*dt);
			double s = Math.sin((i+1)*dt);
			r[i] = new Point2D.Double(this.radius*(1-c)*0.4, -this.radius*s);
		}

		int nPoint = NANGLE * this.nHelix + 1;
		Point2D.Double[] dPoints = new Point2D.Double[nPoint];
		dPoints[0] = edge[0];
		int index = 0;
		for (int i = 0; i < this.nHelix; i++, index+=NANGLE )
		{
			Point2D.Double base = dPoints[index];
			for (int j = 0; j < NANGLE; j++)
			{
				dPoints[index+j+1] = new Point2D.Double(base.x + r[j].x*angle.x - r[j].y*angle.y + (j+1)*pitch.x/NANGLE, base.y + r[j].x*angle.y + r[j].y*angle.x + (j+1)*pitch.y/NANGLE);
			}
		}
		dPoints[nPoint-1] = edge[1];

		this.xPoints[0] = (int)Math.round(this.bar[0].x);
		this.yPoints[0] = (int)Math.round(this.bar[0].y);
		for (int i= 0; i < nPoint; i++)
		{
			this.xPoints[i+1] = (int)Math.round(dPoints[i].x);
			this.yPoints[i+1] = (int)Math.round(dPoints[i].y);

		}
		this.xPoints[this.xPoints.length-1] = (int)Math.round(this.bar[1].x);
		this.yPoints[this.yPoints.length-1] = (int)Math.round(this.bar[1].y);

		adaptFrame();
	}

	@Override
	public void adaptFrame()
	{
//		this.x = this.xPoints[0];
//		this.y = this.yPoints[0];
//		this.width = 0;
//		this.height = 0;
//		for (int i = 1; i < this.xPoints.length; i++)
//		{
//			this.add(this.xPoints[i], this.yPoints[i]);
//		}

//		int left = Math.min(this.xPoints[0], this.xPoints[this.xPoints.length-1]);
//		int right = Math.max(this.xPoints[0], this.xPoints[this.xPoints.length-1]);
//		int top = Math.min(this.yPoints[0], this.yPoints[this.yPoints.length-1]);
//		int bottom = Math.max(this.yPoints[0], this.yPoints[this.yPoints.length-1]);

		int left = Integer.MAX_VALUE;
		int right = Integer.MIN_VALUE;
		for (int i = 0; i < this.xPoints.length; i++)
		{
			int p = this.xPoints[i];
			if (left > p)
			{
				left = p;
			}
			if (right < p)
			{
				right = p;
			}
		}

		int top = Integer.MAX_VALUE;
		int bottom = Integer.MIN_VALUE;
		for (int i = 0; i < this.yPoints.length; i++)
		{
			int p = this.yPoints[i];
			if (top > p)
			{
				top = p;
			}
			if (bottom < p)
			{
				bottom = p;
			}
		}

		this.x = left;
		this.y = top;
		this.width = right - left;
		this.height = bottom - top;
	}

	private void drawMask(Graphics g, int p0x, int p0y, int p1x, int p1y)
	{
		int[] cx = new int[4];
		int[] cy = new int[4];

		int vx = p1x - p0x;
		int vy = p1y - p0y;
		double len = Math.sqrt((double)(vx*vx + vy*vy));

		double tx = -vy/len;
		double ty = vx/len;

		double w = 5;
		cx[0] = p0x + (int)Math.round(w*tx);
		cy[0] = p0y + (int)Math.round(w*ty);
		cx[1] = p0x - (int)Math.round(w*tx);
		cy[1] = p0y - (int)Math.round(w*ty);
		cx[2] = p1x - (int)Math.round(w*tx);
		cy[2] = p1y - (int)Math.round(w*ty);
		cx[3] = p1x + (int)Math.round(w*tx);
		cy[3] = p1y + (int)Math.round(w*ty);
		g.setColor(Color.WHITE);
		g.fillPolygon(cx, cy, cx.length);
	}

	@Override
	protected void drawBraille(Graphics2D g, boolean printing)
	{
		boolean braille = true;

		List<Point> points = Util.newArrayList();
		points.add(new Point(this.xPoints[0], this.yPoints[0]));
		int nPoint = this.xPoints.length;
		Point[] ends = {new Point(this.xPoints[1], this.yPoints[1]), new Point(this.xPoints[nPoint-2], this.yPoints[nPoint-2])};
		points.add(ends[0]);

		double dx = ends[1].x - ends[0].x;
		double dy = ends[1].y - ends[0].y;
		double d = Math.sqrt(dx*dx + dy*dy);
		double px = (double)((ends[1].x - ends[0].x))/(double)(4*this.nHelix);
		double py = (double)((ends[1].y - ends[0].y))/(double)(4*this.nHelix);

		for (int i = 0; i < this.nHelix; i++)
		{
			Point p = new Point();
			p.x = (int) (ends[0].x + px*(4*i+1) + (double)this.radius*dy/d);
			p.y = (int) (ends[0].y + py*(4*i+1) - (double)this.radius*dx/d);
			points.add(p);

			p = new Point();
			p.x = (int) (ends[0].x + px*(4*i+3) - (double)this.radius*dy/d);
			p.y = (int) (ends[0].y + py*(4*i+3) + (double)this.radius*dx/d);
			points.add(p);
		}
		points.add(ends[1]);
		points.add(new Point(this.xPoints[nPoint-1], this.yPoints[nPoint-1]));


		EDGE_TYPE edgeType = this.edgeType;

		int nLine = points.size()-1;
		boolean lb = this.lineBack;
		for (int i = 0; i < nLine; i++)
		{
			if (0 == i)
			{
				switch(edgeType)
				{
				case LARROW:
				case BARROW:
					this.setEdgeType(EDGE_TYPE.LARROW);
					this.setStroke();
					break;
				case LARROW1:
				case BARROW1:
					this.setEdgeType(EDGE_TYPE.LARROW1);
					this.setStroke();
					break;
				default:
					this.setEdgeType(EDGE_TYPE.BUTT);
					this.setStroke();
				}
			}
			else if (i == (nLine-1))
			{
				switch(edgeType)
				{
				case RARROW:
				case BARROW:
					this.setEdgeType(EDGE_TYPE.RARROW);
					this.setStroke();
					break;
				case RARROW1:
				case BARROW1:
					this.setEdgeType(EDGE_TYPE.RARROW1);
					this.setStroke();
					break;
				default:
					this.setEdgeType(EDGE_TYPE.BUTT);
					this.setStroke();
				}
			}
			else
			{
				this.setEdgeType(EDGE_TYPE.BUTT);

				if (this.useBackPoint)
				{
					if (lb)
					{
						this.lineBack = (0 != (i%2));
					}
					else
					{
						this.lineBack = (0 == (i%2));
					}
				}
				this.setStroke();
			}

			Point p1 = points.get(i);
			Point p2 = points.get(i+1);
			this.setDrawProperty(g, braille, true);
			g.drawLine(p1.x, p1.y, p2.x, p2.y);
			this.setDrawProperty(g, braille, false);
		}
		this.lineBack = lb;

		this.setEdgeType(edgeType);
		this.setStroke();

		if (false == printing && isSelected())
		{
			Point p = new Point((int)Math.round(this.bar[0].x), (int)Math.round(this.bar[0].y));
			drawToggle(g, p.x, p.y);
			p.x = (int)Math.round(this.bar[1].x);
			p.y = (int)Math.round(this.bar[1].y);
			drawToggle(g, p.x, p.y);
		}

	}

//	@Override
//	protected void drawBraille(Graphics2D g, boolean printing)
//	{
//		boolean braille = true;
//
//		EDGE_TYPE edgeType = this.edgeType;
//
//		int px = this.xPoints[0];
//		int py = this.yPoints[0];
//		int nLine = this.xPoints.length-1;
//		for (int i = 0; i < nLine; i++)
//		{
//			int x = this.xPoints[i+1];
//			int y = this.yPoints[i+1];
//			int dx = x-px;
//			int dy = y-py;
//			double dd = Math.sqrt((double)(dx*dx+dy*dy));
//			if (dd < this.getDotSpan())
//			{
//				continue;
//			}
//			drawMask(g, this.xPoints[i], this.yPoints[i], this.xPoints[i+1], this.yPoints[i+1]);
//
//			if (0 == i)
//			{
//				switch(edgeType)
//				{
//				case LARROW:
//				case BARROW:
//					this.setEdgeType(EDGE_TYPE.LARROW);
//					this.setStroke();
//					break;
//				case LARROW1:
//				case BARROW1:
//					this.setEdgeType(EDGE_TYPE.LARROW1);
//					this.setStroke();
//					break;
//				default:
//					this.setEdgeType(EDGE_TYPE.BUTT);
//					this.setStroke();
//				}
//			}
//			else if (i == (nLine-1))
//			{
//				switch(edgeType)
//				{
//				case RARROW:
//				case BARROW:
//					this.setEdgeType(EDGE_TYPE.RARROW);
//					this.setStroke();
//					break;
//				case RARROW1:
//				case BARROW1:
//					this.setEdgeType(EDGE_TYPE.RARROW1);
//					this.setStroke();
//					break;
//				default:
//					this.setEdgeType(EDGE_TYPE.BUTT);
//					this.setStroke();
//				}
//			}
//			else
//			{
//				this.setEdgeType(EDGE_TYPE.BUTT);
//				this.setStroke();
//			}
//
//			this.setDrawProperty(g, braille, true);
//			g.drawLine(this.xPoints[i], this.yPoints[i], this.xPoints[i+1], this.yPoints[i+1]);
//			this.setDrawProperty(g, braille, false);
//			px = x;
//			py = y;
//		}
//
//		this.setEdgeType(edgeType);
//		this.setStroke();
//
//		if (false == printing && isSelected())
//		{
//			Point p = new Point((int)Math.round(this.bar[0].x), (int)Math.round(this.bar[0].y));
//			drawToggle(g, p.x, p.y);
//			p.x = (int)Math.round(this.bar[1].x);
//			p.y = (int)Math.round(this.bar[1].y);
//			drawToggle(g, p.x, p.y);
//		}
//
//	}

	@Override
	protected void drawSumiji(Graphics2D g, boolean printing)
	{
		boolean braille = false;

		int nLine = this.xPoints.length-1;
		EDGE_TYPE edgeType = this.edgeType;
		for (int i = 0; i < nLine; i++)
		{
			drawMask(g, this.xPoints[i], this.yPoints[i], this.xPoints[i+1], this.yPoints[i+1]);

			if (0 == i)
			{
				switch(edgeType)
				{
				case LARROW:
				case BARROW:
					this.setEdgeType(EDGE_TYPE.LARROW);
					this.setStroke();
					break;
				case LARROW1:
				case BARROW1:
					this.setEdgeType(EDGE_TYPE.LARROW1);
					this.setStroke();
					break;
				default:
					this.setEdgeType(EDGE_TYPE.BUTT);
					this.setStroke();
				}
			}
			else if (i == (nLine-1))
			{
				switch(edgeType)
				{
				case RARROW:
				case BARROW:
					this.setEdgeType(EDGE_TYPE.RARROW);
					this.setStroke();
					break;
				case RARROW1:
				case BARROW1:
					this.setEdgeType(EDGE_TYPE.RARROW1);
					this.setStroke();
					break;
				default:
					this.setEdgeType(EDGE_TYPE.BUTT);
					this.setStroke();
				}
			}
			else
			{
				this.setEdgeType(EDGE_TYPE.BUTT);
				this.setStroke();
			}
			this.setDrawProperty(g, braille, true);

			g.drawLine(this.xPoints[i], this.yPoints[i], this.xPoints[i+1], this.yPoints[i+1]);

			this.setDrawProperty(g, braille, false);
		}
		this.setEdgeType(edgeType);
		this.setStroke();

		if (false == printing && isSelected())
		{
			Point p = new Point((int)Math.round(this.bar[0].x), (int)Math.round(this.bar[0].y));
			drawToggle(g, p.x, p.y);
			p.x = (int)Math.round(this.bar[1].x);
			p.y = (int)Math.round(this.bar[1].y);
			drawToggle(g, p.x, p.y);
		}
	}

	boolean isNearLine(Point p)
	{
		Line2D.Double line = new Line2D.Double();
		for (int i = 0; i < (this.xPoints.length-1); i++)
		{
			line.setLine(this.xPoints[i], this.yPoints[i], this.xPoints[i+1], this.yPoints[i+1]);
			double h = line.ptSegDist(p.x, p.y);
			if (h < SEP)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hitTest(StatusHint hint, Point p, boolean reshapable)
	{
		if (false == isStatus(STATUS.INITIAL))
		{
			if (isSelected())
			{
				if (null != hint.status)
				{
					switch(hint.status)
					{
					case ERASE:
						if (this.isStatus(STATUS.ERASE))
						{
							return true;
						}
						break;
					case MOVE:
						if (this.hitTestMask(p))
						{
							hint.status = STATUS.ERASE_RESHAPE;
							return true;
						}
						break;
					case ERASE_RESHAPE:
						if (this.hitTestMask(p))
						{
							this.startMoveMask(p);
							return true;
						}
						else
						{
							this.endMoveMask();
						}
						break;
					default:
					}
				}

				for (int i = 0; i < 2; i++)
				{
					Point dp = new Point((int)Math.round(this.bar[i].x), (int)Math.round(this.bar[i].y));
					dp.x = Math.abs(p.x - dp.x);
					dp.y = Math.abs(p.y - dp.y);
					if (SEP > dp.x && SEP > dp.y)
					{
						pointIndex = i;
						hint.status = STATUS.RESHAPE;
						hint.point = p;
						return true;
					}
				}
				if (isNearLine(p))
				{
					hint.status = STATUS.MOVE;
					hint.point = p;
					return true;
				}
			}
			else
			{
				return isNearLine(p);
			}
		}
		return false;
	}

	@Override
	public boolean hitTest(Rectangle rect)
	{
		for (int i = 0; i < this.xPoints.length-1; i++)
		{
			if (rect.intersectsLine((double)this.xPoints[i], (double)this.yPoints[i], (double)this.xPoints[i+1], (double)this.yPoints[i+1]))
			{
				return true;
			}
		}

		if (rect.intersectsLine((double)this.bar[0].x, (double)this.bar[0].y, (double)this.xPoints[0], (double)this.yPoints[0]))
		{
			return true;
		}

		if (rect.intersectsLine((double)this.bar[1].x, (double)this.bar[1].y, (double)this.xPoints[this.xPoints.length-1], (double)this.yPoints[this.yPoints.length-1]))
		{
			return true;
		}

		return false;
	}

	@Override
	public boolean move(StatusHint hint, Point dp)
	{
		if (this.isStatus(STATUS.ERASE_RESHAPE))
		{
			this.moveMask(dp);
			return true;
		}

		super.move(hint, dp);

		this.bar[0].x += dp.x;
		this.bar[0].y += dp.y;
		this.bar[1].x += dp.x;
		this.bar[1].y += dp.y;
		for (int i = 0; i < this.xPoints.length; i++)
		{
			this.xPoints[i] += dp.x;
			this.yPoints[i] += dp.y;
		}
		adaptFrame();

		if (null != hint)
		{
			hint.status = STATUS.MOVE;
			hint.point = dp;
			notifyChanged();
		}
		return true;
	}

	@Override
	public boolean resize(StatusHint hint, Point  dp)
	{
		return this.reshape(hint, dp);
	}

	@Override
	public void resize(Point origin, double xscale, double yscale)
	{
		for (int i = 0; i < 2; i++)
		{
			this.bar[i] = new Point2D.Double(transform(this.bar[i].x, origin.x, xscale), transform(this.bar[i].y, origin.y, yscale));
		}
		calcPath();
		adaptFrame();
		notifyChanged();
	}

	@Override
	public boolean reshape(StatusHint hint, Point dp)
	{
		if (isStatus(STATUS.MOVE))
		{
			return move(hint, dp);
		}
		else if (isStatus(STATUS.INITIAL) || isStatus(STATUS.RESHAPE))
		{
			if (0 <= this.pointIndex)
			{
				Point2D.Double p = this.bar[this.pointIndex];
				p.x += dp.x;
				p.y += dp.y;
				calcPath();
				adaptFrame();
				notifyChanged();
			}
		}

		return true;
	}

	@Override
	public void clicked(DesignPanel panel, Point p)
	{
		select(false);
	}

	private static class TAG extends TAG_BASE
	{
		static final String[] BAR ={ "bar1", "bar2"};
		static final String COORD_SEP = ",";
		static final String BAR_LEN = "barLen";
		static final String RADIUS = "radius";
		static final String NHELIX = "nHerix";
		static final String USE_BACKPOINT = "useBackPoint";
	}

	@Override
	public void export(ExportBase export)
	{
		ShapeSpring copy = this.clone();
		copy.resetOrigin(export.getOrigin());

		ShapeInfo info = new ShapeInfo(SHAPE.SPRING);
		info.obj = copy;

		PairList list = this.makeDesc(copy);

		for (int i = 0; i < 2; ++i)
		{
			int x = (int) Math.round(copy.bar[i].x);
			int y = (int) Math.round(copy.bar[i].y);
			list.add(TAG.BAR[i], String.format("%d%s%d", x, TAG.COORD_SEP, y));
		}
		list.add(TAG.BAR_LEN, copy.barLen);
		list.add(TAG.RADIUS, copy.radius);
		list.add(TAG.NHELIX, copy.nHelix);
		list.add(TAG.USE_BACKPOINT, Boolean.toString(copy.useBackPoint));

		info.setDesc(list);

//		info.dotSize = copy.dotSize;
//		info.dotSpan = copy.dotSpan;
//		info.xPoints = copy.xPoints;
//		info.yPoints = copy.yPoints;
//		info.desc = String.format("%s %d %d %d %d %d %d %d", this.getClass().getSimpleName(), (int)Math.round(copy.bar[0].x), (int)Math.round(copy.bar[0].y), (int)Math.round(copy.bar[1].x), (int)Math.round(copy.bar[1].y), copy.barLen, copy.radius, copy.nHelix);

		export.writeStart(info);
		export.write(info);
		export.writeEnd(info);
	}

	@Override
	public boolean showProperty(DesignPanel panel, Point p)
	{
		ShapeSpringProperty dlg = ShapeSpringProperty.getInstance();
		this.setCommonProperty(dlg);

		dlg.setNHelix(this.nHelix);
		dlg.setBarLen(this.barLen);
		dlg.setRadius(this.radius);
		dlg.setUseBackPoint(this.useBackPoint);

		this.changed = false;
		dlg.setVisible(true);
		if (dlg.isOk())
		{
			this.getCommonProperty(dlg);

			int val = dlg.getNHelix();
			if (val != this.nHelix)
			{
				this.changed = true;
				this.nHelix = val;
			}

			val = dlg.getBarLen();
			if (val != this.barLen)
			{
				this.changed = true;
				this.barLen = val;
			}

			val = dlg.getRadius();
			if (val != this.radius)
			{
				this.changed = true;
				this.radius = val;
			}

			boolean flag = dlg.useBackPoint();
			if (flag != this.useBackPoint)
			{
				this.changed = true;
				this.useBackPoint = flag;
			}

			if (this.changed)
			{
				this.initPoints();
				this.adaptFrame();
			}
		}

		return true;
	}

	public static ShapeSpring parse(ShapeInfo info)
	{
		ShapeSpring obj = null;
		PairList list = PairList.fromString(info.desc);
		List<Object> vals = null;
		vals = list.getValues(TAG.CLASS);
		if (0 == vals.size())
		{
			StringTokenizer st = new StringTokenizer(info.desc);
			String[] words = new String[st.countTokens()];
			for (int i = 0; st.hasMoreTokens(); i++)
			{
				words[i] = st.nextToken();
			}
			if (8 != words.length || 0 != words[0].compareToIgnoreCase(ShapeSpring.class.getSimpleName()))
			{
				Util.logError("ShapeSpring className is invalid.");
				return null;
			}

			try
			{
				obj = new ShapeSpring();
				obj.bar[0] = new Point2D.Double(Integer.parseInt(words[1]), Integer.parseInt(words[2]));
				obj.bar[1] = new Point2D.Double(Integer.parseInt(words[3]), Integer.parseInt(words[4]));
				obj.barLen = Integer.parseInt(words[5]);
				obj.radius = Integer.parseInt(words[6]);
				obj.nHelix = Integer.parseInt(words[7]);
				obj.initPoints();
			}
			catch (Exception e)
			{
				Util.logException(e);
				return null;
			}
		}
		else
		{
			obj = new ShapeSpring();
			obj.parseDesc(list);
			for (int i = 0; i < 2; ++i)
			{
				vals = list.getValues(TAG.BAR[i]);
				if (0 < vals.size() && vals.get(0) instanceof String)
				{
					String t = (String) vals.get(0);
					String[] cs = t.split(TAG.COORD_SEP);
					obj.bar[i].x = java.lang.Double.parseDouble(cs[0]);
					obj.bar[i].y = java.lang.Double.parseDouble(cs[1]);
				}
			}

			vals = list.getValues(TAG.BAR_LEN);
			if (0 < vals.size() && vals.get(0) instanceof String)
			{
				obj.barLen = Integer.parseInt((String) vals.get(0));
			}

			vals = list.getValues(TAG.RADIUS);
			if (0 < vals.size() && vals.get(0) instanceof String)
			{
				obj.radius = Integer.parseInt((String) vals.get(0));
			}

			vals = list.getValues(TAG.NHELIX);
			if (0 < vals.size() && vals.get(0) instanceof String)
			{
				obj.nHelix = Integer.parseInt((String) vals.get(0));
			}

			vals = list.getValues(TAG.USE_BACKPOINT);
			if (0 < vals.size() && vals.get(0) instanceof String)
			{
				obj.useBackPoint = Boolean.parseBoolean((String) vals.get(0));
			}

			obj.initPoints();
		}

		obj.adaptFrame();
		obj.setStroke();
		obj.setFillPaint();

		return obj;
	}
}
