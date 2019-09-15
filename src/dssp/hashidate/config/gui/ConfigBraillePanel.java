package dssp.hashidate.config.gui;

import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

import java.awt.Dimension;

import dssp.brailleLib.Util;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import dssp.hashidate.config.ConfigBraille;
import dssp.hashidate.config.ConfigBraille.DOT;

import javax.swing.SpinnerNumberModel;

import java.awt.FlowLayout;

import javax.swing.BoxLayout;

import java.awt.GridLayout;
import java.io.File;

import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JRadioButton;
import javax.swing.JList;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JCheckBox;

public class ConfigBraillePanel extends JPanel
{
	private JSpinner dotSmall;
	private JSpinner dotMiddle;
	private JSpinner dotLarge;
	private JComboBox<ConfigBraille.DOT> figureSize;
	private JComboBox<ConfigBraille.DOT> brailleSize;
	private JSpinner dotSpaceX;
	private JSpinner boxSpace;
	private JSpinner dotSpaceY;
	private JSpinner lineSpace;
	private JSpinner dotSpan;
	private JList<String> fileList;

	Map<ConfigBraille.DICT, DefaultListModel<String>> fileListMap = Util.newHashMap();
	private JRadioButton btnFormula;
	private JRadioButton btnText;
	private JCheckBox textBraille;

	public ConfigBraillePanel() {
		setOpaque(false);
		setBorder(new EmptyBorder(5, 20, 5, 20));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		JPanel panel_19 = new JPanel();
		panel_19.setBorder(new TitledBorder(null, "\u70B9\u56F3 (0.1mm\u5358\u4F4D)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_19.setOpaque(false);
		add(panel_19);
		panel_19.setLayout(new BoxLayout(panel_19, BoxLayout.X_AXIS));

		JPanel panel_9 = new JPanel();
		panel_19.add(panel_9);
		panel_9.setOpaque(false);

		JLabel label = new JLabel("大きさ");
		panel_9.add(label);

		figureSize = new JComboBox<ConfigBraille.DOT>();
		DOT[] dotSizeList = {DOT.SMALL, DOT.MIDDLE, DOT.LARGE};
		figureSize.setModel(new DefaultComboBoxModel<ConfigBraille.DOT>(dotSizeList));
		figureSize.setPreferredSize(new Dimension(60, 19));
		panel_9.add(figureSize);

		JPanel panel_12 = new JPanel();
		panel_19.add(panel_12);
		panel_12.setOpaque(false);
		JLabel lblNewLabel_4 = new JLabel("間隔");
		panel_12.add(lblNewLabel_4);

		dotSpan = new JSpinner();
		panel_12.add(dotSpan);
		dotSpan.setModel(new SpinnerNumberModel(0, 0, 99, 1));

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(null, "\u70B9\u5B57 (0.1mm\u5358\u4F4D)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_3.setOpaque(false);
		add(panel_3);
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

		JPanel panel_10 = new JPanel();
		panel_3.add(panel_10);
		panel_10.setOpaque(false);
		panel_10.setLayout(new BoxLayout(panel_10, BoxLayout.X_AXIS));

		JPanel panel_20 = new JPanel();
		panel_20.setOpaque(false);
		panel_10.add(panel_20);

		JLabel label_4 = new JLabel("大きさ");
		panel_20.add(label_4);

		JPanel panel_23 = new JPanel();
		panel_23.setOpaque(false);
		panel_10.add(panel_23);

		brailleSize = new JComboBox<ConfigBraille.DOT>();
		panel_23.add(brailleSize);
		brailleSize.setModel(new DefaultComboBoxModel<ConfigBraille.DOT>(dotSizeList));
		brailleSize.setPreferredSize(new Dimension(60, 19));

		JPanel panel_17 = new JPanel();
		panel_3.add(panel_17);
		panel_17.setOpaque(false);
		panel_17.setLayout(new BorderLayout(0, 0));

		JPanel panel_5 = new JPanel();
		panel_5.setOpaque(false);
		panel_17.add(panel_5, BorderLayout.WEST);
		panel_5.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel lblNewLabel_2 = new JLabel("間隔");
		panel_5.add(lblNewLabel_2);

		JPanel panel_4 = new JPanel();
		panel_4.setOpaque(false);
		panel_17.add(panel_4);
		panel_4.setLayout(new GridLayout(0, 2, 0, 0));

		JPanel panel_11 = new JPanel();
		panel_4.add(panel_11);
		panel_11.setOpaque(false);
		panel_11.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 5));

		JLabel label_5 = new JLabel("横方向");
		label_5.setHorizontalAlignment(SwingConstants.TRAILING);
		panel_11.add(label_5);

		dotSpaceX = new JSpinner();
		dotSpaceX.setModel(new SpinnerNumberModel(0, 0, 99, 1));
		panel_11.add(dotSpaceX);

		JPanel panel_13 = new JPanel();
		panel_4.add(panel_13);
		FlowLayout flowLayout = (FlowLayout) panel_13.getLayout();
		flowLayout.setAlignment(FlowLayout.LEADING);
		panel_13.setOpaque(false);

		JLabel label_6 = new JLabel("縦方向");
		panel_13.add(label_6);

		dotSpaceY = new JSpinner();
		dotSpaceY.setModel(new SpinnerNumberModel(0, 0, 99, 1));
		panel_13.add(dotSpaceY);

		JPanel panel_14 = new JPanel();
		FlowLayout flowLayout_3 = (FlowLayout) panel_14.getLayout();
		flowLayout_3.setAlignment(FlowLayout.LEADING);
		panel_4.add(panel_14);
		panel_14.setOpaque(false);

		JLabel label_7 = new JLabel("マス間");
		panel_14.add(label_7);

		boxSpace = new JSpinner();
		boxSpace.setModel(new SpinnerNumberModel(0, 0, 99, 1));
		panel_14.add(boxSpace);

		JPanel panel_15 = new JPanel();
		FlowLayout flowLayout_5 = (FlowLayout) panel_15.getLayout();
		flowLayout_5.setAlignment(FlowLayout.LEADING);
		panel_4.add(panel_15);
		panel_15.setOpaque(false);

		JLabel label_8 = new JLabel("行間");
		panel_15.add(label_8);

		lineSpace = new JSpinner();
		lineSpace.setModel(new SpinnerNumberModel(0, 0, 99, 1));
		panel_15.add(lineSpace);

		JPanel panel_18 = new JPanel();
		add(panel_18);
		panel_18.setBorder(new TitledBorder(null, "\u70B9\u306E\u5927\u304D\u3055 (0.1mm\u5358\u4F4D)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_18.setOpaque(false);

		JPanel panel_6 = new JPanel();
		panel_18.add(panel_6);
		panel_6.setOpaque(false);
		panel_6.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel label_1 = new JLabel("小点");
		label_1.setBorder(new EmptyBorder(0, 0, 0, 5));
		panel_6.add(label_1);
		label_1.setHorizontalAlignment(SwingConstants.RIGHT);

		dotSmall = new JSpinner();
		dotSmall.setModel(new SpinnerNumberModel(0, 0, 99, 1));
		panel_6.add(dotSmall);

		JPanel panel_7 = new JPanel();
		panel_18.add(panel_7);
		panel_7.setOpaque(false);
		panel_7.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel label_2 = new JLabel("中点");
		label_2.setBorder(new EmptyBorder(0, 0, 0, 5));
		panel_7.add(label_2);
		label_2.setHorizontalAlignment(SwingConstants.RIGHT);

		dotMiddle = new JSpinner();
		dotMiddle.setModel(new SpinnerNumberModel(0, 0, 99, 1));
		panel_7.add(dotMiddle);

		JPanel panel_8 = new JPanel();
		panel_18.add(panel_8);
		panel_8.setOpaque(false);
		panel_8.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel label_3 = new JLabel("大点");
		label_3.setBorder(new EmptyBorder(0, 0, 0, 5));
		panel_8.add(label_3);
		label_3.setHorizontalAlignment(SwingConstants.RIGHT);

		dotLarge = new JSpinner();
		dotLarge.setModel(new SpinnerNumberModel(0, 0, 99, 1));
		panel_8.add(dotLarge);

		JPanel panel_24 = new JPanel();
		panel_24.setOpaque(false);
		add(panel_24);

		textBraille = new JCheckBox("テキストを点字に変換する");
		textBraille.setOpaque(false);
		panel_24.add(textBraille);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "\u8F9E\u66F8\u30D5\u30A1\u30A4\u30EB", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setOpaque(false);
		add(panel_2);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.Y_AXIS));

		JPanel panel_22 = new JPanel();
		panel_22.setOpaque(false);
		panel_2.add(panel_22);
		panel_22.setLayout(new BoxLayout(panel_22, BoxLayout.Y_AXIS));

		JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		FlowLayout flowLayout_4 = (FlowLayout) panel_1.getLayout();
		flowLayout_4.setAlignment(FlowLayout.LEADING);
		panel_22.add(panel_1);

		btnText = new JRadioButton("テキスト用");
		btnText.setOpaque(false);
		btnText.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				if (btnText.isSelected())
				{
					selectDict(ConfigBraille.DICT.TEXT);
				}
				else
				{
					selectDict(ConfigBraille.DICT.FORMULA);
				}
			}
		});
		btnText.setSelected(true);
		panel_1.add(btnText);

		btnFormula = new JRadioButton("数式用");
		btnFormula.setOpaque(false);
		panel_1.add(btnFormula);

		JPanel panel_16 = new JPanel();
		panel_22.add(panel_16);
		panel_16.setOpaque(false);
		panel_16.setLayout(new BoxLayout(panel_16, BoxLayout.X_AXIS));

		JScrollPane scrollPane = new JScrollPane();
		panel_16.add(scrollPane);
		scrollPane.setOpaque(false);

		fileList = new JList<String>();
		scrollPane.setViewportView(fileList);

		JPanel panel = new JPanel();
		panel_16.add(panel);
		panel.setOpaque(false);
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));

		JButton btnNewButton = new JButton("追加");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addFile();
			}
		});
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(btnNewButton);

		JButton btnNewButton_1 = new JButton("削除");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				delFile();
			}
		});
		panel.add(btnNewButton_1);

		JPanel panel_21 = new JPanel();
		panel_21.setOpaque(false);
		panel_2.add(panel_21);

		JLabel lblNewLabel_1 = new JLabel("辞書を更新した場合、次回の起動で使用されます");
		panel_21.add(lblNewLabel_1);

		ButtonGroup group = new ButtonGroup();
		group.add(this.btnText);
		group.add(this.btnFormula);
	}

	private void selectDict(ConfigBraille.DICT type)
	{
		DefaultListModel<String> model = this.fileListMap.get(type);
		if (null != model)
		{
			this.fileList.setModel(model);
		}
	}

	private void addFile()
	{
		FileNameExtensionFilter[] extList = {new FileNameExtensionFilter("辞書ファイル(.xml)", "xml")};
		File file = Util.selectFile(null, "sample.xml", extList, false);
		if (null != file)
		{
			DefaultListModel<String> model = (DefaultListModel<String>) this.fileList.getModel();
			model.addElement(file.getAbsolutePath());
		}
	}

	private void delFile()
	{
		int index = this.fileList.getSelectedIndex();
		if (0 > index)
		{
			return;
		}
		DefaultListModel<String> model = (DefaultListModel<String>) this.fileList.getModel();
		model.remove(index);
	}

	public void setFiles(ConfigBraille.DICT type)
	{
		DefaultListModel<String> model = this.fileListMap.get(type);
		if (null == model)
		{
			model = new DefaultListModel<String>();
		}
		else
		{
			model.clear();
		}
		for (String file: type.getFiles())
		{
			model.addElement(file);
		}
		this.fileListMap.put(type, model);

		switch(type)
		{
		case TEXT:
			if (this.btnText.isSelected())
			{
				this.fileList.setModel(model);
			}
			break;
		case FORMULA:
			if (this.btnFormula.isSelected())
			{
				this.fileList.setModel(model);
			}
			break;
		}
	}

	public List<String> getFiles(ConfigBraille.DICT type)
	{
		List<String> list = Util.newArrayList();
		DefaultListModel<String> model = this.fileListMap.get(type);

		int nrow = model.size();
		for (int i = 0; i < nrow; i++)
		{
			String val = model.get(i);
			list.add(val);
		}

		return list;
	}

	public void setDotSize(DOT[] dots)
	{
		for (ConfigBraille.DOT dot: dots)
		{
			this.setDotSize(dot);
		}
	}

	public void setDotSize(ConfigBraille.DOT dot)
	{
		switch(dot)
		{
		case SMALL:
			this.dotSmall.setValue(dot.getSize());
			break;
		case MIDDLE:
			this.dotMiddle.setValue(dot.getSize());
			break;
		case LARGE:
			this.dotLarge.setValue(dot.getSize());
			break;
		case SPACE_X:
			this.dotSpaceX.setValue(dot.getSize());
			break;
		case SPACE_Y:
			this.dotSpaceY.setValue(dot.getSize());
			break;
		case BOX_SPACE:
			this.boxSpace.setValue(dot.getSize());
			break;
		case LINE_SPACE:
			this.lineSpace.setValue(dot.getSize());
			break;
		case DOT_SPAN:
			this.dotSpan.setValue(dot.getSize());
			break;
		}
	}

	public int getDotSize(ConfigBraille.DOT dot)
	{
		int val = 0;
		switch(dot)
		{
		case SMALL:
			val = (int) this.dotSmall.getValue();
			break;
		case MIDDLE:
			val = (int) this.dotMiddle.getValue();
			break;
		case LARGE:
			val = (int) this.dotLarge.getValue();
			break;
		case SPACE_X:
			val = (int) this.dotSpaceX.getValue();
			break;
		case SPACE_Y:
			val = (int) this.dotSpaceY.getValue();
			break;
		case BOX_SPACE:
			val = (int) this.boxSpace.getValue();
			break;
		case LINE_SPACE:
			val = (int) this.lineSpace.getValue();
			break;
		case DOT_SPAN:
			val = (int) this.dotSpan.getValue();
			break;
		}

		return val;
	}

	public void setDotType(ConfigBraille.DOT_TYPE[] types)
	{
		for (ConfigBraille.DOT_TYPE type: types)
		{
			switch(type)
			{
			case BRAILLE:
				this.brailleSize.setSelectedItem(type.getDot());
				break;
			case FIGURE:
				this.figureSize.setSelectedItem(type.getDot());
				break;
			}
		}

		//		for (ConfigBraille.DOT_TYPE type: types)
		//		{
		//			switch(type)
		//			{
		//			case BRAILLE:
		//				this.brailleSize.setSelectedItem(type.getType());
		//				break;
		//			case FIGURE:
		//				this.figureSize.setSelectedItem(type.getType());
		//				break;
		//			}
		//		}
	}

	public DOT getDotType(ConfigBraille.DOT_TYPE type)
	{
		ConfigBraille.DOT dot = null;
		switch(type)
		{
		case BRAILLE:
			dot = (DOT) this.brailleSize.getSelectedItem();
			break;
		case FIGURE:
			dot = (DOT) this.figureSize.getSelectedItem();
			break;
		}
		return dot;
	}

	public void setTextBraille(boolean flag)
	{
		this.textBraille.setSelected(flag);
	}

	public boolean getTextBraille()
	{
		return this.textBraille.isSelected();
	}
}
