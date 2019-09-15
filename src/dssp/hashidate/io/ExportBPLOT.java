package dssp.hashidate.io;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import dssp.hashidate.config.Config;
import dssp.hashidate.misc.FigureType.LINE_TYPE;
import dssp.hashidate.shape.DesignObject;
import dssp.hashidate.shape.ShapeCircle;
import dssp.hashidate.shape.ShapeGraph;
import dssp.hashidate.shape.ShapeGraph.DataInfo;
import dssp.hashidate.shape.ShapePolyline;
import dssp.hashidate.shape.ShapeRegularPolygon;
import dssp.hashidate.shape.ShapeSpline;
import dssp.hashidate.shape.ShapeSpring;
import dssp.hashidate.shape.ShapeText;
import dssp.brailleLib.BrailleBox;
import dssp.brailleLib.BrailleInfo;
import dssp.brailleLib.Util;

/**
 * Bplotコマンドを出力する
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class ExportBPLOT extends ExportBase
{
	private BufferedWriter writer = null;
	private boolean writeHead = false;
	private BrailleRendererForBPLOT renderer;
	private Rectangle2D.Double winArea;
	private boolean upsideDown = true;
	private DesignObject obj;

	/**
	 * 選択された用紙の番号
	 */
	private int selectedPage;
	private static final String[] pages = {
			"A4(10×11インチ)",
			"B5( 8×10インチ)",
			"キャンセル"
	};

	private boolean backPoint = false;
	private String frontSide = "";
	private String backSide = "";
	private boolean onBuffering = true;

	private Dimension pageSize;

	public void setBackPoint(boolean flag)
	{
		this.backPoint = flag;
	}

	public void write(String format, Object...args)
	{
		String line = format;
		if (null != args && 0 < args.length)
		{
			line = String.format(format, args);
		}
		line += "\n";

		if (this.onBuffering)
		{
			if (this.backPoint)
			{
				this.backSide += line;
			}
			else
			{
				this.frontSide += line;
			}
		}
		else
		{
			try
			{
				this.writer.write(line);
			}
			catch (IOException e)
			{
				Util.logException(e);
			}
		}
	}

	private boolean writeHead()
	{
		if (null == writer)
		{
			return false;
		}
		if (writeHead)
		{
			return true;
		}

//		Dimension size = null;
//		switch(this.selectedPage)
//		{
//		case 1:
//			size = Config.getConfig(Config.BPLOT.PAGE_B5);
//			break;
//		default:
//			size = Config.getConfig(Config.BPLOT.PAGE_A4);
//			break;
//		}
//		this.writeComment("原点をページの左下隅にする");
//		this.write("origin %.02f %.02f 1 1", -size.width/20.0, -size.height/20.0);

		writeHead = true;

		return true;
	}

	@Override
	public boolean init(Rectangle area, File file, boolean newFile)
	{
		if (null == area)
		{
			return false;
		}

		this.selectedPage = Util.select3("BPLOT用紙", pages, 0, false, "用紙を選んでください");
		if (this.selectedPage == (pages.length-1))
		{
			return false;
		}

		this.area = area;
		this.file = file;
//		this.resolution = Config.getConfig(Config.BPLOT.RESOLUTION);
		if (null == this.file || newFile)
		{
			FileNameExtensionFilter filter = new FileNameExtensionFilter("BPLOT ファイル(*.txt)", "txt");
			FileNameExtensionFilter[] list = {filter};
			if (null == this.file)
			{
				this.file = Util.selectFile(null, "sample", list, true);
			}
			else
			{
				this.file = Util.selectFile(this.file.getName(), "sample", list, true);
			}
			if(null == this.file)
			{
				return false;
			}
		}
		try
		{
			this.writer = Files.newBufferedWriter(this.file.toPath(), Charset.forName("shift-jis"));
		}
		catch (IOException e)
		{
			Util.logException(e);
			return false;
		}

//		this.write("output");

		switch(this.selectedPage)
		{
		case 0:
			this.pageSize = Config.getConfig(Config.BPLOT.PAGE_A4);
			break;
		case 1:
			this.pageSize = Config.getConfig(Config.BPLOT.PAGE_B5);
			break;
		}

		this.winArea = new Rectangle2D.Double();
		this.winArea.width = Util.mmToPixel(0, this.pageSize.width*10);
		this.winArea.height = Util.mmToPixel(0, this.pageSize.height*10);

		this.upsideDown = Config.getConfig(Config.BPLOT.UPSIDEDOWN);

		this.renderer = BrailleRendererForBPLOT.newInstance(this, this.winArea, this.upsideDown);

		return true;
	}

	@Override
	public  File end()
	{
		try
		{
			this.onBuffering = false;

			this.write("output");

			switch(this.selectedPage)
			{
			case 0:
				this.write("A4");
				break;
			case 1:
				this.write("B5");
				break;
			}

			this.writeComment("原点をページの左下隅にする");
			this.write("origin %.02f %.02f 1 1", -this.pageSize.width/20.0, -this.pageSize.height/20.0);

			if ((boolean) Config.getConfig(Config.BPLOT.DOWNSIDEFIRST))
			{
				if (0 < this.backSide.length())
				{
					double dx = Config.getConfig(Config.BPLOT.MIRROR);
					this.writeComment("裏点にする");
					this.write("mirror 1 %.02f", dx);
					this.writer.write(this.backSide);
					this.writeComment("裏点を解除する");
					this.write("mirror 0 %.02f", dx);
				}
				this.writer.write(this.frontSide);
			}
			else
			{
				this.writer.write(this.frontSide);
				if (0 < this.backSide.length())
				{
					double dx = Config.getConfig(Config.BPLOT.MIRROR);
					this.writeComment("裏点にする");
					this.write("mirror 1 %.02f", dx);
					this.writer.write(this.backSide);
					this.writeComment("裏点を解除する");
					this.write("mirror 0 %.02f", dx);
				}
			}

			this.write("ff");
			this.onBuffering = true;
			if (null != this.writer)
			{
				this.writer.close();
				this.writer = null;

				this.renderer = null;
			}
		}
		catch (Exception e)
		{
			Util.logException(e);
			return null;
		}

		return this.file;
	}

	@Override
	public void openGroup(ShapeInfo info)
	{
		this.write(info);
	}

	@Override
	public boolean write(ShapeInfo info)
	{
		try
		{
			if (false == this.writeHead())
			{
				return false;
			}

			this.obj = info.obj;

			switch(obj.getShape())
			{
			case SHAPE:
				return this.writeShape();
			case CIRCLE:
			case ELLIPSE:
				return this.writeArc();
			case LINE:
			case POLYLINE:
				return this.writePolyline();
			case SPRING:
				return this.writeSpring();
			case POLYGON:
				return this.writePolygon();
			case REGULAR_POLYGON:
				return this.writeRegular();
			case SPLINE:
			case SPLINE_LOOP:
				return this.writeSpline();
				//			return writePathList();
			case RECTANGLE:
				return this.writeRectangle();
			case TEXT:
				return this.writeText();
			case FORMULA:
				return this.writeFormula();
			case GRAPH:
				return this.writeGraph();
			default:
				return this.writeShape();
			}
		}
		catch (Exception e)
		{
			Util.logException(e);
		}
		return false;
	}

	double getDX(int dx)
	{
		return Util.pixelToMM(0, dx)/100.0;
	}

	double getDY(int dy)
	{
		return Util.pixelToMM(0, dy)/100.0;
	}

//	private final static double LIMIT = 0.98;

	double getX(int x)
	{
		return Util.pixelToMM(0, x)/100.0;
	}

	double getY(int y)
	{
		if (this.upsideDown)
		{
			int temp = (int) (this.winArea.height - y);
			return Util.pixelToMM(0, temp)/100.0;
		}

		return Util.pixelToMM(0, y)/100.0;
	}

	double getDotSize()
	{
		int size = Config.getConfig(this.obj.getDotSize());
		return size/10.0;
	}

	double getDotSpan()
	{
		int val = Config.getConfig(Config.BRAILLE.DOT_SPAN);
		float ret = (float)val/10.0f;
		switch(this.obj.getDotSize())
		{
		case SMALL:
			ret *= 0.5f;
			break;
		case LARGE:
			ret *= 1.5f;
			break;
		default:
		}
		switch(this.obj.getLineType())
		{
		case DOT:
			return ret * 0.5f;
		case DASHED:
			return ret * 0.5f;
		default:
			return ret;
		}
//		return Math.round(Util.pixelToMM(0, (int) this.obj.getDotSpan()))/10.0;
	}

	public void writeDot(Config.BRAILLE size)
	{
		switch(size)
		{
		case SMALL:
			this.write("dot 0");
			break;
		case MIDDLE:
			this.write("dot 1");
			break;
		case LARGE:
			this.write("dot 2");
			break;
		default:
		}
	}

	public void writeDash(LINE_TYPE type, boolean on)
	{
		int flag = 0;
		if (on)
		{
			flag = 1;
		}
		switch(type)
		{
		case DOT:
			this.write("design %d 2 1 1 1", flag);
			break;
		case DASHED:
			this.write("design %d 2 1 2 1", flag);
			break;
		default:
		}
	}

	public void writeGap(boolean on, Config.BRAILLE size)
	{
		int flag = (on ? 1: 0);
		int dsize = (int) Config.getConfig(size);
		this.write("gap %d %.02f", flag, (double) dsize/10.0);
	}

	private static final float SMALL_ARROW_LEN = 0.6f;
	private static final float BIG_ARROW_LEN = 1.2f;
	private static final float ARROW_ANGLE = 45.0f;
	private void writeArrow(double x, double y, double x2, double y2)
	{
		float len;
		switch(this.obj.getEdgeType())
		{
		case LARROW:
		case RARROW:
		case BARROW:
			len = SMALL_ARROW_LEN;
			break;
		case LARROW1:
		case RARROW1:
		case BARROW1:
			len = BIG_ARROW_LEN;
			break;
		default:
			return;
		}
		double dx = x2 - x;
		double dy = y2 - y;

		double angle = Math.atan(dy/dx) * 180/Math.PI;
		if (0 > dy)
		{
			angle = 180 + Math.atan(dy/dx) * 180/Math.PI;
		}
		this.write("dart %.02f %.02f %.02f 0.0 %.0f %.02f 0 %.1f", x2, y2, angle, ARROW_ANGLE, len, this.getDotSpan());
	}

	private boolean writeShape()
	{
		if (this.obj instanceof Rectangle)
		{
			return writeRectangle();
		}
		return false;
	}

	private boolean writeSpline()
	{
		if (false == this.writeHead())
		{
			return false;
		}

		ShapeSpline tobj = (ShapeSpline) this.obj;

		double size = this.getDotSpan();
		Point2D.Double start = new Point2D.Double(this.getX(tobj.x), this.getY(tobj.y + tobj.height));
		Point2D.Double end = new Point2D.Double(this.getX(tobj.x + tobj.width), this.getY(tobj.y));
		start.x -= 2*size;
		start.y -= 2*size;
		end.x += 2*size;
		end.y += 2*size;

		this.setBackPoint(this.obj.isBackLine());
		int flag;
		switch(tobj.getShape())
		{
		case SPLINE:
			this.writeComment("曲線");
			flag = 2;
			break;
		case SPLINE_LOOP:
			this.writeComment("閉曲線");
			flag = 4;
			break;
		default:
			return false;
		}
		this.writeDot(this.obj.getDotSize());
		this.writeDash(this.obj.getLineType(), true);
		this.write("spline %.02f %.02f %.02f %.02f %d 0 0 %.1f", start.x, end.x, start.y, end.y, flag, size);

		int nPoints = tobj.getdPoints().size();
		for (int i = 0; i < nPoints; i++)
		{
			Point2D.Double p = tobj.getdPoints().get(i);
			double x = this.getX((int)Math.round(p.x));
			double y = this.getY((int)Math.round(p.y));

			this.write("    %.02f %.02f", x, y);
		}
		this.write("    9999 0");
		this.writeDash(this.obj.getLineType(), false);
		switch(this.obj.getEdgeType())
		{
		case LARROW:
		case LARROW1:
		case BARROW:
		case BARROW1:
			List<Point2D.Double> points = tobj.getdPoints();
			Point2D.Double[] ps = {points.get(0), points.get(0), points.get(1), points.get(2)};
			Path2D.Double seg = tobj.calcPath(ps);

			double[] hp = new double[6];
			double[] tp = new double[6];
			PathIterator it = seg.getPathIterator(null);
			it.currentSegment(hp);
			it.next();
			it.currentSegment(tp);

			double tx, ty, hx, hy;
			tx = this.getX((int) Math.round(tp[0]));
			ty = this.getY((int)Math.round(tp[1]));
			hx = this.getX((int) Math.round(hp[0]));
			hy = this.getY((int)Math.round(hp[1]));
			this.writeArrow(tx, ty, hx, hy);
			break;
		default:
		}
		switch(this.obj.getEdgeType())
		{
		case RARROW:
		case RARROW1:
		case BARROW:
		case BARROW1:
			List<Point2D.Double> points = tobj.getdPoints();
			Path2D.Double seg = null;
			switch(tobj.getShape())
			{
			case SPLINE:
				switch(points.size())
				{
				case 3:
				{
					Point2D.Double[] ps = {points.get(0), points.get(0), points.get(1), points.get(2)};
					seg = tobj.calcPath(ps);
					break;
				}
				default:
				{
					Point2D.Double[] ps = {points.get(points.size()-3), points.get(points.size()-2), points.get(points.size()-1), points.get(points.size()-1)};
					seg = tobj.calcPath(ps);
				}
				}
				break;
			case SPLINE_LOOP:
				Point2D.Double[] ps = {points.get(points.size()-2), points.get(points.size()-1), points.get(0), points.get(0)};
				seg = tobj.calcPath(ps);
				break;
			default:
			}
			double[] hp = new double[6];
			double[] tp = hp;
			for (PathIterator it = seg.getPathIterator(null); !it.isDone(); it.next())			{
				tp = Arrays.copyOf(hp, hp.length);
				it.currentSegment(hp);
			}
			double tx, ty, hx, hy;
			tx = this.getX((int) Math.round(tp[0]));
			ty = this.getY((int)Math.round(tp[1]));
			hx = this.getX((int) Math.round(hp[0]));
			hy = this.getY((int)Math.round(hp[1]));
			this.writeArrow(tx, ty, hx, hy);
			break;
		default:
		}

		return true;
	}

	private boolean writeRectangle()
	{
		this.setBackPoint(this.obj.isBackLine());
		this.writeComment("四角");
		this.writeDot(this.obj.getDotSize());
		this.writeDash(this.obj.getLineType(), true);
		Rectangle tobj = (Rectangle)this.obj;

		double size = this.getDotSpan();
		double left = this.getX(tobj.x);
		double top = this.getY(tobj.y);
		double right = this.getX(tobj.x + tobj.width);
		double bottom = this.getY(tobj.y + tobj.height);

		this.write("rectangle %.02f %.02f %.02f %.02f %.1f", left, top, right, bottom, size);
		this.writeDash(this.obj.getLineType(), false);

		switch(this.obj.getEdgeType())
		{
		case LARROW:
		case LARROW1:
		case BARROW:
		case BARROW1:
			this.writeArrow(right, top, left, top);
			break;
		default:
		}
		switch(this.obj.getEdgeType())
		{
		case RARROW:
		case RARROW1:
		case BARROW:
		case BARROW1:
			this.writeArrow(left, bottom, left, top);
			break;
		default:
		}

		return true;
	}

	private boolean writePolyline()
	{
		if (false == this.writeHead())
		{
			return false;
		}

		ShapePolyline tobj = (ShapePolyline) this.obj;
		double size = this.getDotSpan();

		this.setBackPoint(this.obj.isBackLine());
		this.writeComment("直線/折れ線");
		this.writeDot(this.obj.getDotSize());
		this.writeDash(this.obj.getLineType(), true);
		this.write("cline %.1f", size);

		int[] xPoints = tobj.getxPoints();
		int[] yPoints = tobj.getyPoints();
		int nPoints = xPoints.length;
		for (int i = 0; i < nPoints; i++)
		{
			double x = this.getX(xPoints[i]);
			double y = this.getY(yPoints[i]);
			this.write("    %.02f %.02f", x, y);
		}
		this.write("    9999 0");
		this.writeDash(this.obj.getLineType(), false);

		double sx, sy, ex, ey;
		switch(this.obj.getEdgeType())
		{
		case LARROW:
		case LARROW1:
		case BARROW:
		case BARROW1:
			sx = this.getX(xPoints[1]);
			sy = this.getY(yPoints[1]);
			ex = this.getX(xPoints[0]);
			ey = this.getY(yPoints[0]);
			this.writeArrow(sx, sy, ex, ey);
			break;
		default:
		}
		switch(this.obj.getEdgeType())
		{
		case RARROW:
		case RARROW1:
		case BARROW:
		case BARROW1:
			sx = this.getX(xPoints[xPoints.length-2]);
			sy = this.getY(yPoints[xPoints.length-2]);
			ex = this.getX(xPoints[xPoints.length-1]);
			ey = this.getY(yPoints[xPoints.length-1]);
			this.writeArrow(sx, sy, ex, ey);
			break;
		default:
		}

		return true;
	}

	private boolean writeSpring()
	{
		if (false == this.writeHead())
		{
			return false;
		}

		ShapeSpring tobj = (ShapeSpring) this.obj;

		int[] xPoints = tobj.getxPoints();
		int[] yPoints = tobj.getyPoints();

		List<Point> points = Util.newArrayList();
		points.add(new Point(xPoints[0], yPoints[0]));
		int nPoint = xPoints.length;
		Point[] ends = {new Point(xPoints[1], yPoints[1]), new Point(xPoints[nPoint-2], yPoints[nPoint-2])};
		points.add(ends[0]);

		double dx = ends[1].x - ends[0].x;
		double dy = ends[1].y - ends[0].y;
		double d = Math.sqrt(dx*dx + dy*dy);
		int nHelix = tobj.getNHelix();
		double px = (double)((ends[1].x - ends[0].x))/(double)(4*nHelix);
		double py = (double)((ends[1].y - ends[0].y))/(double)(4*nHelix);

		int r = tobj.getRadius();
		for (int i = 0; i < nHelix; i++)
		{
			Point p = new Point();
			p.x = (int) (ends[0].x + px*(4*i+1) + (double)r*dy/d);
			p.y = (int) (ends[0].y + py*(4*i+1) - (double)r*dx/d);
			points.add(p);

			p = new Point();
			p.x = (int) (ends[0].x + px*(4*i+3) - (double)r*dy/d);
			p.y = (int) (ends[0].y + py*(4*i+3) + (double)r*dx/d);
			points.add(p);
		}
		points.add(ends[1]);
		points.add(new Point(xPoints[nPoint-1], yPoints[nPoint-1]));

		boolean lb = tobj.isBackLine();
		boolean ub = tobj.useBackPoint();
		int nLine = points.size()-1;
		double size = this.getDotSpan();
		if (!ub)
		{
			this.setBackPoint(lb);
			this.writeComment("バネ");

			this.writeDot(this.obj.getDotSize());
			this.writeDash(this.obj.getLineType(), true);
			this.write("cline %.1f", size);
			for (int i = 0; i <= nLine; i++)
			{
				Point p1 = points.get(i);

				double x = this.getX(p1.x);
				double y = this.getY(p1.y);

				this.write("    %.02f %.02f", x, y);
			}
			this.write("    9999 0");
		}
		else
		{
			this.setBackPoint(lb);
			this.writeComment("バネ(裏点使用)");
			this.writeDot(this.obj.getDotSize());
			this.writeDash(this.obj.getLineType(), true);
			for (int i = 0; i < nLine; i+=2)
			{
				Point p1 = points.get(i);
				Point p2 = points.get(i+1);

				double sx = this.getX(p1.x);
				double sy = this.getY(p1.y);
				double ex = this.getX(p2.x);
				double ey = this.getY(p2.y);

				this.write("line %.02f %.02f %.02f %.02f %.1f", sx, sy, ex, ey, size);
			}

			this.setBackPoint(!lb);
			this.writeDot(this.obj.getDotSize());
			this.writeDash(this.obj.getLineType(), true);
			for (int i = 1; i < nLine; i+=2)
			{
				Point p1 = points.get(i);
				Point p2 = points.get(i+1);

				double sx = this.getX(p1.x);
				double sy = this.getY(p1.y);
				double ex = this.getX(p2.x);
				double ey = this.getY(p2.y);

				this.write("line %.02f %.02f %.02f %.02f %.1f", sx, sy, ex, ey, size);
			}
		}

		this.writeDash(this.obj.getLineType(), false);

		switch(this.obj.getEdgeType())
		{
		case LARROW:
		case LARROW1:
		case BARROW:
		case BARROW1:
			double sx = this.getX(xPoints[1]);
			double sy = this.getY(yPoints[1]);
			double ex = this.getX(xPoints[0]);
			double ey = this.getY(yPoints[0]);
			this.writeArrow(sx, sy, ex, ey);
			break;
		default:
		}
		switch(this.obj.getEdgeType())
		{
		case RARROW:
		case RARROW1:
		case BARROW:
		case BARROW1:
			double sx = this.getX(xPoints[nPoint-2]);
			double sy = this.getY(yPoints[nPoint-2]);
			double ex = this.getX(xPoints[nPoint-1]);
			double ey = this.getY(yPoints[nPoint-1]);
			this.writeArrow(sx, sy, ex, ey);
			break;
		default:
		}

		return true;
	}

//	private boolean writeSpring()
//	{
//		if (false == this.writeHead())
//		{
//			return false;
//		}
//
//		ShapeSpring tobj = (ShapeSpring) this.obj;
//
//		this.setBackPoint(this.obj.isBackLine());
//		this.writeComment("バネ");
//		this.writeDot(this.obj.getDotSize());
//		this.writeDash(this.obj.getLineType(), true);
//		double size = this.getDotSpan();
//		int[] xPoints = tobj.getxPoints();
//		int[] yPoints = tobj.getyPoints();
//		int nPoints = xPoints.length;
//		for (int i = 0; i < (nPoints - 1); i++)
//		{
//			double sx = this.getX(xPoints[i]);
//			double sy = this.getY(yPoints[i]);
//			double ex = this.getX(xPoints[i+1]);
//			double ey = this.getY(yPoints[i+1]);
//			this.write("line %.02f %.02f %.02f %.02f %.1f", sx, sy, ex, ey, size);
//		}
//		this.writeDash(this.obj.getLineType(), false);
//
//		switch(this.obj.getEdgeType())
//		{
//		case LARROW:
//		case LARROW1:
//		case BARROW:
//		case BARROW1:
//			double sx = this.getX(xPoints[1]);
//			double sy = this.getY(yPoints[1]);
//			double ex = this.getX(xPoints[0]);
//			double ey = this.getY(yPoints[0]);
//			this.writeArrow(sx, sy, ex, ey);
//			break;
//		default:
//		}
//		switch(this.obj.getEdgeType())
//		{
//		case RARROW:
//		case RARROW1:
//		case BARROW:
//		case BARROW1:
//			double sx = this.getX(xPoints[xPoints.length-2]);
//			double sy = this.getY(yPoints[xPoints.length-2]);
//			double ex = this.getX(xPoints[xPoints.length-1]);
//			double ey = this.getY(yPoints[xPoints.length-1]);
//			this.writeArrow(sx, sy, ex, ey);
//			break;
//		default:
//		}
//
//		return true;
//	}

	private boolean writePolygon()
	{
		if (false == this.writeHead())
		{
			return false;
		}

		ShapePolyline tobj = (ShapePolyline) this.obj;
		double size = this.getDotSpan();

		this.setBackPoint(this.obj.isBackLine());
		this.writeComment("多角形");
		this.writeDot(this.obj.getDotSize());
		this.writeDash(this.obj.getLineType(), true);
		this.write("cline %.1f", size);

		int[] xPoints = tobj.getxPoints();
		int[] yPoints = tobj.getyPoints();
		int nPoints = xPoints.length;
		for (int i = 0; i < nPoints; i++)
		{
			double x = this.getX(xPoints[i]);
			double y = this.getY(yPoints[i]);
			this.write("    %.02f %.02f", x, y);
		}
		double x = this.getX(xPoints[0]);
		double y = this.getY(yPoints[0]);
		this.write("    %.02f %.02f", x, y);
		this.write("    9999 0");
		this.writeDash(this.obj.getLineType(), false);

		double sx, sy, ex, ey;
		switch(this.obj.getEdgeType())
		{
		case LARROW:
		case LARROW1:
		case BARROW:
		case BARROW1:
			sx = this.getX(xPoints[1]);
			sy = this.getY(yPoints[1]);
			ex = this.getX(xPoints[0]);
			ey = this.getY(yPoints[0]);
			this.writeArrow(sx, sy, ex, ey);
			break;
		default:
		}
		switch(this.obj.getEdgeType())
		{
		case RARROW:
		case RARROW1:
		case BARROW:
		case BARROW1:
			sx = this.getX(xPoints[nPoints-1]);
			sy = this.getY(yPoints[nPoints-1]);
			ex = this.getX(xPoints[0]);
			ey = this.getY(yPoints[0]);
			this.writeArrow(sx, sy, ex, ey);
			break;
		default:
		}

		return true;
	}

	private boolean writeRegular()
	{
		if (false == this.writeHead())
		{
			return false;
		}

		ShapeRegularPolygon tobj = (ShapeRegularPolygon) this.obj;
		double size = this.getDotSpan();

		this.setBackPoint(this.obj.isBackLine());
		this.writeComment("正多角形");
		this.writeDot(this.obj.getDotSize());
		this.writeDash(this.obj.getLineType(), true);
		double cx = this.getX((int) Math.round(tobj.getCenterX()));
		double cy = this.getY((int) Math.round(tobj.getCenterY()));
		double r = this.getDX((int)Math.round(tobj.getRadius()));
		this.write("polygon %d %.02f %.02f %.02f %.1f", tobj.getNumberOfCorners(), cx, cy, r, size);
		this.writeDash(this.obj.getLineType(), false);

		int[] xPoints = tobj.getxPoints();
		int[] yPoints = tobj.getyPoints();
		int nPoints = xPoints.length;

		double sx, sy, ex, ey;
		switch(this.obj.getEdgeType())
		{
		case LARROW:
		case LARROW1:
		case BARROW:
		case BARROW1:
			sx = this.getX(xPoints[1]);
			sy = this.getY(yPoints[1]);
			ex = this.getX(xPoints[0]);
			ey = this.getY(yPoints[0]);
			this.writeArrow(sx, sy, ex, ey);
			break;
		default:
		}
		switch(this.obj.getEdgeType())
		{
		case RARROW:
		case RARROW1:
		case BARROW:
		case BARROW1:
			sx = this.getX(xPoints[nPoints-1]);
			sy = this.getY(yPoints[nPoints-1]);
			ex = this.getX(xPoints[0]);
			ey = this.getY(yPoints[0]);
			this.writeArrow(sx, sy, ex, ey);
			break;
		default:
		}

		return true;
	}

	private boolean writeArc()
	{
		if (false == this.writeHead())
		{
			return false;
		}
		ShapeCircle tobj = (ShapeCircle) this.obj;

		this.setBackPoint(this.obj.isBackLine());
		this.writeComment("円/楕円/扇形/円弧/弓形");
		this.writeDot(this.obj.getDotSize());
		this.writeDash(this.obj.getLineType(), true);
		double cx = 0;
		double cy = 0;
		double rx = 0;
		double ry = 0;
		double size = this.getDotSpan();
		switch(tobj.getShape())
		{
		case CIRCLE:
			rx = this.getDX(tobj.width/2);
			ry = this.getDY(tobj.width/2);
			cx = this.getX(tobj.x + tobj.width/2);
			cy = this.getY(tobj.y + tobj.height/2);
			this.write("circle %.02f %.02f %.02f %d %d %.1f", cx, cy, rx, tobj.getAngle(ShapeCircle.ANGLE.START), tobj.getAngle(ShapeCircle.ANGLE.END), size);
			break;
		case ELLIPSE:
			rx = this.getDX(tobj.width/2);
			ry = this.getDY(tobj.width/2);
			cx = this.getX(tobj.x + tobj.width/2);
			cy = this.getY(tobj.y + tobj.height/2);
			this.write("ellipse %.02f %.02f %.02f %.02f %d %d %.1f", cx, cy, rx, ry, tobj.getAngle(ShapeCircle.ANGLE.START), tobj.getAngle(ShapeCircle.ANGLE.END), size);
			break;
		default:
		}

		int start = (int) tobj.getAngle(ShapeCircle.ANGLE.START);
		int arc = (int) tobj.getAngle(ShapeCircle.ANGLE.EXTENT);

		double ds = (double)start * Math.PI/180.0;
		double de = (double)(start + arc)  * Math.PI/180.0;

		double angle = ds;
		double sx = cx + rx * Math.cos(angle);
		double sy = cy + ry * Math.sin(angle);;

		angle = de;
		double ex = cx + rx * Math.cos(angle);
		double ey = cy + ry * Math.sin(angle);;

		switch(tobj.getStyle())
		{
		case PIE:
			// 半径部分を出力する
			this.write("line %.02f %.02f %.02f %.02f %.1f", sx, sy, cx, cy, size);
			this.write("line %.02f %.02f %.02f %.02f %.1f", ex, ey, cx, cy, size);
			break;
		case CHORD:
			// 弦部分を出力する
			this.write("line %.02f %.02f %.02f %.02f %.1f", sx, sy, ex, ey, size);
			break;
		default:
		}
		this.writeDash(this.obj.getLineType(), false);

		switch(tobj.getStyle())
		{
		case PIE:
			switch(this.obj.getEdgeType())
			{
			case LARROW:
			case LARROW1:
			case BARROW:
			case BARROW1:
				angle = ds + 5* Math.PI/180.0;
				double tx = cx + rx * Math.cos(angle);
				double ty = cy + ry * Math.sin(angle);
				double hx = sx;
				double hy = sy;
				this.writeArrow(tx, ty, hx, hy);
				break;
			default:
			}
			switch(this.obj.getEdgeType())
			{
			case RARROW:
			case RARROW1:
			case BARROW:
			case BARROW1:
				angle = de - 5* Math.PI/180.0;
				double tx = cx;
				double ty = cy;
				double hx = sx;
				double hy = sy;
				this.writeArrow(tx, ty, hx, hy);
				break;
			default:
			}
			break;
		case CHORD:
			switch(this.obj.getEdgeType())
			{
			case LARROW:
			case LARROW1:
			case BARROW:
			case BARROW1:
				angle = ds + 5* Math.PI/180.0;
				double tx = cx + rx * Math.cos(angle);
				double ty = cy + ry * Math.sin(angle);;
				double hx = sx;
				double hy = sy;
				this.writeArrow(tx, ty, hx, hy);
				break;
			default:
			}
			switch(this.obj.getEdgeType())
			{
			case RARROW:
			case RARROW1:
			case BARROW:
			case BARROW1:
				double tx = ex;
				double ty = ey;
				double hx = sx;
				double hy = sy;
				this.writeArrow(tx, ty, hx, hy);
				break;
			default:
			}
			break;
		default:
			switch(this.obj.getEdgeType())
			{
			case LARROW:
			case LARROW1:
			case BARROW:
			case BARROW1:
				angle = ds + 5* Math.PI/180.0;
				double tx = cx + rx * Math.cos(angle);
				double ty = cy + ry * Math.sin(angle);;
				double hx = sx;
				double hy = sy;
				this.writeArrow(tx, ty, hx, hy);
				break;
			default:
			}
			switch(this.obj.getEdgeType())
			{
			case RARROW:
			case RARROW1:
			case BARROW:
			case BARROW1:
				angle = de - 5* Math.PI/180.0;
				double tx = cx + rx * Math.cos(angle);
				double ty = cy + ry * Math.sin(angle);;
				double hx = ex;
				double hy = ey;
				this.writeArrow(tx, ty, hx, hy);
				break;
			default:
			}
		}
		return true;
	}

	private boolean writeGraph()
	{
		ShapeGraph tobj = (ShapeGraph) this.obj;

		GraphRendererForBPLOT renderer = new GraphRendererForBPLOT(this);
		renderer.setConfig(tobj.getRenderer().getConfig());

		this.writeComment("グラフ");
		renderer.drawAxisSumiji(null, tobj.x, tobj.y, tobj.width, tobj.height);

		this.writeGap(true, Config.BRAILLE.LARGE);
		for (DataInfo dataInfo: tobj.getDataList())
		{
			renderer.drawSumiji(null, tobj.x, tobj.y, tobj.width, tobj.height, dataInfo);
		}
		this.writeGap(false, Config.BRAILLE.LARGE);

		renderer.drawEnd();

		return true;
	}

	private boolean writeText()
	{
		ShapeText tobj = (ShapeText) this.obj;
		if (false == tobj.isBraille())
		{
			return true;
		}
		this.writeComment("テキスト %s",tobj.getText());

		if ((boolean) Config.getConfig(Config.BPLOT.USE_NABCC))
		{
			double x = this.getX(tobj.x);
			double y = this.getY(tobj.y);

			List<String> lines = Util.newArrayList();
			StringBuilder buf = new StringBuilder();
			for (BrailleInfo braille: tobj.getBrailleList())
			{
				if (braille.isLineBreak())
				{
					lines.add(buf.toString());
					buf.delete(0, buf.length());
				}
				else
				{
					buf.append(braille.getNABCC(true));
				}
			}
			if (0 < buf.length())
			{
				lines.add(buf.toString());
			}
			int lineSpace = Config.getConfig(Config.BRAILLE.LINE_SPACE);
			int spaceY = Config.getConfig(Config.BRAILLE.SPACE_Y);
			this.writeDot(this.obj.getDotSize());
			double dy = (lineSpace + spaceY*2)/100.0;
			for (String line: lines)
			{
				this.write("brl %.02f %.02f %s", x, y, line);
				y -= dy;
			}
		}
		else
		{
			int dotSize = Config.getConfig(tobj.getDotSize());
			this.renderer.setDotSize(Util.mmToPixel(0, dotSize));

			int size = Config.getConfig(Config.BRAILLE.SPACE_X);
			size = Util.mmToPixel(0, size);
			this.renderer.setDotSpaceX(size);

			size = Config.getConfig(Config.BRAILLE.SPACE_Y);
			size = Util.mmToPixel(0, size);
			this.renderer.setDotSpaceY(size);

			size = Config.getConfig(Config.BRAILLE.BOX_SPACE);
			size = Util.mmToPixel(0, size);
			this.renderer.setBoxSpace(size);

			size = Config.getConfig(Config.BRAILLE.LINE_SPACE);
			size = Util.mmToPixel(0, size);
			this.renderer.setLineSpace(size);

			this.renderer.drawBraille(null, tobj.getBrailleList(), tobj.x, tobj.y, false);
		}
		return true;
	}

	private boolean writeFormula()
	{
		ShapeText tobj = (ShapeText) this.obj;

		this.writeComment("数式 " + tobj.getText());

		int dotSize = Config.getConfig(tobj.getDotSize());
		this.renderer.setDotSize(Util.mmToPixel(0, dotSize));

		int size = Config.getConfig(Config.BRAILLE.SPACE_X);
		size = Util.mmToPixel(0, size);
		this.renderer.setDotSpaceX(size);

		size = Config.getConfig(Config.BRAILLE.SPACE_Y);
		size = Util.mmToPixel(0, size);
		this.renderer.setDotSpaceY(size);

		size = Config.getConfig(Config.BRAILLE.BOX_SPACE);
		size = Util.mmToPixel(0, size);
		this.renderer.setBoxSpace(size);

		size = Config.getConfig(Config.BRAILLE.LINE_SPACE);
		size = Util.mmToPixel(0, size);
		this.renderer.setLineSpace(size);

		boolean useNABCC = Config.getConfig(Config.BPLOT.USE_NABCC);

		// NABCCを使わない場合は描画し、使う場合はboxList取得
		this.renderer.setDoPlot(!useNABCC);
		List<BrailleInfo> brailleList = tobj.getBrailleList();
		List<BrailleBox> boxList = this.renderer.drawBraille(null, brailleList, tobj.x, tobj.y, false);
		this.renderer.setDoPlot(useNABCC);

		if (useNABCC)
		{
			List<Point2D.Double> locates = Util.newArrayList();
			double x = 0;
			double y = 0;

			List<String> lines = Util.newArrayList();
			StringBuilder buf = new StringBuilder();
			int nBraille = brailleList.size();
			int boxIndex = 0;
			int nCol = 0;
			for (int i = 0; i < nBraille; i++ )
			{
				BrailleInfo braille = tobj.getBrailleList().get(i);

				String nabcc = braille.getNABCC(false);
				if (braille.isLineBreak())
				{
					lines.add(buf.toString());
					buf.delete(0, buf.length());
				}
				else if (braille.haveTable(BrailleInfo.TABLE.TABLE_OPEN) && 0 < buf.length())
				{
					lines.add(buf.toString());
					buf.delete(0, buf.length());
				}
				else if (braille.haveTable(BrailleInfo.TABLE.ROW_END))
				{
					lines.add(buf.toString());
					buf.delete(0, buf.length());
				}
				else if (braille.haveTable(BrailleInfo.TABLE.CELL_END))
				{
					nCol++;
				}
				else if (braille.haveTable(BrailleInfo.TABLE.ROW_START))
				{
					nCol = 0;
				}
				else if (0 < nCol && braille.haveTable(BrailleInfo.TABLE.CELL_START))
				{
					buf.append(" ");
				}
				else if (false == nabcc.isEmpty())
				{
					if (0 == buf.length())
					{
						Rectangle box = boxList.get(boxIndex);
						x = this.getX(box.x);
						y = this.getY(box.y);
						locates.add(new Point2D.Double(x, y));
					}
					buf.append(nabcc);
					boxIndex += braille.getBoxCount();
				}
			}
			if (0 < buf.length())
			{
				lines.add(buf.toString());
			}
			this.writeDot(this.obj.getDotSize());
			int nLine = lines.size();
			for (int i = 0; i < nLine; i++)
			{
				String line = lines.get(i);
				Point2D.Double p = locates.get(i);
				this.write("brl %.02f %.02f %s", p.x, p.y, line);
			}
		}

		return true;
	}

	public void writeComment(String format, Object...args)
	{
		String comment = String.format(format, args);

		String[] lines = comment.split("\n");

		for (String line: lines)
		{
			this.write(String.format("// %s", line));
		}
	}

}
