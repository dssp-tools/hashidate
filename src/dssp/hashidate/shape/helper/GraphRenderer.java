package dssp.hashidate.shape.helper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

import dssp.brailleLib.BrailleInfo;
import dssp.brailleLib.BrailleRenderer;
import dssp.brailleLib.BrailleTranslater;
import dssp.brailleLib.Util;
import dssp.hashidate.config.Config;
import dssp.hashidate.misc.BrailleStroke;
import dssp.hashidate.misc.FigureType.EDGE_TYPE;
import dssp.hashidate.misc.FigureType.LINE_TYPE;
import dssp.hashidate.shape.DesignObject.BufInfo;
import dssp.hashidate.shape.ShapeGraph.DataInfo;

public class GraphRenderer
{
	protected static final Config.BRAILLE SCALE_SIZE = Config.BRAILLE.LARGE;
	protected static final int SCALE_SPACE = 3;

	public static class Data<X extends Number, Y extends Number> implements Cloneable
	{
		protected X x;
		protected Y y;
		public Data()
		{
		}
		public Data(X x, Y y)
		{
			this.x = x;
			this.y = y;
		}

		@SuppressWarnings("unchecked")
		public Data<X,Y> clone()
		{
			try
			{
				Data<X,Y> obj = (Data<X,Y>) super.clone();
				obj.x = this.x;
				obj.y = this.y;
				return obj;
			}
			catch (CloneNotSupportedException e)
			{
				Util.logException(e);
			}
			return null;
		}

		public X getX()
		{
			return this.x;
		}
		public void setX(X x)
		{
			this.x = x;
		}
		public Y getY()
		{
			return this.y;
		}
		public void setY(Y y)
		{
			this.y = y;
		}
	}

	public static enum GRAPH_TYPE
	{
		LINE("折れ線");

		private String name;
		private GRAPH_TYPE(String name)
		{
			this.name= name;
		}

		public String getType()
		{
			return this.name();
		}

		@Override
		public String toString()
		{
			return this.name;
		}
	}

	public static class Axis implements Cloneable
	{
		double min;
		double max;
		double origin;

		public Axis(double min, double max, double origin)
		{
			this.min = min;
			this.max = max;
			this.origin = origin;
		}

		public double getMin()
		{
			return min;
		}

		public void setMin(double min)
		{
			this.min = min;
		}

		public double getMax()
		{
			return max;
		}

		public void setMax(double max)
		{
			this.max = max;
		}

		public double getOrigin()
		{
			return origin;
		}

		public void setOrigin(double origin)
		{
			this.origin = origin;
		}

		public Axis clone()
		{
			try
			{
				Axis dst = (Axis) super.clone();
				dst.min = this.min;
				dst.max = this.max;
				dst.origin = this.origin;

				return dst;
			}
			catch (CloneNotSupportedException e)
			{
				Util.logException(e);
			}

			return null;
		}
	}

	public static class GraphScale implements Cloneable
	{
		public static enum SHOW
		{
			AUTO("自動"),
			MANUAL("指定"),
			NONE("なし");

			private String text;
			private SHOW(String text)
			{
				this.text = text;
			}

			public static SHOW fromName(String name)
			{
				for (SHOW elm: SHOW.values())
				{
					if (elm.text.equals(name))
					{
						return elm;
					}
				}

				return null;
			}

			@Override
			public String toString()
			{
				return this.text;
			}
		};
		private SHOW showScale = SHOW.AUTO;
		private boolean withLine = true;
		private boolean withLabel = true;
		private double start = 0;
		private double step = 2;
		public double getStart()
		{
			return start;
		}
		public void setStart(double start)
		{
			this.start = start;
		}
		public double getStep()
		{
			return step;
		}
		public void setStep(double step)
		{
			this.step = step;
		}
		public SHOW getShow()
		{
			return this.showScale;
		}
		public void setShow(SHOW showScale)
		{
			this.showScale = showScale;
		}
		public boolean withLine()
		{
			return this.withLine;
		}
		public void setWithLine(boolean withLine)
		{
			this.withLine = withLine;
		}
		public boolean withLabel()
		{
			return this.withLabel;
		}
		public void setWithLabel(boolean withLabel)
		{
			this.withLabel = withLabel;
		}

		private static double[] SCALE = {10, 5, 2};
		private static int NSCALE = 4;

		public void setScale(SHOW show, double min, double max, double origin, boolean withLabel, boolean withLine)
		{
			this.showScale = show;
			this.withLabel = withLabel;
			this.withLine = withLine;
			switch(show)
			{
			case AUTO:
				double range = max-min;
				double order = Math.floor(Math.log10(range))-1;
				for (double scale: SCALE)
				{
					double dstep = scale*Math.pow(10, order);
					int nscale = (int) Math.floor(range/dstep);
					if (nscale >= NSCALE)
					{
						this.step = dstep;
						this.start = (Math.floor((min-origin)/dstep)+1)*dstep;
						break;
					}
				}
				break;
			case MANUAL:
				this.step = max;
				this.start = (Math.floor((min - origin)/this.step)+1)*this.step;
				break;
			default:
			}
		}

		public void reScale(double min, double max, double origin)
		{
			this.start = (Math.floor((min - origin)/this.step)+1)*this.step;
			this.showScale = SHOW.MANUAL;
//			switch(this.showScale)
//			{
//			case AUTO:
//				this.start = (Math.floor(min/this.step)+1)*this.step;
//				break;
//			default:
//				this.start = (Math.floor(min/this.step)+1)*this.step;
//				break;
//			}
		}

		@Override
		public GraphScale clone()
		{
			try
			{
				GraphScale dst = (GraphScale) super.clone();
				dst.showScale = this.showScale;
				dst.start = this.start;
				dst.step = this.step;
				return dst;
			}
			catch (CloneNotSupportedException e)
			{
				Util.logException(e);
			}
			return null;
		}
	}

	public static class GraphConfig implements Cloneable
	{
		// 軸の範囲
		private Axis xAxis = new Axis(0, 10, 0);
		private Axis yAxis = new Axis(0, 10, 0);

		// 軸の色
		private Color axisColor = Color.BLACK;

		// 目盛り
		private GraphScale xScale = new GraphScale();
		private GraphScale yScale = new GraphScale();

		// 目盛ラベル領域
		private int labelWidth;
		private int labelHeight;

		// 上下、左右の隙間
		private int left = 10;
		private int right = 10;
		private int top = 10;
		private int bottom = 10;

		public Axis getXAxis()
		{
			return this.xAxis;
		}

		public Axis getYAxis()
		{
			return this.yAxis;
		}

		public void setAxis(Axis x, Axis y)
		{
			this.xAxis = x;
			this.yAxis = y;
		}

		public Color getAxisColor()
		{
			return axisColor;
		}

		public void setAxisColor(Color axisColor)
		{
			this.axisColor = axisColor;
		}

		public GraphScale getXScale()
		{
			return this.xScale;
		}
		public GraphScale getYScale()
		{
			return this.yScale;
		}

		public int getLeft()
		{
			return this.left;
		}

		public void setLeft(int left)
		{
			this.left = left;
		}

		public int getRight()
		{
			return right;
		}

		public void setRight(int right)
		{
			this.right = right;
		}

		public int getTop()
		{
			return top;
		}

		public void setTop(int top)
		{
			this.top = top;
		}

		public int getBottom()
		{
			return bottom;
		}

		public void setBottom(int bottom)
		{
			this.bottom = bottom;
		}

		public int getLabelWidth()
		{
			return labelWidth;
		}

		public int getLabelHeight()
		{
			return labelHeight;
		}

		@Override
		public GraphConfig clone()
		{
			GraphConfig dst = null;
			try
			{
				dst = (GraphConfig) super.clone();
				dst.xAxis = this.xAxis.clone();
				dst.yAxis = this.yAxis.clone();

				dst.xScale = this.xScale.clone();
				dst.yScale = this.yScale.clone();

				dst.left = this.left;
				dst.right = this.right;
				dst.top = this.top;
				dst.bottom = this.bottom;
			}
			catch (CloneNotSupportedException e)
			{
				Util.logException(e);
			}

			return dst;
		}
	}

	public static class DataConfig implements Cloneable
	{
		// グラフの名前
		private String name;
		public String getName()
		{
			return name;
		}
		public void setName(String name)
		{
			this.name = name;
		}

		// グラフの種類
		private GRAPH_TYPE type;
		public GRAPH_TYPE getType()
		{
			return type;
		}
		public void setType(GRAPH_TYPE type)
		{
			this.type = type;
		}

		private List<Data<Double,Double>> dataList = Util.newArrayList();
		public List<Data<Double,Double>> getData()
		{
			return this.dataList;
		}
		public void setData(List<Data<Double,Double>> list)
		{
			this.dataList = list;
		}

		@Override
		public DataConfig clone()
		{
			DataConfig dst = null;
			try
			{
				dst = (DataConfig) super.clone();
				dst.dataList = Util.newArrayList();
				Util.deepCopyByClone(this.dataList, dst.dataList);
			}
			catch (CloneNotSupportedException e)
			{
				Util.logException(e);
			}
			return dst;
		}
	}

	protected GraphConfig config = null;

	public void setConfig(GraphConfig config)
	{
		this.config = config;

		if (null == this.axisBrailleStroke)
		{
			float dSize = Util.mmToPixel(0, Config.getConfig(Config.BRAILLE.SMALL));
			float dotSpan = Util.mmToPixel(0, Config.getConfig(Config.BRAILLE.DOT_SPAN));
			this.axisBrailleStroke = new BrailleStroke(dSize, dotSpan, LINE_TYPE.SOLID, EDGE_TYPE.BUTT, true);
		}
		if (null == this.scaleBrailleStroke)
		{
			float dSize = Util.mmToPixel(0, Config.getConfig(Config.BRAILLE.MIDDLE));
			float dotSpan = Util.mmToPixel(0, Config.getConfig(Config.BRAILLE.DOT_SPAN));
			this.scaleBrailleStroke = new BrailleStroke(dSize, dotSpan, LINE_TYPE.SOLID, EDGE_TYPE.BUTT, false);
		}
	}

	public GraphConfig getConfig()
	{
		return this.config;
	}

	// グラフ領域
	protected Rectangle graphArea = new Rectangle();
	protected Stroke axisStroke = new BasicStroke();
	protected Stroke scaleStroke = new BasicStroke()
	{
		private float[] dash = {3, 3};

		@Override
		public float[] getDashArray()
		{
			return dash;
		}
	};
	protected Stroke axisBrailleStroke;
	protected Stroke scaleBrailleStroke;

	public Rectangle getBounds(Rectangle b)
	{
		boolean withXLabel = this.config.getXScale().withLabel();
		boolean withYLabel = this.config.getYScale().withLabel();

		if (withXLabel || withYLabel)
		{
			b = new Rectangle(b.x - this.config.getLabelWidth(), b.y, b.width + this.config.getLabelWidth(), b.height + this.config.getLabelHeight());
		}

		return b;
	}

	public BufInfo getBuffer(Graphics2D g, BufInfo info)
	{
		BrailleTranslater translater = BrailleToolkit.getFormulaTranslater();
		BrailleRenderer renderer = BrailleToolkit.getRenderer();

		// 目盛ラベル領域
		int lw = 0;
		int lh = 0;
		Font bf = g.getFont();
		g.setFont(Config.getConfig(Config.FONT.TEXT));
		boolean withXLabel = this.config.getXScale().withLabel();
		boolean withYLabel = this.config.getYScale().withLabel();
		FontMetrics fm = (withXLabel || withYLabel ? g.getFontMetrics(): null);
		List<BrailleInfo> brailleList = Util.newArrayList();
		if (withXLabel)
		{
			int ls = renderer.getLineSpace();
			lh = ls + 2*(renderer.getBoxHeight() + renderer.getDotSize());
		}
		if (withYLabel)
		{
			Rectangle2D b = fm.getStringBounds("X", g);
			int xoffset = (int) b.getWidth();

			double yStep = this.config.getYScale().step;
			for (double sy = this.config.getYScale().start; sy < this.config.yAxis.max; sy+=yStep)
			{
				String label = Double.toString(sy);
				// 墨字の範囲
				b = fm.getStringBounds(label, g);
				// 点字の範囲
				brailleList.clear();
				translater.braileFromSumiji(label, brailleList, true, true);
				brailleList.add(0, translater.getDict().getExtra(BrailleInfo.EXTRA.SUUFU));

				Rectangle bb = renderer.getBound(brailleList, 0, 0, false);

				lw = (int) Math.max(lw, Math.max(b.getWidth(), bb.getWidth()));
			}

			lw += xoffset;
		}
		g.setFont(bf);

		this.config.labelWidth = lw;
		this.config.labelHeight = lh;

		BufferedImage buf = info.getBuf();
		info.setBuf(new BufferedImage(buf.getWidth() + lw, buf.getHeight() + lh, BufferedImage.TYPE_4BYTE_ABGR));
		info.setTg((Graphics2D) info.getBuf().getGraphics());

		return info;
	}

	public void drawAreaFrame(Graphics2D g, int x, int y, int w, int h)
	{
		Color c = g.getColor();
		g.setColor(Config.getConfig(Config.COLOR.BRAILLE));
		g.drawRect(x - this.config.labelWidth, y, w + this.config.labelWidth - 1, h + this.config.labelHeight - 1);
		g.setColor(c);
	}

	/**
	 * 軸を描く(墨字)
	 *
	 * @param g Graphics
	 * @param x x座標
	 * @param y y座標
	 * @param w 幅
	 * @param h 高さ
	 */
	public void drawAxisSumiji(Graphics2D g, int x, int y, int w, int h)
	{
		// グラフ領域
		this.graphArea.x = x + this.config.left;
		this.graphArea.y = y + this.config.top;
		this.graphArea.width = w - this.config.left - this.config.right;
		this.graphArea.height = h - this.config.top - this.config.bottom;

		Shape clip = g.getClip();
		g.setClip(graphArea);

		// 軸の座標を計算する
		Point xstart = this.coord(this.config.xAxis.min, this.config.yAxis.origin);
		Point xend = this.coord(this.config.xAxis.max, this.config.yAxis.origin);

		Point ystart = this.coord(this.config.xAxis.origin,this.config.yAxis.min);
		Point yend = this.coord(this.config.xAxis.origin, this.config.yAxis.max);

		Color bcolor = g.getColor();
		g.setColor(this.config.axisColor);

		// 目盛りを描く
		{
			Stroke back = g.getStroke();
			g.setStroke(this.scaleStroke);
			switch(this.config.getXScale().getShow())
			{
			case AUTO:
			case MANUAL:
				// 目盛線
				if (this.config.getXScale().withLine())
				{
					double xStep = this.config.getXScale().step;
					for (double sx = this.config.getXScale().start; sx < this.config.xAxis.max; sx+=xStep)
					{
						Point top = this.coord(sx, this.config.yAxis.max);
						Point bottom = this.coord(sx, this.config.yAxis.min);
						g.drawLine(top.x, top.y, bottom.x, bottom.y);
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
					double yStep = this.config.getYScale().step;
					for (double sy = this.config.getYScale().start; sy < this.config.yAxis.max; sy+=yStep)
					{
						Point left = this.coord(this.config.xAxis.min, sy);
						Point right = this.coord(this.config.xAxis.max, sy);
						g.drawLine(left.x, left.y, right.x, right.y);
					}
				}
				break;
			default:
			}
			g.setStroke(back);
		}

		// 軸を描く
		{
			Stroke back = g.getStroke();
			g.setStroke(this.axisStroke);
			g.drawLine(xstart.x, xstart.y, xend.x, xend.y);
			g.drawLine(ystart.x, ystart.y, yend.x, yend.y);
			g.setStroke(back);
		}

		g.setClip(clip);

		// 目盛ラベルを描く
		Font bf = g.getFont();
		g.setFont(Config.getConfig(Config.FONT.TEXT));
		boolean withXLabel = this.config.getXScale().withLabel();
		boolean withYLabel = this.config.getYScale().withLabel();
		FontMetrics fm = (withXLabel || withYLabel ? g.getFontMetrics(): null);
		if (withXLabel)
		{
			Point bottom = this.coord(this.config.xAxis.origin, this.config.yAxis.min);
			String label = Double.toString(this.config.xAxis.origin);
			Rectangle2D b = fm.getStringBounds(label, g);
			g.drawString(label, bottom.x - Math.round(b.getWidth()/2), bottom.y + Math.round(-b.getY()));

			double xStep = this.config.getXScale().step;
			for (double sx = this.config.getXScale().start; sx < this.config.xAxis.max; sx+=xStep)
			{
				bottom = this.coord(sx, this.config.yAxis.min);
				label = Double.toString(sx);
				b = fm.getStringBounds(label, g);
				g.drawString(label, bottom.x - Math.round(b.getWidth()/2), bottom.y + Math.round(-b.getY()));
			}
		}
		if (withYLabel)
		{
			Rectangle2D b = fm.getStringBounds("X", g);
			int xoffset = (int) b.getWidth();

			Point left = this.coord(this.config.xAxis.min, this.config.yAxis.origin);
			String label = Double.toString(this.config.yAxis.origin);
			b = fm.getStringBounds(label, g);
			g.drawString(label, left.x - Math.round(b.getWidth()) - xoffset, left.y + Math.round((b.getY() + b.getHeight())/2));

			double yStep = this.config.getYScale().step;
			for (double sy = this.config.getYScale().start; sy < this.config.yAxis.max; sy+=yStep)
			{
				left = this.coord(this.config.xAxis.min, sy);
				label = Double.toString(sy);
				b = fm.getStringBounds(label, g);
				g.drawString(label, left.x - Math.round(b.getWidth()) - xoffset, left.y + Math.round((b.getY() + b.getHeight())/2));
			}
		}

		g.setFont(bf);
		g.setColor(bcolor);
	}

//	private BrailleFigureRenderer renderer = BrailleFigureRenderer.getInstance();

	/**
	 * 軸を描く(点字)
	 *
	 * @param g Graphics
	 * @param dotSize 点の大きさ
	 * @param dotSpan 点の間隔
	 * @param x x座標
	 * @param y y座標
	 * @param w 幅
	 * @param h 高さ
	 */
	public void drawAxisBraille(Graphics2D g, int dotSize, float dotSpan, int x, int y, int w, int h)
	{
		// グラフ領域
		this.graphArea.x = x + this.config.left;
		this.graphArea.y = y + this.config.top;
		this.graphArea.width = w - this.config.left - this.config.right;
		this.graphArea.height = h - this.config.top - this.config.bottom;

		Shape clip = g.getClip();
//		g.setClip(this.graphArea);
		Rectangle cr = (Rectangle) this.graphArea.clone();
		cr.height += this.config.getLabelHeight();
		g.setClip(cr);

		// 軸の座標を計算する
		Point xstart = this.coord(this.config.xAxis.min, this.config.yAxis.origin);
		Point xend = this.coord(this.config.xAxis.max, this.config.yAxis.origin);

		Point ystart = this.coord(this.config.xAxis.origin,this.config.yAxis.min);
		Point yend = this.coord(this.config.xAxis.origin, this.config.yAxis.max);

		BrailleTranslater translater = BrailleToolkit.getFormulaTranslater();
		BrailleRenderer renderer = BrailleToolkit.getRenderer();
		int ls = renderer.getLineSpace();

		// 目盛りを描く
		boolean longXScale = false;
		int dSpan = Util.mmToPixel(0, SCALE_SPACE*10);
		{
			Color bcolor = g.getColor();
			g.setColor(this.config.axisColor);
			Stroke bStroke = g.getStroke();
			g.setStroke(this.scaleBrailleStroke);
			switch(this.config.getXScale().getShow())
			{
			case AUTO:
			case MANUAL:
				// 目盛線
				if (this.config.getXScale().withLine())
				{
					double xStep = this.config.getXScale().step;
					boolean flag = true;
					for (double sx = this.config.getXScale().start; sx < this.config.xAxis.max; sx+=xStep)
					{
						Point top = this.coord(sx, this.config.yAxis.max);
						Point bottom = this.coord(sx, this.config.yAxis.min);
						if (flag)
						{
							bottom.y += ls + renderer.getBoxHeight();
						}
						int diff = (int) Math.abs(top.x - ystart.x);
						if (dSpan < diff)	// 軸と重なる線は描画しない
						{
							g.drawLine(top.x, top.y, bottom.x, bottom.y);
						}
						else
						{
							longXScale = flag;
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
					double yStep = this.config.getYScale().step;
					for (double sy = this.config.getYScale().start; sy < this.config.yAxis.max; sy+=yStep)
					{
						Point left = this.coord(this.config.xAxis.min, sy);
						Point right = this.coord(this.config.xAxis.max, sy);
						int diff = (int) Math.abs(left.y - xstart.y);
						if (dSpan < diff)	// 軸と重なる線は描画しない
						{
//							this.renderer.drawLine(g, dSize, dSpan, left.x, left.y, right.x, right.y, false);
							g.drawLine(left.x, left.y, right.x, right.y);
						}
					}
				}
				break;
			default:
			}
			g.setColor(bcolor);
			g.setStroke(bStroke);
		}

		// 軸を描く
		{
			Color bcolor = g.getColor();
			Stroke bStroke = g.getStroke();
			g.setColor(this.config.axisColor);
			g.setStroke(this.axisBrailleStroke);
			g.drawLine(xstart.x, xstart.y, xend.x, xend.y);
			if (longXScale)
			{
				g.drawLine(ystart.x, ystart.y + ls + renderer.getBoxHeight(), yend.x, yend.y);
			}
			else
			{
				g.drawLine(ystart.x, ystart.y, yend.x, yend.y);
			}
			g.setColor(bcolor);
			g.setStroke(bStroke);
		}
		g.setClip(clip);

		// 目盛ラベルを描く
		List<BrailleInfo> brailleList = Util.newArrayList();

		Font bf = g.getFont();
		g.setFont(Config.getConfig(Config.FONT.TEXT));
		boolean withXLabel = this.config.getXScale().withLabel();
		boolean withYLabel = this.config.getYScale().withLabel();
		if (withXLabel)
		{
			Point bottom = this.coord(this.config.xAxis.origin, this.config.yAxis.min);
			String label = Double.toString(this.config.xAxis.origin);
			brailleList.clear();
			translater.braileFromSumiji(label, brailleList, true, true);
			Rectangle bb = renderer.getBound(brailleList, 0, 0, false);
//			renderer.drawBraille(g, brailleList, bottom.x - (int) Math.round(bb.getWidth()/2), bottom.y, false);
//			renderer.drawBraille(g, brailleList, bottom.x - (int) Math.round(bb.getWidth()/2), bottom.y, false);

			double xStep = this.config.getXScale().step;
			boolean flag = true;
			for (double sx = this.config.getXScale().start; sx < this.config.xAxis.max; sx+=xStep)
			{
				bottom = this.coord(sx, this.config.yAxis.min);
				bottom.y += renderer.getBoxHeight()/2;
				if (flag)
				{
					bottom.y += ls + renderer.getBoxHeight();
				}
				label = Double.toString(sx);

				brailleList.clear();
				translater.braileFromSumiji(label, brailleList, true, true);
//				brailleList.add(0, translater.getDict().getExtra(BrailleInfo.EXTRA.SUUFU));

				bb = renderer.getBound(brailleList, 0, 0, false);
				renderer.drawBraille(g, brailleList, bottom.x - (int) Math.round(bb.getWidth()/2), bottom.y, false);
				flag = !flag;
			}
		}
		if (withYLabel)
		{
			int xoffset = (int) renderer.getBoxSpace();

			Point left = this.coord(this.config.xAxis.min, this.config.yAxis.origin);
			String label = Double.toString(this.config.yAxis.origin);
			brailleList.clear();
			translater.braileFromSumiji(label, brailleList, true, true);
			Rectangle bb = renderer.getBound(brailleList, left.x, left.y, false);
//			renderer.drawBraille(g, brailleList, left.x - (int) bb.getWidth() - xoffset, left.y - (int) Math.round((bb.getHeight())/2), false);
			renderer.drawBraille(g, brailleList, left.x - (int) bb.getWidth() - xoffset, left.y - (int) bb.getHeight(), false);

			double yStep = this.config.getYScale().step;
			for (double sy = this.config.getYScale().start; sy < this.config.yAxis.max; sy+=yStep)
			{
				left = this.coord(this.config.xAxis.min, sy);
				label = Double.toString(sy);

				brailleList.clear();
				translater.braileFromSumiji(label, brailleList, true, true);
//				brailleList.add(0, translater.getDict().getExtra(BrailleInfo.EXTRA.SUUFU));

				bb = renderer.getBound(brailleList, left.x, left.y, false);
//				renderer.drawBraille(g, brailleList, left.x - (int) bb.getWidth() - xoffset, left.y - (int) Math.round((bb.getHeight())/2), false);
				renderer.drawBraille(g, brailleList, left.x - (int) bb.getWidth() - xoffset, left.y - (int) bb.getHeight(), false);
			}
		}

		g.setFont(bf);
	}

	/**
	 * グラフを描く
	 *
	 * @param g Graphics
	 * @param x x座標
	 * @param y y座標
	 * @param w 幅
	 * @param h 高さ
	 * @param info データ
	 */
	public void drawSumiji(Graphics2D g,  int x, int y, int w, int h, DataInfo info)
	{
		switch(info.getConfig().type)
		{
		case LINE:
			this.drawLineSumiji(g,  x, y, w, h, info);
			break;
		}
	}

	/**
	 * グラフを描く
	 *
	 * @param g Graphics
	 * @param dotSize 点の大きさ
	 * @param dotSpan 点の間隔
	 * @param x x座標
	 * @param y y座標
	 * @param w 幅
	 * @param h 高さ
	 * @param info データ
	 */
	public void drawBraille(Graphics2D g, int dotSize, int dotSpan,  int x, int y, int w, int h, DataInfo data)
	{
		switch(data.getConfig().type)
		{
		case LINE:
			this.drawLineBraille(g, dotSize, dotSpan,  x, y, w, h, data);
			break;
		}
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
	protected void drawLineSumiji(Graphics2D g,  int x, int y, int w, int h, DataInfo info)
	{
		DataConfig dconfig = info.getConfig();

		// 点の座標を計算する
		GeneralPath path = new GeneralPath();
		boolean flag = true;
		for (Data<Double, Double> data : dconfig.dataList)
		{
			Point p = this.coord(data);
			if (flag)
			{
				path.moveTo(p.x, p.y);
				flag = false;
			}
			else
			{
				path.lineTo(p.x, p.y);
			}
		}

		// 線を描画する
		Shape clip = g.getClip();
		g.setClip(this.graphArea);
		g.draw(path);
		g.setClip(clip);
	}

	/**
	 * 折れ線グラフを描く
	 *
	 * @param g Graphics
	 * @param dotSize 点の大きさ
	 * @param dotSpan 点の間隔
	 * @param x x座標
	 * @param y y座標
	 * @param w 幅
	 * @param h 高さ
	 * @param info データ
	 */
	protected void drawLineBraille(Graphics2D g, int dotSize, int dotSpan,  int x, int y, int w, int h, DataInfo info)
	{
		DataConfig dconfig = info.getConfig();

		// 点の座標を計算する
		GeneralPath path = new GeneralPath();
		boolean flag = true;
		for (Data<Double, Double> data : dconfig.dataList)
		{
			Point p = this.coord(data);
			if (flag)
			{
				path.moveTo(p.x, p.y);
				flag = false;
			}
			else
			{
				path.lineTo(p.x, p.y);
			}
		}

		// 線を描画する
		Shape clip = g.getClip();
		g.setClip(this.graphArea);
		g.draw(path);
		g.setClip(clip);
	}

	/**
	 * 画面上の座標を計算する
	 *
	 * @param data データ
	 * @return 点の座標
	 */
	protected Point coord(Data<Double, Double> data)
	{
		return this.coord(data.x, data.y);
	}

	/**
	 * 画面上の座標を計算する
	 *
	 * @param x データのx値
	 * @param y データのy値
	 * @return 点の座標
	 */
	protected Point coord(double x, double y)
	{
		Point p = new Point();

		double xrange = this.config.xAxis.max - this.config.xAxis.min;
		p.x = (int) ((x - this.config.xAxis.min) * this.graphArea.width/xrange + this.graphArea.x);

		double yrange = this.config.yAxis.max - this.config.yAxis.min;
		p.y = (int) (-(y - this.config.yAxis.min) * this.graphArea.height/yrange + this.graphArea.y + this.graphArea.height);

		return p;
	}

	protected Point2D.Double dcoord(double x, double y)
	{
		Point2D.Double p = new Point2D.Double();

		double xrange = this.config.xAxis.max - this.config.xAxis.min;
		p.x = (x - this.config.xAxis.min) * this.graphArea.width/xrange + this.graphArea.x;

		double yrange = this.config.yAxis.max - this.config.yAxis.min;
		p.y = -(y - this.config.yAxis.min) * this.graphArea.height/yrange + this.graphArea.y + this.graphArea.height;

		return p;
	}

	/**
	 * 画面上の長さを計算する
	 *
	 * @param dx データのx値
	 * @param dy データのy値
	 * @return 点の格子距離
	 */
	protected Point length(double dx, double dy)
	{
		Point p = new Point();

		double xrange = this.config.xAxis.max - this.config.xAxis.min;
		p.x = (int) (dx * this.graphArea.width/xrange);

		double yrange = this.config.yAxis.max - this.config.yAxis.min;
		p.y = (int) (dy * this.graphArea.height/yrange);

		return p;
	}

	/**
	 * グラフスケールを固定して、軸の範囲を変更する
	 *
	 * @param x 変更後の画面上の位置
	 * @param y 変更後の画面上の位置
	 * @param w 変更後の画面上の幅
	 * @param h 変更後の画面上の高さ
	 */
	public void expand(int x, int y, int w, int h)
	{
		Rectangle newGraphArea = new Rectangle();
		newGraphArea.x = x + config.left;
		newGraphArea.y = y + config.top;
		newGraphArea.width = w - config.left - config.right;
		newGraphArea.height = h - config.top - config.bottom;

		// 原点
		Point2D.Double c = this.dcoord(this.config.xAxis.origin, this.config.yAxis.origin);
		Point2D.Double lt = this.dcoord(this.config.xAxis.min, this.config.yAxis.max);
		Point2D.Double rb = this.dcoord(this.config.xAxis.max, this.config.yAxis.min);

		double xunit = 0;
		if (c.x != rb.x)
		{
			xunit = (config.xAxis.max - config.xAxis.origin)/(rb.x - c.x);
		}
		else if (c.x != lt.x)
		{
			xunit = (config.xAxis.min - config.xAxis.origin)/(lt.x - c.x);
		}
		config.xAxis.min = (newGraphArea.x - c.x) * xunit;
		config.xAxis.max = (newGraphArea.x + newGraphArea.width - c.x) * xunit;

		double yunit = 0;
		if (c.y != lt.y)
		{
			yunit = (config.yAxis.max - config.yAxis.origin)/(c.y - lt.y);
		}
		else if (c.y != rb.y)
		{
			yunit = (config.yAxis.min - config.yAxis.origin)/(c.y - rb.y);
		}
		config.yAxis.max = (c.y - newGraphArea.y) * yunit;
		config.yAxis.min = (c.y - newGraphArea.y - newGraphArea.height) * yunit;

		config.xScale.reScale(config.xAxis.min, config.xAxis.max, config.xAxis.origin);
		config.yScale.reScale(config.yAxis.min, config.yAxis.max, config.yAxis.origin);
	}
}
