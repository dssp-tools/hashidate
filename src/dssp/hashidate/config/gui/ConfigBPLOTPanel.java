package dssp.hashidate.config.gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JSpinner;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;

import javax.swing.border.EmptyBorder;
import javax.swing.JCheckBox;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Map;

import javax.swing.SpinnerNumberModel;

import dssp.brailleLib.Util;
import dssp.hashidate.config.Config;
import dssp.hashidate.config.ConfigBPLOT;
import dssp.hashidate.misc.ColorButton;

import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.UIManager;
import javax.swing.JRadioButton;
import javax.swing.JButton;
import javax.swing.ImageIcon;

import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JTextField;

public class ConfigBPLOTPanel extends JPanel
{
	private JCheckBox upsideDown;
	private JSpinner A4width;
	private JSpinner A4height;
	private JSpinner B5width;
	private JSpinner B5height;
	private JCheckBox showPageFrame;
	private ColorButton pageFrameColor;
	private JSpinner mirror;
	private JRadioButton outputFile;
	private JRadioButton printOut;
	private JComboBox<String> printer;
	private JCheckBox downSideFirst;
	private JTextField exePath;
	private JCheckBox useNABCC;

	public ConfigBPLOTPanel() {
		setBorder(new EmptyBorder(20, 20, 0, 20));
		setOpaque(false);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{385, 0};
		gridBagLayout.rowHeights = new int[]{0, 94, 0, 42, 0, 0};
		gridBagLayout.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

		JPanel panel_2 = new JPanel();
		panel_2.setOpaque(false);
		panel_2.setBorder(new TitledBorder(null, "\u7528\u7D19", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
		gbc_panel_2.insets = new Insets(0, 0, 5, 0);
		gbc_panel_2.fill = GridBagConstraints.BOTH;
		gbc_panel_2.gridx = 0;
		gbc_panel_2.gridy = 0;
		add(panel_2, gbc_panel_2);
		panel_2.setLayout(new GridLayout(0, 1, 0, 0));

		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new EmptyBorder(0, 10, 0, 0));
		panel_3.setOpaque(false);
		panel_2.add(panel_3);

		JLabel lblNewLabel = new JLabel("A4");
		panel_3.add(lblNewLabel);

		A4width = new JSpinner();
		A4width.setModel(new SpinnerNumberModel(204, 1, 999, 1));
		panel_3.add(A4width);

		JLabel lblNewLabel_1 = new JLabel("[mm] ×");
		panel_3.add(lblNewLabel_1);

		A4height = new JSpinner();
		A4height.setModel(new SpinnerNumberModel(234, 1, 999, 1));
		panel_3.add(A4height);

		JLabel lblNewLabel_2 = new JLabel("[mm]");
		panel_3.add(lblNewLabel_2);

		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new EmptyBorder(0, 10, 0, 0));
		panel_4.setOpaque(false);
		panel_2.add(panel_4);

		JLabel lblB = new JLabel("B5");
		panel_4.add(lblB);

		B5width = new JSpinner();
		B5width.setModel(new SpinnerNumberModel(204, 1, 999, 1));
		panel_4.add(B5width);

		JLabel label_3 = new JLabel("[mm] ×");
		panel_4.add(label_3);

		B5height = new JSpinner();
		B5height.setModel(new SpinnerNumberModel(234, 1, 999, 1));
		panel_4.add(B5height);

		JLabel label_4 = new JLabel("[mm]");
		panel_4.add(label_4);

		JPanel panel_6 = new JPanel();
		panel_6.setOpaque(false);
		panel_6.setBorder(new EmptyBorder(0, 10, 0, 0));
		panel_2.add(panel_6);

		showPageFrame = new JCheckBox("用紙枠を表示する");
		showPageFrame.setOpaque(false);
		showPageFrame.setSelected(true);
		panel_6.add(showPageFrame);

		pageFrameColor = new ColorButton("");
		pageFrameColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectPageFrameColor();
			}
		});
		pageFrameColor.setText(" ");
		panel_6.add(pageFrameColor);

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "\u51FA\u529B", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_1.setOpaque(false);
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel_1.insets = new Insets(0, 0, 5, 0);
		gbc_panel_1.anchor = GridBagConstraints.NORTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 1;
		add(panel_1, gbc_panel_1);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));

		JPanel panel_9 = new JPanel();
		panel_9.setOpaque(false);
		panel_1.add(panel_9);
		panel_9.setLayout(new BoxLayout(panel_9, BoxLayout.X_AXIS));

		JPanel panel_14 = new JPanel();
		panel_14.setOpaque(false);
		panel_9.add(panel_14);
		panel_14.setLayout(new GridLayout(0, 1, 0, 0));

		outputFile = new JRadioButton("ファイル");
		outputFile.setOpaque(false);
		panel_14.add(outputFile);

		printOut = new JRadioButton("印字");
		panel_14.add(printOut);
		printOut.setOpaque(false);

		JPanel panel_5 = new JPanel();
		panel_5.setOpaque(false);
		panel_9.add(panel_5);
		panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));

		JPanel panel_13 = new JPanel();
		panel_13.setOpaque(false);
		panel_5.add(panel_13);
		panel_13.setLayout(new BoxLayout(panel_13, BoxLayout.Y_AXIS));

		JPanel panel_11 = new JPanel();
		panel_11.setOpaque(false);
		panel_13.add(panel_11);
		FlowLayout flowLayout = (FlowLayout) panel_11.getLayout();
		flowLayout.setAlignment(FlowLayout.LEADING);

		JLabel lblBplot = new JLabel("EXEファイル");
		panel_11.add(lblBplot);

		exePath = new JTextField();
		panel_11.add(exePath);
		exePath.setColumns(18);

		JButton btnNewButton = new JButton(new ImageIcon(ConfigBPLOTPanel.class.getResource("/img/folder_16x16.png")));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectPath();
			}
		});
		panel_11.add(btnNewButton);
		//		btnNewButton.setIcon(new ImageIcon(ConfigBPLOTPanel.class.getResource("/img/folder_16x16.png")));

		JPanel panel_12 = new JPanel();
		panel_12.setOpaque(false);
		FlowLayout flowLayout_2 = (FlowLayout) panel_12.getLayout();
		flowLayout_2.setAlignment(FlowLayout.LEADING);
		panel_13.add(panel_12);

		JLabel label = new JLabel("プリンタ");
		panel_12.add(label);

		printer = new JComboBox<String>();
		panel_12.add(printer);

		JPanel panel_16 = new JPanel();
		panel_16.setOpaque(false);
		panel_16.setBorder(new TitledBorder(null, "\u88CF\u70B9", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel_16 = new GridBagConstraints();
		gbc_panel_16.insets = new Insets(0, 0, 5, 0);
		gbc_panel_16.fill = GridBagConstraints.BOTH;
		gbc_panel_16.gridx = 0;
		gbc_panel_16.gridy = 2;
		add(panel_16, gbc_panel_16);

		JLabel lblmm = new JLabel("裏点の位置補正");
		panel_16.add(lblmm);

		mirror = new JSpinner();
		panel_16.add(mirror);
		mirror.setModel(new SpinnerNumberModel(0.0, 0.0, 10.0, 0.0));

		JLabel lblmm_1 = new JLabel("[mm]");
		panel_16.add(lblmm_1);

		downSideFirst = new JCheckBox("裏点を先に出力する");
		downSideFirst.setOpaque(false);
		panel_16.add(downSideFirst);

		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setBorder(new TitledBorder(null, "\u63CF\u753B\u5C5E\u6027", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.anchor = GridBagConstraints.NORTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 3;
		add(panel, gbc_panel);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		upsideDown = new JCheckBox("原点を左下にとる");
		panel.add(upsideDown);
		upsideDown.setSelected(true);
		upsideDown.setOpaque(false);

		useNABCC = new JCheckBox("NABCCを出力する");
		useNABCC.setSelected(true);
		panel.add(useNABCC);

		for (ConfigBPLOT.PRINTER_NAME name: ConfigBPLOT.PRINTER_NAME.values())
		{
			printer.addItem(name.name());
		}

		ButtonGroup group = new ButtonGroup();
		group.add(this.outputFile);
		group.add(this.printOut);
	}

	public void setSize(Map<Config.BPLOT, Dimension> pages)
	{
		{
			Dimension size = pages.get(Config.BPLOT.PAGE_A4);
			this.A4width.setValue(size.width);
			this.A4height.setValue(size.height);
		}
		{
			Dimension size = pages.get(Config.BPLOT.PAGE_B5);
			this.B5width.setValue(size.width);
			this.B5height.setValue(size.height);
		}
	}

	public void getSize(Map<Config.BPLOT, Dimension> pages)
	{
		{
			int w = (int) this.A4width.getValue();
			int h = (int) this.A4height.getValue();
			pages.put(Config.BPLOT.PAGE_A4, new Dimension(w,h));
		}
		{
			int w = (int) this.B5width.getValue();
			int h = (int) this.B5height.getValue();
			pages.put(Config.BPLOT.PAGE_B5, new Dimension(w,h));
		}
	}

	public void setShowPageFrame(boolean flag)
	{
		this.showPageFrame.setSelected(flag);
	}

	public boolean getShowPageFrame()
	{
		return this.showPageFrame.isSelected();
	}

	public void setPageFrameColor(Color color)
	{
		this.pageFrameColor.setPaintColor(color);
	}

	public Color getPageFrameColor()
	{
		return this.pageFrameColor.getPaintColor();
	}

	public void setUpsideDown(boolean flag)
	{
		this.upsideDown.setSelected(flag);
	}

	public boolean getUpsideDown()
	{
		return this.upsideDown.isSelected();
	}

	private void selectPageFrameColor()
	{
		Color color = ColorButton.showColorChooser("用紙枠の色", this.pageFrameColor.getPaintColor());
		if (null != color)
		{
			this.pageFrameColor.setPaintColor(color);
		}
	}

	public double getMirror()
	{
		return (Double) mirror.getValue();
	}

	public void setMirror(double val)
	{
		this.mirror.setValue(val);
	}

	private void selectPath()
	{
		String fileName = this.exePath.getText();

		FileNameExtensionFilter[] extList = {new FileNameExtensionFilter("EXEファイル(*.exe)", "exe")};
		File file = Util.selectFile(fileName, "BPlot.exe", extList, false);
		if (null != file)
		{
			this.exePath.setText(file.getAbsolutePath());
		}
	}

	public boolean isOutputFile()
	{
		return this.outputFile.isSelected();
	}

	public void setOutputFile(boolean outputFile)
	{
		this.outputFile.setSelected(outputFile);
		this.printOut.setSelected(!outputFile);
	}

	public String getExePath()
	{
		return this.exePath.getText();
	}

	public void setExePath(String exePath)
	{
		this.exePath.setText(exePath);
	}

	public String getPrinter()
	{
		return this.printer.getSelectedItem().toString();
	}

	public void setPrinter(String printer)
	{
		this.printer.setSelectedItem(printer);
	}

	public boolean isDownsideFirst()
	{
		return this.downSideFirst.isSelected();
	}

	public void setDownsideFirst(boolean downsideFirst)
	{
		this.downSideFirst.setSelected(downsideFirst);
	}

	public boolean isUseNABCC()
	{
		return this.useNABCC.isSelected();
	}

	public void setUseNABCC(boolean useNABCC)
	{
		this.useNABCC.setSelected(useNABCC);
	}


}
