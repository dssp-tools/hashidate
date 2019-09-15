package dssp.hashidate.shape.property.graph;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import dssp.brailleLib.Util;
import dssp.hashidate.shape.ShapeGraph.DataInfo;
import dssp.hashidate.shape.property.ShapeGraphProperty;

public abstract class GraphPropertyBase extends JPanel
{
	ShapeGraphProperty parent;
	DataInfo dataInfo = new DataInfo();

	/**
	 * Create the panel.
	 */
	public GraphPropertyBase(String name)
	{
		this.dataInfo.getConfig().setName(name);
	}

	public void setData(DataInfo info)
	{
		this.dataInfo = info;
	}

	public abstract DataInfo getData();

	protected void removeThis()
	{
		JTabbedPane parent = (JTabbedPane) this.getParent();
		int index = parent.getSelectedIndex();
		parent.setSelectedIndex(index-1);
		parent.remove(this);
	}

	protected void rename()
	{
		String name = Util.select4("名前変更", this.dataInfo.getConfig().getName(), true, "新しい名前を入力してください");
		if (null == name)
		{
			return;
		}

		JTabbedPane parent = (JTabbedPane) this.getParent();
		int index = parent.getSelectedIndex();
		parent.setTitleAt(index, name);

		this.dataInfo.getConfig().setName(name);
	}

	public abstract boolean checkGraph();
}
