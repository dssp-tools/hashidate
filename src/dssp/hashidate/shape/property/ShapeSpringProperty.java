package dssp.hashidate.shape.property;
import javax.swing.JPanel;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JSpinner;

import java.awt.BorderLayout;

import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;

public class ShapeSpringProperty extends ShapeProperty
{
	private static ShapeProperty instance;
	private JSpinner barLen;
	private JSpinner nHelix;
	private JSpinner radius;
	private JCheckBox useBackPoint;
	public static ShapeSpringProperty getInstance()
	{
		if (null == instance)
		{
			instance = new ShapeSpringProperty();
		}
		return (ShapeSpringProperty) instance;
	}

	protected ShapeSpringProperty()
	{
		super();

		JPanel panel = new JPanel();
		tabbedPane.addTab("バネ", null, panel, null);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 30, 0, 100));
		panel.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new GridLayout(0, 2, 5, 10));

		JLabel label = new JLabel("巻き数");
		label.setHorizontalAlignment(SwingConstants.TRAILING);
		panel_1.add(label);

		nHelix = new JSpinner();
		nHelix.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		panel_1.add(nHelix);

		JLabel label_1 = new JLabel("取っ手の長さ");
		label_1.setHorizontalAlignment(SwingConstants.TRAILING);
		panel_1.add(label_1);

		barLen = new JSpinner();
		barLen.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		panel_1.add(barLen);

		JLabel lblNewLabel = new JLabel("半径");
		lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		panel_1.add(lblNewLabel);

		radius = new JSpinner();
		radius.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		panel_1.add(radius);

		JLabel lblNewLabel_1 = new JLabel("奥の点");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.TRAILING);
		panel_1.add(lblNewLabel_1);

		useBackPoint = new JCheckBox("裏点にする");
		useBackPoint.setToolTipText("奥の点を裏点にする場合は、注意書きをすること");
		panel_1.add(useBackPoint);
	}

	public void setNHelix(int val)
	{
		this.nHelix.setValue(val);
	}

	public int getNHelix()
	{
		return (int) this.nHelix.getValue();
	}

	public void setBarLen(int val)
	{
		this.barLen.setValue(val);
	}

	public int getBarLen()
	{
		return (int) this.barLen.getValue();
	}

	public void setRadius(int val)
	{
		this.radius.setValue(val);
	}

	public int getRadius()
	{
		return (int) this.radius.getValue();
	}

	public void setUseBackPoint(boolean flag)
	{
		this.useBackPoint.setSelected(flag);
	}

	public boolean useBackPoint()
	{
		return this.useBackPoint.isSelected();
	}
}
