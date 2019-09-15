package dssp.hashidate.shape.helper;

import java.awt.FontMetrics;
import java.awt.Point;

import dssp.hashidate.DesignPanel;
import dssp.hashidate.ObjectManager;
import dssp.hashidate.io.ShapeInfo;
import dssp.hashidate.misc.ObjectUndoManager;
import dssp.hashidate.shape.DesignObject;
import dssp.hashidate.shape.SHAPE;
import dssp.hashidate.shape.ShapeCircle;
import dssp.hashidate.shape.ShapeGraph;
import dssp.hashidate.shape.ShapeGroup;
import dssp.hashidate.shape.ShapeImage;
import dssp.hashidate.shape.ShapePolyline;
import dssp.hashidate.shape.ShapeRectangle;
import dssp.hashidate.shape.ShapeRegularPolygon;
import dssp.hashidate.shape.ShapeSpline;
import dssp.hashidate.shape.ShapeSpring;
import dssp.hashidate.shape.ShapeText;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class ObjectFactory
{
	private static ObjectFactory instance = null;
	private static ObjectManager objMan = null;
	private static DesignPanel panel = null;

	public static final ObjectFactory getInstance()
	{
		if (null == instance)
		{
			instance = new ObjectFactory();
		}

		return instance;
	}

	public void set(ObjectManager objMan, DesignPanel panel)
	{
		ObjectFactory.objMan = objMan;
		ObjectFactory.panel = panel;
	}

	public DesignObject newObject(DesignObject.StatusHint hint, SHAPE type, Point p)
	{
		DesignObject obj = null;
		switch(type)
		{
		case RECTANGLE:
			obj = new ShapeRectangle(p);
			break;
		case CIRCLE:
		case ELLIPSE:
			obj = new ShapeCircle(p, type);
			break;
		case LINE:
		case POLYLINE:
		case POLYGON:
			obj = new ShapePolyline(hint, p, type);
			break;
		case REGULAR_POLYGON:
			obj = new ShapeRegularPolygon(hint, p, type);
			break;
		case SPLINE:
		case SPLINE_LOOP:
			obj = new ShapeSpline(hint, p, type);
			break;
		case SPRING:
			obj = new ShapeSpring(hint, p);
			break;
		case TEXT:
			obj = new ShapeText(p);
			break;
		case GRAPH:
			obj = new ShapeGraph(p);
			break;
		case IMAGE:
			obj = new ShapeImage(p);
			break;
		case GROUP:
			obj = new ShapeGroup();
			break;
		default:
			return null;
		}
		if (false == obj.init(ObjectFactory.panel))
		{
			return null;
		}
		obj.setObjMan(ObjectFactory.objMan);
		int index = ObjectFactory.objMan.addObject(obj);
		if (SHAPE.GROUP != type)
		{
			ObjectUndoManager.getInstance().add(new ObjectUndoManager.EditInfo(index, obj));
		}

		return obj;
	}

	public DesignObject buildObject(SHAPE type, ShapeInfo info)
	{
		DesignObject obj = null;
		switch(type)
		{
		case RECTANGLE:
			obj = ShapeRectangle.parse(info);;
			break;
		case CIRCLE:
		case ELLIPSE:
			obj = ShapeCircle.parse(info);
			break;
		case LINE:
		case POLYGON:
		case POLYLINE:
			obj = ShapePolyline.parse(info);
			break;
		case REGULAR_POLYGON:
			obj = ShapeRegularPolygon.parse(info);
			break;
		case SPLINE:
		case SPLINE_LOOP:
			obj = ShapeSpline.parse(info);
			break;
		case SPRING:
			obj = ShapeSpring.parse(info);;
			break;
		case TEXT:
		case FORMULA:
			FontMetrics fontMetrics = ObjectFactory.panel.getFontMetrics(info.font);
			obj = ShapeText.parse(info, fontMetrics);
			break;
		case GRAPH:
			obj = ShapeGraph.parse(info);
			break;
		case IMAGE:
			obj = ShapeImage.parse(info);
			break;
		case GROUP:
			obj = new ShapeGroup();
			break;
		default:
			return null;
		}
		obj.setObjMan(ObjectFactory.objMan);

		return obj;
	}
}
