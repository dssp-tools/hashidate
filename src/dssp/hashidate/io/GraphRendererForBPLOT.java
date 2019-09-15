package dssp.hashidate.io;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import dssp.brailleLib.BrailleInfo;
import dssp.brailleLib.BrailleRenderer;
import dssp.brailleLib.BrailleTranslater;
import dssp.brailleLib.Util;
import dssp.hashidate.config.Config;
import dssp.hashidate.shape.ShapeGraph.DataInfo;
import dssp.hashidate.shape.helper.BrailleToolkit;
import dssp.hashidate.shape.helper.GraphRenderer;

public class GraphRendererForBPLOT extends GraphRenderer
{
	private ExportBPLOT export;

	public GraphRendererForBPLOT(ExportBPLOT export)
	{
		this.export = export;
	}

	private void writeLine(double sx, double sy, double ex, double ey, double dotSpan)
	{
		this.export.write("line %f %f %f %f %.1f", sx, sy, ex, ey, dotSpan);
	}

	private void writeOrigin(double x, double y)
	{
		this.export.write("origin %f %f 1 1", x, y);
	}

	static final int SCALE_LEN = 2;
	static final int DOT_SIZE_AXIS = 2;
	static final int DOT_SIZE_SCALE = 0;
	private void writeAxis(double xmin, double xmax, double xstep, double ymin, double ymax, double ystep)
	{
		this.export.write("xaxis %f %f %f %d %d", xmin, xmax, xstep, SCALE_LEN, DOT_SIZE_AXIS);
		this.export.write("yaxis %f %f %f %d %d", ymin, ymax, ystep, SCALE_LEN, DOT_SIZE_AXIS);
	}

	public void drawEnd()
	{
		this.export.writeComment("原点を戻す");
		Point origin = this.coord(this.config.getXAxis().getOrigin(), this.config.getYAxis().getOrigin());
		double x = this.export.getX(origin.x);
		double y = this.export.getY(origin.y);
		this.writeOrigin(-x, -y);
	}

	/**
	 * 軸を描く
	 *
	 * @param g Graphics
	 * @param x x座標
	 * @param y y座標
	 * @param w 幅
	 * @param h 高さ
	 */
	@Override
	public void drawAxisSumiji(Graphics2D g, int x, int y, int w, int h)
	{
		// グラフ領域
		this.graphArea.x = x + this.config.getLeft();
		this.graphArea.y = y + this.config.getTop();
		this.graphArea.width = w - this.config.getLeft() - this.config.getRight();
		this.graphArea.height = h - this.config.getTop() - this.config.getBottom();

		// 原点位置を出力する
		this.export.writeComment("原点を座標軸の交点に移動");
		Point origin = this.coord(this.config.getXAxis().getOrigin(), this.config.getYAxis().getOrigin());
		double ox = this.export.getX(origin.x);
		double oy = this.export.getY(origin.y);
		this.writeOrigin(ox, oy);

		// 軸の座標を計算する
		this.export.writeComment("座標軸");
		this.export.writeDot(Config.BRAILLE.SMALL);
		Point start = this.coord(this.config.getXAxis().getMin(), this.config.getYAxis().getMin());
		double sx = this.export.getX(start.x);
		double sy = this.export.getY(start.y);
		Point end = this.coord(this.config.getXAxis().getMax(), this.config.getYAxis().getMax());
		double ex = this.export.getX(end.x);
		double ey = this.export.getY(end.y);
		Point step = this.length(this.getConfig().getXScale().getStep(), this.getConfig().getYScale().getStep());
		double spx = this.export.getDX(step.x);
		double spy = this.export.getDY(step.y);
		this.writeAxis(sx - ox, ex - ox, spx, sy - oy, ey - oy, spy);

		// 目盛ラベルを描く
		BrailleTranslater translater = BrailleToolkit.getFormulaTranslater();
		BrailleRenderer renderer = BrailleToolkit.getRenderer();
		double ls = this.export.getDY(renderer.getLineSpace());

		List<BrailleInfo> brailleList = Util.newArrayList();

		boolean withXLabel = this.config.getXScale().withLabel();
		boolean withYLabel = this.config.getYScale().withLabel();
		StringBuffer buf = new StringBuffer();
		if (withXLabel)
		{
			this.export.writeComment("x軸上の目盛りラベル");
			double xStep = this.config.getXScale().getStep();
			boolean flag = true;
			for (double scx = this.config.getXScale().getStart(); scx < this.config.getXAxis().getMax(); scx+=xStep)
			{
				Point bottom = this.coord(scx, this.config.getYAxis().getMin());

				double bx = this.export.getX(bottom.x) - ox;
				double by = this.export.getY(bottom.y) - oy;
				by -= this.export.getDY(renderer.getBoxHeight()/2);
				if (flag)
				{
					by -= ls + this.export.getDY(renderer.getBoxHeight());
				}

				String label = Double.toString(scx);
				brailleList.clear();
				translater.braileFromSumiji(label, brailleList, true, true);
//				brailleList.add(0, translater.getDict().getExtra(BrailleInfo.EXTRA.SUUFU));

				Rectangle bb = renderer.getBound(brailleList, bottom.x, bottom.y, false);

				buf.delete(0, buf.length());
				for (BrailleInfo info: brailleList)
				{
					buf.append(info.getNABCC(false));
				}
				this.export.write("brl %.02f %.02f %s", bx - this.export.getDX((int) (bb.getWidth()/2)), by, buf.toString());

				flag = !flag;
			}
		}
		if (withYLabel)
		{
			this.export.writeComment("y軸上の目盛りラベル");
			double yStep = this.config.getYScale().getStep();
			for (double scy = this.config.getYScale().getStart(); scy < this.config.getYAxis().getMax(); scy+=yStep)
			{
				Point left = this.coord(this.config.getXAxis().getMin(), scy);

				double lx = this.export.getX(left.x) - ox;
				double ly = this.export.getY(left.y) - oy;

				String label = Double.toString(scy);

				brailleList.clear();
				translater.braileFromSumiji(label, brailleList, true, true);
//				brailleList.add(0, translater.getDict().getExtra(BrailleInfo.EXTRA.SUUFU));

				Rectangle bb = renderer.getBound(brailleList, left.x, left.y, false);

				buf.delete(0, buf.length());
				for (BrailleInfo info: brailleList)
				{
					buf.append(info.getNABCC(false));
				}
				this.export.write("brl %.02f %.02f %s", lx - this.export.getDX(bb.width + renderer.getBoxSpace()), ly + this.export.getDY(renderer.getBoxHeight()), buf.toString());
			}
		}

		// 裏点にする
		this.export.setBackPoint(true);
		this.export.writeComment("原点を座標軸の交点に移動");
		this.writeOrigin(ox, oy);

		// 目盛り線を描く
		switch(this.config.getXScale().getShow())
		{
		case AUTO:
		case MANUAL:
			// 目盛線
			if (this.config.getXScale().withLine())
			{
				this.export.writeComment("x軸上の目盛線");
				this.export.writeDot(SCALE_SIZE);
				double xStep = this.config.getXScale().getStep();
				boolean flag = true;
				for (double scx = this.config.getXScale().getStart(); scx < this.config.getXAxis().getMax(); scx+=xStep)
				{
					Point top = this.coord(scx, this.config.getYAxis().getMax());
					Point bottom = this.coord(scx, this.config.getYAxis().getMin());

					double tx = this.export.getX(top.x) - ox;
					double ty = this.export.getY(top.y) - oy;
					double bx = this.export.getX(bottom.x) - ox;
					double by = this.export.getY(bottom.y) - oy;
					if (flag)
					{
						by -= ls + this.export.getDY(renderer.getBoxHeight());
					}

					if ((SCALE_SPACE/10) < Math.abs(tx))	// 軸と重なる線は出力しない
					{
						this.writeLine(tx, ty,  bx, by, SCALE_SPACE);
					}

					flag = !flag;
				}
			}
			break;
		default:
		}
		switch(this.config.getYScale().getShow())
		{
		case AUTO:
		case MANUAL:
			// 目盛線
			if (this.config.getYScale().withLine())
			{
				this.export.writeComment("y軸上の目盛線");
				this.export.writeDot(SCALE_SIZE);
				double yStep = this.config.getYScale().getStep();
				for (double scy = this.config.getYScale().getStart(); scy < this.config.getYAxis().getMax(); scy+=yStep)
				{
					Point left = this.coord(this.config.getXAxis().getMin(), scy);
					Point right = this.coord(this.config.getXAxis().getMax(), scy);

					double lx = this.export.getX(left.x) - ox;
					double ly = this.export.getY(left.y) - oy;
					double rx = this.export.getX(right.x) - ox;
					double ry = this.export.getY(right.y) - oy;
					if ((SCALE_SPACE/10) < Math.abs(ly))	// 軸と重なる線は出力しない
					{
						this.writeLine(lx, ly,  rx, ry, SCALE_SPACE);
					}
				}
			}
			break;
		default:
		}

		this.export.writeComment("原点を戻す");
		this.writeOrigin(-ox, -oy);

		// 表点に戻す
		this.export.setBackPoint(false);
	}

	/**
	 * 折れ線グラフを描く
	 *
	 * @param g Graphics
	 * @param x x座標
	 * @param y y座標
	 * @param w 幅
	 * @param h 高さ
	 * @param info データ
	 */
	@Override
	protected void drawLineSumiji(Graphics2D g,  int x, int y, int w, int h, DataInfo info)
	{
		DataConfig dconfig = info.getConfig();

		Point origin = this.coord(this.config.getXAxis().getOrigin(), this.config.getYAxis().getOrigin());
		double ox = this.export.getX(origin.x);
		double oy = this.export.getY(origin.y);

		Rectangle2D.Double graphArea = new Rectangle2D.Double();
		graphArea.x = this.config.getXAxis().getMin();
		graphArea.y = this.config.getYAxis().getMin();
		graphArea.width = this.config.getXAxis().getMax() - this.config.getXAxis().getMin();
		graphArea.height = this.config.getYAxis().getMax() - this.config.getYAxis().getMin();

		// 点の座標を計算する
		List<Point2D.Double> plist = Util.newArrayList();
		Data<Double,Double> prev = null;
		for (Data<Double, Double> data : dconfig.getData())
		{
			if (graphArea.contains(data.getX(), data.getY()))	// グラフ内
			{
				// 直前点がグラフ外なら直前点を出力する
				if (null != prev && false == graphArea.contains(prev.getX(), prev.getY()))
				{
					Point p = this.coord(prev);

					double px = this.export.getX(p.x);
					double py = this.export.getY(p.y);

					px -= ox;
					py -= oy;

					plist.add(new Point2D.Double(px, py));
				}

				Point p = this.coord(data);

				double px = this.export.getX(p.x);
				double py = this.export.getY(p.y);

				px -= ox;
				py -= oy;

				plist.add(new Point2D.Double(px, py));
			}
			else	// グラフ外
			{
				// 直前点がグラフ内なら出力する
				if (null != prev && graphArea.contains(prev.getX(), prev.getY()))
				{
					Point p = this.coord(data);

					double px = this.export.getX(p.x);
					double py = this.export.getY(p.y);

					px -= ox;
					py -= oy;

					plist.add(new Point2D.Double(px, py));
				}
			}
			prev = data;
		}

		// 線を出力する
		this.export.writeComment("%s %s", dconfig.getName(), info.getFuncText());
		this.export.writeDot(Config.BRAILLE.MIDDLE);
		this.export.writeDash(info.getLineType(), true);

		double size = this.export.getDotSpan();
		Point start = this.coord(this.config.getXAxis().getMin(), this.config.getYAxis().getMin());
		double sx = this.export.getX(start.x) - ox;
		double sy = this.export.getY(start.y) - oy;
		Point end = this.coord(this.config.getXAxis().getMax(), this.config.getYAxis().getMax());
		double ex = this.export.getX(end.x) - ox;
		double ey = this.export.getY(end.y) - oy;
		this.export.write("spline %f %f %f %f 1 0 0 %.1f", sx, ex, sy, ey, size);

		for (Point2D.Double p : plist)
		{
			this.export.write("    %f %f", p.x, p.y);
		}
		this.export.write("    9999 0");
		this.export.writeDash(info.getLineType(), false);
	}
}
