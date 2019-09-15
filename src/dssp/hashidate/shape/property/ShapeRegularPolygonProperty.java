package dssp.hashidate.shape.property;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;

public class ShapeRegularPolygonProperty extends ShapeProperty
{
	private static ShapeProperty instance;
	private JSpinner nCorner;

	public static ShapeRegularPolygonProperty getInstance()
	{
		if (null == instance)
		{
			instance = new ShapeRegularPolygonProperty();
		}
		return (ShapeRegularPolygonProperty) instance;
	}

	public ShapeRegularPolygonProperty()
	{

		JPanel panel = new JPanel();
		tabbedPane.addTab("正多角形", null, panel, null);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 50, 0, 100));
		panel.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new GridLayout(1, 0, 10, 0));

		JLabel label = new JLabel("角数");
		label.setHorizontalAlignment(SwingConstants.TRAILING);
		panel_1.add(label);

		nCorner = new JSpinner();
		nCorner.setModel(new SpinnerNumberModel(new Integer(3), new Integer(3), null, new Integer(1)));
		panel_1.add(nCorner);
		
		this.tabbedPane.setSelectedIndex(1);
	}

	public void setNCorner(int val)
	{
		this.nCorner.setValue(val);
	}

	public int getNCorner()
	{
		return (int) this.nCorner.getValue();
	}
}
