package dssp.hashidate.misc;

import java.awt.BasicStroke;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.Arrays;

import dssp.hashidate.misc.FigureType.EDGE_TYPE;
import dssp.hashidate.misc.FigureType.LINE_TYPE;

public class LineStroke implements Stroke
{
	protected float[] dashArray;
	protected float lineSize = 1.0f;
	protected LINE_TYPE lineType = LINE_TYPE.SOLID;
	protected EDGE_TYPE edgeType = EDGE_TYPE.BUTT;

	public LineStroke(float size)
	{
		this(size, null, null);
	}

	public LineStroke(LINE_TYPE lineType)
	{
		this(0, lineType, null);
	}

	public LineStroke(float size, LINE_TYPE lineType)
	{
		this(size, lineType, null);
	}

	public LineStroke(float size, LINE_TYPE lineType, EDGE_TYPE edgeType)
	{
		this.setLineSize(size);
		this.setLineType(lineType);
		this.setEdgeType(edgeType);
	}

	public float getLineSize()
	{
		return lineSize;
	}

	public LINE_TYPE getLineType()
	{
		return lineType;
	}

	public EDGE_TYPE getEdgeType()
	{
		return edgeType;
	}

	public void setLineSize(float lineSize)
	{
		if (1.0f < lineSize)
		{
			this.lineSize = lineSize;
			this.setDash(this.lineType);
		}
	}

	public void setLineType(LINE_TYPE lineType)
	{
		if (null == lineType || lineType == this.lineType)
		{
			return;
		}
		this.lineType = lineType;

		this.setDash(lineType);
	}

	protected void setDash(LINE_TYPE lineType)
	{
		switch(lineType)
		{
		case SOLID:
			this.dashArray = null;
			break;
		case DOT:
			this.dashArray = new float[2];
			this.dashArray[0] = this.lineSize;
			this.dashArray[1] = 2*this.lineSize;
			break;
		case DASHED:
			this.dashArray = new float[2];
			this.dashArray[0] = 5;
			this.dashArray[1] = 5;
			break;
		}
	}

	public void setEdgeType(EDGE_TYPE edgeType)
	{
		if (null != edgeType)
		{
			this.edgeType = edgeType;
		}
	}

	protected static float SMALL_ARROW = 1.0f;
	protected static float BIG_ARROW = 1.5f;

	@Override
	public Shape createStrokedShape(Shape shape)
	{
		BasicStroke stroke = new BasicStroke(this.lineSize, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1.0f, this.dashArray, 0.0f);
		GeneralPath path = new GeneralPath(stroke.createStrokedShape(shape));

		float[] first = null;
		float[][] prev = {new float[6], new float[6]};
		float[] coords = new float[6];
		boolean flag = true;
		int type = 0;
		int preType = 0;
	    for (PathIterator it = shape.getPathIterator(null); !it.isDone(); it.next())
	    {
	    	preType = type;

    		type = it.currentSegment(coords);
    		float ratio = 0;
	    	switch(type)
	    	{
	    	case PathIterator.SEG_MOVETO:
	    		first = Arrays.copyOf(coords, coords.length);
	    		break;
	    	case PathIterator.SEG_LINETO:
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

	    return path;
	}

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

		path.moveTo(lx, ly);
		path.lineTo(ex, ey);
		path.lineTo(rx, ry);
		path.closePath();
	}
}
