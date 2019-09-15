package dssp.hashidate.shape.property.graph;

import dssp.brailleLib.Util;
import dssp.hashidate.shape.ShapeGraph.DataInfo;
import dssp.hashidate.shape.ShapeGraph.FUNC_TYPE;
import dssp.hashidate.shape.helper.GraphRenderer.Data;
import dssp.hashidate.shape.helper.GraphRenderer.DataConfig;
import dssp.hashidate.shape.helper.GraphRenderer.GRAPH_TYPE;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.awt.FlowLayout;

import javax.swing.JComboBox;

import java.awt.Dimension;

import javax.swing.border.TitledBorder;
import javax.swing.border.EmptyBorder;

import dssp.hashidate.config.Config;
import dssp.hashidate.misc.ColorButton;
import dssp.hashidate.misc.FigureType.LINE_TYPE;
import javax.swing.JRadioButton;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.BoxLayout;

public class GraphDataProperty extends GraphPropertyBase
{
	private JTable table;
	private JComboBox<GRAPH_TYPE> graphType;
	private ColorButton lineColor;
	private JComboBox<ImageIcon> lineType;
	private JCheckBox backLine;
	private ButtonGroup lineSize = new ButtonGroup();
	private JButton addRow;
	private JButton delRow;
	private JButton insertRow;
	public GraphDataProperty(String name)
	{
		super(name);

		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(0, 5, 0, 0));
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JLabel label = new JLabel("データ    ");
		panel.add(label);

		JPanel panel_7 = new JPanel();
		FlowLayout flowLayout_2 = (FlowLayout) panel_7.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEFT);
		flowLayout_2.setVgap(0);
		panel.add(panel_7);

		graphType = new JComboBox<GRAPH_TYPE>();
		panel_7.add(graphType);
		graphType.setPreferredSize(new Dimension(80, 19));

		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);
		panel.add(panel_1);

		JButton btnNewButton_1 = new JButton("名前変更");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				rename();
			}
		});
		panel_1.add(btnNewButton_1);

		JButton btnNewButton = new JButton("　削除　");
		panel_1.add(btnNewButton);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				removeThis();
			}
		});

		JPanel panel_2 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_2.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		add(panel_2, BorderLayout.SOUTH);

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(null, "\u7DDA\u306E\u8A2D\u5B9A", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.add(panel_3);
		panel_3.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new EmptyBorder(0, 10, 0, 0));
		panel_3.add(panel_4);
		panel_4.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

		JLabel label_1 = new JLabel("色");
		panel_4.add(label_1);

		lineColor = new ColorButton("");
		lineColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectColor();
			}
		});
		lineColor.setPreferredSize(new Dimension(33, 20));
		panel_4.add(lineColor);

		JPanel panel_5 = new JPanel();
		panel_4.add(panel_5);

		JLabel label_2 = new JLabel("線の太さ");
		panel_5.add(label_2);

		JRadioButton radioButton = new JRadioButton("細線");
		panel_5.add(radioButton);
		this.lineSize.add(radioButton);

		JRadioButton radioButton_1 = new JRadioButton("通常");
		panel_5.add(radioButton_1);
		this.lineSize.add(radioButton_1);

		JRadioButton radioButton_2 = new JRadioButton("太線");
		panel_5.add(radioButton_2);
		this.lineSize.add(radioButton_2);

		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new EmptyBorder(0, 10, 0, 0));
		panel_3.add(panel_6);
		panel_6.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

		JLabel label_3 = new JLabel("線種");
		panel_6.add(label_3);

		lineType = new JComboBox<ImageIcon>();
		panel_6.add(lineType);

		backLine = new JCheckBox("裏線");
		panel_6.add(backLine);

		JPanel panel_9 = new JPanel();
		add(panel_9, BorderLayout.CENTER);
		panel_9.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel_9.add(scrollPane);

		table = new JTable();
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				switch(arg0.getKeyCode())
				{
				case KeyEvent.VK_V:
					if (arg0.isControlDown())
					{
						paste();
						arg0.consume();
					}
					break;
//				case KeyEvent.VK_DELETE:
//					delRow();
//					arg0.consume();
//					break;
				}
			}
		});
		table.setModel(new DefaultTableModel(
				new Object[][] {
						//										{null, null},
				},
				new String[] {
						"\u6A2A\u8EF8\u5024", "\u7E26\u8EF8\u5024"
				}
				));
		scrollPane.setViewportView(table);

		JPanel panel_10 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_10.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEADING);
		panel_9.add(panel_10, BorderLayout.NORTH);

		addRow = new JButton("データ追加");
		panel_10.add(addRow);

		insertRow = new JButton("データ挿入");
		insertRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				insertRow();
			}
		});
		panel_10.add(insertRow);

		delRow = new JButton("データ削除");
		panel_10.add(delRow);
		delRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delRow();
			}
		});
		addRow.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addRow();
			}
		});

		this.init();
	}

	private void init()
	{
		for (LINE_TYPE type: LINE_TYPE.values())
		{
			this.lineType.addItem(type.getIcon());
		}

//		DefaultTableModel model = (DefaultTableModel) this.table.getModel();
//		model.addTableModelListener(new TableModelListener()
//		{
//			@Override
//			public void tableChanged(TableModelEvent arg0)
//			{
//				if (TableModelEvent.UPDATE == arg0.getType())
//				{
//					int rowIndex = arg0.getLastRow();
//					addRow(rowIndex);
//				}
//			}
//		});

		int selectedIndex = 0;
		int index = 0;
		for (GRAPH_TYPE type: GRAPH_TYPE.values())
		{
			this.graphType.addItem(type);
			if (type == this.dataInfo.getConfig().getType())
			{
				selectedIndex = index;
			}
			index++;
		}
		this.graphType.setSelectedIndex(selectedIndex);

		this.setData(this.dataInfo);
	}

//	private void addRow(int rowIndex)
//	{
//		DefaultTableModel model = (DefaultTableModel) table.getModel();
//		if (rowIndex == (model.getRowCount()-1))
//		{
//			String[] row = {"",""};
//			model.addRow(row);
//		}
//	}

	private void addRow()
	{
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		String[] row = {"",""};
		model.addRow(row);
	}

	private void insertRow()
	{
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		int index = this.table.getSelectedRow();
		String[] row = {"",""};
		if (0 > index)
		{
			model.insertRow(0, row);
		}
		else
		{
			model.insertRow(index, row);
		}
	}

	private void delRow()
	{
		DefaultTableModel model = (DefaultTableModel) this.table.getModel();
		int[] indexes = this.table.getSelectedRows();
		int nIndex = indexes.length;
		for (int i = (nIndex-1); i >= 0; i--)
		{
			int index = indexes[i];
			model.removeRow(index);
		}
	}

	private void paste()
	{
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		try
		{
			if (cb.isDataFlavorAvailable(DataFlavor.stringFlavor))
			{
				int index = this.table.getSelectedRow();

				String text = (String) cb.getData(DataFlavor.stringFlavor);

				DefaultTableModel model = (DefaultTableModel) this.table.getModel();
				String[] rows = text.split("\n");
				for (int i = 0; i < rows.length; i++)
				{
					String[] cells = rows[i].split("\t");
					model.insertRow(i + index, cells);
				}
			}
		}
		catch (UnsupportedFlavorException | IOException e)
		{
			Util.logException(e);
		}
	}

	@Override
	public DataInfo getData()
	{
		DataInfo info = this.dataInfo.clone();

		info.setFuncType(FUNC_TYPE.DATA);
		info.getConfig().setType(GRAPH_TYPE.LINE);

		DataConfig dataConf = info.getConfig();
		List<Data<Double, Double>> dataList = dataConf.getData();
		dataList.clear();

		DefaultTableModel model = (DefaultTableModel) this.table.getModel();
//		int nRow = model.getRowCount()-1;
		int nRow = model.getRowCount();
		int nCol = model.getColumnCount();
		String[] cells = new String[nCol];
		try
		{
			for (int i = 0; i < nRow; i++)
			{
				for (int j = 0; j < nCol; j++)
				{
					cells[j] = (String) model.getValueAt(i, j);
				}
				double x = Double.parseDouble(cells[0]);
				double y = Double.parseDouble(cells[1]);
				Data<Double, Double> data = new Data<Double,Double>(x, y);
				dataList.add(data);
			}

			int index = this.lineType.getSelectedIndex();
			info.setLineType(LINE_TYPE.getType(index));

			info.setLineColor(this.lineColor.getPaintColor());

			index = 0;
			Enumeration<AbstractButton> e = this.lineSize.getElements();
			while (e.hasMoreElements())
			{
				AbstractButton b = e.nextElement();
				if (b.isSelected())
				{
					switch(index)
					{
					case 0:
						info.setLineSize(Config.BRAILLE.SMALL);
						break;
					case 2:
						info.setLineSize(Config.BRAILLE.LARGE);
						break;
					default:
						info.setLineSize(Config.BRAILLE.MIDDLE);
						break;
					}
				}
				++index;
			}

			info.setBackLine(this.backLine.isSelected());

			info.setLineStroke();

			return info;
		}
		catch (NumberFormatException ex)
		{

		}

		return null;
	}

	@Override
	public void setData(DataInfo info)
	{
		super.setData(info);

		DefaultTableModel model = (DefaultTableModel) this.table.getModel();
		int nRow = model.getRowCount()-1;
		for (int i = 0; i < nRow; i++)
		{
			model.removeRow(0);
		}

		DataConfig dataConf = info.getConfig();
		List<Data<Double, Double>> dataList = dataConf.getData();
		int index = 0;
		for (Data<Double, Double> data: dataList)
		{
			String[] row = {Double.toString(data.getX()), Double.toString(data.getY())};
			model.insertRow(index, row);
			index++;
		}

		index = LINE_TYPE.getIndex(info.getLineType());
		this.lineType.setSelectedIndex(index);

		this.lineColor.setPaintColor(info.getLineColor());

		int s = 0;
		switch(info.getLineSize())
		{
		case SMALL:
			s = 0;
			break;
		case LARGE:
			s = 2;
			break;
		default:
			s = 1;
		}

		index = 0;
		Enumeration<AbstractButton> e = this.lineSize.getElements();
		while (e.hasMoreElements())
		{
			AbstractButton b = e.nextElement();
			if (index == s)
			{
				b.setSelected(true);
				return;
			}
			++index;
		}

		this.backLine.setSelected(info.isBackLine());
	}

	@Override
	public boolean checkGraph()
	{
		DefaultTableModel model = (DefaultTableModel) this.table.getModel();
		int nRow = model.getRowCount()-1;
		int nCol = model.getColumnCount();
		for (int i = 0; i < nRow; i++)
		{
			for (int j = 0; j < nCol; j++)
			{
				String val = (String) model.getValueAt(i, j);
				if (null == val || 0 == val.length())
				{
					Util.notify("データが無いか、一部抜けています。");
					return false;
				}
			}
		}
		return true;
	}

	private void selectColor()
	{
		Color color = ColorButton.showColorChooser("線の色", this.lineColor.getPaintColor());
		if (null != color)
		{
			this.lineColor.setPaintColor(color);
		}
	}
}
