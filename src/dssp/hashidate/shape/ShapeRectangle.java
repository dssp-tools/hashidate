package dssp.hashidate.shape;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.StringTokenizer;

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
public class ShapeRectangle extends DesignObject
{
	protected Rectangle2D.Double base = new Rectangle2D.Double();

	ShapeRectangle()
	{
		this.shape = SHAPE.RECTANGLE;
		this.initPopupMenu();
	};

	@Override
	public ShapeRectangle clone()
	{
		ShapeRectangle obj = (ShapeRectangle) super.clone();

		obj.base = (Rectangle2D.Double) this.base.clone();

		return obj;
	}

	public ShapeRectangle(Point p)
	{
		this();

		this.base.x = p.x;
		this.base.y = p.y;
		this.base.width = DEFAULT_WIDTH;
		this.base.height = DEFAULT_HEIGHT;
		adaptFrame();
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

	@Override
	public void adaptFrame()
	{
		this.x = (int)Math.round(this.base.x);
		this.y = (int)Math.round(this.base.y);
		this.width = (int)Math.round(this.base.width);
		this.height = (int)Math.round(this.base.height);
	}

	@Override
	protected void drawBraille(Graphics2D g, boolean printing)
	{
		boolean braille = true;
		if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType)
		{
			this.setFillProperty(g, braille, true);
			g.fillRect(this.x, this.y, this.width, this.height);
			this.setFillProperty(g, braille, false);
		}

		this.setDrawProperty(g, braille, true);
		g.drawRect(this.x, this.y, this.width, this.height);
		this.setDrawProperty(g, braille, false);

		if (isSelected() && false == printing)
		{
			drawToggle(g, this.x, this.y);
			drawToggle(g, this.x + this.width, this.y);
			drawToggle(g, this.x + this.width, this.y + this.height);
			drawToggle(g, this.x, this.y + this.height);
		}
	}

	@Override
	protected void drawSumiji(Graphics2D g, boolean printing)
	{
		boolean braille = false;
		if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType)
		{
			this.setFillProperty(g, braille, true);
			g.fillRect(this.x, this.y, this.width, this.height);
			this.setFillProperty(g, braille, false);
		}

		this.setDrawProperty(g, braille, true);
		g.drawRect(this.x, this.y, this.width, this.height);
		this.setDrawProperty(g, braille, false);

		if (isSelected() && false == printing)
		{
			drawToggle(g, this.x, this.y);
			drawToggle(g, this.x + this.width, this.y);
			drawToggle(g, this.x + this.width, this.y + this.height);
			drawToggle(g, this.x, this.y + this.height);
		}
	}

	boolean isNear(int x, int y, Point p)
	{
		return (Math.abs(x - p.x) < SEP && Math.abs(y - p.y) < SEP);
	}

	@Override
	public boolean hitTest(StatusHint hint, Point p, boolean reshapable)
	{
		int left = this.x;
		int right = this.x + this.width;
		int top = this.y;
		int bottom = this.y + this.height;

		int ld = Math.abs(left - p.x);
		int rd = Math.abs(right - p.x);
		int td = Math.abs(top - p.y);
		int bd = Math.abs(bottom - p.y);
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

			if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType && this.contains(p))
			{
				hint.status = STATUS.MOVE;
				hint.point = p;
				return true;
			}
			if (ld < SEP)
			{
				if (reshapable && td < SEP) // 左上角
				{
					hint.status = STATUS.RESIZE;
					hint.resizeIndex = CONTROL.LEFTTOP;
					hint.point = p;
					return true;
				}
				else if (reshapable && bd < SEP) // 左下角
				{
					hint.status = STATUS.RESIZE;
					hint.resizeIndex = CONTROL.LEFTBOTTOM;
					hint.point = p;
					return true;
				}
				else if (p.y > top && p.y < bottom)	// 左辺
				{
					hint.status = STATUS.MOVE;
					hint.point = p;
					return true;
				}
			}
			else if (rd < SEP)
			{
				if (reshapable && td < SEP) // 右上角
				{
					hint.status = STATUS.RESIZE;
					hint.resizeIndex = CONTROL.RIGHTTOP;
					hint.point = p;
					return true;
				}
				else if (reshapable && bd < SEP) // 右下角
				{
					hint.status = STATUS.RESIZE;
					hint.resizeIndex = CONTROL.RIGHTBOTTOM;
					hint.point = p;
					return true;
				}
				else if (p.y > top && p.y < bottom)	// 右辺
				{
					hint.status = STATUS.MOVE;
					hint.point = p;
					return true;
				}
			}
			else
			{
				if (p.x > left && p.x < right)
				{
					if (td < SEP) // 上辺
					{
						hint.status = STATUS.MOVE;
						hint.point = p;
						return true;
					}
					else if (bd < SEP) // 下辺
					{
						hint.status = STATUS.MOVE;
						hint.point = p;
						return true;
					}
				}
			}
		}
		else
		{
			if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType)
			{
				return this.contains(p);
			}
			else
			{
				if (ld < SEP || rd < SEP)
				{
					if (p.y < bottom && p.y > top)	// 左辺か右辺
					{
						return true;
					}
				}
				else if (td < SEP || bd < SEP)
				{
					if (p.x > left && p.x < right)	// 上辺か下辺
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean hitTest(Rectangle rect)
	{
		return this.intersects(rect);
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

		this.base.x += dp.x;
		this.base.y += dp.y;
		adaptFrame();

		if (null != hint)
		{
			hint.status = STATUS.MOVE;
			hint.point = dp;
			this.notifyChanged();
		}

		return true;
	}

	@Override
	public boolean resize(StatusHint hint, Point dp)
	{
		double left = this.base.x;
		double right = this.base.x + this.base.width;
		double top = this.base.y;
		double bottom = this.base.y + this.base.height;
		hint.status = STATUS.RESIZE;
		hint.resizeIndex = getResize();
		switch(hint.resizeIndex)
		{
		case LEFTTOP:
			left += dp.x;
			top += dp.y;
			break;
		case LEFTBOTTOM:
			left += dp.x;
			bottom += dp.y;
			break;
		case RIGHTTOP:
			right += dp.x;
			top += dp.y;
			break;
		case RIGHTBOTTOM:
			right += dp.x;
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
			double tmp = top;
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
		this.base.x = left;
		this.base.y = top;
		this.base.width = right - left;
		this.base.height = bottom - top;
		adaptFrame();
		notifyChanged();

		return true;
	}

	@Override
	public void resize(Point origin, double xscale, double yscale)
	{
		double left = this.base.x - origin.x;
		double right = this.base.x + this.base.width - origin.x;
		double top = this.base.y - origin.y;
		double bottom = this.base.y + this.base.height - origin.y;

		left = left * xscale + origin.x;
		right = right * xscale + origin.x;
		top = top * yscale + origin.y;
		bottom = bottom * yscale + origin.y;

		this.base.x = Math.min(left,  right);
		this.base.y = Math.min(top, bottom);
		this.base.width = Math.abs(right - left);
		this.base.height = Math.abs(bottom - top);

		adaptFrame();
		notifyChanged();
	}


	@Override
	public boolean reshape(StatusHint hint, Point dp)
	{
		// 移動
		if (isStatus(STATUS.MOVE))
		{
			return move(hint, dp);
		}

		// 変形
		if (isStatus(STATUS.RESIZE))
		{
			return resize(hint, dp);
		}

		return false;
	}

	private static class TAG extends TAG_BASE
	{
	}

	@Override
	public void export(ExportBase export)
	{
		ShapeRectangle copy = this.clone();
		copy.resetOrigin(export.getOrigin());

		ShapeInfo info = new ShapeInfo(SHAPE.RECTANGLE);
		info.obj = copy;

		PairList list = this.makeDesc(copy);

		info.setDesc(list);

//		info.dotSize = copy.dotSize;
//		info.dotSpan = copy.dotSpan;
//		info.desc = String.format("%s %d %d %d %d", this.getClass().getSimpleName(), copy.x, copy.y, copy.width, copy.height);

		export.writeStart(info);
		export.write(info);
		export.writeEnd(info);
	}

	@Override
	public boolean showProperty(DesignPanel panel, Point p)
	{
		return super.showProperty(panel, p);
	}

	public static ShapeRectangle parse(ShapeInfo info)
	{
		ShapeRectangle obj = new ShapeRectangle();
		obj.shape = info.getType();

		PairList list = PairList.fromString(info.desc);
		List<Object> vals = list.getValues(TAG.CLASS);
		if (0 == vals.size())
		{
			StringTokenizer st = new StringTokenizer(info.desc);
			String[] words = new String[st.countTokens()];
			for (int i = 0; st.hasMoreTokens(); i++)
			{
				words[i] = st.nextToken();
			}
			if (5 != words.length || 0 != words[0].compareToIgnoreCase(ShapeRectangle.class.getSimpleName()))
			{
				obj.base.x = info.x;
				obj.base.y = info.y;
				obj.base.width = info.width;
				obj.base.height = info.height;
				obj.adaptFrame();
			}
			else
			{
				try
				{
					obj.base.x = (double)Integer.parseInt(words[1]);
					obj.base.y = (double)Integer.parseInt(words[2]);
					obj.base.width = (double)Integer.parseInt(words[3]);
					obj.base.height = (double)Integer.parseInt(words[4]);
					obj.adaptFrame();
				}
				catch (Exception e)
				{
					Util.logException(e);
					return null;
				}
			}
		}
		else
		{
			obj.parseDesc(list);
			vals = list.getValues(TAG_BASE.X);
			if (0 < vals.size() && vals.get(0) instanceof String)
			{
				obj.base.x = java.lang.Double.parseDouble((String) vals.get(0));
			}
			vals = list.getValues(TAG_BASE.Y);
			if (0 < vals.size() && vals.get(0) instanceof String)
			{
				obj.base.y = java.lang.Double.parseDouble((String) vals.get(0));
			}
			vals = list.getValues(TAG_BASE.WIDTH);
			if (0 < vals.size() && vals.get(0) instanceof String)
			{
				obj.base.width = java.lang.Double.parseDouble((String) vals.get(0));
			}
			vals = list.getValues(TAG_BASE.HEIGHT);
			if (0 < vals.size() && vals.get(0) instanceof String)
			{
				obj.base.height = java.lang.Double.parseDouble((String) vals.get(0));
			}
		}

		obj.adaptFrame();
		obj.setStroke();
		obj.setFillPaint();

		return obj;
	}
}
