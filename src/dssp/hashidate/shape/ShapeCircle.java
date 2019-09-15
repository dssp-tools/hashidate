package dssp.hashidate.shape;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.EnumMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;

import dssp.hashidate.DesignPanel;
import dssp.hashidate.io.ExportBase;
import dssp.hashidate.io.ShapeInfo;
import dssp.hashidate.misc.FigureType;
import dssp.hashidate.misc.FigureType.LINE_TYPE;
import dssp.hashidate.misc.LineStroke;
import dssp.hashidate.misc.ObjectPopupMenu;
import dssp.hashidate.misc.PairList;
import dssp.brailleLib.Util;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public class ShapeCircle extends DesignObject
{
	public static enum STYLE
	{
		/**
		 * 円弧(既定値)
		 */
		ARC(0),
		/**
		 * 扇
		 */
		PIE(1),
		/**
		 * 弓形
		 */
		CHORD(2);

		private final int type;

		STYLE(int type)
		{
			this.type = type;
		}

		static STYLE parse(String name)
		{
			try
			{
				int val = Integer.parseInt(name);
				for (STYLE obj: STYLE.values())
				{
					if (obj.type == val)
					{
						return obj;
					}
				}
				return null;
			}
			catch (NumberFormatException ex)
			{
				return STYLE.valueOf(name);
			}
		}

		/**
		 * nameの値を取得する
		 *
		 * @param name
		 * @return 値場見つからない場合は既定値
		 */
		static STYLE getValueOf(String name)
		{
			try
			{
				return valueOf(name);
			}
			catch (IllegalArgumentException ex)
			{
				return STYLE.ARC;
			}
		}
	};
	private Rectangle2D.Double base = new Rectangle2D.Double();
	private STYLE style = STYLE.ARC;

	public STYLE getStyle()
	{
		return style;
	}

	private void setStyle(STYLE style)
	{
		this.style = style;

		this.adaptFrame();
	}

	public static enum ANGLE implements HINT_OPTION
	{
		START,
		END,
		EXTENT;
	}
	private EnumMap<ANGLE, Integer> angle = new EnumMap<ANGLE, Integer>(ANGLE.class);
	private EnumMap<ANGLE, Point> toggle = new EnumMap<ANGLE, Point>(ANGLE.class);
	private Point2D.Double center = new Point2D.Double();

	public ShapeCircle()
	{
		this.shape = SHAPE.ELLIPSE;

		this.angle.put(ANGLE.START, 0);
		this.angle.put(ANGLE.END, 360);

		for (ANGLE key: ANGLE.values())
		{
			if (ANGLE.EXTENT == key)
			{
				continue;
			}
			this.toggle.put(key, new Point());
		}

//		initMenu();
		initPopupMenu();
	};

	@Override
	public ShapeCircle clone()
	{
		ShapeCircle obj = (ShapeCircle)super.clone();

		obj.base = (Rectangle2D.Double) this.base.clone();
		obj.angle = this.angle.clone();
		obj.toggle = this.toggle.clone();
		for (ANGLE key: ANGLE.values())
		{
			if (ANGLE.EXTENT == key)
			{
				continue;
			}
			int val = this.angle.get(key);
			obj.angle.put(key, val);
			obj.toggle.put(key, (Point) this.toggle.get(key).clone());
		}
		obj.style = this.style;

		obj.arc = new Arc2D.Double(this.arc.getBounds2D(), this.arc.getAngleStart(), this.arc.getAngleExtent(), this.arc.getArcType());
		obj.center = (Point2D.Double) this.center.clone();

		return obj;
	}

	public ShapeCircle(Point p, SHAPE shape)
	{
		this();
		this.base.x = p.x;
		this.base.y = p.y;
		this.base.width = DEFAULT_WIDTH;
		this.base.height = DEFAULT_HEIGHT;
		this.shape = shape;
		adaptFrame();
	}

	private static ObjectPopupMenu popupMenu;

	// 円、楕円
	private static ButtonGroup menuGroup1;
	private static JRadioButtonMenuItem menuCircle;
	private static JRadioButtonMenuItem menuEllipse;
	// 円弧、扇形、弓形
	private static ButtonGroup menuGroup2;
	private static JRadioButtonMenuItem menuArc;
	private static JRadioButtonMenuItem menuPie;
	private static JRadioButtonMenuItem menuChord;

	private void initPopupMenu()
	{
		if (null != popupMenu)
		{
			return;
		}
		popupMenu = new ObjectPopupMenu();
		this.initCommonMenu(popupMenu);

		popupMenu.addSeparator();

		menuGroup1 = new ButtonGroup();

		menuCircle = new JRadioButtonMenuItem("円");
		popupMenu.add(menuCircle);
		menuGroup1.add(menuCircle);

		menuEllipse = new JRadioButtonMenuItem("楕円");
		popupMenu.add(menuEllipse);
		menuGroup1.add(menuEllipse);

		popupMenu.addSeparator();

		menuGroup2 = new ButtonGroup();

		menuArc = new JRadioButtonMenuItem("円弧");
		popupMenu.add(menuArc);
		menuGroup2.add(menuArc);

		menuPie = new JRadioButtonMenuItem("扇形");
		popupMenu.add(menuPie);
		menuGroup2.add(menuPie);

		menuChord = new JRadioButtonMenuItem("弓形");
		popupMenu.add(menuChord);
		menuGroup2.add(menuChord);
	}

	@Override
	public void showMenu(Point location)
	{
		if (null == location || popupMenu.isVisible())
		{
			popupMenu.setVisible(false);
			return;
		}

		menuCircle.setSelected(false);
		menuEllipse.setSelected(false);
		switch(this.shape)
		{
		case CIRCLE:
			menuCircle.setSelected(true);
			break;
		case ELLIPSE:
			menuEllipse.setSelected(true);
			break;
		default:
		}

		menuArc.setSelected(false);
		menuPie.setSelected(false);
		menuChord.setSelected(false);
		switch(this.style)
		{
		case ARC:
			menuArc.setSelected(true);
			break;
		case PIE:
			menuPie.setSelected(true);
			break;
		case CHORD:
			menuChord.setSelected(true);
			break;
		}

		popupMenu.show(this, location);;
	}

	@Override
	public void menuCalled(String name, Point location, StatusHint hint)
	{
		switch(name)
		{
		case "円":
			setShape(SHAPE.CIRCLE);
			break;
		case "楕円":
			setShape(SHAPE.ELLIPSE);
			break;
		case "円弧":
			setStyle(STYLE.ARC);
			break;
		case "扇形":
			setStyle(STYLE.PIE);
			break;
		case "弓形":
			setStyle(STYLE.CHORD);
			break;
		}
	}

	private boolean setShape(SHAPE shape)
	{
		assert (SHAPE.CIRCLE != shape && SHAPE.ELLIPSE != shape);

		if (shape == SHAPE.CIRCLE && this.width != this.height)
		{
			double radius;
			if(Util.select1("短軸に合わせますか？\r\n「いいえ」を選ぶと長軸に合わせます。"))
			{
				if (this.width > this.height)
				{
					this.base.width = this.base.height;
					radius = this.base.height/2;
					this.base.x = this.center.x - radius;
				}
				else
				{
					this.base.height = this.base.width;
					radius = this.base.width/2;
					this.base.y = this.center.y - radius;
				}
			}
			else
			{
				if (this.width < this.height)
				{
					this.base.width = this.base.height;
					radius = this.base.height/2;
					this.base.x = this.center.x - radius;
				}
				else
				{
					this.base.height = this.base.width;
					radius = this.base.width/2;
					this.base.y = this.center.y - radius;
				}
			}
		}

		this.shape = shape;

		this.adaptFrame();

		return true;
	}

	@Override
	protected void drawBraille(Graphics2D g, boolean printing)
	{
		boolean braille = true;
		if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType)
		{
			this.setFillProperty(g, braille, true);
			g.fill(this.arc);
			this.setFillProperty(g, braille, false);
		}

		this.setDrawProperty(g, braille, true);
		g.draw(this.arc);
		this.setDrawProperty(g, braille, false);

		if (isSelected() && false == printing)
		{
			this.drawArcToggles(g);

			int dangle = this.getAngle(ANGLE.EXTENT);
			if (360 != dangle)
			{
				Stroke back = g.getStroke();
				g.setStroke(restArc);
				g.drawArc(this.x, this.y, this.width, this.height, this.angle.get(ANGLE.END), 360-dangle);
				g.setStroke(back);
			}
		}
	}

	Arc2D arc = new Arc2D.Double();

	private void setArc()
	{
		int dangle = this.getAngle(ANGLE.EXTENT);

		switch(style)
		{
		case ARC:
			arc.setArc(this.x,this.y, this.width, this.height, this.angle.get(ANGLE.START), dangle, Arc2D.OPEN);
			break;
		case PIE:
			arc.setArc(this.x,this.y, this.width, this.height, this.angle.get(ANGLE.START), dangle, Arc2D.PIE);
			break;
		case CHORD:
			arc.setArc(this.x,this.y, this.width, this.height, this.angle.get(ANGLE.START), dangle, Arc2D.CHORD);
			break;
		}
	}

	public Arc2D getArc()
	{
		return arc;
	}

	private static Stroke restArc = new LineStroke(LINE_TYPE.DASHED);

	@Override
	protected void drawSumiji(Graphics2D g, boolean printing)
	{
		boolean braille = false;
		int dangle = (int) this.arc.getAngleExtent();
		if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType)
		{
			this.setFillProperty(g, braille, true);
			g.fill(this.arc);
			this.setFillProperty(g, braille, false);
		}

		this.setDrawProperty(g, braille, true);
		g.draw(this.arc);
		this.setDrawProperty(g, braille, false);

		if (isSelected() && false == printing)
		{
			this.drawArcToggles(g);

			if (360 != dangle)
			{
				Stroke back = g.getStroke();
				g.setStroke(restArc);
				g.drawArc(this.x, this.y, this.width, this.height, this.angle.get(ANGLE.END), 360-dangle);
				g.setStroke(back);
			}
		}
	}

//	private int getArcAngle()
//	{
//		int sa = this.angle.get(ANGLE.START);
//		int ea = this.angle.get(ANGLE.END);
//
//		if (sa < ea)
//		{
//			return (ea-sa);
//		}
//		else
//		{
//			return (ea + 360 - sa);
//		}
//	}

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

		for (ANGLE key: ANGLE.values())
		{
			if (ANGLE.EXTENT == key)
			{
				continue;
			}
			Point p = this.toggle.get(key);
			this.drawToggle(g, p.x, p.y);
			cx = (int)Math.round(this.center.x);
			cy = (int)Math.round(this.center.y);
			g.drawLine(cx, cy, p.x, p.y);
		}
	}

	@Override
	public void adaptFrame()
	{
		this.x = (int)Math.round(this.base.x);
		this.y = (int)Math.round(this.base.y);
		this.width = (int)Math.round(this.base.width);
		this.height = (int)Math.round(this.base.height);

		this.center.x = x + width/2;
		this.center.y = y + height/2;
		for (ANGLE key: ANGLE.values())
		{
			if (ANGLE.EXTENT == key)
			{
				continue;
			}
			this.calcToggle(this.angle.get(key), this.toggle.get(key));
		}

		this.setArc();
	}

	private Point calcToggle(int angle, Point toggle)
	{
		double rx = this.width/2;
		double ry = this.height/2;
		double x = rx*Math.cos(angle*Math.PI/180.0);
		double y = ry*Math.sin(-angle*Math.PI/180.0);

		double d1 = Math.sqrt(x*x + y*y);
		double d2 = d1 + 2*DesignObject.TOGGLE;
		x = d2*x/d1;
		y = d2*y/d1;

		toggle.x = (int)Math.round(this.center.x + x);
		toggle.y = (int)Math.round(this.center.y + y);

		return toggle;
	}

	boolean isNear(Point p)
	{
		double rx = this.width/2;
		double ry = this.height/2;

		double vx = p.x - this.center.x;
		double vy = p.y - this.center.y;

		double tvy = rx*vy/ry;
		double tlen = Math.sqrt(vx*vx + tvy*tvy);
		double dx = rx*vx/tlen - vx;
		double dy = ry*tvy/tlen - vy;
		double d = Math.sqrt(dx*dx + dy*dy);

		boolean ret = false;
		if (this.isSelected())
		{
			ret = (SEP > d);
		}
		else
		{
			if (SEP > d)
			{
				int rangle = this.calcAngle(p);
				int sa = this.angle.get(ANGLE.START);
				int ea = this.angle.get(ANGLE.END);

				ret = (ea < 360 ? rangle >= sa && rangle <= ea : rangle >= sa || rangle <= (ea-360));
			}
		}
		return ret;
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

			if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType && this.arc.contains(p))
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

			for (ANGLE key: ANGLE.values())
			{
				if (ANGLE.EXTENT == key)
				{
					continue;
				}
				Point ap = this.toggle.get(key);
				if (SEP > Math.abs(ap.x - p.x) && SEP > Math.abs(ap.y - p.y))
				{
					hint.status = STATUS.RESHAPE;
					hint.resizeIndex = CONTROL.OTHER;
					hint.point = p;
					hint.option = key;
					return true;
				}
			}

			if(isNear(p))
			{
				hint.status = STATUS.MOVE;
				hint.point = p;
				return true;
			}
		}
		else
		{
			if (FigureType.FILL_TYPE.TRANSPARENT != this.fillType)
			{
				return this.arc.contains(p);
			}
			return isNear(p);
		}
		return false;
	}

	@Override
	public boolean hitTest(Rectangle rect)
	{
		Ellipse2D.Double shape = new Ellipse2D.Double(this.x, this.y, this.width, this.height);
		Area area = new Area(shape);
		return area.intersects(rect);
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
			notifyChanged();
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

		this.center.x = (right + left)/2;
		this.center.y = (bottom + top)/2;

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
			// 開始と終了の角度を入れ替える
			int ea = this.angle.get(ANGLE.START);
			int sa = this.angle.get(ANGLE.END);

			// y軸に対して反転
			sa = (180 >= sa ? 180 - sa : 540 - sa);
			ea = (180 >= ea ? 180 - ea : 540 - ea);

			this.angle.put(ANGLE.START, sa);
			this.angle.put(ANGLE.END, ea);
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
			// 開始と終了の角度を入れ替える
			int ea = this.angle.get(ANGLE.START);
			int sa = this.angle.get(ANGLE.END);

			// x軸に対して反転
			sa = 360 - sa;
			ea = 360 - ea;

			this.angle.put(ANGLE.START, sa);
			this.angle.put(ANGLE.END, ea);
		}
		switch(shape)
		{
		case ELLIPSE:
			this.base.width = right - left;
			this.base.height = bottom - top;
			break;
		case CIRCLE:
			switch(hint.resizeIndex)
			{
			case TOP:
			case BOTTOM:
				this.base.height = bottom - top;
				this.base.width = this.base.height;
				break;
			case LEFT:
			case RIGHT:
				this.base.width = right - left;
				this.base.height = this.base.width;
				break;
			default:
			}
			break;
		default:
		}
		this.base.x = this.center.x - this.base.width/2;
		this.base.y = this.center.y - this.base.height/2;
		adaptFrame();
		notifyChanged();

		return true;
	}

	@Override
	public void resize(Point origin, double xscale, double yscale)
	{
		if (this.shape == SHAPE.CIRCLE)
		{
			if (xscale != yscale)
			{
				this.shape = SHAPE.ELLIPSE;
			}
		}

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

		if (0 > xscale)
		{
			if (0 < yscale)
			{
				// 開始と終了の角度を入れ替える
				int ea = this.angle.get(ANGLE.START);
				int sa = this.angle.get(ANGLE.END);

				// y軸に対して反転
				sa = (180 >= sa ? 180 - sa : 540 - sa);
				ea = (180 >= ea ? 180 - ea : 540 - ea);

				this.angle.put(ANGLE.START, sa);
				this.angle.put(ANGLE.END, ea);
			}
		}
		else if (0 < xscale)
		{
			if (0 > yscale)
			{
				// 開始と終了の角度を入れ替える
				int ea = this.angle.get(ANGLE.START);
				int sa = this.angle.get(ANGLE.END);

				// x軸に対して反転
				sa = 360 - sa;
				ea = 360 - ea;

				this.angle.put(ANGLE.START, sa);
				this.angle.put(ANGLE.END, ea);
			}
		}

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
		else if (isStatus(STATUS.RESIZE))
		{
			return resize(hint, dp);
		}
		else if (isStatus(STATUS.RESHAPE))
		{
			return setAngle(hint, dp);
		}

		return false;
	}

	private int calcAngle(Point p)
	{
		// カーソル-中心点とx軸正方向との間の角度
		double tx = p.x - this.center.x;
		double ty = this.center.y - p.y;	// 上を正とするy軸
		if (0 == Math.abs(tx) || 0 == Math.abs(ty))
		{
			// 中心点上はスキップ
			return -1;
		}

		double td = Math.sqrt(tx*tx + ty*ty);
		double ta = Math.asin(ty/td);
		if (0 <= tx)	// 第1、第4象限
		{
			if (0 <= ty)	// 第1象限
			{
			}
			else	// 第4象限
			{
				ta = 2*Math.PI + ta;
			}
		}
		else	// 第2、第3象限
		{
			ta = Math.PI - ta;
		}

		int rangle = -1;
		switch(this.shape)
		{
		case CIRCLE:
			rangle = (int) Math.round(ta*180/Math.PI);
			break;
		case ELLIPSE:
			// 楕円とカーソル-中心点との交点
			double rx = this.width/2;
			double ry = this.height/2;
			double tan = Math.tan(ta);
			double ix = 0;
			double iy = 0;
			if (1 >= Math.abs(tan))
			{
				ix = rx*ry/Math.sqrt(ry*ry + tan*tan*rx*rx);
				iy = tan*ix;
			}
			else
			{
				tan = Math.cos(ta)/Math.sin(ta);
				iy = rx*ry/Math.sqrt(tan*tan*ry*ry + rx*rx);
				ix = tan*ix;
			}
			if (0 > (ix*tx))	// txとixの正負を合わせる
			{
				ix = -ix;
			}
			if (0 > (iy*ty))	// tyとiyの正負を合わせる
			{
				iy = -iy;
			}

			// 制御点-中心点とx軸性方向の間の角度を求める
			double aa = Math.asin(iy/ry);
			if (0 <= tx)	// 第1、第4象限
			{
				if (0 <= ty)	// 第1象限
				{
				}
				else	// 第4象限
				{
					aa = 2*Math.PI + aa;
				}
			}
			else	// 第2、第3象限
			{
				aa = Math.PI - aa;
			}
			rangle = (int) Math.round(aa*180/Math.PI);
			break;
		default:
		}

		return rangle;
	}

	private boolean setAngle(StatusHint hint, Point dp)
	{
		ANGLE pos = (ANGLE) hint.option;
		Point tp = this.toggle.get(pos);
		dp.x += tp.x;
		dp.y += tp.y;

		int rangle = this.calcAngle(dp);
		if (0 > rangle)
		{
			return true;
		}
		this.angle.put(pos, rangle);

		adaptFrame();
		notifyChanged();
		return true;
	}

	public int getAngle(ANGLE name)
	{
		switch(name)
		{
		case START:
		case END:
			return this.angle.get(name);
		default:
			int sa = this.angle.get(ANGLE.START);
			int ea = this.angle.get(ANGLE.END);

			if (sa < ea)
			{
				return (ea-sa);
			}
			else
			{
				return (ea + 360 - sa);
			}
		}
	}

	private static class TAG extends TAG_BASE
	{
		static final String STYLE = "style";
	}

	@Override
	public void export(ExportBase export)
	{
		ShapeCircle copy = this.clone();
		copy.resetOrigin(export.getOrigin());

		ShapeInfo info = new ShapeInfo(SHAPE.CIRCLE);
		info.obj = copy;

		PairList list = this.makeDesc(copy);

		for (ANGLE key: ANGLE.values())
		{
			if (ANGLE.EXTENT == key)
			{
				continue;
			}
			list.add(key.name(), copy.angle.get(key));
		}
		list.add(TAG.STYLE, copy.style.name());
		info.setDesc(list);

//		info.x = copy.x;
//		info.y = copy.y;
//		info.width = copy.width;
//		info.height = copy.height;
//		info.angle = copy.angle.clone();
//		for (ANGLE key: ANGLE.values())
//		{
//			int val = copy.angle.get(key);
//			info.angle.put(key, val);
//		}
//		info.arc = copy.arc;
//		info.setType(copy.shape);
//		info.dotSize = copy.dotSize;
//		info.dotSpan = copy.dotSpan;
//		info.desc = String.format("%s %d %d %d %d %s %d %d %s", this.getClass().getSimpleName(), copy.x, copy.y, copy.width, copy.height, copy.style, copy.angle.get(ANGLE.START), copy.angle.get(ANGLE.END), copy.shape);

		export.writeStart(info);
		export.write(info);
		export.writeEnd(info);
	}

	@Override
	public boolean showProperty(DesignPanel panel, Point p)
	{
		return super.showProperty(panel, p);
	}

	public static ShapeCircle parse(ShapeInfo info)
	{
		ShapeCircle obj = new ShapeCircle();
		obj.shape = info.getType();

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
			if (8 > words.length || 9 < words.length || 0 != words[0].compareToIgnoreCase(ShapeCircle.class.getSimpleName()))
			{
				obj.base.x = info.x;
				obj.base.y = info.y;
				obj.base.width = info.width;
				obj.base.height = info.height;
			}
			else
			{
				try
				{
					obj.base.x = java.lang.Double.parseDouble(words[1]);
					obj.base.y = java.lang.Double.parseDouble(words[2]);
					obj.base.width = java.lang.Double.parseDouble(words[3]);
					obj.base.height = java.lang.Double.parseDouble(words[4]);
					obj.style = STYLE.parse(words[5]);
					obj.angle.put(ANGLE.START, new Integer(words[6]));
					obj.angle.put(ANGLE.END, new Integer(words[7]));
					if (9 == words.length)
					{
						obj.shape = SHAPE.valueOf(words[8]);
					}
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
			vals = list.getValues(TAG.X);
			if (0 < vals.size() && vals.get(0) instanceof String)
			{
				obj.base.x = java.lang.Double.parseDouble((String) vals.get(0));
			}
			vals = list.getValues(TAG.Y);
			if (0 < vals.size() && vals.get(0) instanceof String)
			{
				obj.base.y = java.lang.Double.parseDouble((String) vals.get(0));
			}
			vals = list.getValues(TAG.WIDTH);
			if (0 < vals.size() && vals.get(0) instanceof String)
			{
				obj.base.width = java.lang.Double.parseDouble((String) vals.get(0));
			}
			vals = list.getValues(TAG.HEIGHT);
			if (0 < vals.size() && vals.get(0) instanceof String)
			{
				obj.base.height = java.lang.Double.parseDouble((String) vals.get(0));
			}
			vals = list.getValues(TAG.STYLE);
			if (0 < vals.size() && vals.get(0) instanceof String)
			{
				obj.style = STYLE.getValueOf((String) vals.get(0));
			}
			for (ANGLE key: ANGLE.values())
			{
				if (ANGLE.EXTENT == key)
				{
					continue;
				}
				vals = list.getValues(key.name());
				if (0 < vals.size() && vals.get(0) instanceof String)
				{
					obj.angle.put(key, Integer.parseInt((String) vals.get(0)));
				}
			}
		}

		obj.adaptFrame();
		obj.setStroke();
		obj.setFillPaint();

		return obj;
	}
}
