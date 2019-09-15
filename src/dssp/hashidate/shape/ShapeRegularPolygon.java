package dssp.hashidate.shape;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.List;

import dssp.hashidate.DesignPanel;
import dssp.hashidate.io.ExportBase;
import dssp.hashidate.io.ShapeInfo;
import dssp.hashidate.misc.FigureType;
import dssp.hashidate.misc.ObjectPopupMenu;
import dssp.hashidate.misc.PairList;
import dssp.hashidate.shape.property.ShapeRegularPolygonProperty;

public class ShapeRegularPolygon extends ShapePolyline
{
	protected int numberOfCorners = 3;
	protected double radius = 50;

	public ShapeRegularPolygon()
	{
		super();
		this.shape = SHAPE.REGULAR_POLYGON;
		this.initPopupMenu();
	}

	public ShapeRegularPolygon(StatusHint hint, Point p, SHAPE shape)
	{
		this();

		this.x = p.x;
		this.y = p.y;

		this.showProperty(null, p);
		if (false == this.changed)
		{
			this.initPoints();
			this.adaptFrame();
		}
	}

	@Override
	public ShapeRegularPolygon clone()
	{
		ShapeRegularPolygon obj = (ShapeRegularPolygon) super.clone();

		obj.numberOfCorners = this.numberOfCorners;
		obj.radius = this.radius;

		return obj;
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

//		popupMenu.addSeparator();
//		JMenuItem item;
//		item = new JMenuItem("多角形にする");
//		popupMenu.add(item);
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
		case "多角形にする":
			hint.status = STATUS.CHANGE;
			popupMenu.callback(hint);
			break;
		}
	}

	/**
	 * 正多角形の頂点を計算する
	 */
	private void initPoints()
	{
		this.width = (int) Math.round(this.radius*2);
		this.height = (int) Math.round(this.radius*2);

		if (this.dPoints.size() > this.numberOfCorners)
		{
			for (int i=(this.dPoints.size()-1); i>= this.numberOfCorners; i--)
			{
				this.dPoints.remove(i);
			}
		}
		else if (this.dPoints.size() < this.numberOfCorners)
		{
			for (int i = this.dPoints.size(); i < this.numberOfCorners; i++)
			{
				this.dPoints.add(new Point2D.Double());
			}
		}

		Point2D.Double c = new Point2D.Double();
		c.x = this.getCenterX();
		c.y = this.getCenterY();
		for (int i = 0; i < this.numberOfCorners; i++)
		{
			double a = i*2*Math.PI/this.numberOfCorners;
			Point2D.Double tp = this.dPoints.get(i);
			tp.x = c.x + this.radius*Math.sin(a);
			tp.y = c.y - this.radius*Math.cos(a);
		}
	}

	/*
	 * 角数を取得する
	 *
	 * @return 角数
	 */
	public int getNumberOfCorners()
	{
		return this.numberOfCorners;
	}

	/**
	 * 半径を取得する
	 *
	 * @return 半径
	 */
	public double getRadius()
	{
		return this.radius;
	}

	@Override
	public void adaptFrame()
	{
	}

	@Override
	public boolean move(StatusHint hint, Point dp)
	{
		if (super.move(hint, dp))
		{
			this.x += dp.x;
			this.y += dp.y;
			return true;
		}

		return false;
	}

	@Override
	protected void drawBraille(Graphics2D g, boolean printing)
	{
		boolean braille = true;

		if (null != this.dPoints && 1 < this.dPoints.size())
		{
			this.calcPoints();

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

			if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType)
			{
				this.setFillProperty(g, braille, true);
				g.fill(this.path);
				this.setFillProperty(g, braille, false);
			}

			this.setDrawProperty(g, braille, true);
			g.drawPolygon(xPoints, yPoints, xPoints.length);
			this.setDrawProperty(g, braille, false);
			if (false == printing && false == this.isStatus(STATUS.INITIAL) && this.isSelected())
			{
				drawFrame(g);
			}
		}
	}

	@Override
	protected void drawFrame(Graphics g)
	{
		Graphics2D g2 = (Graphics2D)g;
		BasicStroke st0 = (BasicStroke)g2.getStroke();
		float[] dash = {10.0f, 3.0f};
		BasicStroke st = new BasicStroke(st0.getLineWidth(), BasicStroke.CAP_BUTT, st0.getLineJoin(), st0.getMiterLimit(), dash, 0.0f);
		g2.setStroke(st);
		g2.setColor(this.frameColor);
		g.drawArc(this.x, this.y, this.width, this.height, 0, 360);

		g2.setStroke(st0);
		this.drawArcToggles(g);
	}

	private void drawArcToggles(Graphics g)
	{
		int rx = this.width/2;
		int ry = this.height/2;
		int cx = this.x + rx;
		int cy = this.y + ry;
		this.drawToggle(g, cx, cy - ry);
		this.drawToggle(g, cx + rx, cy);
		this.drawToggle(g, cx, cy + ry);
		this.drawToggle(g, cx - rx, cy);
	}

	@Override
	public boolean hitTest(StatusHint hint, Point p, boolean reshapable)
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

			int rx = this.width/2;
			int ry = this.height/2;
			int cx = this.x + rx;
			int cy = this.y + ry;
			if (SEP > Math.abs(p.x - cx))
			{
				if (SEP > Math.abs(p.y - (cy - ry)))
				{
					hint.status = STATUS.RESIZE;
					hint.resizeIndex = CONTROL.TOP;
					hint.point = p;
					return true;
				}
				else if (SEP > Math.abs(p.y - (cy + ry)))
				{
					hint.status = STATUS.RESIZE;
					hint.resizeIndex = CONTROL.BOTTOM;
					hint.point = p;
					return true;
				}
			}
			else if (SEP > Math.abs(p.y - cy))
			{
				if (SEP > Math.abs(p.x - (cx - rx)))
				{
					hint.status = STATUS.RESIZE;
					hint.resizeIndex = CONTROL.LEFT;
					hint.point = p;
					return true;
				}
				else if (SEP > Math.abs(p.x - (cx + rx)))
				{
					hint.status = STATUS.RESIZE;
					hint.resizeIndex = CONTROL.RIGHT;
					hint.point = p;
					return true;
				}
			}
			else if (this.isNearLine(p))
			{
				hint.status = STATUS.MOVE;
				hint.point = p;
				return true;
			}


			return false;
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

	@Override
	public boolean reshape(StatusHint hint, Point dp)
	{
		// 移動
		if (isStatus(STATUS.MOVE))
		{
			return this.move(hint, dp);
		}

		// リサイズ
		if (true == isStatus(STATUS.RESIZE))
		{
			return this.resize(hint, dp);
		}

		return false;
	}

	@Override
	public boolean resize(StatusHint hint, Point dp)
	{
/*		double left = this.x;
		double right = this.x + this.width;
		double top = this.y;
		double bottom = this.y + this.height;

		hint.status = STATUS.RESIZE;
		hint.resizeIndex = getResize();
		switch(hint.resizeIndex)
		{
		case LEFT:
			left += dp.x;
			break;
		case TOP:
			top += dp.y;
			break;
		case RIGHT:
			right += dp.x;
			break;
		case BOTTOM:
			bottom += dp.y;
			break;
		default:
		}
		hint.point.translate(dp.x, dp.y);
		if (left > right)
		{
			double tmp = left;
			left = right;
			right = tmp;
			switch(getResize())
			{
			case LEFT:
				hint.resizeIndex = CONTROL.RIGHT;
				break;
			case RIGHT:
				hint.resizeIndex = CONTROL.LEFT;
				break;
			default:
			}
		}
		if (top > bottom)
		{
			double tmp = top;
			top = bottom;
			bottom = tmp;
			switch(hint.resizeIndex)
			{
			case TOP:
				hint.resizeIndex = CONTROL.BOTTOM;
				break;
			case BOTTOM:
				hint.resizeIndex = CONTROL.TOP;
				break;
			default:
			}
		}
		switch(hint.resizeIndex)
		{
		case TOP:
			this.y = (int) top;
		case BOTTOM:
			this.radius = (bottom-top)/2;
			break;
		case LEFT:
			this.x = (int) left;
		case RIGHT:
			this.radius = (right-left)/2;
			break;
		default:
		}
*/
		double left = this.x;
		double right = this.x + this.width;
		double top = this.y;
		double bottom = this.y + this.height;

		Point2D.Double center = new Point2D.Double();
		center.x = (right + left)/2;
		center.y = (bottom + top)/2;

		hint.status = STATUS.RESIZE;
		hint.resizeIndex = getResize();
		switch(hint.resizeIndex)
		{
		case LEFT:
			left += 2*dp.x;
			break;
		case TOP:
			top += 2*dp.y;
			break;
		case RIGHT:
			right += 2*dp.x;
			break;
		case BOTTOM:
			bottom += 2*dp.y;
			break;
		default:
		}
		hint.point.translate(dp.x, dp.y);
		if (left > right)
		{
			double tmp = left;
			left = right;
			right = tmp;
			switch(getResize())
			{
			case LEFT:
				hint.resizeIndex = CONTROL.RIGHT;
				break;
			case RIGHT:
				hint.resizeIndex = CONTROL.LEFT;
				break;
			default:
			}
		}
		if (top > bottom)
		{
			double tmp = top;
			top = bottom;
			bottom = tmp;
			switch(hint.resizeIndex)
			{
			case TOP:
				hint.resizeIndex = CONTROL.BOTTOM;
				break;
			case BOTTOM:
				hint.resizeIndex = CONTROL.TOP;
				break;
			default:
			}
		}
		switch(hint.resizeIndex)
		{
		case TOP:
		case BOTTOM:
			this.height = (int) (bottom - top);
			this.width = this.height;
			break;
		case LEFT:
		case RIGHT:
			this.width = (int) (right - left);
			this.height = this.width;
			break;
		default:
		}
		this.x = (int) (center.x - this.width/2);
		this.y = (int) (center.y - this.height/2);

		this.radius = this.width/2;

		this.initPoints();
		this.adaptFrame();
		this.notifyChanged();

		return true;
	}

	@Override
	public void resize(Point origin, double xscale, double yscale)
	{
		double left = this.x - origin.x;
		double right = this.x + this.width - origin.x;
		double top = this.y - origin.y;
		double bottom = this.y + this.height - origin.y;

		left = left * xscale + origin.x;
		right = right * xscale + origin.x;
		top = top * yscale + origin.y;
		bottom = bottom * yscale + origin.y;

		this.x = (int) Math.min(left,  right);
		this.y = (int) Math.min(top, bottom);
		this.radius = Math.abs(right - left)/2;

		this.initPoints();
		this.adaptFrame();
		this.notifyChanged();
	}

	private static class TAG extends TAG_BASE
	{
		static final String NCORNER = "ncorner";
		static final String RADIUS = "radius";
	}

	@Override
	public boolean showProperty(DesignPanel panel, Point p)
	{
		ShapeRegularPolygonProperty dlg = ShapeRegularPolygonProperty.getInstance();
		this.setCommonProperty(dlg);

		dlg.setNCorner(this.numberOfCorners);

		this.changed = false;
		dlg.setVisible(true);
		if (dlg.isOk())
		{
			this.getCommonProperty(dlg);

			int val = dlg.getNCorner();
			if (val != this.numberOfCorners)
			{
				this.changed = true;
				this.numberOfCorners = val;
			}
			if (this.changed)
			{
				this.initPoints();
				this.adaptFrame();
			}
		}

		return true;
	}

	@Override
	public void export(ExportBase export)
	{
		ShapeRegularPolygon copy = this.clone();
		copy.resetOrigin(export.getOrigin());
		copy.initPoints();;

		ShapeInfo info = new ShapeInfo(this.shape);
		info.obj = copy;

		PairList list = this.makeDesc(copy);

		list.add(TAG.NCORNER, copy.numberOfCorners);
		list.add(TAG.RADIUS, copy.radius);
		info.setDesc(list);

		export.writeStart(info);
		export.write(info);
		export.writeEnd(info);
	}

	public static ShapeRegularPolygon parse(ShapeInfo info)
	{
		ShapeRegularPolygon obj;

		PairList list = PairList.fromString(info.desc);
		List<Object> vals = null;
		vals = list.getValues(TAG.CLASS);
		obj = new ShapeRegularPolygon();

		obj.parseDesc(list);
		vals = list.getValues(TAG.NCORNER);
		if (0 < vals.size() && vals.get(0) instanceof String)
		{
			obj.numberOfCorners = java.lang.Integer.parseInt((String) vals.get(0));
		}
		vals = list.getValues(TAG.RADIUS);
		if (0 < vals.size() && vals.get(0) instanceof String)
		{
			obj.radius = java.lang.Double.parseDouble((String) vals.get(0));
		}
		obj.initPoints();

		obj.adaptFrame();
		obj.setStroke();
		obj.setFillPaint();

		return obj;
	}
}
