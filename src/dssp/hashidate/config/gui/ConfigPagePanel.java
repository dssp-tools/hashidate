package dssp.hashidate.config.gui;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dssp.brailleLib.Util;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.ListSelectionModel;

import dssp.hashidate.config.PageInfo;
import dssp.hashidate.misc.CheckBoxCellRenderer;
import dssp.hashidate.misc.HorizontalAlignmentHeaderRenderer;
import dssp.hashidate.misc.PageInfoCellRenderer;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class ConfigPagePanel extends JPanel
{
	private JTable pageTable;
	private static final int ROW_HEIGHT = 26;

	public ConfigPagePanel() {
		setOpaque(false);
		setBorder(new EmptyBorder(20, 20, 20, 20));
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setOpaque(false);
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setOpaque(false);
		panel.add(scrollPane);

		pageTable = new JTable();
		pageTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		pageTable.setModel(new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
						"名称", "幅 [mm]", "高さ [mm]", "デフォルト"
				}
				) {
			Class<?>[] columnTypes = new Class<?>[] {
					String.class, Integer.class, Integer.class, Boolean.class
			};
			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		scrollPane.setViewportView(pageTable);

		JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		panel_1.setBorder(new EmptyBorder(0, 5, 0, 0));
		add(panel_1, BorderLayout.EAST);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[]{57, 0};
		gbl_panel_1.rowHeights = new int[]{21, 21, 0};
		gbl_panel_1.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel_1.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_1.setLayout(gbl_panel_1);

		JButton button = new JButton("追加");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addRow();
			}
		});
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.anchor = GridBagConstraints.WEST;
		gbc_button.insets = new Insets(0, 0, 5, 0);
		gbc_button.gridx = 0;
		gbc_button.gridy = 0;
		panel_1.add(button, gbc_button);

		JButton button_1 = new JButton("削除");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delRow();
			}
		});
		GridBagConstraints gbc_button_1 = new GridBagConstraints();
		gbc_button_1.anchor = GridBagConstraints.WEST;
		gbc_button_1.gridx = 0;
		gbc_button_1.gridy = 1;
		panel_1.add(button_1, gbc_button_1);

		pageTable.getColumnModel().getColumn(0).setHeaderRenderer(new HorizontalAlignmentHeaderRenderer(SwingConstants.CENTER));
		PageInfoCellRenderer renderer = new PageInfoCellRenderer();
		for (int i = 0; i < 3; i++)
		{
			pageTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}
		pageTable.getColumnModel().getColumn(3).setCellRenderer(new CheckBoxCellRenderer(SwingConstants.CENTER));
		pageTable.setRowHeight(ROW_HEIGHT);
	}

	public void setPageInfo(Collection<PageInfo> list, PageInfo defPage)
	{
		DefaultTableModel model = (DefaultTableModel)pageTable.getModel();
		int nRow = model.getRowCount();
		for (int i = (nRow-1); i >=0; i--)
		{
			model.removeRow(i);
		}

		Set<PageInfo> set = Util.newTreeSet();
		set.addAll(list);
		for (PageInfo info: set)
		{
			boolean flag = false;
			if (info.equals(defPage))
			{
				flag = true;
			}
			Object[] row = {info.getName(), info.getWidth(), info.getHeight(), flag};
			model.addRow(row);
		}
	}

	public List<PageInfo> getPageInfo()
	{
		List<PageInfo> list = Util.newArrayList();

		DefaultTableModel model = (DefaultTableModel)pageTable.getModel();
		int nRow = model.getRowCount();
		for (int i = 0; i < nRow; i++)
		{
			String name = (String) model.getValueAt(i, 0);
			int w = (Integer) model.getValueAt(i, 1);
			int h = (Integer) model.getValueAt(i, 2);
			PageInfo info = new PageInfo(name, w, h);
			list.add(info);
		}

		return list;
	}

	public int getDefault()
	{
		int index = -1;
		DefaultTableModel model = (DefaultTableModel)pageTable.getModel();
		int nRow = model.getRowCount();
		for (int i = 0; i < nRow; i++)
		{
			boolean flag = (Boolean) model.getValueAt(i, 3);
			if (flag)
			{
				index = i;
			}
		}

		return index;
	}

	private void addRow()
	{
		String name = "新規";
		DefaultTableModel model = (DefaultTableModel)pageTable.getModel();
		int nRow = model.getRowCount();
		int index = 0;
		for (int i = 0; i < nRow; i++)
		{
			String buf = model.getValueAt(i, 0).toString();
			if (buf.startsWith(name))
			{
				try
				{
					String num = buf.substring(name.length());
					int tmp = Integer.parseInt(num);
					index = Math.max(index, tmp);
				}
				catch (NumberFormatException ex)
				{
				}
			}
		}
		if (0 < index)
		{
			name = String.format("%s%d", name, index+1);
		}

		Object[] row = {name, 1, 1, false};
		model.addRow(row);

		int selIndex = model.getRowCount()-1;
		this.pageTable.getSelectionModel().setSelectionInterval(selIndex, selIndex);;
	}

	private void delRow()
	{
		int[] list = this.pageTable.getSelectedRows();
		if (0 == list.length)
		{
			return;
		}
		DefaultTableModel model = (DefaultTableModel)pageTable.getModel();
		for (int i = (list.length-1); i>= 0; i--)
		{
			int index = list[i];
			model.removeRow(index);
		}
	}
}
