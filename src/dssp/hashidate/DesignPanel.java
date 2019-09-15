package dssp.hashidate;

import javax.swing.JPanel;

import dssp.hashidate.config.Config;
import dssp.hashidate.config.PageInfo;
import dssp.hashidate.misc.CursorFactory;
import dssp.hashidate.shape.DesignObject;
import dssp.brailleLib.Util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class DesignPanel extends JPanel implements Printable, Pageable
{
	ObjectManager objMan = null;
	PageFormat pf;

	Image buffer = null;

	/**
	 * m_objManを設定します。
	 * @param m_objMan m_objMan
	 */
	void setObjectManager(ObjectManager objMan) {
	    this.objMan = objMan;
	    PageInfo pageInfo = this.objMan.getPageInfo();
	    int w = Util.mmToPixel(0, pageInfo.getWidth()*10);
	    int h = Util.mmToPixel(0, pageInfo.getHeight()*10);
	    this.setPreferredSize(new Dimension(w,h));
	    this.buffer = null;
	    invalidate();
	}

	Graphics getBufferGraphics()
	{
		if (null == this.buffer)
		{
			this.buffer = createImage(this.getWidth(), this.getHeight());
		}
		Graphics g = this.buffer.getGraphics();
		Graphics pg = this.getGraphics();
		g.setColor(pg.getColor());
		g.setFont(pg.getFont());

		return g;
	}

	void flush(Graphics bg)
	{
		bg.dispose();
		Graphics g = getGraphics();
		g.drawImage(this.buffer, 0, 0, this);
		g.dispose();
	}

	void clear(Graphics g)
	{
		g.setColor(getBackground());
		g.fillRect(0, 0, this.getWidth(), this.getHeight());

		if ((boolean) Config.getConfig(Config.BPLOT.SHOW_PAGE_FRAME))
		{
			Color color = Config.getConfig(Config.BPLOT.PAGE_FRAME_COLOR);
			Color back = g.getColor();
			g.setColor(color);

			Config.BPLOT[] pages ={Config.BPLOT.PAGE_A4, Config.BPLOT.PAGE_B5};
			for (Config.BPLOT page: pages)
			{
				Dimension size = Config.getConfig(page);
				g.drawRect(0, 0, Util.mmToPixel(0, size.width*10), Util.mmToPixel(0, size.height*10));
			}

			g.setColor(back);
		}
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		if (null == this.objMan)
		{
			return;
		}

		if ((boolean) Config.getConfig(Config.BPLOT.SHOW_PAGE_FRAME))
		{
			Color color = Config.getConfig(Config.BPLOT.PAGE_FRAME_COLOR);
			Color back = g.getColor();
			g.setColor(color);

			Config.BPLOT[] pages ={Config.BPLOT.PAGE_A4, Config.BPLOT.PAGE_B5};
			for (Config.BPLOT page: pages)
			{
				Dimension size = Config.getConfig(page);
				g.drawRect(0, 0, Util.mmToPixel(0, size.width*10), Util.mmToPixel(0, size.height*10));
			}

			g.setColor(back);
		}

		this.objMan.draw((Graphics2D) g, DesignObject.DRAW_MODE.DISPLAY);
	}

	public final void changeCursor(DesignObject.StatusHint hint)
	{
		if (null == hint.status)
		{
			return;
		}
		switch(hint.status)
		{
		case INITIAL:
			setReshapeCursor();
			break;
		case RESHAPE:
			setReshapeCursor();
			break;
		case MOVE:
			setMoveCursor();
			break;
		case RESIZE:
			setResizeCursor(hint.resizeIndex);
			break;
		case EDIT:
			if (hint.option instanceof DesignObject.EDIT_TYPE)
			{
				switch((DesignObject.EDIT_TYPE) hint.option)
				{
				case ADD:
					this.setCursor(CursorFactory.getCursor(CursorFactory.TYPE.ADD));
					break;
				case DEL:
					this.setCursor(CursorFactory.getCursor(CursorFactory.TYPE.DEL));
					break;
				}
			}
			break;
		case ERASE_RESHAPE:
			this.setEraseCursor();
			break;
		default:
		}
	}

	public void setResizeCursor(DesignObject.CONTROL corner)
	{
		switch(corner)
		{
		case LEFT:
		case RIGHT:
			setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
			break;
		case TOP:
		case BOTTOM:
			setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
			break;
		case LEFTTOP:
		case RIGHTBOTTOM:
			setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
			break;
		case LEFTBOTTOM:
		case RIGHTTOP:
			setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
			break;
		default:
		}
	}
	public void setReshapeCursor()
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}
	public void setMoveCursor()
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	}
	void setHandCursor()
	{
		setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	}
	void setEraseCursor()
	{
		setCursor(CursorFactory.getCursor(CursorFactory.TYPE.ERASE));
	}
	public void resetCursor()
	{
		setCursor(Cursor.getDefaultCursor());
	}

	// 印刷 Printableの実装

	void setPageFormat(PageFormat pf)
	{
		this.pf = pf;
	}

	public int getNumberOfPages()
	{
		return 1;
	}

	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException
	{
		return this.pf;
	}

	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException
	{
		return this;
	}

	public int print(Graphics g, PageFormat pf, int pageIndex)
			throws PrinterException
	{
		if (pageIndex > 1)
		{
			return Printable.NO_SUCH_PAGE;
		}

		g.translate((int)pf.getImageableX(), (int)pf.getImageableY());
		double sx = pf.getImageableWidth()/this.getWidth();
		double sy = pf.getImageableHeight()/this.getHeight();
		double scale = Math.min(sx, sy);

		((Graphics2D)g).scale(scale, scale);

		this.objMan.draw((Graphics2D) g, DesignObject.DRAW_MODE.PRINT);

		return Printable.PAGE_EXISTS;
	}
}
