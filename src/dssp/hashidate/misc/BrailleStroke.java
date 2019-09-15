package dssp.hashidate.misc;

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Arrays;

import dssp.hashidate.misc.FigureType.EDGE_TYPE;
import dssp.hashidate.misc.FigureType.LINE_TYPE;

/**
 * 点図用Stroke
 *
 * @author yagi
 *
 */
public class BrailleStroke extends LineStroke
{
	protected float dotSpan;
	protected boolean front;

	protected static float SMALL_ARROW = 1.5f;
	protected static float BIG_ARROW = 3.0f;

	/**
	 *
	 * @param size 点の大きさ
	 * @param dotSpan 点の間隔
	 * @param lineType 線種
	 * @param edgeType 端点の形
	 * @param front true=表点 false=裏点
	 */
	public BrailleStroke(float size, float dotSpan, LINE_TYPE lineType, EDGE_TYPE edgeType, boolean front)
	{
		super(size, lineType, edgeType);

		this.dotSpan = dotSpan;
		this.front = front;
	}

	protected void drawDot(GeneralPath path, Point2D.Float p)
	{
		this.drawDot(path, p.x, p.y);
	}

	protected void drawDot(GeneralPath path, float x, float y)
	{
		Ellipse2D.Float obj = new Ellipse2D.Float(x - this.lineSize/2, y - this.lineSize/2, this.lineSize, this.lineSize);
		path.append(obj, false);
	}

	protected float Coef(int n, int m, float t)
	{
		if (n < m)
		{
			throw new IllegalArgumentException("n < m");
		}

		float c;
		if (0 == m || n == m)
		{
			c = 1.0f;
		}
		else
		{
			float v1 = 1.0f;
			for (int i = (n-m+1); i <= n; ++i)
			{
				v1 *= (float)i;
			}
			float v2 = 1.0f;
			for (int i = 1; i <= m; ++i)
			{
				v2 *= (float)i;
			}
			c = v1/v2;
		}
		float v = c*(float)(Math.pow(t, m) * Math.pow(1-t,n-m));

		return v;
	}

	@Override
	public Shape createStrokedShape(Shape shape)
	{
		GeneralPath path = new GeneralPath();

		float[] first = null;
		float[][] prev = {new float[6], new float[6]};
		float[] coords = new float[6];
		boolean flag = true;
		int type = 0;
		int preType = 0;

		double dd = this.dotSpan;
		Point2D.Float prevP = new Point2D.Float();
		Point2D.Float cP = new Point2D.Float();
		Point2D.Float dP = new Point2D.Float();
		Point2D.Float[] mP = {new Point2D.Float(), new Point2D.Float()};
		float dist = 0;
		int nP;
		Point2D.Float[] tP;

		int count = 0;
		int[] cindex = null;
		switch(this.lineType)
		{
		case DOT:
			cindex = new int[2];
			cindex[0] = 2;
			cindex[1] = 4;
			break;
		case DASHED:
			cindex = new int[1];
			cindex[0] = 2;
			break;
		default:
		}
	    for (PathIterator it = shape.getPathIterator(null); !it.isDone(); it.next())
	    {
	    	preType = type;

    		type = it.currentSegment(coords);
    		float ratio = 0;
	    	switch(type)
	    	{
	    	case PathIterator.SEG_MOVETO:
	    		first = Arrays.copyOf(coords, coords.length);
	    		prevP.setLocation(coords[0], coords[1]);
	    		this.drawDot(path, prevP);
	    		break;
	    	case PathIterator.SEG_LINETO:
	    		cP.setLocation(coords[0], coords[1]);
	    		dP.setLocation(cP.x - prevP.x, cP.y - prevP.y);
	    		dist = (float) Math.sqrt(dP.x * dP.x + dP.y * dP.y);
	    		while (dist > dd)
	    		{
	    			prevP.setLocation(dd*dP.x/dist + prevP.x, dd*dP.y/dist + prevP.y);
	    			boolean drawFlag = true;
	    			if (null != cindex)
	    			{
	    				for (int ci: cindex)
	    				{
	    					if (count == ci)
	    					{
	    						drawFlag = false;
	    						break;
	    					}
	    				}
	    			}
	    			if (drawFlag)
	    			{
	    				this.drawDot(path, prevP);
	    			}
		    		dP.setLocation(cP.x - prevP.x, cP.y - prevP.y);
		    		dist = (float) Math.sqrt(dP.x * dP.x + dP.y * dP.y);
	    			dd = this.dotSpan;
	    			if (null != cindex)
	    			{
	    				count = (count == cindex[cindex.length-1] ? 0: count+1);
	    			}
	    		}
	    		prevP.setLocation(cP);
	    		dd = dd - dist;
	    		switch(this.edgeType)
	    		{
	    		case LARROW:
	    		case BARROW:
	    			ratio = SMALL_ARROW;
	    			break;
	    		case LARROW1:
	    		case BARROW1:
	    			ratio = BIG_ARROW;
	    			break;
	    		default:
	    		}
	    		break;
	    	case PathIterator.SEG_CLOSE:
	    		cP.setLocation(first[0], first[1]);
	    		dP.setLocation(cP.x - prevP.x, cP.y - prevP.y);
	    		dist = (float) Math.sqrt(dP.x * dP.x + dP.y * dP.y);
	    		while (dist > dd)
	    		{
	    			prevP.setLocation(dd*dP.x/dist + prevP.x, dd*dP.y/dist + prevP.y);
	    			boolean drawFlag = true;
	    			if (null != cindex)
	    			{
	    				for (int ci: cindex)
	    				{
	    					if (count == ci)
	    					{
	    						drawFlag = false;
	    						break;
	    					}
	    				}
	    			}
	    			if (drawFlag)
	    			{
	    				this.drawDot(path, prevP);
	    			}
		    		dP.setLocation(cP.x - prevP.x, cP.y - prevP.y);
		    		dist = (float) Math.sqrt(dP.x * dP.x + dP.y * dP.y);
	    			dd = this.dotSpan;
	    			if (null != cindex)
	    			{
	    				count = (count == cindex[cindex.length-1] ? 0: count+1);
	    			}
	    		}
	    		prevP.setLocation(cP);
	    		dd = dd - dist;
	    		switch(this.edgeType)
	    		{
	    		case LARROW:
	    		case BARROW:
	    			ratio = SMALL_ARROW;
	    			break;
	    		case LARROW1:
	    		case BARROW1:
	    			ratio = BIG_ARROW;
	    			break;
	    		default:
	    		}
	    		break;
	    	case PathIterator.SEG_CUBICTO:
	    		mP[0].setLocation(coords[0], coords[1]);
	    		mP[1].setLocation(coords[2], coords[3]);
	    		cP.setLocation(coords[4], coords[5]);
	    		dP.setLocation(cP.x - prevP.x, cP.y - prevP.y);
	    		dist = (float) Math.sqrt(dP.x * dP.x + dP.y * dP.y);
	    		nP = (int)(dist/this.dotSpan);
	    		tP = new Point2D.Float[nP+1];
	    		tP[0] = new Point2D.Float(prevP.x, prevP.y);
	    		for (int i = 1; i < nP; ++i)
	    		{
	    			float t = (float)i / (float)nP;
	    			float[] C = {Coef(3,0,t), Coef(3,1,t), Coef(3,2,t), Coef(3,3,t)};
	    			tP[i] = new Point2D.Float();
	    			tP[i].x = C[0]*prevP.x + C[1]*mP[0].x + C[2]*mP[1].x + C[3]*cP.x;
	    			tP[i].y = C[0]*prevP.y + C[1]*mP[0].y + C[2]*mP[1].y + C[3]*cP.y;
	    		}
	    		tP[nP] = new Point2D.Float(cP.x, cP.y);

	    		for (int i = 1; i <= nP; ++i)
	    		{
		    		cP.setLocation(tP[i].x, tP[i].y);
		    		dP.setLocation(cP.x - prevP.x, cP.y - prevP.y);
		    		dist = (float) Math.sqrt(dP.x * dP.x + dP.y * dP.y);
		    		while (dist > dd)
		    		{
		    			prevP.setLocation(dd*dP.x/dist + prevP.x, dd*dP.y/dist + prevP.y);
		    			boolean drawFlag = true;
		    			if (null != cindex)
		    			{
		    				for (int ci: cindex)
		    				{
		    					if (count == ci)
		    					{
		    						drawFlag = false;
		    						break;
		    					}
		    				}
		    			}
		    			if (drawFlag)
		    			{
		    				this.drawDot(path, prevP);
		    			}
			    		dP.setLocation(cP.x - prevP.x, cP.y - prevP.y);
			    		dist = (float) Math.sqrt(dP.x * dP.x + dP.y * dP.y);
		    			dd = this.dotSpan;
		    			if (null != cindex)
		    			{
		    				count = (count == cindex[cindex.length-1] ? 0: count+1);
		    			}
		    		}
		    		prevP.setLocation(cP);
		    		dd = dd - dist;
	    		}

	    		switch(this.edgeType)
	    		{
	    		case LARROW:
	    		case BARROW:
	    			ratio = SMALL_ARROW;
	    			break;
	    		case LARROW1:
	    		case BARROW1:
	    			ratio = BIG_ARROW;
	    			break;
	    		default:
	    		}
	    		break;
	    	case PathIterator.SEG_QUADTO:
	    		mP[0].setLocation(coords[0], coords[1]);
	    		cP.setLocation(coords[2], coords[3]);
	    		dP.setLocation(cP.x - prevP.x, cP.y - prevP.y);
	    		dist = (float) Math.sqrt(dP.x * dP.x + dP.y * dP.y);
	    		nP = (int)(dist/this.dotSpan);
	    		tP = new Point2D.Float[nP+1];
	    		tP[0] = new Point2D.Float(prevP.x, prevP.y);
	    		for (int i = 1; i < nP; ++i)
	    		{
	    			float t = (float)i / (float)nP;
	    			float[] C = {Coef(2,0,t), Coef(2,1,t), Coef(2,2,t)};
	    			tP[i] = new Point2D.Float();
	    			tP[i].x = C[0]*prevP.x + C[1]*mP[0].x + C[2]*cP.x;
	    			tP[i].y = C[0]*prevP.y + C[1]*mP[0].y + C[2]*cP.y;
	    		}
	    		tP[nP] = new Point2D.Float(cP.x, cP.y);

	    		for (int i = 1; i <= nP; ++i)
	    		{
		    		cP.setLocation(tP[i].x, tP[i].y);
		    		dP.setLocation(cP.x - prevP.x, cP.y - prevP.y);
		    		dist = (float) Math.sqrt(dP.x * dP.x + dP.y * dP.y);
		    		while (dist > dd)
		    		{
		    			prevP.setLocation(dd*dP.x/dist + prevP.x, dd*dP.y/dist + prevP.y);
		    			boolean drawFlag = true;
		    			if (null != cindex)
		    			{
		    				for (int ci: cindex)
		    				{
		    					if (count == ci)
		    					{
		    						drawFlag = false;
		    						break;
		    					}
		    				}
		    			}
		    			if (drawFlag)
		    			{
		    				this.drawDot(path, prevP);
		    			}
			    		dP.setLocation(cP.x - prevP.x, cP.y - prevP.y);
			    		dist = (float) Math.sqrt(dP.x * dP.x + dP.y * dP.y);
		    			dd = this.dotSpan;
		    			if (null != cindex)
		    			{
		    				count = (count == cindex[cindex.length-1] ? 0: count+1);
		    			}
		    		}
		    		prevP.setLocation(cP);
		    		dd = dd - dist;
	    		}

	    		switch(this.edgeType)
	    		{
	    		case LARROW:
	    		case BARROW:
	    			ratio = SMALL_ARROW;
	    			break;
	    		case LARROW1:
	    		case BARROW1:
	    			ratio = BIG_ARROW;
	    			break;
	    		default:
	    		}
	    		break;
	    	default:
	    	}
	    	if (flag && 0 < ratio)
	    	{
    			this.addArrow(path, coords[0], coords[1], prev[0][0], prev[0][1], ratio);
    			flag = false;
	    	}

	    	if (PathIterator.SEG_CLOSE != type)
	    	{
	    		for (int i = 0; i < coords.length; ++i)
	    		{
	    			prev[1][i] = prev[0][i];
	    			prev[0][i] = coords[i];
	    		}
	    	}
	    }

	    float ratio = 0;
	    switch(this.edgeType)
	    {
	    case RARROW:
	    case BARROW:
	    	ratio = SMALL_ARROW;
	    	break;
	    case RARROW1:
	    case BARROW1:
	    	ratio = BIG_ARROW;
	    	break;
	    default:
		}
	    if (0 < ratio)
	    {
	    	switch(type)
	    	{
	    	case PathIterator.SEG_LINETO:
				this.addArrow(path, prev[1][0], prev[1][1], coords[0], coords[1], ratio);
	    		break;
	    	case PathIterator.SEG_CLOSE:
	    		if (shape instanceof Rectangle)
	    		{
	    			this.addArrow(path, prev[1][0], prev[1][1], prev[0][0], prev[0][1], ratio);
	    		}
	    		else
	    		{
	    			switch(preType)
	    			{
	    			case PathIterator.SEG_LINETO:
		    			this.addArrow(path, prev[0][0], prev[0][1], first[0], first[1], ratio);
		    			break;
	    			case PathIterator.SEG_CUBICTO:
		    			this.addArrow(path, prev[0][4], prev[0][5], first[0], first[1], ratio);
	    				break;
	    			case PathIterator.SEG_QUADTO:
		    			this.addArrow(path, prev[0][2], prev[0][2], first[0], first[1], ratio);
	    				break;
	    			}
	    		}
	    		break;
	    	case PathIterator.SEG_CUBICTO:
				this.addArrow(path, coords[2], coords[3], coords[4], coords[5], ratio);
	    		break;
	    	case PathIterator.SEG_QUADTO:
				this.addArrow(path, coords[0], coords[1], coords[2], coords[3], ratio);
	    		break;
	    	}
	    }

	    if (front)
	    {
	    	return path;
	    }
	    else
	    {
	    	BasicStroke stroke = new BasicStroke(1.0f);
	    	return new GeneralPath(stroke.createStrokedShape(path));
	    }
	}

	@Override
	protected void addArrow(GeneralPath path, float sx, float sy, float ex, float ey, float ratio)
	{
		float w = this.lineSize + 4*ratio;
		float h = 10*ratio;

		float Vx= ex - sx;
		float Vy= ey - sy;
		float v = (float) Math.sqrt(Vx*Vx+Vy*Vy);
		float Ux= Vx/v;
		float Uy= Vy/v;

		float lx = ex - Uy*w - Ux*h;
		float ly = ey + Ux*w - Uy*h;
		float rx = ex + Uy*w - Ux*h;
		float ry = ey - Ux*w - Uy*h;

		float dd = this.dotSpan;
		Point2D.Float prevP = new Point2D.Float(ex, ey);
		Point2D.Float cP = new Point2D.Float(lx, ly);
		Point2D.Float dP = new Point2D.Float(cP.x - prevP.x, cP.y - prevP.y);
		float dist = (float) Math.sqrt(dP.x*dP.x + dP.y*dP.y);
		while (dist > dd)
		{
			prevP.setLocation(dd*dP.x/dist + prevP.x, dd*dP.y/dist + prevP.y);
			this.drawDot(path, prevP);
			dP.setLocation(cP.x - prevP.x, cP.y - prevP.y);
			dist = (float) Math.sqrt(dP.x*dP.x + dP.y*dP.y);
			dd = this.dotSpan;
		}

		dd = this.dotSpan;
		prevP.setLocation(ex, ey);
		cP.setLocation(rx, ry);
		dP.setLocation(cP.x - prevP.x, cP.y - prevP.y);
		dist = (float) Math.sqrt(dP.x*dP.x + dP.y*dP.y);
		while (dist > dd)
		{
			prevP.setLocation(dd*dP.x/dist + prevP.x, dd*dP.y/dist + prevP.y);
			this.drawDot(path, prevP);
			dP.setLocation(cP.x - prevP.x, cP.y - prevP.y);
			dist = (float) Math.sqrt(dP.x*dP.x + dP.y*dP.y);
			dd = this.dotSpan;
		}
	}
}
