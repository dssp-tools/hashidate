package dssp.hashidate.shape;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JMenuItem;

import dssp.hashidate.DesignPanel;
import dssp.hashidate.io.ExportBase;
import dssp.hashidate.io.ShapeInfo;
import dssp.hashidate.misc.FigureType;
import dssp.hashidate.misc.ObjectPopupMenu;
import dssp.hashidate.misc.PairList;
import dssp.brailleLib.Util;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public class ShapePolyline extends DesignObject
{
	protected int pointIndex = -1;
	// リサイズによる数値計算誤差の蓄積を軽減するためにdoubleで持っておく
	protected List<Point2D.Double> dPoints = Util.newArrayList();
	// 描画用の位置
	protected int[] xPoints = null;
	protected int[] yPoints = null;
	protected GeneralPath path = new GeneralPath();

	public int[] getxPoints()
	{
		return xPoints;
	}

	public int[] getyPoints()
	{
		return yPoints;
	}

	public ShapePolyline()
	{
		super();
		this.shape = SHAPE.POLYLINE;

		this.initPopupMenu();
	}

	@Override
	public ShapePolyline clone()
	{
		ShapePolyline obj = (ShapePolyline)super.clone();

		obj.dPoints = Util.newArrayList();
		Util.deepCopyByClone(this.dPoints, obj.dPoints);

		obj.xPoints = null;
		obj.yPoints = null;

		obj.calcPoints();
		obj.adaptFrame();

		return obj;
	}

	private ShapePolyline(List<Point2D.Double> src, SHAPE shape)
	{
		this();

		assert (SHAPE.LINE != shape && SHAPE.POLYLINE != shape && SHAPE.POLYGON != shape);

		Util.deepCopyByClone(src, this.dPoints);
		if (2 == src.size())
		{
			this.shape = SHAPE.LINE;
		}
		else
		{
			this.shape = shape;
		}
		this.calcPoints();
		this.adaptFrame();
	}

	public ShapePolyline(StatusHint hint, Point p, SHAPE shape)
	{
		this();

		if (SHAPE.LINE != shape && SHAPE.POLYLINE != shape && SHAPE.POLYGON != shape)
		{
			throw new IllegalArgumentException(String.format("Illegal shape %s", shape));
		}

		select(true);
		hint.status = STATUS.INITIAL;
		hint.point = p;
		this.dPoints.add(new Point2D.Double(p.x, p.y));
		this.dPoints.add(new Point2D.Double(p.x, p.y));
		pointIndex = 1;
		this.shape = shape;
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

		popupMenu.addSeparator();

		JMenuItem item;
		item = new JMenuItem("点を追加する");
		popupMenu.add(item);

		item = new JMenuItem("点を削除する");
		popupMenu.add(item);
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

	@Override
	public void menuCalled(String name, Point location, StatusHint hint)
	{
		switch(name)
		{
		case "点を追加する":
			hint.status = STATUS.EDIT;
			hint.option = EDIT_TYPE.ADD;
			popupMenu.callback(hint);
			break;
		case "点を削除する":
			hint.status = STATUS.EDIT;
			hint.option = EDIT_TYPE.DEL;
			popupMenu.callback(hint);
			break;
		}
	}

	void addPoint(int px, int py)
	{
		this.addPoint(px,py,this.dPoints.size());
	}

	void addPoint(int x, int y, int index)
	{
		this.addPointAt(x,y,index);
		this.notifyChanged();
	}

	void addPointAt(int x, int y, int index)
	{
		this.dPoints.add(index, new Point2D.Double(x,y));
		if (SHAPE.LINE == this.shape && 2 < this.dPoints.size())
		{
			this.shape = SHAPE.POLYLINE;
		}
		adaptFrame();
	}

	boolean remove(int index)
	{
		if (0 > index || index > (this.dPoints.size() - 1) || 2 == this.dPoints.size())
		{
			return false;
		}
		this.dPoints.remove(index);
		if (SHAPE.POLYLINE == this.shape && 2 == this.dPoints.size())
		{
			this.shape = SHAPE.LINE;
		}
		adaptFrame();
		notifyChanged();

		return true;
	}

	@Override
	public void adaptFrame()
	{
		Point2D.Double p = this.dPoints.get(0);
		this.x = (int)Math.round(p.x);
		this.y = (int)Math.round(p.y);
		this.width = 0;
		this.height = 0;
		for (int i = 1; i < this.dPoints.size(); i++)
		{
			Point2D.Double tp = this.dPoints.get(i);
			int x = (int)Math.round(tp.x);
			int y = (int)Math.round(tp.y);
			this.add(x, y);
		}
//		this.x -= TOGGLE;
//		this.y -= TOGGLE;
//		this.width += TOGGLE * 2;
//		this.height += TOGGLE * 2;
	}

	@Override
	public void clicked(DesignPanel panel, Point p)
	{
		switch(this.shape)
		{
		case LINE:
			this.doubleClicked(panel, p);
			if (2 < this.dPoints.size())
			{
				this.shape = SHAPE.POLYLINE;
			}
			break;
		case POLYLINE:
		case POLYGON:
			if (isStatus(STATUS.INITIAL))
			{
				this.addPoint(p.x, p.y, this.dPoints.size());
				pointIndex = this.dPoints.size()-1;
			}
			break;
		default:
		}
	}

	@Override
	public void doubleClicked(DesignPanel panel, Point p)
	{
		Point2D.Double tp = this.dPoints.get(this.dPoints.size()-1);
		if (p.x == tp.x && p.y == tp.y)
		{
			this.remove(this.dPoints.size() - 1);
		}
		this.select(false);
	}

	protected void calcPoints()
	{
		if (null == this.xPoints || this.xPoints.length != this.dPoints.size())
		{
			this.xPoints = new int[this.dPoints.size()];
			this.yPoints = new int[this.dPoints.size()];
		}
		for (int i = 0; i < this.dPoints.size(); i++)
		{
			Point2D.Double tp = this.dPoints.get(i);
			this.xPoints[i] = (int)Math.round(tp.x);
			this.yPoints[i] = (int)Math.round(tp.y);
		}
		this.path.reset();
		this.path.moveTo(xPoints[0], yPoints[0]);
		for (int i = 1; i < xPoints.length; i++)
		{
			this.path.lineTo(xPoints[i], yPoints[i]);
		}

	}

	@Override
	protected void drawBraille(Graphics2D g, boolean printing)
	{
		boolean braille = true;

		if (null != this.dPoints && 1 < this.dPoints.size())
		{
			calcPoints();

			switch(this.shape)
			{
			case LINE:
				this.setDrawProperty(g, braille, true);
				g.drawLine(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
				this.setDrawProperty(g, braille, false);
				if (false == printing && false == this.isStatus(STATUS.INITIAL) && this.isSelected())
				{
					drawToggles(g, xPoints, yPoints);
				}
				break;
			case POLYLINE:
				if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType)
				{
					this.setFillProperty(g, braille, true);
					g.fill(path);
					this.setFillProperty(g, braille, false);
				}
				this.setDrawProperty(g, braille, true);
				g.draw(path);
				this.setDrawProperty(g, braille, false);
				if (false == printing && false == this.isStatus(STATUS.INITIAL) && this.isSelected())
				{
					drawFrame(g);
					drawToggles(g, xPoints, yPoints);
				}
				break;
			case POLYGON:
				if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType)
				{
					this.setFillProperty(g, braille, true);
					g.fill(path);
					this.setFillProperty(g, braille, false);
				}
				this.setDrawProperty(g, braille, true);
				g.drawPolygon(xPoints, yPoints, xPoints.length);
				this.setDrawProperty(g, braille, false);
				if (false == printing && false == this.isStatus(STATUS.INITIAL) && this.isSelected())
				{
					drawFrame(g);
					drawToggles(g, xPoints, yPoints);
				}
				break;
			default:
			}
		}
	}

	@Override
	protected void drawSumiji(Graphics2D g, boolean printing)
	{
		boolean braille = false;
		if (null != this.dPoints && 1 < this.dPoints.size())
		{
			this.calcPoints();

			switch(this.shape)
			{
			case LINE:
				this.setDrawProperty(g, braille, true);
				g.drawLine(xPoints[0], yPoints[0], xPoints[1], yPoints[1]);
				this.setDrawProperty(g, braille, false);
				if (false == printing && false == this.isStatus(STATUS.INITIAL) && this.isSelected())
				{
					drawToggles(g, xPoints, yPoints);
				}
				break;
			case POLYLINE:
				if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType)
				{
					this.setFillProperty(g, braille, true);
					g.fill(this.path);
					this.setFillProperty(g, braille, false);
				}

				this.setDrawProperty(g, braille, true);
				g.draw(path);
//				g.drawPolyline(xPoints, yPoints, xPoints.length);
				this.setDrawProperty(g, braille, false);
				if (false == printing && false == this.isStatus(STATUS.INITIAL) && this.isSelected())
				{
					drawFrame(g);
					drawToggles(g, xPoints, yPoints);
				}
				break;
			case POLYGON:
				if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType)
				{
					this.setFillProperty(g, braille, true);
					g.fillPolygon(xPoints, yPoints, xPoints.length);
					this.setFillProperty(g, braille, false);
				}

				this.setDrawProperty(g, braille, true);
				g.drawPolygon(xPoints, yPoints, xPoints.length);
				this.setDrawProperty(g, braille, false);
				if (false == printing && false == this.isStatus(STATUS.INITIAL) && this.isSelected())
				{
					drawFrame(g);
					drawToggles(g, xPoints, yPoints);
				}
				break;
			case REGULAR_POLYGON:
				if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType)
				{
					this.setFillProperty(g, braille, true);
					g.fillPolygon(xPoints, yPoints, xPoints.length);
					this.setFillProperty(g, braille, false);
				}

				this.setDrawProperty(g, braille, true);
				g.drawPolygon(xPoints, yPoints, xPoints.length);
				this.setDrawProperty(g, braille, false);
				if (false == printing && false == this.isStatus(STATUS.INITIAL) && this.isSelected())
				{
					drawFrame(g);
				}
				break;
			default:
			}
		}
	}

	@Override
	protected void drawFrame(Graphics g)
	{
		Rectangle frame = new Rectangle(this.x - TOGGLE, this.y - TOGGLE, this.width + TOGGLE*2, this.height + TOGGLE*2);
		this.drawFrame(g, frame);
	}

	private boolean isNear(int x, int y, Point p)
	{
		return (Math.abs(x - p.x) < SEP && Math.abs(y - p.y) < SEP);
	}

	private int findSegment(Point p)
	{
		Line2D.Double line = new Line2D.Double();
		for (int i = 0; i < (xPoints.length-1); i++)
		{
			line.setLine(xPoints[i], yPoints[i], xPoints[i+1], yPoints[i+1]);
			double h = line.ptSegDist(p.x, p.y);
			if (h < SEP)
			{
				return i;
			}
		}
		if (this.shape == SHAPE.POLYGON)
		{
			int index = this.xPoints.length-1;
			line.setLine(xPoints[index], yPoints[index], xPoints[0], yPoints[0]);
			double h = line.ptSegDist(p.x, p.y);
			if (h < SEP)
			{
				return index;
			}
		}

		return -1;
	}

	protected boolean isNearLine(Point p)
	{
		return (0 <= this.findSegment(p));
	}

	@Override
	public boolean hitTest(StatusHint hint, Point p, boolean reshapable)
	{
		if (null != xPoints && false == isStatus(STATUS.INITIAL))
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

				if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType && this.path.contains(p))
				{
					hint.status = STATUS.MOVE;
					hint.point = p;
					return true;
				}

				pointIndex = -1;
				if (reshapable)
				{
					if (STATUS.EDIT == hint.status)
					{
						EDIT_TYPE type = (EDIT_TYPE) hint.option;
						switch(type)
						{
						case ADD:
							if (isNearLine(p))
							{
								hint.point = p;
								return true;
							}
							break;
						case DEL:
							for (int i = 0; i < xPoints.length; i++)
							{
								if (isNear(xPoints[i], yPoints[i], p))
								{
									pointIndex = i;
									hint.point = p;
									return true;
								}
							}
							break;
						}
						return true;
					}
					else
					{
						for (int i = 0; i < xPoints.length; i++)
						{
							if (isNear(xPoints[i], yPoints[i], p))
							{
								pointIndex = i;
								hint.status = STATUS.RESHAPE;
								hint.point = p;
								return true;
							}
						}
						if (0 > pointIndex)
						{
							if (2 < xPoints.length)
							{
								if (hitTestFrame(hint, p, reshapable))
								{
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
					}
				}
			}
			else
			{
				if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType)
				{
					return this.path.contains(p);
				}
				return isNearLine(p);
			}
		}
		return false;
	}

	@Override
	public boolean hitTest(Rectangle rect)
	{
		for (int i = 0; i < this.dPoints.size()-1; i++)
		{
			Point2D.Double p1 = this.dPoints.get(i);
			Point2D.Double p2 = this.dPoints.get(i+1);
			if (rect.intersectsLine(p1.x, p1.y, p2.x, p2.y))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean hitTestFrame(StatusHint hint, Point p, boolean reshapable)
	{
		Rectangle frame = new Rectangle(this.x - TOGGLE, this.y - TOGGLE, this.width + TOGGLE*2, this.height + TOGGLE*2);
		return hitTestFrame(hint, p, reshapable, frame);
	}

	@Override
	public boolean reshape(StatusHint hint, Point dp)
	{
		// 移動
		if (isStatus(STATUS.MOVE))
		{
			return move(hint, dp);
		}

		// リサイズ
		if (true == isStatus(STATUS.RESIZE))
		{
			return resize(hint, dp);
		}

		// 変形
		if (0 <= this.pointIndex && this.pointIndex < this.dPoints.size())
		{
			Point2D.Double p = this.dPoints.get(this.pointIndex);
			p.x += dp.x;
			p.y += dp.y;
			adaptFrame();
			notifyChanged();
			return true;
		}

		return false;
	}

	@Override
	public boolean edit(StatusHint hint, Point p)
	{
		EDIT_TYPE type = (EDIT_TYPE) hint.option;
		int index = -1;
		switch(type)
		{
		case ADD:
			index = this.findSegment(p);
			if (0 > index)
			{
				hint.status = null;
				hint.point = p;
				return false;
			}
			this.pointIndex = index+1;
			this.addPoint(p.x, p.y, pointIndex);
			this.calcPoints();
			this.adaptFrame();

			hint.status = STATUS.RESHAPE;
			hint.point = p;
			break;
		case DEL:
			if (0 > this.pointIndex)
			{
				hint.status = null;
				hint.point = p;
				return false;
			}

			this.remove(pointIndex);
			pointIndex = -1;
			this.calcPoints();
			this.adaptFrame();

			if (2 == dPoints.size())
			{
//				this.isLine = true;
				this.shape = SHAPE.LINE;
			}

			hint.status = null;
			hint.point = p;
		}

		return true;
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

		for (int i = 0; i < this.dPoints.size(); i++)
		{
			Point2D.Double p = this.dPoints.get(i);
			p.x += dp.x;
			p.y += dp.y;
		}
		adaptFrame();

		if (null != hint)
		{
			hint.status = STATUS.MOVE;
			notifyChanged();
		}

		return true;
	}

	@Override
	public boolean resize(StatusHint hint, Point dp)
	{
		// リサイズの準備
		int left = this.x;
		int right = this.x + this.width;
		int top = this.y;
		int bottom = this.y + this.height;

		double w = right - left;
		double h = bottom - top;
		Point origin = new Point();
		double xscale = 0;
		double yscale = 0;
		hint.status = STATUS.RESIZE;
		hint.resizeIndex = getResize();
		switch(hint.resizeIndex)
		{
		case LEFTTOP:
			origin.x = right;
			origin.y = bottom;
			xscale = (w - dp.x)/w;
			yscale = (h - dp.y)/h;
			break;
		case LEFTBOTTOM:
			origin.x = right;
			origin.y = top;
			xscale = (w - dp.x)/w;
			yscale = (h + dp.y)/h;
			break;
		case RIGHTTOP:
			origin.x = left;
			origin.y = bottom;
			xscale = (w + dp.x)/w;
			yscale = (h - dp.y)/h;
			break;
		case RIGHTBOTTOM:
			origin.x = left;
			origin.y = top;
			xscale = (w + dp.x)/w;
			yscale = (h + dp.y)/h;
			break;
		default:
		}

		// 枠のリサイズ
		left = this.x;
		right = this.x + this.width;
		top = this.y;
		bottom = this.y + this.height;
		switch(hint.resizeIndex)
		{
		case LEFTTOP:
			left += dp.x + TOGGLE;
			top += dp.y + TOGGLE;
			break;
		case LEFTBOTTOM:
			left += dp.x + TOGGLE;
			bottom += dp.y - TOGGLE;
			break;
		case RIGHTTOP:
			right += dp.x - TOGGLE;
			top += dp.y + TOGGLE;
			break;
		case RIGHTBOTTOM:
			right += dp.x - TOGGLE;
			bottom += dp.y - TOGGLE;
			break;
		default:
		}
		// 幅 or 高さがゼロになるのを予防
		if (0 >= Math.abs(right - left) || 0 >= Math.abs(bottom - top))
		{
			return false;
		}
		hint.point.translate(dp.x, dp.y);
		if (left > right)
		{
			int tmp = left;
			left = right;
			right = tmp;
			switch(hint.resizeIndex)
			{
			case LEFTTOP:
				hint.resizeIndex = CONTROL.RIGHTTOP;
				break;
			case LEFTBOTTOM:
				hint.resizeIndex = CONTROL.RIGHTBOTTOM;
				break;
			case RIGHTTOP:
				hint.resizeIndex = CONTROL.LEFTTOP;
				break;
			case RIGHTBOTTOM:
				hint.resizeIndex = CONTROL.LEFTBOTTOM;
				break;
			default:
			}
		}
		if (top > bottom)
		{
			int tmp = top;
			top = bottom;
			bottom = tmp;
			switch(hint.resizeIndex)
			{
			case LEFTTOP:
				hint.resizeIndex = CONTROL.LEFTBOTTOM;
				break;
			case LEFTBOTTOM:
				hint.resizeIndex = CONTROL.LEFTTOP;
				break;
			case RIGHTTOP:
				hint.resizeIndex = CONTROL.RIGHTBOTTOM;
				break;
			case RIGHTBOTTOM:
				hint.resizeIndex = CONTROL.RIGHTTOP;
				break;
			default:
			}
		}

		// リサイズ
		resize(origin, xscale, yscale);

		return true;
	}

	@Override
	public void resize(Point origin, double xscale, double yscale)
	{
		for (int i = 0; i < this.dPoints.size(); i++)
		{
			Point2D.Double p = this.dPoints.get(i);
			this.dPoints.set(i, new Point2D.Double(transform(p.x, origin.x, xscale), transform(p.y, origin.y, yscale)));
		}

		adaptFrame();
		notifyChanged();
	}

	private static class TAG extends TAG_BASE
	{
		static final String POINT = "point";
		static final String SEP_COORD = ",";
		static final String SEP_POINT = ";";
	}

	@Override
	public void export(ExportBase export)
	{
		ShapePolyline copy = this.clone();
		copy.resetOrigin(export.getOrigin());
		copy.calcPoints();

		ShapeInfo info = new ShapeInfo(this.shape);
		info.obj = copy;

		PairList list = this.makeDesc(copy);

		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < copy.xPoints.length; ++i)
		{
			if (0 < i)
			{
				buf.append(TAG.SEP_POINT);
			}
			buf.append(String.format("%d%s%d", copy.xPoints[i], TAG.SEP_COORD, copy.yPoints[i]));
		}
		list.add(TAG.POINT, buf.toString());
		info.setDesc(list);

//		info.obj = copy;
//		info.dotSize = copy.dotSize;
//		info.dotSpan = copy.dotSpan;
//		info.xPoints = copy.xPoints;
//		info.yPoints = copy.yPoints;
//		info.desc = copy.getClass().getSimpleName();
//		for (int i = 0; i < copy.xPoints.length; i++)
//		{
//			info.desc += " " + copy.xPoints[i] + " " + copy.yPoints[i];
//		}
//		info.desc += " " + copy.shape.toString();

		export.writeStart(info);
		export.write(info);
		export.writeEnd(info);
	}

	@Override
	public boolean showProperty(DesignPanel panel, Point p)
	{
		return super.showProperty(panel, p);
	}

	public static ShapePolyline parse(ShapeInfo info)
	{
		ShapePolyline obj;

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
			if (0 != words[0].compareToIgnoreCase(ShapePolyline.class.getSimpleName()))
			{
				Util.logError("ShapePolyline className is invalid.");
				return null;
			}

			try
			{
				List<Point2D.Double> ps = Util.newArrayList();
				int nPoints = (words.length - 1)/2;
				for (int i = 0; i < nPoints; i++)
				{
					int index = i*2 + 1;
					int x = Integer.parseInt(words[index]);
					int y = Integer.parseInt(words[index+1]);
					ps.add(new Point2D.Double(x,y));
				}
				SHAPE shape = null;
				if (2 == nPoints)
				{
					shape = SHAPE.LINE;
				}
				else
				{
					switch((words.length-1) % 2)
					{
					case 0:
						shape = SHAPE.POLYLINE;
						break;
					case 1:
						shape = SHAPE.valueOf(words[words.length-1]);
						break;
					}
				}
				obj = new ShapePolyline(ps, shape);
			}
			catch (Exception e)
			{
				Util.logException(e);
				return null;
			}
		}
		else
		{
			obj = new ShapePolyline();

			obj.parseDesc(list);
			vals = list.getValues(TAG.POINT);
			if (0 < vals.size() && vals.get(0) instanceof String)
			{
				String t = (String) vals.get(0);
				String[] points = t.split(TAG.SEP_POINT);
				for (int i = 0; i < points.length; ++i)
				{
					String[] cs = points[i].split(TAG.SEP_COORD);
					double x = java.lang.Double.parseDouble(cs[0]);
					double y = java.lang.Double.parseDouble(cs[1]);
					obj.dPoints.add(new Point2D.Double(x,y));
				}
			}
		}

		obj.adaptFrame();
		obj.setStroke();
		obj.setFillPaint();

		return obj;
	}
}
