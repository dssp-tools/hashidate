package dssp.hashidate.io;

import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.List;

import dssp.hashidate.config.Config;
import dssp.brailleLib.BrailleBox;
import dssp.brailleLib.BrailleInfo;
import dssp.brailleLib.BrailleRenderer;
import dssp.brailleLib.Util;

public final class BrailleRendererForBPLOT extends BrailleRenderer
{
	private boolean upsideDown;
	private Rectangle2D.Double winArea;
	private ExportBPLOT export;
	private boolean doPlot = true;

	public void setDoPlot(boolean doPolot)
	{
		this.doPlot = doPolot;
	}

	private BrailleRendererForBPLOT(ExportBPLOT export, Rectangle2D.Double area, boolean upsideDown)
	{
		this.export = export;
		this.winArea = area;
		this.upsideDown = upsideDown;
	}

	public static BrailleRendererForBPLOT newInstance(ExportBPLOT export, Rectangle2D.Double area, boolean upsideDown)
	{
		if (null == export)
		{
			throw new IllegalArgumentException("null writer");
		}

		return new BrailleRendererForBPLOT(export, area, upsideDown);
	}

	private double getX(int x)
	{
		return Util.pixelToMM(0, x)/100.0;
	}

	private double getY(int y)
	{
		if (this.upsideDown)
		{
			int temp = (int) (this.winArea.height - y);
			return Util.pixelToMM(0, temp)/100.0;
		}

		return Util.pixelToMM(0, y)/100.0;
	}

	@Override
	protected void plot(Graphics g, int x, int y)
	{
		if (this.doPlot)
		{
			this.export.write("plot %f %f", this.getX(x), this.getY(y));
		}
	}

	@Override
	public List<BrailleBox> drawBraille(Graphics g, List<BrailleInfo> brailleList, int bX, int bY, boolean drawExtra)
	{
		int size = 1;
		Config.BRAILLE[] types = {Config.BRAILLE.SMALL, Config.BRAILLE.MIDDLE, Config.BRAILLE.LARGE};
		for (int i = 0; i < types.length; i++)
		{
			int tsize = Config.getConfig(types[i]);
			int psize = Util.mmToPixel(0, tsize);
			if (psize == this.DOT_SIZE)
			{
//				size = Util.mmToPixel(this.resolution.x, tsize);
				size = i;
			}
		}

		if (this.doPlot)
		{
			this.export.write("dot %d", size);
		}
		return super.drawBraille(g, brailleList, bX, bY, drawExtra);
	}

//	@Override
//	protected void drawDots(Graphics g, int[] dots, Rectangle rect)
//	{
//		int cX = rect.x + this.DOT_SIZE/2;
//		int cY = rect.y + this.DOT_SIZE/2;
//
//		try
//		{
//			final int TURN_INDEX = BrailleInfo.MAX_DOT_COUNT/2 + 1;
//			for (int i = 0; i < BrailleInfo.MAX_DOT_COUNT; i++)
//			{
//				int code = i+1;
//				int dX = cX;
//				int dY = cY;
//				if(0 < code && TURN_INDEX > code)
//				{
//					dY += (code - 1) * this.DOT_SIZE * 2;
//				}
//				else
//				{
//					dX += this.DOT_SIZE * 2;
//					dY += (code - TURN_INDEX) * this.DOT_SIZE * 2;
//				}
//
//				if (null != dots)
//				{
//					for (int j = 0; j < dots.length; j++)
//					{
//						if (code == dots[j])
//						{
//							this.plot(dX, dY);
//						}
//					}
//				}
//			}
//		}
//		catch (Exception ex)
//		{
//			Util.logException(ex);
//		}
//	}
//
//	@Override
//	protected void drawRowDots(Graphics g, int x, int y, boolean open)
//	{
//		int cX = x + (open ? this.DOT_SIZE/2 : this.DOT_SIZE*5/2);
//		int cY = y + this.DOT_SIZE/2;
//
//		try
//		{
//			this.plot(cX, cY);
//			this.plot(cX, cY + this.DOT_SIZE*2);
//		}
//		catch (Exception ex)
//		{
//			Util.logException(ex);
//		}
//	}
}
