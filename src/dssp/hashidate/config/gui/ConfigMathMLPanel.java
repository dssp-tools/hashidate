package dssp.hashidate.config.gui;

import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.JButton;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import dssp.brailleLib.Util;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.ListSelectionModel;

import dssp.hashidate.misc.HorizontalAlignmentHeaderRenderer;
import dssp.hashidate.misc.StringCellRenderer;

public class ConfigMathMLPanel extends JPanel
{
	private JTable table;
	private static final int ROW_HEIGHT = 26;

	public ConfigMathMLPanel() {
		setOpaque(false);
		setBorder(new EmptyBorder(20, 25, 5, 17));
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		add(panel, BorderLayout.EAST);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{57, 0};
		gbl_panel.rowHeights = new int[]{21, 21, 0};
		gbl_panel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JButton btnNewButton = new JButton("追加");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addFile();
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.WEST;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 0);
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 0;
		panel.add(btnNewButton, gbc_btnNewButton);

		JButton btnNewButton_1 = new JButton("削除");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delFile();
			}
		});
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.anchor = GridBagConstraints.WEST;
		gbc_btnNewButton_1.gridx = 0;
		gbc_btnNewButton_1.gridy = 1;
		panel.add(btnNewButton_1, gbc_btnNewButton_1);

		JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setOpaque(false);
		panel_1.add(scrollPane);

		table = new JTable();
		table.setOpaque(false);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setModel(new DefaultTableModel(
				new Object[][] {
				},
				new String[] {
						"辞書ファイル"
				}
				) {
			Class<?>[] columnTypes = new Class[] {
					String.class
			};
			public Class<?> getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		scrollPane.setViewportView(table);

		table.getColumnModel().getColumn(0).setHeaderRenderer(new HorizontalAlignmentHeaderRenderer(SwingConstants.CENTER));
		table.getColumnModel().getColumn(0).setCellRenderer(new StringCellRenderer());
		table.setRowHeight(ROW_HEIGHT);

		JLabel lblNewLabel = new JLabel("辞書を更新した場合、次回の起動で使用されます");
		lblNewLabel.setBorder(new EmptyBorder(5, 20, 5, 0));
		add(lblNewLabel, BorderLayout.SOUTH);
	}

	private void addFile()
	{
		DefaultTableModel model = (DefaultTableModel) this.table.getModel();
		String[] row = {"新規ファイル"};
		model.addRow(row);
	}

	private void delFile()
	{
		int index = this.table.getSelectedRow();
		if (0 > index)
		{
			return;
		}
		DefaultTableModel model = (DefaultTableModel) this.table.getModel();
		model.removeRow(index);
	}

	public void setFiles(List<String> list)
	{
		if (null == list)
		{
			throw new IllegalArgumentException("リストがnull");
		}

		DefaultTableModel model = (DefaultTableModel) this.table.getModel();
		int nrow = model.getRowCount();
		for (int i = (nrow-1); i >= 0; i--)
		{
			model.removeRow(i);
		}
		for (String file: list)
		{
			String[] row = {file};
			model.addRow(row);
		}
	}

	public List<String> getFiles()
	{
		List<String> list = Util.newArrayList();

		DefaultTableModel model = (DefaultTableModel) this.table.getModel();
		int nrow = model.getRowCount();
		for (int i = 0; i < nrow; i++)
		{
			String val = (String) model.getValueAt(i, 0);
			list.add(val);
		}

		return list;
	}
}
