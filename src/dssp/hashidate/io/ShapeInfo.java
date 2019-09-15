package dssp.hashidate.io;

import java.awt.Font;
import java.awt.Point;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.EnumMap;
import java.util.List;

import dssp.hashidate.config.Config;
import dssp.hashidate.misc.PairList;
import dssp.hashidate.shape.DesignObject;
import dssp.hashidate.shape.SHAPE;
import dssp.hashidate.shape.ShapeCircle;
import dssp.brailleLib.BrailleDict;
import dssp.brailleLib.BrailleInfo;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class ShapeInfo
{
//	public static enum TYPE {SHAPE, CIRCLE, ELLIPSE, POLYLINE, POLYGON, SPLINE, RECTANGLE, TEXT, FORMULA, GROUP};
//	private TYPE type;
	private SHAPE type;

	// 共通
	public String desc = "";
	public int x = 0;
	public int y = 0;
	public int width = 0;
	public int height = 0;

	// Shape用
	public DesignObject obj = null;

	// ShapePolyline用
	public int[] xPoints = null;
	public int[] yPoints = null;

	// Spline用
	public List<Path2D.Double> pathList = null;

	// ShapeCircle用
	public EnumMap<ShapeCircle.ANGLE, Integer> angle = null;
	public Arc2D arc;

	// テキスト/数式用
	public String text = null;
	public String mathML = null;
	public Font font = null;
	public Point base = new Point();
	public Point2D.Double scale = new Point2D.Double(1,1);
	public BrailleDict dict = null;

	// 点図・点字用
	public List<BrailleInfo> brailleList = null;
	public Config.BRAILLE dotSize;
	public float dotSpan;
	public int dotSpaceX;
	public int dotSpaceY;
	public int boxSpace;
	public int lineSpace;

	private PairList descList;

	public ShapeInfo(SHAPE type)
	{
		this.type = type;
	}

	public void setType(SHAPE type)
	{
		this.type = type;
	}

	public SHAPE getType()
	{
		return this.type;
	}

	public void setDesc(PairList list)
	{
		this.descList = list;
	}

	public void setDesc(String text)
	{
		this.descList = PairList.fromString(text);
	}

	/**
	 * SVGのdescを取得する<br/>
	 * ・objがnullまたはPairListの場合はPairListのリスト返す<br/>
	 * ・objが文字列の場合はPairList.toString()を返す
	 *
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T getDesc(T obj)
	{
		T ret = null;
		if (null == obj || obj instanceof PairList)
		{
			ret = (T) this.descList;
		}
		else if (obj instanceof String)
		{
			if (null == this.descList)
			{
				ret = (T) this.desc;
			}
			else
			{
				ret = (T) this.descList.toString();
			}
		}

		return ret;
	}
}
