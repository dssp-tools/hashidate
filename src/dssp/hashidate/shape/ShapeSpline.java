package dssp.hashidate.shape;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
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

public class ShapeSpline extends DesignObject
{
	// 通過点
	protected List<Point2D.Double> dPoints = Util.newArrayList();
	public List<Point2D.Double> getdPoints()
	{
		return dPoints;
	}

	// 描画用：通過点間の曲線のリスト
	protected List<Path2D.Double> pathList = Util.newArrayList();
	public List<Path2D.Double> getPathList()
	{
		return pathList;
	}

	protected int pointIndex = 0;

	protected int MIN_POINT = 5;
	protected GeneralPath path = new GeneralPath();

	@Override
	public ShapeSpline clone()
	{
		ShapeSpline obj = (ShapeSpline)super.clone();

		obj.dPoints = Util.newArrayList();
		Util.deepCopyByClone(this.dPoints, obj.dPoints);

		obj.pathList = Util.newArrayList();

		obj.adaptFrame();

		return obj;
	}

	private ShapeSpline()
	{
		super();
		this.initPopupMenu();
	}

	private ShapeSpline(List<Point2D.Double> src, SHAPE shape)
	{
		this();
		assert (null == shape || (SHAPE.SPLINE != shape && SHAPE.SPLINE_LOOP != shape));

		this.shape = shape;
		Util.deepCopyByClone(src, this.dPoints);
		this.adaptFrame();
	}

	public ShapeSpline(StatusHint hint, Point p, SHAPE shape)
	{
		this();
		if (null == shape || (SHAPE.SPLINE != shape && SHAPE.SPLINE_LOOP != shape))
		{
			throw new IllegalArgumentException(String.format("Invlid shape %s", shape));
		}

		select(true);
		hint.status = STATUS.INITIAL;
		hint.point = p;
		this.dPoints.add(new Point2D.Double(p.x, p.y));
		this.dPoints.add(new Point2D.Double(p.x, p.y));
		pointIndex = 1;
		this.shape = shape;
		this.adaptFrame();
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
		addPoint(px,py,this.dPoints.size());
	}

	void addPoint(int x, int y, int index)
	{
		addPointAt(x,y,index);
		notifyChanged();
	}

	void addPointAt(int x, int y, int index)
	{
		this.dPoints.add(index, new Point2D.Double(x,y));
		adaptFrame();
	}

	boolean remove(int index)
	{
		if (0 > index || index > (this.dPoints.size() - 1) || 2 == this.dPoints.size())
		{
			return false;
		}
		this.dPoints.remove(index);
		this.adaptFrame();
		this.notifyChanged();

		return true;
	}

	@Override
	public void clicked(DesignPanel panel, Point p)
	{
		if (isStatus(STATUS.INITIAL))
		{
			this.addPoint(p.x, p.y, this.dPoints.size());
			pointIndex = this.dPoints.size()-1;
			this.adaptFrame();
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

	@Override
	protected void drawSumiji(Graphics2D g, boolean printing)
	{
		boolean braille = false;

		this.path.reset();
		for (Path2D.Double partial: this.pathList)
		{
			this.path.append(partial, true);
		}

		if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType)
		{
			this.setFillProperty(g, braille, true);
			g.fill(this.path);
			this.setFillProperty(g, braille, false);
		}

		this.setDrawProperty(g, braille, true);
		g.draw(this.path);

		this.setDrawProperty(g, braille, false);
		if (false == printing && false == this.isStatus(STATUS.INITIAL) && this.isSelected())
		{
			drawFrame(g);
			for (Point2D.Double cp: this.dPoints)
			{
				int x = (int) Math.round(cp.x);
				int y = (int) Math.round(cp.y);
				this.drawToggle(g, new Point(x, y));
			}
		}
	}

	@Override
	protected void drawBraille(Graphics2D g, boolean printing)
	{
		boolean braille = true;

		this.path.reset();
		for (Path2D.Double partial: this.pathList)
		{
			this.path.append(partial, true);
		}

		if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType)
		{
			this.setFillProperty(g, braille, true);
			g.fill(this.path);
			this.setFillProperty(g, braille, false);
		}

		this.setDrawProperty(g, braille, true);
		g.draw(this.path);
		this.setDrawProperty(g, braille, false);

		if (false == printing && false == this.isStatus(STATUS.INITIAL) && this.isSelected())
		{
			drawFrame(g);
			for (Point2D.Double cp: this.dPoints)
			{
				int x = (int) Math.round(cp.x);
				int y = (int) Math.round(cp.y);
				this.drawToggle(g, new Point(x, y));
			}
		}
	}

	@Override
	protected void drawFrame(Graphics g)
	{
		Rectangle frame = new Rectangle(this.x - TOGGLE, this.y - TOGGLE, this.width + TOGGLE*2, this.height + TOGGLE*2);
		drawFrame(g, frame);
	}

	private int findSegment(Point p)
	{
		// 点と曲線との最短距離を求める
		double dist = java.lang.Double.MAX_VALUE;

		int nSeg = this.pathList.size();
		double[] coords = new double[2];
		Line2D.Double seg = new Line2D.Double();
		for (int i = 0; i < nSeg; i++)
		{
			Path2D.Double partial = this.pathList.get(i);
			Point2D.Double prevPoint = null;

			for (PathIterator it = partial.getPathIterator(null); false == it.isDone(); it.next())
			{
				it.currentSegment(coords);
				if (null == prevPoint)
				{
					prevPoint = new Point2D.Double(coords[0], coords[1]);
				}
				else
				{
					seg.setLine(coords[0], coords[1], prevPoint.x, prevPoint.y);
					dist = seg.ptSegDist(p.x, p.y);
					if (dist < SEP)
					{
						return i;
					}
					prevPoint.x = coords[0];
					prevPoint.y = coords[1];
				}
			}
		}

		return -1;
	}

	protected boolean isNearLine(Point p)
	{
		return (0 <= this.findSegment(p));
	}

	@Override
	public boolean hitTestFrame(StatusHint hint, Point p, boolean reshapable)
	{
		Rectangle frame = new Rectangle(this.x - TOGGLE, this.y - TOGGLE, this.width + TOGGLE*2, this.height + TOGGLE*2);
		return hitTestFrame(hint, p, reshapable, frame);
	}

	@Override
	public boolean hitTest(StatusHint hint, Point p, boolean reshapable)
	{
		if (false == isStatus(STATUS.INITIAL))
		{
			if (this.isSelected())
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
							// 曲線
							if (this.isNearLine(p))
							{
								hint.point = p;
								return true;
							}
							break;
						case DEL:
							// 通過点
							int nPoints = this.dPoints.size();
							for (int i = 0; i < nPoints; i++)
							{
								Point2D.Double cp = this.dPoints.get(i);
								if (Math.abs(p.x - cp.x) < SEP && Math.abs(p.y - cp.y)< SEP)
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
						// 選択枠
						if (this.hitTestFrame(hint, p, reshapable))
						{
							return true;
						}

						// 通過点
						int nPoints = this.dPoints.size();
						for (int i = 0; i < nPoints; i++)
						{
							Point2D.Double cp = this.dPoints.get(i);
							if (Math.abs(p.x - cp.x) < SEP && Math.abs(p.y - cp.y)< SEP)
							{
								pointIndex = i;
								hint.status = STATUS.RESHAPE;
								hint.point = p;
								return true;
							}
						}

						// 曲線
						if (0 > pointIndex)
						{
							if (this.isNearLine(p))
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
				return this.isNearLine(p);
			}
		}
		return false;
	}

	@Override
	public boolean hitTest(Rectangle rect)
	{
		if (null == this.pathList)
		{
			return false;
		}
		double[] coords = new double[2];
		Line2D.Double seg = new Line2D.Double();
		for (Path2D.Double partial: this.pathList)
		{
			Point2D.Double prevPoint = null;

			for (PathIterator it = partial.getPathIterator(null); false == it.isDone(); it.next())
			{
				it.currentSegment(coords);
				if (null == prevPoint)
				{
					prevPoint = new Point2D.Double(coords[0], coords[1]);
				}
				else
				{
					seg.setLine(coords[0], coords[1], prevPoint.x, prevPoint.y);
					if (seg.intersects(rect.x, rect.y, rect.width, rect.height))
					{
						return true;
					}
					prevPoint.x = coords[0];
					prevPoint.y = coords[1];
				}
			}
		}
		return false;
	}

	@Override
	public void adaptFrame()
	{
		this.calcCurve();

		Point2D.Double p = this.dPoints.get(0);
		Rectangle2D.Double rect = new Rectangle2D.Double(p.x, p.y, 0, 0);
		for (Path2D.Double seg: this.pathList)
		{
			Rectangle2D bound = seg.getBounds2D();
			rect.add(bound);
		}

		this.x = (int) Math.round(rect.x);
		this.y = (int) Math.round(rect.y);
		this.width = (int) Math.round(rect.width);
		this.height = (int) Math.round(rect.height);
	}

	private void calcCurve()
	{
		// Catmull-Romスプライン曲線を生成する
		this.pathList.clear();
		int nPoints = this.dPoints.size();
		switch(nPoints)
		{
		case 0:
		case 1:
			break;
		case 2:
			Path2D.Double line = new Path2D.Double();
			Point2D.Double[] cp = {this.dPoints.get(0), this.dPoints.get(1)};
			line.moveTo(cp[0].x, cp[0].y);
			line.lineTo(cp[1].x, cp[1].y);
			this.pathList.add(line);
			break;
		default:
			switch(this.shape)
			{
			case SPLINE:
			{
				Point2D.Double[] ps = {this.dPoints.get(0), this.dPoints.get(0), this.dPoints.get(1), this.dPoints.get(2)};

				Path2D.Double seg = this.calcPath(ps);
				this.pathList.add(seg);

				if (3 < nPoints)
				{
					for (int i = 0; i < (nPoints-3); i++)
					{
						Point2D.Double[] p = {this.dPoints.get(i), this.dPoints.get(i+1), this.dPoints.get(i+2), this.dPoints.get(i+3)};

						seg = this.calcPath(p);
						this.pathList.add(seg);
					}
				}

				Point2D.Double[] pe = {this.dPoints.get(nPoints-3), this.dPoints.get(nPoints-2), this.dPoints.get(nPoints-1), this.dPoints.get(nPoints-1)};

				seg = this.calcPath(pe);
				this.pathList.add(seg);
				break;
			}
			case SPLINE_LOOP:
			{
				Point2D.Double[] ps = {this.dPoints.get(nPoints-1), this.dPoints.get(0), this.dPoints.get(1), this.dPoints.get(2)};

				Path2D.Double seg = this.calcPath(ps);
				this.pathList.add(seg);

				if (3 < nPoints)
				{
					for (int i = 0; i < (nPoints-3); i++)
					{
						Point2D.Double[] p = {this.dPoints.get(i), this.dPoints.get(i+1), this.dPoints.get(i+2), this.dPoints.get(i+3)};

						seg = this.calcPath(p);
						this.pathList.add(seg);
					}
				}

				Point2D.Double[] pb = {this.dPoints.get(nPoints-3), this.dPoints.get(nPoints-2), this.dPoints.get(nPoints-1), this.dPoints.get(0)};

				seg = this.calcPath(pb);
				this.pathList.add(seg);

				Point2D.Double[] pe = {this.dPoints.get(nPoints-2), this.dPoints.get(nPoints-1), this.dPoints.get(0), this.dPoints.get(1)};

				seg = this.calcPath(pe);
				this.pathList.add(seg);
				break;
			}
			default:
			}
		}
	}

	private Point2D.Double velocity(Point2D.Double p1, Point2D.Double p2)
	{
		double vx = (p2.x - p1.x)/2;
		double vy = (p2.y - p1.y)/2;
		return new Point2D.Double(vx, vy);
	}

	public Path2D.Double calcPath(Point2D.Double[] p)
	{
		// 速度
		Point2D.Double[] v = {this.velocity(p[0], p[2]), this.velocity(p[1], p[3])};

		// 係数
		double a = 2*p[1].x - 2*p[2].x + v[0].x + v[1].x;
		double b = -3*p[1].x + 3*p[2].x - 2*v[0].x - v[1].x;
		double c = v[0].x;
		double d = p[1].x;
		double[] ax = {a,b,c,d};

		a = 2*p[1].y - 2*p[2].y + v[0].y + v[1].y;
		b = -3*p[1].y + 3*p[2].y - 2*v[0].y - v[1].y;
		c = v[0].y;
		d = p[1].y;
		double[] ay = {a, b, c,d};

		double dx = p[2].x - p[3].x;
		double dy = p[2].y - p[3].y;
		double dd = Math.sqrt(dx*dx + dy*dy);
		int nPoint = (int) Math.ceil(dd/this.getDotSpan());
		if (MIN_POINT > nPoint)
		{
			nPoint = MIN_POINT;
		}

		Path2D.Double seg = null;
		while(true)
		{
			seg = new Path2D.Double();
			double px = p[1].x;
			double py = p[1].y;
			seg.moveTo(px, py);
			double maxD = java.lang.Double.MIN_VALUE;
			for (int i = 0; i < nPoint; i++)
			{
				double t = (double)(i+1)/nPoint;

				double x = ax[3] + t*(ax[2] + t*(ax[1] + t*ax[0]));
				double y = ay[3] + t*(ay[2] + t*(ay[1] + t*ay[0]));

				seg.lineTo(x, y);

				dx = x - px;
				dy = y - py;
				maxD = Math.max(maxD, Math.sqrt(dx*dx + dy*dy));

				px = x;
				py = y;
			}
			if (maxD < this.dotSpan)
			{
				break;
			}
			nPoint *= 2;
		}
		return seg;
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
		if (0 <= pointIndex && pointIndex < this.dPoints.size())
		{
			Point2D.Double p = this.dPoints.get(pointIndex);
			p.x += dp.x;
			p.y += dp.y;
			adaptFrame();
			notifyChanged();
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
			pointIndex = index+1;
			this.addPoint(p.x, p.y, pointIndex);
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
			this.adaptFrame();

			hint.status = null;
			hint.point = p;
		}

		return true;
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
		ShapeSpline copy = this.clone();
		copy.resetOrigin(export.getOrigin());

		ShapeInfo info = new ShapeInfo(this.shape);
		info.obj = copy;

		PairList list = this.makeDesc(copy);

		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < copy.dPoints.size(); ++i)
		{
			if (0 < i)
			{
				buf.append(TAG.SEP_POINT);
			}
			int x = (int) Math.round(copy.dPoints.get(i).x);
			int y = (int) Math.round(copy.dPoints.get(i).y);
			buf.append(String.format("%d%s%d", x, TAG.SEP_COORD, y));
		}
		list.add(TAG.POINT, buf.toString());

		info.setDesc(list);

//		info.dotSize = copy.dotSize;
//		info.dotSpan = copy.dotSpan;
//		info.xPoints = new int[copy.dPoints.size()];
//		info.yPoints = new int[copy.dPoints.size()];
//		int index = 0;
//		for (Point2D.Double p: copy.dPoints)
//		{
//			info.xPoints[index] = (int) Math.round(p.x);
//			info.yPoints[index] = (int) Math.round(p.y);
//			index++;
//		}
//		info.pathList = copy.pathList;
//		info.desc = copy.getClass().getSimpleName();
//		info.x = this.x;
//		info.y = this.y;
//		info.width = this.width;
//		info.height = this.height;
//		for (Point2D.Double p: copy.dPoints)
//		{
//			int x = (int) Math.round(p.x);
//			int y = (int) Math.round(p.y);
//			info.desc += " " + x + " " + y;
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

	public static ShapeSpline parse(ShapeInfo info)
	{
		ShapeSpline obj = null;

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
			if (0 != words[0].compareToIgnoreCase(ShapeSpline.class.getSimpleName()))
			{
				Util.logError("ShapeSpline className is invalid.");
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
				SHAPE shape = SHAPE.valueOf(words[words.length-1]);
				obj = new ShapeSpline(ps, shape);
			}
			catch (Exception e)
			{
				Util.logException(e);
				return null;
			}
		}
		else
		{
			obj = new ShapeSpline();
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
