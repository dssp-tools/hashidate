package dssp.hashidate.shape;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import javax.swing.JMenuItem;

import dssp.hashidate.DesignPanel;
import dssp.hashidate.ObjectManager;
import dssp.hashidate.io.ExportBase;
import dssp.hashidate.io.ShapeInfo;
import dssp.hashidate.misc.ObjectPopupMenu;
import dssp.hashidate.misc.PairList;
import dssp.brailleLib.Util;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public class ShapeGroup extends DesignObject
{
	private List<DesignObject> group = Util.newArrayList();

	public ShapeGroup()
	{
		this.shape = SHAPE.GROUP;
		this.initPopupMenu();
	}

//	ShapeGroup(ShapeGroup src)
//	{
//		super(src);
//
//		for (int i = 0; i < src.group.size(); i++)
//		{
//			group.add(src.group.get(i).clone());
//		}
//	}

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
	protected void initCommonMenu(ObjectPopupMenu menu)
	{
		JMenuItem menuItem;

		menuItem = new JMenuItem("一つ上へ移動");
		menu.add(menuItem);

		menuItem = new JMenuItem("一つ下へ移動");
		menu.add(menuItem);

		menuItem = new JMenuItem("一番上へ移動");
		menu.add(menuItem);

		menuItem = new JMenuItem("一番下へ移動");
		menu.add(menuItem);
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
	public ShapeGroup clone()
	{
		ShapeGroup obj = (ShapeGroup)super.clone();
		obj.group = Util.newArrayList();

		for (DesignObject child: this.group)
		{
			obj.group.add(child.clone());
		}

		return obj;
	}

	public void setObjects(List<DesignObject> objList)
	{
		for (int i = 0; i < objList.size(); i++)
		{
			this.addObject(objList.get(i));
		}
	}

	@Override
	public void setObjMan(ObjectManager objMan)
	{
		super.setObjMan(objMan);
		int count = group.size();
		for (int i = 0; i < count; i++)
		{
			group.get(i).setObjMan(objMan);
		}
	}

	@Override
	protected void drawBraille(Graphics2D g, boolean printing)
	{
		int count = group.size();
		for (int i = 0; i < count; i++)
		{
			group.get(i).drawBraille(g, printing);
		}
		if (isSelected())
		{
			drawFrame(g);
		}
	}

	@Override
	protected void drawSumiji(Graphics2D g, boolean printing)
	{
		int count = group.size();
		for (int i = 0; i < count; i++)
		{
			group.get(i).drawSumiji(g, printing);
		}
		if (isSelected())
		{
			drawFrame(g);
		}
	}

	@Override
	public boolean hitTest(StatusHint hint, Point p, boolean reshapable)
	{
		if (isSelected())
		{
			int left = this.x;
			int right = this.x + this.width;
			int top = this.y;
			int bottom = this.y + this.height;

			int ld = Math.abs(left - p.x);
			int rd = Math.abs(right - p.x);
			int td = Math.abs(top - p.y);
			int bd = Math.abs(bottom - p.y);

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
			int count = group.size();
			for (int i = 0; i < count; i++)
			{
				if (group.get(i).hitTest(hint, p, false))
				{
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean hitTest(Rectangle rect)
	{
		int count = group.size();
		for (int i = 0; i < count; i++)
		{
			if (group.get(i).hitTest(rect))
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void adaptFrame()
	{
		if (0 == group.size())
		{
			return;
		}
		this.setBounds(group.get(0));
		for (int i = 1; i < group.size(); i++)
		{
			this.add((Rectangle)group.get(i));
		}
	}

	@Override
	public boolean reshape(StatusHint hint, Point dp)
	{
		// 変形
		if (isStatus(STATUS.RESIZE))
		{
			return this.resize(hint, dp);
		}

		return false;
	}

	@Override
	public boolean move(StatusHint hint, Point dp)
	{
		boolean ret = true;

		this.x += dp.x;
		this.y += dp.y;
		int count = group.size();
		for (int i = 0; i < count; i++)
		{
			ret &= group.get(i).move(hint, dp);
		}

		if (null != hint)
		{
			hint.status = STATUS.MOVE;
			notifyChanged();
		}

		return ret;
	}

	@Override
	public boolean resize(StatusHint hint, Point dp)
	{
		Point origin = new Point();
		int left = x;
		int right = x + width;
		int top = y;
		int bottom = y + height;
		hint.status = STATUS.RESIZE;
		hint.resizeIndex = getResize();
		switch(hint.resizeIndex)
		{
		case LEFTTOP:
			left += dp.x;
			top += dp.y;
			origin.x = right;
			origin.y = bottom;
			break;
		case LEFTBOTTOM:
			left += dp.x;
			bottom += dp.y;
			origin.x = right;
			origin.y = top;
			break;
		case RIGHTTOP:
			right += dp.x;
			top += dp.y;
			origin.x = left;
			origin.y = bottom;
			break;
		case RIGHTBOTTOM:
			right += dp.x;
			bottom += dp.y;
			origin.x = left;
			origin.y = top;
			break;
		default:
		}
		hint.point.translate(dp.x, dp.y);
		double xscale = (right - left)/(double)width;
		double yscale = (bottom -top)/(double)height;
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
//				putStatus(panel, STATUS.RESIZE, FRAME.RIGHTTOP, p);
				hint.resizeIndex = CONTROL.RIGHTTOP;
				break;
			default:
			}
		}
		// 幅 or 高さがゼロになるのを予防
//		if (TOGGLE >= Math.abs(right - left) || TOGGLE >= Math.abs(bottom -top))
		if (0 == Math.abs(right - left) || 0 == Math.abs(bottom -top))
		{
			return false;
		}

		resize(origin, xscale, yscale);

		return true;
	}

	@Override
	public void resize(Point origin, double dxscale, double dyscale)
	{
		for (int i = 0; i < group.size(); i++)
		{
			DesignObject obj = group.get(i);
			obj.resize(origin, dxscale, dyscale);
		}

		adaptFrame();
		notifyChanged();
	}

	@Override
	public void export(ExportBase export)
	{
		ShapeInfo info = new ShapeInfo(SHAPE.GROUP);
		PairList list = new PairList();
		list.add(TAG_BASE.CLASS, this.getClass().getSimpleName());
		info.setDesc(list);
		export.openGroup(info);
		int count = group.size();
		for (int i = 0; i < count; i++)
		{
			group.get(i).export(export);
		}
		export.closeGroup(info);
	}

	public int count()
	{
		return group.size();
	}

	public boolean addObject(DesignObject obj)
	{
		if (group.add(obj))
		{
			if (1 == group.size())
			{
				this.setRect(obj);
			}
			else
			{
				this.add((Rectangle)obj);
			}
			return true;
		}

		return false;
	}

	public DesignObject getObject(int index)
	{
		if (0 > index || group.size() <= index)
		{
			return null;
		}
		return group.get(index);
	}

	public boolean hasObject(DesignObject obj)
	{
		return group.contains(obj);
	}

	@Override
	public boolean showProperty(DesignPanel panel, Point p)
	{
		return super.showProperty(panel, p);
	}
}
