package dssp.hashidate.shape.property.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import dssp.brailleLib.Util;
import dssp.hashidate.config.Config;
import dssp.hashidate.misc.ColorButton;
import dssp.hashidate.misc.FigureType.LINE_TYPE;
import dssp.hashidate.shape.ShapeGraph.DataInfo;
import dssp.hashidate.shape.ShapeGraph.FUNC_TYPE;
import dssp.hashidate.shape.helper.FormulaHandler;
import dssp.hashidate.shape.helper.GraphRenderer.GRAPH_TYPE;

public class GraphFuncProperty extends GraphPropertyBase
{
	private JTextField rangeMin;
	private JTextField rangeMax;
	private JTextField rangeParam;
	private JTextArea funcText;
	private JPanel funcImage;
	private JTable constTable;

	private ButtonGroup rangeGroup = new ButtonGroup();
	private ButtonGroup paramGroup = new ButtonGroup();
	private JTextArea mathMLText;
	private JRadioButton useXRange;
	private JRadioButton useNumber;
	private JCheckBox useAxisRange;
	private JRadioButton useYRange;
	private JRadioButton useStep;

	public GraphFuncProperty(String name)
	{
		super(name);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel panel_9 = new JPanel();
		panel_9.setBorder(new EmptyBorder(0, 5, 0, 0));
		add(panel_9);
		panel_9.setLayout(new BoxLayout(panel_9, BoxLayout.X_AXIS));

		JLabel lblNewLabel = new JLabel("関数 f(x) TeXまたはMathML  ");
		panel_9.add(lblNewLabel);

		JPanel panel_4 = new JPanel();
		FlowLayout flowLayout_1 = (FlowLayout) panel_4.getLayout();
		flowLayout_1.setAlignment(FlowLayout.LEFT);
		flowLayout_1.setVgap(0);
		panel_9.add(panel_4);

		graphType = new JComboBox<GRAPH_TYPE>();
		panel_4.add(graphType);
		graphType.setPreferredSize(new Dimension(80, 19));

		JPanel panel_10 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_10.getLayout();
		flowLayout.setAlignment(FlowLayout.RIGHT);
		flowLayout.setVgap(0);
		flowLayout.setHgap(0);
		panel_9.add(panel_10);

		JButton btnRename = new JButton("名前変更");
		btnRename.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				rename();
			}
		});
		panel_10.add(btnRename);

		JButton btnDelete = new JButton("　削除　");
		panel_10.add(btnDelete);
		btnDelete.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				removeThis();
			}
		});
		btnDelete.setToolTipText("このグラフを削除する");

		JPanel panel_15 = new JPanel();
		add(panel_15);
		panel_15.setLayout(new GridLayout(0, 2, 0, 0));

		JPanel panel_2 = new JPanel();
		panel_15.add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));

		JPanel panel_3 = new JPanel();
		panel_2.add(panel_3, BorderLayout.CENTER);
		panel_3.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setPreferredSize(new Dimension(2, 220));
		scrollPane_1.setMinimumSize(new Dimension(23, 100));
		panel_3.add(scrollPane_1, BorderLayout.CENTER);

		funcText = new JTextArea();
		funcText.setToolTipText("");
		funcText.getDocument().addDocumentListener(new DocumentListener()
		{

			@Override
			public void changedUpdate(DocumentEvent arg0)
			{
				makeMathML();
			}

			@Override
			public void insertUpdate(DocumentEvent arg0)
			{
				makeMathML();
			}

			@Override
			public void removeUpdate(DocumentEvent arg0)
			{
				makeMathML();
			}

		});
		scrollPane_1.setViewportView(funcText);

		JPanel panel_1 = new JPanel();
		panel_3.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setToolTipText("関数に使う定数（例：円周率、光の速さ）");
		scrollPane.setPreferredSize(new Dimension(2, 120));
		panel_1.add(scrollPane);

		constTable = new JTable();
		constTable.setModel(new DefaultTableModel(new Object[][] {},
				new String[] { "\u5B9A\u6570\u540D", "\u6570\u5024" }));
		scrollPane.setViewportView(constTable);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(0, 5, 0, 0));
		panel_1.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JLabel lblNewLabel_1 = new JLabel("定数　");
		panel.add(lblNewLabel_1);

		JButton btnNewButton = new JButton("追加");
		btnNewButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				DefaultTableModel model = (DefaultTableModel) constTable.getModel();
				String[] row = { "", "" };
				model.addRow(row);
			}
		});
		panel.add(btnNewButton);

		JButton btnNewButton_1 = new JButton("削除");
		btnNewButton_1.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int index = constTable.getSelectedRow();
				DefaultTableModel model = (DefaultTableModel) constTable.getModel();
				model.removeRow(index);
			}
		});
		panel.add(btnNewButton_1);

		JPanel panel_7 = new JPanel();
		panel_15.add(panel_7);
		panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.Y_AXIS));

		JPanel panel_8 = new JPanel();
		panel_7.add(panel_8);
		panel_8.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane_2 = new JScrollPane();
		panel_8.add(scrollPane_2);
		scrollPane_2.setPreferredSize(new Dimension(2, 80));

		mathMLText = new JTextArea();
		mathMLText.setBackground(new Color(245, 245, 245));
		mathMLText.setEditable(false);
		scrollPane_2.setViewportView(mathMLText);

		funcImage = new JPanel()
		{
			@Override
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				redrawFuncImage(g);
			}
		};
		panel_7.add(funcImage);
		funcImage.setBackground(Color.WHITE);
		funcImage.setPreferredSize(new Dimension(10, 120));

		JPanel panel_13 = new JPanel();
		add(panel_13);
		panel_13.setBorder(new TitledBorder(null, "\u8A08\u7B97\u306E\u5C5E\u6027", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		panel_13.setLayout(new BoxLayout(panel_13, BoxLayout.X_AXIS));

		JPanel panel_5 = new JPanel();
		panel_13.add(panel_5);
		panel_5.setBorder(new EmptyBorder(0, 10, 0, 10));
		panel_5.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

		useXRange = new JRadioButton("xの範囲");
		useXRange.setSelected(true);
		panel_5.add(useXRange);

		useYRange = new JRadioButton("yの範囲");
		panel_5.add(useYRange);
		this.rangeGroup.add(useYRange);

		useAxisRange = new JCheckBox("軸の範囲を使う");
		useAxisRange.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent arg0)
			{
				switchUsedRange(arg0.getStateChange() == ItemEvent.SELECTED);
			}
		});
		useAxisRange.setSelected(true);
		panel_5.add(useAxisRange);

		JLabel lblNewLabel_2 = new JLabel("　最小");
		panel_5.add(lblNewLabel_2);

		rangeMin = new JTextField();
		rangeMin.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(FocusEvent e)
			{
				setFocus((JTextField) e.getSource());
			}
		});
		rangeMin.setHorizontalAlignment(SwingConstants.TRAILING);
		rangeMin.setEnabled(false);
		rangeMin.setText("0");
		panel_5.add(rangeMin);
		rangeMin.setColumns(5);

		JLabel lblNewLabel_3 = new JLabel("　最大");
		panel_5.add(lblNewLabel_3);

		rangeMax = new JTextField();
		rangeMax.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(FocusEvent e)
			{
				setFocus((JTextField) e.getSource());
			}
		});
		rangeMax.setHorizontalAlignment(SwingConstants.TRAILING);
		rangeMax.setEnabled(false);
		rangeMax.setText("10");
		panel_5.add(rangeMax);
		rangeMax.setColumns(5);

		this.rangeGroup.add(useXRange);

		JPanel panel_6 = new JPanel();
		panel_13.add(panel_6);
		panel_6.setBorder(new EmptyBorder(0, 10, 0, 0));
		panel_6.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));

		useNumber = new JRadioButton("点の数");
		useNumber.setSelected(true);
		panel_6.add(useNumber);

		useStep = new JRadioButton("間隔");
		panel_6.add(useStep);

		rangeParam = new JTextField();
		rangeParam.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusGained(FocusEvent e)
			{
				setFocus((JTextField) e.getSource());
			}
		});
		rangeParam.setHorizontalAlignment(SwingConstants.TRAILING);
		rangeParam.setText("50");
		rangeParam.setColumns(10);
		panel_6.add(rangeParam);
		this.paramGroup.add(useNumber);
		this.paramGroup.add(useStep);

		JPanel panel_11 = new JPanel();
		add(panel_11);
		panel_11.setLayout(new BorderLayout(0, 0));

		JPanel panel_12 = new JPanel();
		panel_12.setBorder(
				new TitledBorder(null, "\u7DDA\u306E\u8A2D\u5B9A", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_11.add(panel_12);
		panel_12.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
		//		panel_13.setLayout(new BoxLayout((Container) null, BoxLayout.Y_AXIS));

		JPanel panel_14 = new JPanel();
		panel_14.setBorder(new EmptyBorder(0, 10, 0, 0));
		panel_12.add(panel_14);
		panel_14.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

		JLabel label = new JLabel("色");
		panel_14.add(label);

		lineColor = new ColorButton("");
		lineColor.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				selectColor();
			}
		});
		lineColor.setPreferredSize(new Dimension(33, 20));
		panel_14.add(lineColor);

		JPanel panel_16 = new JPanel();
		panel_14.add(panel_16);

		JLabel label_1 = new JLabel("線の太さ");
		panel_16.add(label_1);

		JRadioButton radioButton = new JRadioButton("細線");
		panel_16.add(radioButton);
		this.lineSize.add(radioButton);

		JRadioButton radioButton_1 = new JRadioButton("通常");
		panel_16.add(radioButton_1);
		this.lineSize.add(radioButton_1);

		JRadioButton radioButton_2 = new JRadioButton("太線");
		panel_16.add(radioButton_2);
		this.lineSize.add(radioButton_2);

		JPanel panel_17 = new JPanel();
		panel_17.setBorder(new EmptyBorder(0, 10, 0, 0));
		panel_12.add(panel_17);
		panel_17.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

		JLabel label_2 = new JLabel("線種");
		panel_17.add(label_2);

		lineType = new JComboBox<ImageIcon>();
		panel_17.add(lineType);

		backLine = new JCheckBox("裏線");
		panel_17.add(backLine);

		this.init();
	}

	private void init()
	{
		this.mathMLText.setText("MathML");

		for (LINE_TYPE type : LINE_TYPE.values())
		{
			this.lineType.addItem(type.getIcon());
		}

		int selectedIndex = 0;
		int index = 0;
		for (GRAPH_TYPE type : GRAPH_TYPE.values())
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

	private void setFocus(JTextField field)
	{
		field.setSelectionStart(0);
		field.setSelectionEnd(field.getText().length());
	}

	private void switchUsedRange(boolean use)
	{
		if (null != this.rangeMin)
		{
			this.rangeMin.setEnabled(!use);
		}
		if (null != this.rangeMax)
		{
			this.rangeMax.setEnabled(!use);
		}
	}

	Image image;
	private JComboBox<GRAPH_TYPE> graphType;
	private ColorButton lineColor;
	private JComboBox<ImageIcon> lineType;
	private JCheckBox backLine;
	private ButtonGroup lineSize = new ButtonGroup();

	private void redrawFuncImage(Graphics g)
	{
		if (null != this.image)
		{
			int x = (this.funcImage.getWidth() - image.getWidth(this)) / 2;
			int y = (this.funcImage.getHeight() - image.getHeight(this)) / 2;
			g.drawImage(this.image, x, y, this);
		}
	}

	@Override
	public void setData(DataInfo info)
	{
		super.setData(info);

		this.funcText.setText(this.dataInfo.getFuncText());

		if (this.dataInfo.isUseXRange())
		{
			this.useXRange.setSelected(true);
		} else
		{
			this.useYRange.setSelected(true);
		}
		//		this.useXRange.setSelected(this.dataInfo.isUseXRange());
		this.useAxisRange.setSelected(this.dataInfo.isUseAxisRange());
		this.rangeMin.setText(Double.toString(this.dataInfo.getMin()));
		this.rangeMax.setText(Double.toString(this.dataInfo.getMax()));

		if (this.dataInfo.isUseNumber())
		{
			this.useNumber.setSelected(true);
		} else
		{
			this.useStep.setSelected(true);
		}
		if (this.dataInfo.isUseNumber())
		{
			this.rangeParam.setText(Integer.toString((int) this.dataInfo.getParam()));
		} else
		{
			this.rangeParam.setText(Double.toString(this.dataInfo.getParam()));
		}

		DefaultTableModel model = (DefaultTableModel) this.constTable.getModel();
		int nrow = model.getRowCount();
		for (int i = (nrow - 1); i >= 0; i--)
		{
			model.removeRow(i);
		}
		Map<String, Double> table = this.dataInfo.getConstTable();
		for (String name : table.keySet())
		{
			double val = table.get(name);
			Object[] row = { name, val };
			model.addRow(row);
		}

		int index = LINE_TYPE.getIndex(info.getLineType());
		this.lineType.setSelectedIndex(index);

		this.lineColor.setPaintColor(info.getLineColor());

		int s = 0;
		switch (info.getLineSize())
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
				break;
			}
			++index;
		}

		this.backLine.setSelected(info.isBackLine());
	}

	@Override
	public DataInfo getData()
	{
		DataInfo info = this.dataInfo.clone();

		info.setFuncType(FUNC_TYPE.FUNCTION);
		info.getConfig().setType(GRAPH_TYPE.LINE);

		info.setFuncText(this.funcText.getText());
		info.setMathMLText(this.mathMLText.getText());

		info.setRange(this.useXRange.isSelected(), this.useAxisRange.isSelected(),
				Double.parseDouble(this.rangeMin.getText()), Double.parseDouble(this.rangeMax.getText()));
		info.setParam(this.useNumber.isSelected(), Double.parseDouble(this.rangeParam.getText()));

		DefaultTableModel model = (DefaultTableModel) this.constTable.getModel();
		@SuppressWarnings("unchecked")
		Vector<Vector<?>> values = model.getDataVector();
		Map<String, Double> table = info.getConstTable();
		table.clear();
		for (Vector<?> row : values)
		{
			String name = row.get(0).toString();
			String val = row.get(1).toString();
			table.put(name, Double.parseDouble(val));
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
				switch (index)
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

	private void makeMathML()
	{
		FormulaHandler fh = FormulaHandler.getInstance();
		String text = this.funcText.getText().trim();
		String mathML = null;
		mathML = fh.TeXtoMathML(text, true, "utf-8", 20, Font.ITALIC);
		if (null == mathML)
		{
			mathML = text;
		}

		this.mathMLText.setText(mathML);
		this.mathMLText.setCaretPosition(0);

		this.image = fh.getImageForMathML(mathML, ((Font) Config.getConfig(Config.FONT.FORMULA)).getFamily());
		this.funcImage.repaint();
	}

	@Override
	public boolean checkGraph()
	{
		String funcText = this.funcText.getText();
		if (0 == funcText.length())
		{
			Util.notify("関数が入力されていません");
			return false;
		}
		String mathML = this.mathMLText.getText();
		if (0 == mathML.length())
		{
			Util.notify("関数がMathMLに変換できていません");
			return false;
		}

		String min = this.rangeMin.getText();
		if (0 == min.length())
		{
			Util.notify("計算範囲の最小値が入力されていません");
			return false;
		}
		String max = this.rangeMax.getText();
		if (0 == max.length())
		{
			Util.notify("計算範囲の最小値が入力されていません");
			return false;
		}
		String param = this.rangeParam.getText();
		if (0 == param.length())
		{
			Util.notify("点の数か、間隔が入力されていません");
			return false;
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
