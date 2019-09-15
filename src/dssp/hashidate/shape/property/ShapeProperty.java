package dssp.hashidate.shape.property;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;

import java.awt.GridLayout;
import java.util.Enumeration;

import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;

import dssp.hashidate.config.Config;
import dssp.hashidate.misc.ColorButton;
import dssp.hashidate.misc.FigureType.EDGE_TYPE;
import dssp.hashidate.misc.FigureType.FILL_TYPE;
import dssp.hashidate.misc.FigureType.LINE_TYPE;

import javax.swing.JCheckBox;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JTabbedPane;

public class ShapeProperty extends JDialog
{

	private final JPanel contentPanel = new JPanel();
	private JComboBox<ImageIcon> lineType;
	private JComboBox<ImageIcon> edgeType;

	/**
	 * Create the dialog.
	 */
	protected ShapeProperty()
	{
		setModal(true);
		setTitle("プロパティ");
		setBounds(100, 100, 450, 378);
		getContentPane().setLayout(new BorderLayout(0, 0));
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JPanel panel = new JPanel();
				buttonPane.add(panel);
				panel.setLayout(new GridLayout(0, 2, 0, 0));
				{
					JButton okButton = new JButton("OK");
					okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							ok = true;
							setVisible(false);
						}
					});
					panel.add(okButton);
					okButton.setActionCommand("OK");
					getRootPane().setDefaultButton(okButton);
				}
				{
					JButton cancelButton = new JButton("キャンセル");
					cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							ok = false;
							setVisible(false);
						}
					});
					panel.add(cancelButton);
					cancelButton.setActionCommand("Cancel");
				}
			}
		}
		{
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			getContentPane().add(tabbedPane, BorderLayout.CENTER);
			tabbedPane.addTab("共通", null, contentPanel, null);
			contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			contentPanel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel = new JPanel();
				contentPanel.add(panel, BorderLayout.CENTER);
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				{
					JPanel panel_3 = new JPanel();
					panel.add(panel_3);
					panel_3.setBorder(new TitledBorder(null, "\u7DDA\u306E\u8A2D\u5B9A", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
					{
						JPanel panel_1 = new JPanel();
						panel_3.add(panel_1);
						panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
						{
							JPanel panel_1_1 = new JPanel();
							panel_1.add(panel_1_1);
							FlowLayout fl_panel_1_1 = (FlowLayout) panel_1_1.getLayout();
							fl_panel_1_1.setAlignment(FlowLayout.LEFT);
							{
								JLabel lblNewLabel_3 = new JLabel("色");
								panel_1_1.add(lblNewLabel_3);
							}
							{
								lineColor = new ColorButton("");
								lineColor.setPreferredSize(new Dimension(33, 20));
								lineColor.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent e) {
										selectColor(lineColor, "線の色");
									}
								});
								panel_1_1.add(lineColor);
							}
						}
						{
							JPanel panel_1_1 = new JPanel();
							panel_1.add(panel_1_1);
							panel_1_1.setLayout(new BoxLayout(panel_1_1, BoxLayout.X_AXIS));
							{
								JLabel lblNewLabel = new JLabel("線の太さ");
								panel_1_1.add(lblNewLabel);
							}
							{
								JPanel panel_2 = new JPanel();
								panel_1_1.add(panel_2);
								{
									JRadioButton narrow = new JRadioButton("細線");
									panel_2.add(narrow);
									this.lineSize.add(narrow);
								}
								{
									JRadioButton normal = new JRadioButton("通常");
									panel_2.add(normal);
									this.lineSize.add(normal);
								}
								{
									JRadioButton wide = new JRadioButton("太線");
									panel_2.add(wide);
									this.lineSize.add(wide);
								}
							}
						}
						{
							JPanel panel_1_1 = new JPanel();
							panel_1.add(panel_1_1);
							FlowLayout fl_panel_1_1 = (FlowLayout) panel_1_1.getLayout();
							fl_panel_1_1.setAlignment(FlowLayout.LEFT);
							{
								JLabel label = new JLabel("線種");
								panel_1_1.add(label);
							}
							{
								lineType = new JComboBox<ImageIcon>();
								panel_1_1.add(lineType);
							}
							{
								JLabel lblNewLabel_1 = new JLabel("端点");
								panel_1_1.add(lblNewLabel_1);
							}
							{
								edgeType = new JComboBox<ImageIcon>();
								panel_1_1.add(edgeType);
							}
						}
						{
							JPanel panel_2 = new JPanel();
							FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
							flowLayout.setAlignment(FlowLayout.LEFT);
							panel_1.add(panel_2);
							{
								lineBack = new JCheckBox("裏線");
								panel_2.add(lineBack);
							}
						}
					}
				}
				{
					JPanel panel_2 = new JPanel();
					panel_2.setBorder(new TitledBorder(null, "\u5857\u308A\u3064\u3076\u3057", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					panel.add(panel_2);
					panel_2.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
					{
						JPanel panel_1 = new JPanel();
						panel_2.add(panel_1);
						panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
						{
							JPanel panel_1_1 = new JPanel();
							FlowLayout flowLayout = (FlowLayout) panel_1_1.getLayout();
							flowLayout.setAlignment(FlowLayout.LEFT);
							panel_1.add(panel_1_1);
							{
								JLabel label = new JLabel("色");
								panel_1_1.add(label);
							}
							{
								fillColor = new ColorButton("");
								fillColor.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent arg0) {
										selectColor(fillColor, "塗りつぶしの色");
									}
								});
								fillColor.setPreferredSize(new Dimension(33, 20));
								panel_1_1.add(fillColor);
							}
							{
								JLabel lblNewLabel_2 = new JLabel("パターン");
								panel_1_1.add(lblNewLabel_2);
							}
							{
								fillType = new JComboBox<ImageIcon>();
								fillType.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent arg0) {
										setFillControls();
									}
								});
								panel_1_1.add(fillType);
							}
						}
						{
							JPanel panel_3 = new JPanel();
							panel_1.add(panel_3);
							{
								JLabel lblNewLabel_4 = new JLabel("点の大きさ");
								panel_3.add(lblNewLabel_4);
							}
							{
								JRadioButton small = new JRadioButton("小");
								panel_3.add(small);
								this.fillSize.add(small);
							}
							{
								JRadioButton middle = new JRadioButton("中");
								panel_3.add(middle);
								this.fillSize.add(middle);
							}
							{
								JRadioButton large = new JRadioButton("大");
								panel_3.add(large);
								this.fillSize.add(large);
							}
						}
						{
							JPanel panel_1_1 = new JPanel();
							FlowLayout flowLayout = (FlowLayout) panel_1_1.getLayout();
							flowLayout.setAlignment(FlowLayout.LEFT);
							panel_1.add(panel_1_1);
							{
								fillBack = new JCheckBox("裏点");
								panel_1_1.add(fillBack);
							}
						}
					}
				}
			}
		}

		init();
	}

	private boolean ok = false;
	private ButtonGroup lineSize = new ButtonGroup();
	private ButtonGroup fillSize = new ButtonGroup();
	private ColorButton lineColor;
	private ColorButton fillColor;
	private void init()
	{
		for (LINE_TYPE type: LINE_TYPE.values())
		{
			this.lineType.addItem(type.getIcon());
		}
		for (EDGE_TYPE type:EDGE_TYPE.values())
		{
			this.edgeType.addItem(type.getIcon());
		}
		for (FILL_TYPE type: FILL_TYPE.values())
		{
			this.fillType.addItem(type.getIcon());
		}
	}

	private void selectColor(ColorButton btn, String title)
	{
		Color color = ColorButton.showColorChooser(title, btn.getPaintColor());
		if (null != color)
		{
			btn.setPaintColor(color);
		}
	}

	private void setFillControls()
	{
		boolean flag = (0 != this.fillType.getSelectedIndex());

		this.fillColor.setEnabled(flag);
		Enumeration<AbstractButton> e = this.fillSize.getElements();
		while (e.hasMoreElements())
		{
			AbstractButton b = e.nextElement();
			b.setEnabled(flag);
		}
		this.fillBack.setEnabled(flag);
	}

	private static ShapeProperty instance;
	private JCheckBox lineBack;
	private JCheckBox fillBack;
	private JComboBox<ImageIcon> fillType;
	protected JTabbedPane tabbedPane;
	public static ShapeProperty getInstance()
	{
		if (null == instance)
		{
			instance = new ShapeProperty();
		}
		return instance;
	}

	public void addTab(String title, JPanel panel)
	{
		this.tabbedPane.addTab(title, panel);
	}

	public boolean isOk()
	{
		return ok;
	}

	public void setLineType(LINE_TYPE type)
	{
		int index = LINE_TYPE.getIndex(type);
		this.lineType.setSelectedIndex(index);
	}

	public LINE_TYPE getLineType()
	{
		int index = this.lineType.getSelectedIndex();
		return LINE_TYPE.getType(index);
	}

	public void setEdgeType(EDGE_TYPE type)
	{
		int index = EDGE_TYPE.getIndex(type);
		this.edgeType.setSelectedIndex(index);
	}

	public EDGE_TYPE getEdgeType()
	{
		int index = this.edgeType.getSelectedIndex();
		return EDGE_TYPE.getType(index);
	}

	public void setLineSize(Config.BRAILLE size)
	{
		int s = 0;
		switch(size)
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

		int index = 0;
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
	}

	public Config.BRAILLE getLineSize()
	{
		int index = 0;
		Enumeration<AbstractButton> e = this.lineSize.getElements();
		while (e.hasMoreElements())
		{
			AbstractButton b = e.nextElement();
			if (b.isSelected())
			{
				switch(index)
				{
				case 0:
					return Config.BRAILLE.SMALL;
				case 2:
					return Config.BRAILLE.LARGE;
				default:
					return Config.BRAILLE.MIDDLE;
				}
			}
			++index;
		}

		return Config.BRAILLE.MIDDLE;
	}

	public void setLineColor(Color color)
	{
		this.lineColor.setPaintColor(color);;
	}

	public Color getLineColor()
	{
		return this.lineColor.getPaintColor();
	}

	public void setLineBack(boolean flag)
	{
		this.lineBack.setSelected(flag);
	}

	public boolean isLineBack()
	{
		return this.lineBack.isSelected();
	}

	public void setFillType(FILL_TYPE type)
	{
		int index = FILL_TYPE.getIndex(type);
		this.fillType.setSelectedIndex(index);

		this.setFillControls();
	}

	public FILL_TYPE getFillType()
	{
		int index = this.fillType.getSelectedIndex();
		return FILL_TYPE.getType(index);
	}

	public void setFillSize(Config.BRAILLE size)
	{
		int s = 0;
		switch(size)
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

		int index = 0;
		Enumeration<AbstractButton> e = this.fillSize.getElements();
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
	}

	public Config.BRAILLE getFillSize()
	{
		int index = 0;
		Enumeration<AbstractButton> e = this.fillSize.getElements();
		while (e.hasMoreElements())
		{
			AbstractButton b = e.nextElement();
			if (b.isSelected())
			{
				switch(index)
				{
				case 0:
					return Config.BRAILLE.SMALL;
				case 2:
					return Config.BRAILLE.LARGE;
				default:
					return Config.BRAILLE.MIDDLE;
				}
			}
			++index;
		}

		return Config.BRAILLE.MIDDLE;
	}

	public void setFillColor(Color color)
	{
		this.fillColor.setPaintColor(color);;
	}

	public Color getFillColor()
	{
		if (0 == this.fillType.getSelectedIndex())
		{
			return null;
		}

		return this.fillColor.getPaintColor();
	}

	public void setFillBack(boolean flag)
	{
		this.fillBack.setSelected(flag);
	}

	public boolean isFillBack()
	{
		return this.fillBack.isSelected();
	}

}