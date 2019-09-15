package dssp.hashidate.config.gui;

import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.BoxLayout;

import java.awt.BasicStroke;
import java.awt.FlowLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;

import javax.swing.SwingConstants;
import javax.swing.JToggleButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import dssp.hashidate.config.Config;
import dssp.hashidate.misc.ColorButton;
import dssp.brailleLib.Util;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.Map;
import java.awt.GridLayout;

public class ConfigColorPanel extends JPanel
{
	//	private JButton btnLineColor;
	//	private JButton btnFrameColor;
	private ColorButton btnLineColor;
	private ColorButton btnFrameColor;
	private ColorButton btnPaintColor;
	private JPanel sampleArea;

	/**
	 * Create the panel.
	 */
	public ConfigColorPanel()
	{
		setOpaque(false);
		setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(20, 20, 0, 0));
		panel.setOpaque(false);
		add(panel, BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel panel_1 = new JPanel();
		panel_1.setOpaque(false);
		panel.add(panel_1);
		panel_1.setLayout(new BorderLayout(0, 0));

		JPanel panel_5 = new JPanel();
		panel_5.setOpaque(false);
		panel_5.setBorder(new EmptyBorder(0, 20, 5, 0));
		FlowLayout flowLayout = (FlowLayout) panel_5.getLayout();
		flowLayout.setAlignment(FlowLayout.LEADING);
		panel_1.add(panel_5, BorderLayout.SOUTH);

		JPanel panel_3 = new JPanel();
		panel_3.setOpaque(false);
		panel_5.add(panel_3);
		panel_3.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel label_2 = new JLabel("線・文字");
		label_2.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_3.add(label_2);

		//		btnLineColor = new JButton("");
		btnLineColor = new ColorButton("");
		btnLineColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectLineColor();
			}
		});
		btnLineColor.setPreferredSize(new Dimension(19, 19));
		panel_3.add(btnLineColor);

		JPanel panel_9 = new JPanel();
		panel_9.setOpaque(false);
		panel_5.add(panel_9);
		panel_9.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JLabel label_4 = new JLabel("内部");
		label_4.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_9.add(label_4);

		btnPaintColor = new ColorButton("");
		btnPaintColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectPaintColor();
			}
		});
		btnPaintColor.setPreferredSize(new Dimension(19, 19));
		panel_9.add(btnPaintColor);

		JPanel panel_8 = new JPanel();
		panel_8.setOpaque(false);
		panel_5.add(panel_8);

		JLabel label_5 = new JLabel("点字枠");
		label_5.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_8.add(label_5);

		btnBrailleColor = new ColorButton("");
		btnBrailleColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				selectBrailleColor();
			}
		});
		btnBrailleColor.setPreferredSize(new Dimension(19, 19));
		panel_8.add(btnBrailleColor);

		JPanel panel_4 = new JPanel();
		panel_4.setOpaque(false);
		panel_5.add(panel_4);

		JLabel label_3 = new JLabel("選択枠");
		label_3.setHorizontalAlignment(SwingConstants.RIGHT);
		panel_4.add(label_3);

		//		btnFrameColor = new JButton("");
		btnFrameColor = new ColorButton("");
		btnFrameColor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				selectFrameColor();
			}
		});
		btnFrameColor.setPreferredSize(new Dimension(19, 19));
		panel_4.add(btnFrameColor);

		JLabel label = new JLabel("色の既定値");
		panel_1.add(label, BorderLayout.NORTH);

		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new EmptyBorder(0, 0, 0, 20));
		panel_6.setOpaque(false);
		panel.add(panel_6);
		panel_6.setLayout(new GridLayout(1, 1, 0, 0));

		JPanel panel_11 = new JPanel();
		panel_11.setOpaque(false);
		panel_6.add(panel_11);
		panel_11.setLayout(new BorderLayout(0, 0));

		JLabel label_6 = new JLabel("線の種類");
		panel_11.add(label_6, BorderLayout.NORTH);

		JPanel panel_12 = new JPanel();
		panel_12.setOpaque(false);
		panel_12.setBorder(new EmptyBorder(5, 0, 5, 0));
		panel_11.add(panel_12, BorderLayout.SOUTH);

		JToggleButton toggleButton_4 = new JToggleButton("");
		toggleButton_4.setPreferredSize(new Dimension(30, 30));
		toggleButton_4.setEnabled(false);
		panel_12.add(toggleButton_4);

		JToggleButton toggleButton_5 = new JToggleButton("");
		toggleButton_5.setPreferredSize(new Dimension(30, 30));
		toggleButton_5.setEnabled(false);
		panel_12.add(toggleButton_5);

		JToggleButton toggleButton_6 = new JToggleButton("");
		toggleButton_6.setPreferredSize(new Dimension(30, 30));
		toggleButton_6.setEnabled(false);
		panel_12.add(toggleButton_6);

		JToggleButton toggleButton_7 = new JToggleButton("");
		toggleButton_7.setPreferredSize(new Dimension(30, 30));
		toggleButton_7.setEnabled(false);
		panel_12.add(toggleButton_7);

		JPanel panel_10 = new JPanel();
		panel_10.setOpaque(false);
		panel_6.add(panel_10);
		panel_10.setLayout(new BorderLayout(0, 0));

		JLabel label_1 = new JLabel("内部のパターン");
		panel_10.add(label_1, BorderLayout.NORTH);

		JPanel panel_7 = new JPanel();
		panel_10.add(panel_7);
		panel_7.setOpaque(false);
		panel_7.setBorder(new EmptyBorder(5, 0, 5, 0));

		JToggleButton toggleButton = new JToggleButton("");
		toggleButton.setEnabled(false);
		toggleButton.setPreferredSize(new Dimension(30, 30));
		panel_7.add(toggleButton);

		JToggleButton toggleButton_1 = new JToggleButton("");
		toggleButton_1.setEnabled(false);
		toggleButton_1.setPreferredSize(new Dimension(30, 30));
		panel_7.add(toggleButton_1);

		JToggleButton toggleButton_2 = new JToggleButton("");
		toggleButton_2.setEnabled(false);
		toggleButton_2.setPreferredSize(new Dimension(30, 30));
		panel_7.add(toggleButton_2);

		JToggleButton toggleButton_3 = new JToggleButton("");
		toggleButton_3.setEnabled(false);
		toggleButton_3.setPreferredSize(new Dimension(30, 30));
		panel_7.add(toggleButton_3);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new EmptyBorder(5, 20, 10, 20));
		panel_2.setOpaque(false);
		add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));

		sampleArea = new JPanel()
		{
			@Override
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);

				drawSample(g);
			}
		};
		sampleArea.setBackground(Color.WHITE);
		sampleArea.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_2.add(sampleArea, BorderLayout.CENTER);
	}

	private ColorButton btnBrailleColor;

	private void selectLineColor()
	{
		Color color = ColorButton.showColorChooser("線・文字の色", this.btnLineColor.getPaintColor());
		if (null != color)
		{
			this.btnLineColor.setPaintColor(color);
			this.sampleArea.repaint();
		}
	}

	private void selectPaintColor()
	{
		Color color = ColorButton.showColorChooser("内部の色", this.btnPaintColor.getPaintColor());
		if (null != color)
		{
			this.btnPaintColor.setPaintColor(color);
			this.sampleArea.repaint();
		}
	}

	private void selectBrailleColor()
	{
		Color color = ColorButton.showColorChooser("点字枠の色", this.btnBrailleColor.getPaintColor());
		if (null != color)
		{
			this.btnBrailleColor.setPaintColor(color);
			this.sampleArea.repaint();
		}
	}

	private void selectFrameColor()
	{
		Color color = ColorButton.showColorChooser("選択枠の色", this.btnFrameColor.getPaintColor());
		if (null != color)
		{
			this.btnFrameColor.setPaintColor(color);
			this.sampleArea.repaint();
		}
	}

	public void setColor(Map<Config.COLOR, Color> map)
	{
		for (Config.COLOR key: Config.COLOR.values())
		{
			Color color = map.get(key);
			switch(key)
			{
			case LINE:
				this.btnLineColor.setPaintColor(color);
				break;
			case PAINT:
				this.btnPaintColor.setPaintColor(color);
				break;
			case BRAILLE:
				this.btnBrailleColor.setPaintColor(color);
				break;
			case FRAME:
				this.btnFrameColor.setPaintColor(color);
				break;
			}
		}
	}

	public Map<Config.COLOR, Color> getColor()
	{
		Map<Config.COLOR, Color> map = Util.newHashMap();

		map.put(Config.COLOR.LINE, this.btnLineColor.getPaintColor());
		map.put(Config.COLOR.PAINT, this.btnPaintColor.getPaintColor());
		map.put(Config.COLOR.BRAILLE, this.btnBrailleColor.getPaintColor());
		map.put(Config.COLOR.FRAME, this.btnFrameColor.getPaintColor());

		return map;
	}

	private void drawSample(Graphics g)
	{
		if (null == g)
		{
			g = this.sampleArea.getGraphics();
		}
		Graphics2D g2 = (Graphics2D) g;

		Rectangle rect = this.sampleArea.getBounds();
		rect.x = rect.width/2 - 50;
		rect.y = rect.height/2 - 25;
		rect.width = 100;
		rect.height = 50;

		Config.BRAILLE dotSize = Config.getConfig(Config.BRAILLE.FIGURE);
		int size = Config.getConfig(dotSize);
		Stroke stroke = new BasicStroke(Util.mmToPixel(0, size));

		g2.setColor(this.btnLineColor.getPaintColor());
		Stroke back = g2.getStroke();
		g2.setStroke(stroke);
		g2.drawOval(rect.x, rect.y, rect.width, rect.height);
		g2.setStroke(back);

		g2.setColor(this.btnPaintColor.getPaintColor());
		g2.fillOval(rect.x+size, rect.y+size, rect.width-2*size, rect.height-2*size);

		g2.setColor(this.btnBrailleColor.getPaintColor());
		g2.drawRect(rect.x-5, rect.y-5, rect.width+10, rect.height+10);

		g2.setColor(this.btnFrameColor.getPaintColor());
		g2.drawRect(rect.x, rect.y, rect.width, rect.height);
	}
}
