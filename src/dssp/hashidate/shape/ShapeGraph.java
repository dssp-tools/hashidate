package dssp.hashidate.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import dssp.brailleLib.Util;
import dssp.hashidate.DesignPanel;
import dssp.hashidate.config.Config;
import dssp.hashidate.io.ExportBase;
import dssp.hashidate.io.ShapeInfo;
import dssp.hashidate.misc.FigureType.EDGE_TYPE;
import dssp.hashidate.misc.FigureType.LINE_TYPE;
import dssp.hashidate.misc.BrailleStroke;
import dssp.hashidate.misc.LineStroke;
import dssp.hashidate.misc.Pair;
import dssp.hashidate.misc.PairList;
import dssp.hashidate.shape.helper.FunctionBody;
import dssp.hashidate.shape.helper.GraphRenderer;
import dssp.hashidate.shape.helper.GraphRenderer.Axis;
import dssp.hashidate.shape.helper.GraphRenderer.Data;
import dssp.hashidate.shape.helper.GraphRenderer.DataConfig;
import dssp.hashidate.shape.helper.GraphRenderer.GRAPH_TYPE;
import dssp.hashidate.shape.helper.GraphRenderer.GraphConfig;
import dssp.hashidate.shape.helper.GraphRenderer.GraphScale;
import dssp.hashidate.shape.helper.GraphRenderer.GraphScale.SHOW;
import dssp.hashidate.shape.property.ShapeGraphProperty;

/**
 * @author yagi
 *
 */
public class ShapeGraph extends ShapeRectangle
{
	GraphRenderer renderer = new GraphRenderer();
	public GraphRenderer getRenderer()
	{
		return this.renderer;
	}

	public ShapeGraph()
	{
		this.shape = SHAPE.GRAPH;
	}

	public ShapeGraph(Point p)
	{
		this();

		this.base.x = p.x;
		this.base.y = p.y;
		this.base.width = 300;
		this.base.height = 300;
		this.adaptFrame();

	}

	@Override
	public ShapeGraph clone()
	{
		ShapeGraph obj = (ShapeGraph) super.clone();

		obj.renderer = this.renderer;
		obj.dataList = Util.newArrayList();
		Util.deepCopyByClone(this.dataList, obj.dataList);

		obj.adaptFrame();
		for (DataInfo d: obj.getDataList())
		{
			d.setLineStroke();
		}

		return obj;
	}

	public static enum FUNC_TYPE
	{
		FUNCTION,
		DATA;
	}

	public static class DataInfo implements Cloneable
	{
		public DataInfo()
		{
			this.init();
		}

		private void init()
		{
			this.constTable.put("π", Math.PI);
			this.constTable.put("e", Math.E);;

			this.setLineStroke();
		}

		private DataConfig config = new GraphRenderer.DataConfig();
		public DataConfig getConfig()
		{
			return config;
		}

		private FUNC_TYPE funcType = FUNC_TYPE.FUNCTION;
		public FUNC_TYPE getFuncType()
		{
			return funcType;
		}
		public void setFuncType(FUNC_TYPE funcType)
		{
			this.funcType = funcType;
		}

		private String funcText = "";
		public String getFuncText()
		{
			return funcText;
		}
		public void setFuncText(String funcText)
		{
			this.funcText = funcText;
		}

		private String mathMLText = "";
		public String getMathMLText()
		{
			return mathMLText;
		}
		public void setMathMLText(String mathMLText)
		{
			this.mathMLText = mathMLText;
		}

		private FunctionBody func;
		public FunctionBody getFunc()
		{
			return func;
		}
		public void setFunc(FunctionBody func)
		{
			this.func = func;
		}

		private Map<String, java.lang.Double> constTable = Util.newHashMap();
		public Map<String, java.lang.Double> getConstTable()
		{
			return constTable;
		}

		private boolean useXRange = true;
		private boolean useAxisRange = true;
		private double min;
		private double max;

		public void setRange(boolean useXRange, boolean useAxisRange, double min, double max)
		{
			this.useXRange = useXRange;
			this.useAxisRange = useAxisRange;
			this.min = min;
			this.max = max;
		}
		public boolean isUseXRange()
		{
			return this.useXRange;
		}
		public void setUseXRange(boolean useXRange)
		{
			this.useXRange = useXRange;
		}
		public boolean isUseAxisRange()
		{
			return this.useAxisRange;
		}
		public void setUseAxisRange(boolean useAxisRange)
		{
			this.useAxisRange = useAxisRange;
		}
		public double getMin()
		{
			return this.min;
		}
		public void setMin(double min)
		{
			this.min = min;
		}
		public double getMax()
		{
			return this.max;
		}
		public void setMax(double max)
		{
			this.max = max;
		}

		public void setUseNumber(boolean useNumber)
		{
			this.useNumber = useNumber;
		}

		public void setParam(double param)
		{
			this.param = param;
		}

		private boolean useNumber = true;
		private double param = 50;

		public void setParam(boolean useNumber, double param)
		{
			this.useNumber = useNumber;
			this.param = param;
		}
		public boolean isUseNumber()
		{
			return this.useNumber;
		}
		public double getParam()
		{
			return this.param;
		}

		private LINE_TYPE lineType = LINE_TYPE.SOLID;
		public LINE_TYPE getLineType()
		{
			return lineType;
		}

		public void setLineType(LINE_TYPE lineType)
		{
			this.lineType = lineType;
		}

		private Color lineColor = Color.BLACK;
		public Color getLineColor()
		{
			return lineColor;
		}

		public void setLineColor(Color lineColor)
		{
			this.lineColor = lineColor;
		}

		private Config.BRAILLE dotSize = Config.BRAILLE.MIDDLE;
		public Config.BRAILLE getLineSize()
		{
			return this.dotSize;
		}

		public void setLineSize(Config.BRAILLE size)
		{
			this.dotSize = size;
		}

		private LineStroke lineStroke;
		private BrailleStroke brailleStroke;
		public void setLineStroke()
		{
			float psize;
			switch(this.dotSize)
			{
			case SMALL:
				psize = 1;
				break;
			case LARGE:
				psize = 4;
				break;
			default:
				psize = 2;
			}

			this.lineStroke = new LineStroke(psize, this.lineType, EDGE_TYPE.BUTT);

			Integer v = Config.getConfig(this.dotSize);
			float dSize = Util.mmToPixel(0, (null == v ? 0: v));
			v = Config.getConfig(Config.BRAILLE.DOT_SPAN);
			float dotSpan = Util.mmToPixel(0, (null == v ? 0: v));
			this.brailleStroke = new BrailleStroke(dSize, dotSpan, this.lineType, EDGE_TYPE.BUTT, !this.backLine);
		}

		private boolean backLine = false;
		public boolean isBackLine()
		{
			return backLine;
		}

		public void setBackLine(boolean backLine)
		{
			this.backLine = backLine;
		}

		@Override
		public DataInfo clone()
		{
			try
			{
				DataInfo dst = (DataInfo) super.clone();

				dst.config = this.config.clone();
				dst.setFuncType(this.funcType);
				dst.setFuncText(new String(this.funcText));
				dst.setMathMLText(new String(this.mathMLText));
				if (null != this.func)
				{
					dst.func = this.func.clone();
				}
				dst.setRange(this.useXRange, this.useAxisRange, this.min, this.max);
				dst.setParam(this.useNumber, this.param);
				dst.constTable = Util.newHashMap();
				for (String key: this.constTable.keySet())
				{
					double val = this.constTable.get(key);
					dst.constTable.put(key, val);
				}
				dst.lineType = this.lineType;
				dst.lineColor = this.lineColor;
				dst.backLine = this.backLine;

				return dst;
			}
			catch (CloneNotSupportedException e)
			{
				Util.logException(e);
			}
			return null;
		}
	}

	private List<DataInfo> dataList = Util.newArrayList();

	public List<DataInfo> getDataList()
	{
		return dataList;
	}

	private static ShapeGraphProperty property = new ShapeGraphProperty();

	@Override
	public boolean init(DesignPanel panel)
	{
		GraphRenderer.GraphConfig graphConf = this.renderer.getConfig();
		if (null == graphConf)
		{
			graphConf = new GraphRenderer.GraphConfig();
			this.renderer.setConfig(graphConf);
		}

		return this.showProperty(panel, this.getLocation());

/*		property.clear();

		property.setConfig(graphConf);
		property.setVisible(true);
		if (false == property.toUpdate())
		{
			return false;
		}

		property.getConfig(graphConf);

		this.dataList.clear();
		property.getData(this.dataList);
		if (0 == this.dataList.size())
		{
			return false;
		}

		this.calcPoint();

		return true;
*/
	}

	private void calcPoint(boolean build)
	{
		for (DataInfo info : this.dataList)
		{
			if (FUNC_TYPE.DATA == info.getFuncType())
			{
				continue;
			}

//			String mathML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><math xmlns=\"http://www.w3.org/1998/Math/MathML\" display=\"inline\" fontFamily=\"Serif\" mathsize=\"12pt\" mathvariant=\"italic\">    <mtext>sin</mtext>    <mo>(</mo>    <mtext>x</mtext>    <mo>)</mo></math>";
			String mathML = info.getMathMLText();
			double xmin = (info.isUseAxisRange() ? this.renderer.getConfig().getXAxis().getMin() : info.getMin());
			double xmax = (info.isUseAxisRange() ? this.renderer.getConfig().getXAxis().getMax() : info.getMax());

			if (build)
			{
				info.setFunc(FunctionBody.build(mathML, info.getConstTable()));
				if (null == info.getFunc())
				{
					Util.notify(String.format("%sの関数を解釈できませんでした\n書き方を確認して下さい", info.getClass().getName()));
					continue;
				}
			}
			DataConfig dataConf = info.getConfig();
			List<Data<java.lang.Double, java.lang.Double>> dataList = dataConf.getData();
			if (null == dataList)
			{
				dataList = Util.newArrayList();
				dataConf.setData(dataList);
			}
			else
			{
				dataList.clear();
			}

			FunctionBody func = info.getFunc();
			if (info.useXRange)
			{
				if (info.useNumber)
				{
					int nPoints = (int) info.getParam();
					double dx = (xmax - xmin) / (double) nPoints;
					for (int i = 0; i <= nPoints; i++)
					{
						double x = xmin + dx * i;
						double y = func.calc(x);

						Data<java.lang.Double, java.lang.Double> data = new Data<java.lang.Double, java.lang.Double>(x, y);
						dataList.add(data);
					}
				}
				else
				{
					double dx = info.getParam();
					for (double x = xmin; x <= xmax; x += dx)
					{
						double y = func.calc(x);
						Data<java.lang.Double, java.lang.Double> data = new Data<java.lang.Double, java.lang.Double>(x, y);
						dataList.add(data);
					}
				}
			}
			else
			{
				double x = func.calc(0);
				if (info.useAxisRange)
				{
					Axis yAxis = this.renderer.getConfig().getYAxis();
					Data<java.lang.Double, java.lang.Double> data = new Data<java.lang.Double, java.lang.Double>(x, yAxis.getMin());
					dataList.add(data);
					data = new Data<java.lang.Double, java.lang.Double>(x, yAxis.getMax());
					dataList.add(data);
				}
				else
				{
					Data<java.lang.Double, java.lang.Double> data = new Data<java.lang.Double, java.lang.Double>(x, info.getMin());
					dataList.add(data);
					data = new Data<java.lang.Double, java.lang.Double>(x, info.getMax());
					dataList.add(data);
				}
			}
		}
	}

	private void drawFrame(Graphics2D g, boolean printing)
	{
		if (isSelected() && false == printing)
		{
			g.setColor(this.frameColor);
			g.drawRect(this.x, this.y, this.width, this.height);

			drawToggle(g, this.x, this.y);
			drawToggle(g, this.x + this.width, this.y);
			drawToggle(g, this.x + this.width, this.y + this.height);
			drawToggle(g, this.x, this.y + this.height);

		}
	}

	@Override
	public Rectangle getBounds()
	{
		return this.renderer.getBounds(super.getBounds());
	}

	@Override
	protected BufInfo getBuffer(Graphics2D g)
	{
		BufInfo info = new BufInfo();
//		info.setBuf(new BufferedImage(this.width + 4*TOGGLE, this.height + 4*TOGGLE, BufferedImage.TYPE_4BYTE_ABGR));
		info.setBuf(new BufferedImage(this.width, this.height, BufferedImage.TYPE_4BYTE_ABGR));

		info = this.renderer.getBuffer(g, info);
		GraphConfig config = this.renderer.getConfig();
//		info.getTg().translate(-this.x + 2*TOGGLE + config.getLabelWidth(), -this.y + 2*TOGGLE + config.getLabelHeight());
//		info.getTg().translate(-this.x + 2*TOGGLE + config.getLabelWidth(), -this.y + 2*TOGGLE);
		info.getTg().translate(-this.x + config.getLabelWidth(), -this.y);

		return info;
	}

	@Override
	protected void drawBuf(Graphics2D g, BufferedImage buf)
	{
		GraphConfig config = this.renderer.getConfig();
//		g.drawImage(buf, this.x - 2*TOGGLE - config.getLabelWidth(), this.y - 2*TOGGLE - config.getLabelHeight(), null);
//		g.drawImage(buf, this.x - 2*TOGGLE - config.getLabelWidth(), this.y - 2*TOGGLE, null);
		g.drawImage(buf, this.x - config.getLabelWidth(), this.y, null);
	}

	@Override
	protected void drawSumiji(Graphics2D g, boolean printing)
	{
		this.renderer.drawAxisSumiji((Graphics2D) g, this.x, this.y, this.width, this.height);

		Stroke bStroke = g.getStroke();
		Color bColor = g.getColor();

		for (DataInfo dataInfo: this.dataList)
		{
			g.setStroke(dataInfo.lineStroke);
			g.setColor(dataInfo.lineColor);

			this.renderer.drawSumiji(g, this.x, this.y, this.width, this.height, dataInfo);
		}

		g.setStroke(bStroke);
		g.setColor(bColor);

		this.drawFrame(g, printing);
		if (false == printing)
		{
			this.renderer.drawAreaFrame(g, this.x, this.y, this.width, this.height);
		}
	}

	@Override
	protected void drawBraille(Graphics2D g, boolean printing)
	{
//		int dSize = Util.mmToPixel(0, (int) Config.getConfig(this.dotSize));
		int dSize = Util.mmToPixel(0, (int) Config.getConfig(Config.BRAILLE.SMALL));
		this.renderer.drawAxisBraille((Graphics2D) g, dSize, this.getDotSpan(), this.x, this.y, this.width, this.height);

		for (DataInfo dataInfo: this.dataList)
		{
			Stroke bStroke = g.getStroke();
			Color bColor = g.getColor();

			g.setStroke(dataInfo.brailleStroke);
			g.setColor(dataInfo.lineColor);
			this.renderer.drawSumiji(g, this.x, this.y, this.width, this.height, dataInfo);

			g.setStroke(bStroke);
			g.setColor(bColor);
		}

		this.drawFrame(g, printing);
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
			}
			else
			{
				if (this.contains(p))
				{
					hint.status = STATUS.MOVE;
					hint.point = p;
					return true;
				}
			}
		}
		else
		{
			return this.contains(p);
		}

		return false;
	}

	@Override
	public boolean hitTest(Rectangle rect)
	{
		return this.intersects(rect);
	}

	@Override
	public boolean showProperty(DesignPanel panel, Point p)
	{
		property.clear();

		GraphConfig graphConf = this.renderer.getConfig();

		property.setConfig(graphConf);
		property.setData(this.dataList);

		this.changed = false;
		property.setVisible(true);
		if (false == property.toUpdate())
		{
			return true;
		}

		property.getConfig(graphConf);

		this.dataList.clear();
		property.getData(this.dataList);
		if (0 < this.dataList.size())
		{
			this.calcPoint(true);
		}

		this.changed = true;
		this.notifyChanged();

		return true;
	}

	@Override
	public boolean resize(StatusHint hint, Point dp)
	{
		boolean ret = super.resize(hint, dp);

		if (0 != (hint.modifiers & InputEvent.SHIFT_MASK))
		{
			this.renderer.expand(this.x, this.y, this.width, this.height);
			this.calcPoint(false);
		}

		return ret;
	}

	private static class TAG
	{
		static final String CLASS = "class";
		static final String X = "x";
		static final String Y = "y";
		static final String WIDTH = "width";
		static final String HEIGHT = "height";
		static final String AXISCOLOR = "axisColor";
		static final String XAXIS_MIN = "xAxis.min";
		static final String XAXIS_MAX = "xAxis.max";
		static final String XAXIS_ORIGIN = "xAxis.origin";
		static final String YAXIS_MIN = "yAxis.min";
		static final String YAXIS_MAX = "yAxis.max";
		static final String YAXIS_ORIGIN = "yAxis.origin";
		static final String XSCALE_SHOW = "xScale.show";
		static final String XSCALE_START = "xScale.start";
		static final String XSCALE_STEP = "xScale.step";
		static final String XSCALE_WITHLINE = "xScale.withLine";
		static final String XSCALE_WITHLABEL = "xScale.withLabel";
		static final String YSCALE_SHOW = "yScale.show";
		static final String YSCALE_START = "yScale.start";
		static final String YSCALE_STEP = "yScale.step";
		static final String YSCALE_WITHLINE = "yScale.withLine";
		static final String YSCALE_WITHLABEL = "yScale.withLabel";
		static final String PADDING_LEFT = "padding.left";
		static final String PADDING_RIGHT = "padding.right";
		static final String PADDING_TOP = "padding.top";
		static final String PADDING_BOTTOM = "padding.bottom";
		static final String NAME = "name";
		static final String FUNCTYPE = "funcType";
		static final String FUNC = "func";
		static final String MATHML = "mathML";
		static final String USEAXISRANGE = "useAxisRange";
		static final String USENUMBER = "useNumber";
		static final String USEXRANGE = "useXRange";
		static final String MIN = "min";
		static final String MAX = "max";
		static final String PARAM = "param";
		static final String GRAPH_TYPE = "graph.type";
		static final String GRAPH_COLOR = "graph.color";
		static final String GRAPH_LINE_TYPE = "graph.lineType";
		static final String GRAPH_DOT_SIZE = "graph.dotSize";
		static final String GRAPH_BACK_LINE = "graph.backLine";
		static final String P_INDEX = "p.index";
		static final String P_X = "p.x";
		static final String P_Y = "p.y";
	}

	@Override
	public void export(ExportBase export)
	{
		ShapeGraph copy = this.clone();
		copy.resetOrigin(export.getOrigin());

		PairList descList = new PairList();
		descList.add(TAG.CLASS, this.getClass().getSimpleName());
		descList.add(TAG.X, copy.x);
		descList.add(TAG.Y, copy.y);
		descList.add(TAG.WIDTH, copy.width);
		descList.add(TAG.HEIGHT, copy.height);

		GraphConfig config = this.renderer.getConfig();
		descList.add(TAG.AXISCOLOR, Util.colorString(config.getAxisColor()));
		{
			Axis a = config.getXAxis();
			descList.add(TAG.XAXIS_MIN, (java.lang.Double)a.getMin());
			descList.add(TAG.XAXIS_MAX, (java.lang.Double)a.getMax());
			descList.add(TAG.XAXIS_ORIGIN, (java.lang.Double)a.getOrigin());
		}
		{
			Axis a = config.getYAxis();
			descList.add(TAG.YAXIS_MIN, (java.lang.Double)a.getMin());
			descList.add(TAG.YAXIS_MAX, (java.lang.Double)a.getMax());
			descList.add(TAG.YAXIS_ORIGIN, (java.lang.Double)a.getOrigin());
		}
		{
			GraphScale s = config.getXScale();
			descList.add(TAG.XSCALE_SHOW, s.getShow());
			descList.add(TAG.XSCALE_START, (java.lang.Double)s.getStart());
			descList.add(TAG.XSCALE_STEP, (java.lang.Double)s.getStep());
			descList.add(TAG.XSCALE_WITHLINE, (Boolean)s.withLine());
			descList.add(TAG.XSCALE_WITHLABEL, (Boolean)s.withLabel());
		}
		{
			GraphScale s = config.getYScale();
			descList.add(TAG.YSCALE_SHOW, s.getShow());
			descList.add(TAG.YSCALE_START, (java.lang.Double)s.getStart());
			descList.add(TAG.YSCALE_STEP, (java.lang.Double)s.getStep());
			descList.add(TAG.YSCALE_WITHLINE, (Boolean)s.withLine());
			descList.add(TAG.YSCALE_WITHLABEL, (Boolean)s.withLabel());
		}
		descList.add(TAG.PADDING_LEFT, (Integer)config.getLeft());
		descList.add(TAG.PADDING_RIGHT, (Integer)config.getRight());
		descList.add(TAG.PADDING_TOP, (Integer)config.getTop());
		descList.add(TAG.PADDING_BOTTOM, (Integer)config.getBottom());

		for (DataInfo data: this.dataList)
		{
			descList.add(TAG.NAME, data.getConfig().getName());
			descList.add(TAG.FUNCTYPE, data.getFuncType());
			descList.add(TAG.FUNC, data.getFuncText());
			descList.add(TAG.MATHML, data.getMathMLText());
			descList.add(TAG.USEAXISRANGE, (Boolean)data.isUseAxisRange());
			descList.add(TAG.USENUMBER, (Boolean)data.isUseNumber());
			descList.add(TAG.USEXRANGE, (Boolean)data.isUseXRange());
			descList.add(TAG.MIN, (java.lang.Double)data.getMin());
			descList.add(TAG.MAX, (java.lang.Double)data.getMax());
			descList.add(TAG.PARAM, (java.lang.Double)data.getParam());

			DataConfig d = data.getConfig();
			descList.add(TAG.GRAPH_TYPE, d.getType().name());
			descList.add(TAG.GRAPH_COLOR, Util.colorString(data.getLineColor()));
			descList.add(TAG.GRAPH_LINE_TYPE, data.lineType.name());
			descList.add(TAG.GRAPH_DOT_SIZE, data.dotSize.name());
			descList.add(TAG.GRAPH_BACK_LINE, Boolean.toString(data.isBackLine()));

			if (FUNC_TYPE.DATA == data.getFuncType())
			{
				for (int i = 0; i < d.getData().size(); i++)
				{
					Data<java.lang.Double, java.lang.Double> p = d.getData().get(i);
					descList.add(TAG.P_INDEX, (Integer)i);
					descList.add(TAG.P_X, p.getX());
					descList.add(TAG.P_Y, p.getY());
				}
			}
		}

		ShapeInfo info = new ShapeInfo(SHAPE.GRAPH);
		info.dotSize = copy.dotSize;
		info.dotSpan = copy.dotSpan;
		info.obj = copy;
		info.setDesc(descList);

		export.writeStart(info);
		export.write(info);
		export.writeEnd(info);
	}


	public static ShapeGraph parse(ShapeInfo info)
	{
		ShapeGraph obj = new ShapeGraph();
		GraphConfig config = new GraphConfig();
		obj.renderer.setConfig(config);
		DataInfo data = null;
		Data<java.lang.Double, java.lang.Double> p = null;

		PairList list = info.getDesc(null);

		for (Pair pair: list)
		{
			String val = (String) pair.getValue();
			switch(pair.getName())
			{
			case TAG.X:
				obj.base.x = java.lang.Double.parseDouble(val);
				break;
			case TAG.Y:
				obj.base.y = java.lang.Double.parseDouble(val);
				break;
			case TAG.WIDTH:
				obj.base.width = java.lang.Double.parseDouble(val);
				break;
			case TAG.HEIGHT:
				obj.base.height = java.lang.Double.parseDouble(val);
				break;
			case TAG.AXISCOLOR:
				config.setAxisColor(Util.getColor(val));
				break;
			case TAG.XAXIS_MIN:
				config.getXAxis().setMin(java.lang.Double.parseDouble(val));
				break;
			case TAG.XAXIS_MAX:
				config.getXAxis().setMax(java.lang.Double.parseDouble(val));
				break;
			case TAG.XAXIS_ORIGIN:
				config.getXAxis().setOrigin(java.lang.Double.parseDouble(val));
				break;
			case TAG.YAXIS_MIN:
				config.getYAxis().setMin(java.lang.Double.parseDouble(val));
				break;
			case TAG.YAXIS_MAX:
				config.getYAxis().setMax(java.lang.Double.parseDouble(val));
				break;
			case TAG.YAXIS_ORIGIN:
				config.getYAxis().setOrigin(java.lang.Double.parseDouble(val));
				break;
			case TAG.XSCALE_SHOW:
				config.getXScale().setShow(SHOW.fromName(val));
				break;
			case TAG.XSCALE_START:
				config.getXScale().setStart(java.lang.Double.parseDouble(val));
				break;
			case TAG.XSCALE_STEP:
				config.getXScale().setStep(java.lang.Double.parseDouble(val));
				break;
			case TAG.XSCALE_WITHLINE:
				config.getXScale().setWithLine(Boolean.parseBoolean(val));
				break;
			case TAG.XSCALE_WITHLABEL:
				config.getXScale().setWithLabel(Boolean.parseBoolean(val));
				break;
			case TAG.YSCALE_SHOW:
				config.getYScale().setShow(SHOW.fromName(val));
				break;
			case TAG.YSCALE_START:
				config.getYScale().setStart(java.lang.Double.parseDouble(val));
				break;
			case TAG.YSCALE_STEP:
				config.getYScale().setStep(java.lang.Double.parseDouble(val));
				break;
			case TAG.YSCALE_WITHLINE:
				config.getYScale().setWithLine(Boolean.parseBoolean(val));
				break;
			case TAG.YSCALE_WITHLABEL:
				config.getYScale().setWithLabel(Boolean.parseBoolean(val));
				break;
			case TAG.PADDING_LEFT:
				config.setLeft(Integer.parseInt(val));
				break;
			case TAG.PADDING_RIGHT:
				config.setRight(Integer.parseInt(val));
				break;
			case TAG.PADDING_TOP:
				config.setTop(Integer.parseInt(val));
				break;
			case TAG.PADDING_BOTTOM:
				config.setBottom(Integer.parseInt(val));
				break;
			case TAG.NAME:
				data = new DataInfo();
				obj.dataList.add(data);
				data.getConfig().setName(val);
				break;
			case TAG.FUNCTYPE:
				data.setFuncType(FUNC_TYPE.valueOf(val));
				break;
			case TAG.FUNC:
				data.setFuncText(val);
				break;
			case TAG.MATHML:
				data.setMathMLText(val);
				break;
			case TAG.USEAXISRANGE:
				data.setUseAxisRange(Boolean.parseBoolean(val));
				break;
			case TAG.USENUMBER:
				data.setUseNumber(Boolean.parseBoolean(val));
				break;
			case TAG.USEXRANGE:
				data.setUseXRange(Boolean.parseBoolean(val));
				break;
			case TAG.MIN:
				data.setMin(java.lang.Double.parseDouble(val));
				break;
			case TAG.MAX:
				data.setMax(java.lang.Double.parseDouble(val));
				break;
			case TAG.PARAM:
				data.setParam(java.lang.Double.parseDouble(val));
				break;
			case TAG.GRAPH_TYPE:
				data.getConfig().setType(GRAPH_TYPE.valueOf(val));
				break;
			case TAG.GRAPH_COLOR:
				data.setLineColor(Util.getColor(val));
				break;
			case TAG.GRAPH_LINE_TYPE:
				data.setLineType(LINE_TYPE.getValueOf(val));
				break;
			case TAG.GRAPH_DOT_SIZE:
				data.setLineSize(Config.BRAILLE.valueOf(val));
				break;
			case TAG.GRAPH_BACK_LINE:
				data.setBackLine(Boolean.getBoolean(val));
				break;
			case TAG.P_INDEX:
				p = new Data<java.lang.Double, java.lang.Double>();
				data.getConfig().getData().add(p);
				break;
			case TAG.P_X:
				p.setX(java.lang.Double.parseDouble(val));
				break;
			case TAG.P_Y:
				p.setY(java.lang.Double.parseDouble(val));
				break;
			}
		}
		obj.adaptFrame();
		for (DataInfo d: obj.getDataList())
		{
			d.setLineStroke();
		}
		obj.calcPoint(true);

		return obj;
	}
}
