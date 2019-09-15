package dssp.hashidate.shape.property;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JTextField;

import dssp.brailleLib.Util;
import dssp.hashidate.shape.ShapeGraph.DataInfo;
import dssp.hashidate.shape.ShapeGraph.FUNC_TYPE;
import dssp.hashidate.shape.helper.GraphRenderer.Axis;
import dssp.hashidate.shape.helper.GraphRenderer.GraphConfig;
import dssp.hashidate.shape.helper.GraphRenderer.GraphScale;
import dssp.hashidate.shape.helper.GraphRenderer.GraphScale.SHOW;
import dssp.hashidate.shape.property.graph.GraphDataProperty;
import dssp.hashidate.shape.property.graph.GraphFuncProperty;
import dssp.hashidate.shape.property.graph.GraphPropertyBase;

import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JCheckBox;

import java.awt.Component;

import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.awt.Color;
import java.awt.SystemColor;
import javax.swing.border.EtchedBorder;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class ShapeGraphProperty extends JDialog
{

	private final JPanel contentPanel = new JPanel();
	private JTextField xAxisMin;
	private JTextField xAxisMax;
	private JTextField yAxisMin;
	private JTextField yAxisMax;
	private JTextField xAxisOrigin;
	private JTextField yAxisOrigin;
	private JTabbedPane graphTab;

	private boolean added = false;
	private int prevSelectedIndex = -1;

	/**
	 * Create the dialog.
	 */
	public ShapeGraphProperty()
	{
		setTitle("グラフ属性");
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowOpened(WindowEvent arg0)
			{
				update = false;
			}
		});
		setModal(true);
		setBounds(100, 100, 745, 700);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			panel.setBackground(SystemColor.control);
			panel.setBorder(new EmptyBorder(5, 0, 5, 0));
			contentPanel.add(panel, BorderLayout.NORTH);
			panel.setLayout(new GridLayout(0, 1, 0, 5));
			{
				JPanel panel_1 = new JPanel();
				panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
				panel.add(panel_1);
				panel_1.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel_2 = new JPanel();
					panel_2.setBorder(new EmptyBorder(0, 5, 0, 0));
					panel_1.add(panel_2, BorderLayout.CENTER);
					panel_2.setLayout(new GridLayout(0, 1, 0, 0));
					{
						JPanel panel_2_1 = new JPanel();
						panel_2.add(panel_2_1);
						panel_2_1.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
						{
							JLabel lblNewLabel = new JLabel("範囲　　最小");
							panel_2_1.add(lblNewLabel);
						}
						{
							xAxisMin = new JTextField();
							xAxisMin.addFocusListener(new FocusAdapter() {
								@Override
								public void focusGained(FocusEvent arg0) {
									setFocus((JTextField) arg0.getComponent());
								}
							});
							xAxisMin.setHorizontalAlignment(SwingConstants.TRAILING);
							panel_2_1.add(xAxisMin);
							xAxisMin.setColumns(7);
						}
						{
							JLabel lblNewLabel_2 = new JLabel("　最大");
							panel_2_1.add(lblNewLabel_2);
						}
						{
							xAxisMax = new JTextField();
							xAxisMax.addFocusListener(new FocusAdapter() {
								@Override
								public void focusGained(FocusEvent e) {
									setFocus((JTextField) e.getSource());
								}
							});
							xAxisMax.setHorizontalAlignment(SwingConstants.TRAILING);
							panel_2_1.add(xAxisMax);
							xAxisMax.setColumns(7);
						}
						{
							JLabel lblNewLabel_6 = new JLabel("　縦軸交点");
							panel_2_1.add(lblNewLabel_6);
						}
						{
							xAxisOrigin = new JTextField();
							xAxisOrigin.addFocusListener(new FocusAdapter() {
								@Override
								public void focusGained(FocusEvent e) {
									setFocus((JTextField) e.getSource());
								}
							});
							xAxisOrigin.setHorizontalAlignment(SwingConstants.TRAILING);
							panel_2_1.add(xAxisOrigin);
							xAxisOrigin.setColumns(7);
						}
					}
					{
						JPanel panel_3 = new JPanel();
						panel_2.add(panel_3);
						{
							panel_3.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
						}
						{
							JLabel lblNewLabel_13 = new JLabel("目盛り　");
							panel_3.add(lblNewLabel_13);
						}
						{
							showScaleX = new JComboBox<SHOW>();
							showScaleX.addItemListener(new ItemListener() {
								public void itemStateChanged(ItemEvent arg0) {
									enableScale();
								}
							});
							showScaleX.setPreferredSize(new Dimension(60, 19));
							panel_3.add(showScaleX);
						}
						{
							JLabel lblNewLabel_9 = new JLabel("　幅");
							panel_3.add(lblNewLabel_9);
						}
						{
							xScaleStep = new JTextField();
							xScaleStep.addFocusListener(new FocusAdapter() {
								@Override
								public void focusGained(FocusEvent e) {
									setFocus((JTextField) e.getSource());
								}
							});
							xScaleStep.setHorizontalAlignment(SwingConstants.TRAILING);
							xScaleStep.setEnabled(false);
							panel_3.add(xScaleStep);
							xScaleStep.setColumns(10);
						}
						{
							withLabelX = new JCheckBox("ラベル");
							withLabelX.setSelected(true);
							panel_3.add(withLabelX);
						}
						{
							withLineX = new JCheckBox("目盛線");
							withLineX.setSelected(true);
							panel_3.add(withLineX);
						}
					}
				}
				{
					JLabel lblNewLabel_1 = new JLabel("　横軸　");
					lblNewLabel_1.setOpaque(true);
					lblNewLabel_1.setBackground(new Color(255, 255, 255));
					panel_1.add(lblNewLabel_1, BorderLayout.WEST);
				}
			}
			{
				JPanel panel_1 = new JPanel();
				panel_1.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
				panel_1.setOpaque(false);
				panel.add(panel_1);
				panel_1.setLayout(new BorderLayout(0, 0));
				{
					JLabel lblNewLabel_3 = new JLabel("　縦軸　");
					lblNewLabel_3.setBackground(new Color(255, 255, 255));
					lblNewLabel_3.setOpaque(true);
					panel_1.add(lblNewLabel_3, BorderLayout.WEST);
				}
				{
					JPanel panel_2 = new JPanel();
					panel_2.setBorder(new EmptyBorder(0, 5, 0, 0));
					panel_1.add(panel_2, BorderLayout.CENTER);
					panel_2.setLayout(new GridLayout(0, 1, 0, 0));
					{
						JPanel panel_2_1 = new JPanel();
						panel_2.add(panel_2_1);
						panel_2_1.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
						{
							JLabel lblNewLabel_4 = new JLabel("範囲　　最小");
							panel_2_1.add(lblNewLabel_4);
						}
						{
							yAxisMin = new JTextField();
							yAxisMin.addFocusListener(new FocusAdapter() {
								@Override
								public void focusGained(FocusEvent e) {
									setFocus((JTextField) e.getSource());
								}
							});
							yAxisMin.setHorizontalAlignment(SwingConstants.TRAILING);
							panel_2_1.add(yAxisMin);
							yAxisMin.setColumns(7);
						}
						{
							JLabel lblNewLabel_5 = new JLabel("　最大");
							panel_2_1.add(lblNewLabel_5);
						}
						{
							yAxisMax = new JTextField();
							yAxisMax.addFocusListener(new FocusAdapter() {
								@Override
								public void focusGained(FocusEvent e) {
									setFocus((JTextField) e.getSource());
								}
							});
							yAxisMax.setHorizontalAlignment(SwingConstants.TRAILING);
							panel_2_1.add(yAxisMax);
							yAxisMax.setColumns(7);
						}
						{
							JLabel lblNewLabel_7 = new JLabel("　横軸交点");
							panel_2_1.add(lblNewLabel_7);
						}
						{
							yAxisOrigin = new JTextField();
							yAxisOrigin.addFocusListener(new FocusAdapter() {
								@Override
								public void focusGained(FocusEvent e) {
									setFocus((JTextField) e.getSource());
								}
							});
							yAxisOrigin.setHorizontalAlignment(SwingConstants.TRAILING);
							panel_2_1.add(yAxisOrigin);
							yAxisOrigin.setColumns(7);
						}
					}
					{
						JPanel panel_3 = new JPanel();
						panel_2.add(panel_3);
						{
							panel_3.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
						}
						{
							JLabel lblNewLabel_12 = new JLabel("目盛り　");
							panel_3.add(lblNewLabel_12);
						}
						{
							showScaleY = new JComboBox<SHOW>();
							showScaleY.addItemListener(new ItemListener() {
								public void itemStateChanged(ItemEvent e) {
									enableScale();
								}
							});
							showScaleY.setPreferredSize(new Dimension(60, 19));
							panel_3.add(showScaleY);
						}
						{
							JLabel lblNewLabel_11 = new JLabel("　幅");
							panel_3.add(lblNewLabel_11);
						}
						{
							yScaleStep = new JTextField();
							yScaleStep.addFocusListener(new FocusAdapter() {
								@Override
								public void focusGained(FocusEvent e) {
									setFocus((JTextField) e.getSource());
								}
							});
							yScaleStep.setHorizontalAlignment(SwingConstants.TRAILING);
							yScaleStep.setEnabled(false);
							panel_3.add(yScaleStep);
							yScaleStep.setColumns(10);
						}
						{
							withLabelY = new JCheckBox("ラベル");
							withLabelY.setSelected(true);
							panel_3.add(withLabelY);
						}
						{
							withLineY = new JCheckBox("目盛線");
							withLineY.setSelected(true);
							panel_3.add(withLineY);
						}
					}
				}
			}
		}
		{
			graphTab = new JTabbedPane(JTabbedPane.TOP);
			graphTab.setToolTipText("「＋」タブをクリックしてグラフを追加してください");
			contentPanel.add(graphTab, BorderLayout.CENTER);
			{
				JPanel panel = new JPanel();
				panel.setToolTipText("");
				graphTab.addTab("+", null, panel, null);
				graphTab.setToolTipTextAt(0, "グラフを追加");
				panel.setLayout(new BorderLayout(0, 0));
				graphTab.setSelectedIndex(-1);
			}
			graphTab.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent arg0)
				{
					int index = graphTab.getSelectedIndex();
					if (added)
					{
						added = false;
					}
					else if (index == (graphTab.getTabCount() - 1))
					{
						if (null == addGraph(null, null))
						{
							graphTab.setSelectedIndex(prevSelectedIndex);
						}
					}
					prevSelectedIndex = index;
				}
			});
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setPreferredSize(new Dimension(79, 21));
				okButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						close(true);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("キャンセル");
				cancelButton.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						close(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}

		this.clear();
		this.setMinimumSize(this.getSize());
		for (SHOW show: SHOW.values())
		{
			this.showScaleX.addItem(show);
			this.showScaleY.addItem(show);
		}
		this.showScaleX.setSelectedItem(SHOW.AUTO);
		this.showScaleY.setSelectedItem(SHOW.AUTO);
	}

	private void setFocus(JTextField field)
	{
		field.setSelectionStart(0);
		field.setSelectionEnd(field.getText().length());
	}

	private static final String MIN = "0";
	private static final String MAX = "10";
	private static final String ORIGIN = "0";

	public void clear()
	{
		graphTab.setSelectedIndex(-1);
		int nTab = this.graphTab.getTabCount()-1;
		for (int i =0; i<nTab; i++)
		{
			this.graphTab.remove(0);
		}
		this.xAxisMin.setText(MIN);
		this.xAxisMax.setText(MAX);
		this.xAxisOrigin.setText(ORIGIN);
		this.yAxisMin.setText(MIN);
		this.yAxisMax.setText(MAX);
		this.yAxisOrigin.setText(ORIGIN);
	}

	public void setConfig(GraphConfig config)
	{
		Axis xAxis = config.getXAxis();
		this.xAxisMin.setText(Double.toString(xAxis.getMin()));
		this.xAxisMax.setText(Double.toString(xAxis.getMax()));
		this.xAxisOrigin.setText(Double.toString(xAxis.getOrigin()));

		Axis yAxis = config.getYAxis();
		this.yAxisMin.setText(Double.toString(yAxis.getMin()));
		this.yAxisMax.setText(Double.toString(yAxis.getMax()));
		this.yAxisOrigin.setText(Double.toString(yAxis.getOrigin()));

		GraphScale xScale = config.getXScale();
		this.showScaleX.setSelectedItem(xScale.getShow());
		this.xScaleStep.setText(Double.toString(xScale.getStep()));
		this.withLabelX.setSelected(xScale.withLabel());
		this.withLineX.setSelected(xScale.withLine());

		GraphScale yScale = config.getYScale();
		this.showScaleY.setSelectedItem(yScale.getShow());
		this.yScaleStep.setText(Double.toString(yScale.getStep()));
		this.withLabelY.setSelected(yScale.withLabel());
		this.withLineY.setSelected(yScale.withLine());
	}

	public GraphConfig getConfig(GraphConfig config)
	{
		Axis xAxis = config.getXAxis();
		xAxis.setMin(Double.parseDouble(this.xAxisMin.getText()));
		xAxis.setMax(Double.parseDouble(this.xAxisMax.getText()));
		xAxis.setOrigin(Double.parseDouble(this.xAxisOrigin.getText()));

		Axis yAxis = config.getYAxis();
		yAxis.setMin(Double.parseDouble(this.yAxisMin.getText()));
		yAxis.setMax(Double.parseDouble(this.yAxisMax.getText()));
		yAxis.setOrigin(Double.parseDouble(this.yAxisOrigin.getText()));

		GraphScale xScale = config.getXScale();
		SHOW showX = (SHOW) this.showScaleX.getSelectedItem();
		switch(showX)
		{
		case MANUAL:
			xScale.setScale(showX, Double.parseDouble(this.xAxisMin.getText()), Double.parseDouble(this.xScaleStep.getText()), Double.parseDouble(this.xAxisOrigin.getText()), this.withLabelX.isSelected(), this.withLineX.isSelected());
			break;
		default:
			xScale.setScale(showX, Double.parseDouble(this.xAxisMin.getText()), Double.parseDouble(this.xAxisMax.getText()), Double.parseDouble(this.xAxisOrigin.getText()), this.withLabelX.isSelected(), this.withLineX.isSelected());
			break;
		}
		GraphScale yScale = config.getYScale();
		SHOW showY = (SHOW) this.showScaleY.getSelectedItem();
		switch(showY)
		{
		case MANUAL:
			yScale.setScale(showY, Double.parseDouble(this.yAxisMin.getText()), Double.parseDouble(this.yScaleStep.getText()), Double.parseDouble(this.yAxisOrigin.getText()), this.withLabelY.isSelected(), this.withLineY.isSelected());
			break;
		default:
			yScale.setScale(showY, Double.parseDouble(this.yAxisMin.getText()), Double.parseDouble(this.yAxisMax.getText()), Double.parseDouble(this.yAxisOrigin.getText()), this.withLabelY.isSelected(), this.withLineY.isSelected());
			break;
		}
		return config;
	}

	public void setData(List<DataInfo> dataList)
	{
		graphTab.setSelectedIndex(-1);
		int nTab = this.graphTab.getTabCount()-1;
		for (int i =0; i<nTab; i++)
		{
			this.graphTab.remove(0);
		}
		for (DataInfo info: dataList)
		{
			GraphPropertyBase graph = this.addGraph(info.getFuncType(), info.getConfig().getName());
			graph.setData(info);
		}

		if (0 < dataList.size())
		{
			this.graphTab.setSelectedIndex(0);
		}
	}

	public List<DataInfo> getData(List<DataInfo> dataList)
	{
		int nGraph = this.graphTab.getTabCount() - 1;
		for (int i = 0; i < nGraph; i++)
		{
			GraphPropertyBase graph = (GraphPropertyBase) this.graphTab.getComponentAt(i);
			DataInfo info = graph.getData();
			dataList.add(info);
		}

		return dataList;
	}

	private GraphPropertyBase addGraph(FUNC_TYPE type, String name)
	{
		if (null == type)
		{
			String[] graphs = {"関数グラフ", "データグラフ","キャンセル"};
			int select = Util.select3("グラフ追加", graphs, graphs.length-1, true, "追加するグラフを選んでください");
			switch(select)
			{
			case 0:
				type = FUNC_TYPE.FUNCTION;
				break;
			case 1:
				type = FUNC_TYPE.DATA;
				break;
			default:
				return null;
			}
		}

		int index = (null != type ? this.graphTab.getTabCount()-1 : this.graphTab.getSelectedIndex());
		if (0 > index)
		{
			index = 0;
		}
		if (null == name)
		{
			for (int i = 0; ; i++)
			{
				name = String.format("グラフ%d", i + 1);
				int nTab = this.graphTab.getTabCount();
				for (int j = 0; j < nTab; j++)
				{
					String title = this.graphTab.getTitleAt(j);
					if (title.equals(name))
					{
						name = null;
						break;
					}
				}
				if (null != name)
				{
					break;
				}
			}
		}
		GraphPropertyBase graph = null;
		switch(type)
		{
		case FUNCTION:
			this.added = true;
			graph = new GraphFuncProperty(name);
			break;
		case DATA:
			this.added = true;
			graph = new GraphDataProperty(name);
			break;
		default:
		}
		if (null != graph)
		{
			this.graphTab.insertTab(name, null, graph, name, index);
			this.graphTab.setSelectedIndex(this.graphTab.getTabCount()-2);
		}
		return graph;
	}

	private boolean update = false;
	private JTextField xScaleStep;
	private JTextField yScaleStep;
	private JComboBox<SHOW> showScaleY;
	private JComboBox<SHOW> showScaleX;
	private JCheckBox withLineX;
	private JCheckBox withLineY;
	private JCheckBox withLabelX;
	private JCheckBox withLabelY;

	private void close(boolean update)
	{
		if (update)
		{
			int nGraph = this.graphTab.getTabCount();
			for (int i = 0; i < nGraph; i++)
			{
				Component c = this.graphTab.getComponentAt(i);
				if (c instanceof GraphPropertyBase)
				{
					GraphPropertyBase graph = (GraphPropertyBase) c;
					if (false == graph.checkGraph())
					{
						this.graphTab.setSelectedIndex(i);
						return;
					}
				}
			}
		}
		this.update = update;
		this.setVisible(false);
	}

	public boolean toUpdate()
	{
		return this.update;
	}

	private void enableScale()
	{
		boolean xflag = ((SHOW) this.showScaleX.getSelectedItem() == SHOW.MANUAL);
		this.xScaleStep.setEnabled(xflag);

		boolean yflag = ((SHOW) this.showScaleY.getSelectedItem() == SHOW.MANUAL);
		this.yScaleStep.setEnabled(yflag);
	}
}
