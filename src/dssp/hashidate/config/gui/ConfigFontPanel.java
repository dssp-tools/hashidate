package dssp.hashidate.config.gui;

import javax.swing.JPanel;

import java.awt.GridLayout;
import java.awt.BorderLayout;

import javax.swing.ComboBoxModel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JCheckBox;
import javax.swing.SpinnerNumberModel;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

import javax.swing.border.EmptyBorder;
import javax.swing.SwingConstants;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Color;

import javax.swing.border.LineBorder;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import dssp.hashidate.config.Config;

public class ConfigFontPanel extends JPanel
{
	private JComboBox<String> textFamily;
	private JSpinner textSize;
	private JCheckBox textBold;
	private JComboBox<String> formulaFamilty;
	private JSpinner formulaSize;
	private JCheckBox formulaBold;
	private JCheckBox formulaItalic;
	private JCheckBox textItalic;
	private JLabel formulaSample;
	private JLabel textSample;

	public ConfigFontPanel() {
		setOpaque(false);
		setBorder(new EmptyBorder(0, 20, 20, 20));
		setLayout(new GridLayout(2, 0, 0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(10, 0, 0, 0));
		panel.setOpaque(false);
		add(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 0, 0, 0));
		panel_1.setOpaque(false);
		add(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		String[] fontNames = ge.getAvailableFontFamilyNames();

		JPanel panel_7 = new JPanel();
		panel_7.setOpaque(false);
		panel.add(panel_7, BorderLayout.NORTH);
		panel_7.setLayout(new BorderLayout(0, 0));

		JPanel panel_6 = new JPanel();
		panel_6.setOpaque(false);
		panel_6.setBorder(new EmptyBorder(0, 0, 5, 0));
		panel_6.setPreferredSize(new Dimension(60, 10));
		panel_7.add(panel_6, BorderLayout.WEST);
		panel_6.setLayout(new BorderLayout(0, 0));

		JLabel label = new JLabel("テキスト");
		label.setBorder(new EmptyBorder(0, 10, 0, 10));
		panel_6.add(label);

		JPanel panel_8 = new JPanel();
		panel_8.setOpaque(false);
		panel_7.add(panel_8, BorderLayout.CENTER);
		panel_8.setLayout(new GridLayout(2, 0, 0, 0));

		JPanel panel_3 = new JPanel();
		panel_8.add(panel_3);
		panel_3.setOpaque(false);
		panel_3.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));

		JLabel lblNewLabel = new JLabel("フォント名");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_3.add(lblNewLabel);

		textFamily = new JComboBox<String>();
		textFamily.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				drawSample(Config.FONT.TEXT);
			}
		});
		panel_3.add(textFamily);

		JLabel lblNewLabel_1 = new JLabel("サイズ");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_3.add(lblNewLabel_1);

		textSize = new JSpinner();
		textSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				drawSample(Config.FONT.TEXT);
			}
		});
		textSize.setPreferredSize(new Dimension(40, 20));
		panel_3.add(textSize);

		this.textSize.setModel(new SpinnerNumberModel(new Integer(12), new Integer(4), null, new Integer(1)));

		JPanel panel_9 = new JPanel();
		panel_9.setBorder(new EmptyBorder(0, 0, 0, 0));
		panel_9.setOpaque(false);
		panel_8.add(panel_9);
		panel_9.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));

		JLabel lblNewLabel_2 = new JLabel("スタイル");
		lblNewLabel_2.setPreferredSize(new Dimension(44, 13));
		panel_9.add(lblNewLabel_2);
		lblNewLabel_2.setHorizontalAlignment(SwingConstants.RIGHT);

		JPanel panel_4 = new JPanel();
		panel_9.add(panel_4);
		panel_4.setOpaque(false);
		panel_4.setLayout(new GridLayout(0, 3, 0, 0));

		textItalic = new JCheckBox("イタリック");
		textItalic.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				drawSample(Config.FONT.TEXT);
			}
		});
		textItalic.setOpaque(false);
		panel_4.add(textItalic);

		textBold = new JCheckBox("太字");
		textBold.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				drawSample(Config.FONT.TEXT);
			}
		});
		textBold.setOpaque(false);
		panel_4.add(textBold);

		textSample = new JLabel("ABC abc あいう アイウ");
		textSample.setHorizontalAlignment(SwingConstants.CENTER);
		textSample.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.add(textSample, BorderLayout.CENTER);

		JPanel panel_10 = new JPanel();
		panel_10.setOpaque(false);
		panel_1.add(panel_10, BorderLayout.NORTH);
		panel_10.setLayout(new BorderLayout(0, 0));

		JPanel panel_11 = new JPanel();
		panel_11.setOpaque(false);
		panel_11.setBorder(new EmptyBorder(0, 0, 5, 0));
		panel_11.setPreferredSize(new Dimension(60, 10));
		panel_10.add(panel_11, BorderLayout.WEST);
		panel_11.setLayout(new BorderLayout(0, 0));

		JLabel label_1 = new JLabel("数式");
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		label_1.setBorder(new EmptyBorder(0, 10, 0, 10));
		panel_11.add(label_1);

		JPanel panel_12 = new JPanel();
		panel_12.setOpaque(false);
		panel_10.add(panel_12, BorderLayout.CENTER);
		panel_12.setLayout(new GridLayout(2, 0, 0, 0));

		JPanel panel_2 = new JPanel();
		panel_12.add(panel_2);
		panel_2.setOpaque(false);
		panel_2.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));

		JLabel label_2 = new JLabel("フォント名");
		label_2.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_2.add(label_2);

		formulaFamilty = new JComboBox<String>();
		formulaFamilty.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				drawSample(Config.FONT.FORMULA);
			}
		});
		panel_2.add(formulaFamilty);

		JLabel label_3 = new JLabel("サイズ");
		label_3.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_2.add(label_3);

		formulaSize = new JSpinner();
		formulaSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				drawSample(Config.FONT.FORMULA);
			}
		});
		formulaSize.setPreferredSize(new Dimension(40, 20));
		panel_2.add(formulaSize);
		this.formulaSize.setModel(new SpinnerNumberModel(new Integer(12), new Integer(4), null, new Integer(1)));

		JPanel panel_13 = new JPanel();
		panel_13.setOpaque(false);
		FlowLayout flowLayout = (FlowLayout) panel_13.getLayout();
		flowLayout.setAlignment(FlowLayout.LEADING);
		panel_12.add(panel_13);

		JLabel label_4 = new JLabel("スタイル");
		label_4.setPreferredSize(new Dimension(44, 13));
		panel_13.add(label_4);
		label_4.setHorizontalAlignment(SwingConstants.RIGHT);

		JPanel panel_5 = new JPanel();
		panel_13.add(panel_5);
		panel_5.setOpaque(false);
		panel_5.setLayout(new GridLayout(0, 3, 0, 0));

		formulaItalic = new JCheckBox("イタリック");
		formulaItalic.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				drawSample(Config.FONT.FORMULA);
			}
		});
		formulaItalic.setOpaque(false);
		panel_5.add(formulaItalic);

		formulaBold = new JCheckBox("太字");
		formulaBold.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				drawSample(Config.FONT.FORMULA);
			}
		});
		formulaBold.setOpaque(false);
		panel_5.add(formulaBold);

		formulaSample = new JLabel("ABC abc αβγ");
		formulaSample.setBorder(new LineBorder(new Color(0, 0, 0)));
		formulaSample.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(formulaSample, BorderLayout.CENTER);

		for (int i = 0; i < fontNames.length; i++)
		{
			this.textFamily.addItem(fontNames[i]);
			this.formulaFamilty.addItem(fontNames[i]);
		}
	}

	private void setFamily(JComboBox<String> comp, String name)
	{
		ComboBoxModel<?> model = comp.getModel();
		int size = model.getSize();
		for (int i = 0; i < size; i++)
		{
			String val = (String) model.getElementAt(i);
			if (name.equals(val))
			{
				comp.setSelectedIndex(i);
				break;
			}
		}
	}

	public void setFont(Config.FONT type, Font font)
	{
		int style = font.getStyle();
		switch(type)
		{
		case TEXT:
			this.setFamily(this.textFamily, font.getFamily());
			this.textSize.setValue(font.getSize());

			this.textItalic.setSelected(0 != (style & Font.ITALIC));
			this.textBold.setSelected(0 != (style & Font.BOLD));

			break;
		case FORMULA:
			this.setFamily(this.formulaFamilty, font.getFamily());
			this.formulaSize.setValue(font.getSize());

			this.formulaItalic.setSelected(0 != (style & Font.ITALIC));
			this.formulaBold.setSelected(0 != (style & Font.BOLD));

			break;
		}
	}

	public Font getFont(Config.FONT type)
	{
		String name = "";
		int size = 0;
		int style = 0;
		switch(type)
		{
		case TEXT:
			name = (String) this.textFamily.getSelectedItem();
			size = (int) this.textSize.getValue();
			style = (this.textItalic.isSelected() ? Font.ITALIC : Font.PLAIN);
			if (this.textBold.isSelected())
			{
				style |= Font.BOLD;
			}
			break;
		case FORMULA:
			name = (String) this.formulaFamilty.getSelectedItem();
			size = (int) this.formulaSize.getValue();
			style = (this.formulaItalic.isSelected() ? Font.ITALIC : Font.PLAIN);
			if (this.formulaBold.isSelected())
			{
				style |= Font.BOLD;
			}
			break;
		}

		Font font = new Font(name, style, size);

		return font;
	}

	void drawSample(Config.FONT type)
	{
		Font font = this.getFont(type);

		switch(type)
		{
		case TEXT:
			this.textSample.setFont(font);
			this.textSample.repaint();
			break;
		case FORMULA:
			this.formulaSample.setFont(font);
			this.formulaSample.repaint();
			break;
		}
	}
}
