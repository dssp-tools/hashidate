package dssp.hashidate.misc;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

public class StringCellRenderer implements TableCellRenderer
{
	private static EmptyBorder border = new EmptyBorder(0, 5, 0, 0);
	public StringCellRenderer()
	{
	}
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		TableCellRenderer r = table.getDefaultRenderer(String.class);
		JLabel l = (JLabel)r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		l.setBorder(border);
		return l;
	}
}
