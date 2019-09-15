package dssp.hashidate.io;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public abstract class ExportBase
{
	protected File file = null;
	protected Rectangle area;
	public Point getOrigin()
	{
		return new Point(this.area.x, this.area.y);
	}

	public abstract boolean init(Rectangle area, File file, boolean newFile);
	public abstract File end();
	public abstract boolean write(ShapeInfo info);

	public void writeStart(ShapeInfo info){};
	public void writeEnd(ShapeInfo info){};
	public void openGroup(ShapeInfo info){};
	public void closeGroup(ShapeInfo info){};


}
