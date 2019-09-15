package dssp.hashidate.misc;

import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class CheckBoxCellRenderer extends DefaultTableCellRenderer
{
	public static final int ROW_MARGIN = 5;
	private int horizontalAlignment = SwingConstants.LEFT;
	private JCheckBox renderer = new JCheckBox();
	public CheckBoxCellRenderer(int horizontalAlignment)
	{
		this.horizontalAlignment = horizontalAlignment;
		renderer.setHorizontalAlignment(this.horizontalAlignment);
	}
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
//		return super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
		TableCellRenderer r = table.getCellRenderer(row, 0);
		JLabel l = (JLabel)r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		renderer.setBackground(l.getBackground());

		Boolean flag = (Boolean) value;
		if (flag)
		{
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			int nRow = model.getRowCount();
			for (int i = 0; i < nRow; i++)
			{
				if (row != i)
				{
					model.setValueAt(false, i, column);
				}
			}
		}
		renderer.setSelected(flag);

		return renderer;
	}
	@Override
	protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
	}
}
