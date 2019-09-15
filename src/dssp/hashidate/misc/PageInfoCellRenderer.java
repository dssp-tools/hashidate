package dssp.hashidate.misc;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public class PageInfoCellRenderer implements TableCellRenderer
{
	private static EmptyBorder border = new EmptyBorder(0, 5, 0, 0);
	public PageInfoCellRenderer()
	{
	}
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		TableCellRenderer r = table.getDefaultRenderer(String.class);
		JLabel l = (JLabel)r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		l.setBorder(border);

		if (isSelected)
		{
			l.setForeground(Color.WHITE);
		}
		else
		{
			l.setForeground(Color.BLACK);
		}
		switch(column)
		{
		case 0:
			String name = value.toString();
			TableModel model = table.getModel();
			int nRow = model.getRowCount();
			for (int i = 0; i < nRow; i++)
			{
				if (i != row)
				{
					String buf = model.getValueAt(i, 0).toString();
					if (buf.equals(name))
					{
						l.setForeground(Color.RED);
						break;
					}
				}
			}
			break;
		case 1:
		case 2:
			try
			{
				int val = Integer.parseInt(value.toString());
				if (1 > val)
				{
					l.setForeground(Color.red);
				}
			}
			catch (NumberFormatException ex)
			{
				l.setForeground(Color.red);
			}
			break;
		}

		return l;
	}
}
