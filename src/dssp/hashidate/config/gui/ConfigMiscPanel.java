package dssp.hashidate.config.gui;

import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JSpinner;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JTextField;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.SpinnerNumberModel;

import dssp.hashidate.config.Config;

public class ConfigMiscPanel extends JPanel
{
	private JTextField svgPlainXScale;
	private JTextField svgPlainYScale;
	private JSpinner moveStep;
	private JTextField svgItalicXScale;
	private JTextField svgItalicYScale;

	public ConfigMiscPanel() {
		setOpaque(false);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{450, 0};
		gridBagLayout.rowHeights = new int[]{60, 150, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setBorder(new EmptyBorder(20, 20, 0, 0));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{135, 40, 0};
		gbl_panel.rowHeights = new int[]{20, 0};
		gbl_panel.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		JLabel label = new JLabel("カーソルによる図形の移動量 [画素数]");
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.anchor = GridBagConstraints.WEST;
		gbc_label.insets = new Insets(0, 0, 0, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		panel.add(label, gbc_label);

		moveStep = new JSpinner();
		moveStep.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		moveStep.setPreferredSize(new Dimension(40, 20));
		GridBagConstraints gbc_moveStep = new GridBagConstraints();
		gbc_moveStep.anchor = GridBagConstraints.NORTHWEST;
		gbc_moveStep.gridx = 1;
		gbc_moveStep.gridy = 0;
		panel.add(moveStep, gbc_moveStep);

		JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		panel_1.setBorder(new EmptyBorder(0, 20, 0, 0));
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		add(panel_1, gbc_panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		JLabel lblSvg = new JLabel("数式をSVG出力する時の拡大・縮小率(0以上)");
		panel_1.add(lblSvg, BorderLayout.NORTH);

		JPanel panel_2 = new JPanel();
		panel_2.setOpaque(false);
		panel_2.setBorder(new EmptyBorder(5, 0, 0, 0));
		panel_1.add(panel_2);
		GridBagLayout gbl_panel_2 = new GridBagLayout();
		gbl_panel_2.columnWidths = new int[]{430, 0};
		gbl_panel_2.rowHeights = new int[]{28, 66, 0};
		gbl_panel_2.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel_2.setLayout(gbl_panel_2);

		JPanel panel_3 = new JPanel();
		panel_3.setOpaque(false);
		panel_3.setBorder(new EmptyBorder(0, 20, 0, 0));
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.insets = new Insets(0, 0, 5, 0);
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 0;
		panel_2.add(panel_3, gbc_panel_3);
		GridBagLayout gbl_panel_3 = new GridBagLayout();
		gbl_panel_3.columnWidths = new int[]{61, 35, 86, 47, 86, 0};
		gbl_panel_3.rowHeights = new int[]{18, 0};
		gbl_panel_3.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_3.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_3.setLayout(gbl_panel_3);

		JLabel label_3 = new JLabel("通常");
		GridBagConstraints gbc_label_3 = new GridBagConstraints();
		gbc_label_3.fill = GridBagConstraints.BOTH;
		gbc_label_3.insets = new Insets(0, 0, 0, 5);
		gbc_label_3.gridx = 0;
		gbc_label_3.gridy = 0;
		panel_3.add(label_3, gbc_label_3);

		JLabel label_1 = new JLabel("幅");
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.anchor = GridBagConstraints.EAST;
		gbc_label_1.fill = GridBagConstraints.VERTICAL;
		gbc_label_1.insets = new Insets(0, 0, 0, 5);
		gbc_label_1.gridx = 1;
		gbc_label_1.gridy = 0;
		panel_3.add(label_1, gbc_label_1);

		svgPlainXScale = new JTextField();
		GridBagConstraints gbc_svgPlainXScale = new GridBagConstraints();
		gbc_svgPlainXScale.fill = GridBagConstraints.BOTH;
		gbc_svgPlainXScale.insets = new Insets(0, 0, 0, 5);
		gbc_svgPlainXScale.gridx = 2;
		gbc_svgPlainXScale.gridy = 0;
		panel_3.add(svgPlainXScale, gbc_svgPlainXScale);
		svgPlainXScale.setColumns(10);

		this.svgPlainXScale.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void changedUpdate(DocumentEvent arg0)
			{
				checkScale(svgPlainXScale);
			}

			@Override
			public void insertUpdate(DocumentEvent arg0)
			{
				checkScale(svgPlainXScale);
			}

			@Override
			public void removeUpdate(DocumentEvent arg0)
			{
				checkScale(svgPlainXScale);
			}

		});

		JLabel label_2 = new JLabel("高さ");
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.anchor = GridBagConstraints.EAST;
		gbc_label_2.fill = GridBagConstraints.VERTICAL;
		gbc_label_2.insets = new Insets(0, 0, 0, 5);
		gbc_label_2.gridx = 3;
		gbc_label_2.gridy = 0;
		panel_3.add(label_2, gbc_label_2);

		svgPlainYScale = new JTextField();
		GridBagConstraints gbc_svgPlainYScale = new GridBagConstraints();
		gbc_svgPlainYScale.fill = GridBagConstraints.BOTH;
		gbc_svgPlainYScale.gridx = 4;
		gbc_svgPlainYScale.gridy = 0;
		panel_3.add(svgPlainYScale, gbc_svgPlainYScale);
		svgPlainYScale.setColumns(10);

		this.svgPlainYScale.getDocument().addDocumentListener(new DocumentListener()
		{

			@Override
			public void changedUpdate(DocumentEvent arg0)
			{
				checkScale(svgPlainYScale);
			}

			@Override
			public void insertUpdate(DocumentEvent arg0)
			{
				checkScale(svgPlainYScale);
			}

			@Override
			public void removeUpdate(DocumentEvent arg0)
			{
				checkScale(svgPlainYScale);
			}

		});

		JPanel panel_4 = new JPanel();
		panel_4.setOpaque(false);
		panel_4.setBorder(new EmptyBorder(0, 20, 0, 0));
		GridBagConstraints gbc_panel_4 = new GridBagConstraints();
		gbc_panel_4.fill = GridBagConstraints.BOTH;
		gbc_panel_4.gridx = 0;
		gbc_panel_4.gridy = 1;
		panel_2.add(panel_4, gbc_panel_4);
		GridBagLayout gbl_panel_4 = new GridBagLayout();
		gbl_panel_4.columnWidths = new int[] {61, 35, 86, 47, 86, 0};
		gbl_panel_4.rowHeights = new int[] {18, 0};
		gbl_panel_4.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_panel_4.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		panel_4.setLayout(gbl_panel_4);

		JLabel label_4 = new JLabel("イタリック");
		GridBagConstraints gbc_label_4 = new GridBagConstraints();
		gbc_label_4.fill = GridBagConstraints.BOTH;
		gbc_label_4.insets = new Insets(0, 0, 0, 5);
		gbc_label_4.gridx = 0;
		gbc_label_4.gridy = 0;
		panel_4.add(label_4, gbc_label_4);

		JLabel label_5 = new JLabel("幅");
		GridBagConstraints gbc_label_5 = new GridBagConstraints();
		gbc_label_5.anchor = GridBagConstraints.EAST;
		gbc_label_5.fill = GridBagConstraints.VERTICAL;
		gbc_label_5.insets = new Insets(0, 0, 0, 5);
		gbc_label_5.gridx = 1;
		gbc_label_5.gridy = 0;
		panel_4.add(label_5, gbc_label_5);

		svgItalicXScale = new JTextField();
		svgItalicXScale.setColumns(10);
		GridBagConstraints gbc_svgItalicXScale = new GridBagConstraints();
		gbc_svgItalicXScale.fill = GridBagConstraints.BOTH;
		gbc_svgItalicXScale.insets = new Insets(0, 0, 0, 5);
		gbc_svgItalicXScale.gridx = 2;
		gbc_svgItalicXScale.gridy = 0;
		panel_4.add(svgItalicXScale, gbc_svgItalicXScale);

		JLabel label_6 = new JLabel("高さ");
		GridBagConstraints gbc_label_6 = new GridBagConstraints();
		gbc_label_6.anchor = GridBagConstraints.EAST;
		gbc_label_6.fill = GridBagConstraints.VERTICAL;
		gbc_label_6.insets = new Insets(0, 0, 0, 5);
		gbc_label_6.gridx = 3;
		gbc_label_6.gridy = 0;
		panel_4.add(label_6, gbc_label_6);

		svgItalicYScale = new JTextField();
		svgItalicYScale.setColumns(10);
		GridBagConstraints gbc_svgItalicYScale = new GridBagConstraints();
		gbc_svgItalicYScale.fill = GridBagConstraints.BOTH;
		gbc_svgItalicYScale.gridx = 4;
		gbc_svgItalicYScale.gridy = 0;
		panel_4.add(svgItalicYScale, gbc_svgItalicYScale);

		this.svgItalicXScale.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void changedUpdate(DocumentEvent arg0)
			{
				checkScale(svgItalicXScale);
			}

			@Override
			public void insertUpdate(DocumentEvent arg0)
			{
				checkScale(svgItalicXScale);
			}

			@Override
			public void removeUpdate(DocumentEvent arg0)
			{
				checkScale(svgItalicXScale);
			}

		});

		this.svgItalicYScale.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void changedUpdate(DocumentEvent arg0)
			{
				checkScale(svgItalicYScale);
			}

			@Override
			public void insertUpdate(DocumentEvent arg0)
			{
				checkScale(svgItalicYScale);
			}

			@Override
			public void removeUpdate(DocumentEvent arg0)
			{
				checkScale(svgItalicYScale);
			}

		});
	}

	public void setMoveStep(int val)
	{
		this.moveStep.setValue(val);
	}

	public int getMoveStep()
	{
		return (int) this.moveStep.getValue();
	}

	public void setScale(Config.MISC key, Point2D.Double scale)
	{
		switch(key)
		{
		case PLAIN:
			this.svgPlainXScale.setText(Double.toString(scale.x));
			this.svgPlainYScale.setText(Double.toString(scale.y));
			break;
		case ITALIC:
			this.svgItalicXScale.setText(Double.toString(scale.x));
			this.svgItalicYScale.setText(Double.toString(scale.y));
			break;
		default:
		}
	}

	public Point2D.Double getScale(Config.MISC key)
	{
		Point2D.Double val = null;
		switch(key)
		{
		case PLAIN:
			val = new Point2D.Double(this.checkScale(this.svgPlainXScale), this.checkScale(this.svgPlainYScale));
			break;
		case ITALIC:
			val = new Point2D.Double(this.checkScale(this.svgItalicXScale), this.checkScale(this.svgItalicYScale));
			break;
		default:
		}

		return val;
	}

	public double checkScale(JTextField field)
	{
		if (null == field)
		{
			return 1.0;
		}
		double val = 1.0;
		String text = field.getText();
		try
		{
			val = Double.parseDouble(text);
			if (0 >= val)
			{
				field.setForeground(Color.RED);
			}
			else
			{
				field.setForeground(Color.BLACK);
			}
		}
		catch (NumberFormatException ex)
		{
			field.setForeground(Color.RED);
		}

		return val;
	}
}
