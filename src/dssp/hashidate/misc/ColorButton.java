package dssp.hashidate.misc;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;

import dssp.brailleLib.Util;

/**
 * 色塗りボタン
 *
 * @author yagi
 *
 */
public class ColorButton extends JButton
{
	private boolean pushed = false;
	private Color paintColor;

	public ColorButton(String label)
	{
		this.setText(label);

		addMouseListener(new MouseAdapter()
		{
			public void mousePressed(MouseEvent e)
			{
				pushed = true;
			};
			public void mouseReleased(MouseEvent e)
			{
				pushed = false;
			};
		});
	};

	/**
	 * 塗りつぶし色を設定する
	 *
	 * @param color 塗りつぶし色
	 */
	public void setPaintColor(Color color)
	{
		this.paintColor = color;
	}

	/**
	 * 塗りつぶし色を取得する
	 *
	 * @return 塗りつぶし色
	 */
	public Color getPaintColor()
	{
		return this.paintColor;
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		if (null != this.paintColor && false == pushed)
		{
			g.setColor(this.paintColor);
			g.fillRect(3, 3, getWidth()-6, getHeight()-6);
		}
		g.drawString(getText(), 10, getHeight() / 2);
	};

	private static boolean isOK = false;
	private static JColorChooser chooser = new JColorChooser();
	private static JDialog chooserDlg = null;

	public static Color showColorChooser(String title, Color color)
	{
		ColorButton.isOK = false;
		ColorButton.chooser.setColor(color);
		if (null == ColorButton.chooserDlg)
		{
			ColorButton.chooserDlg = JColorChooser.createDialog(null, title, true, chooser, new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					isOK = true;
				}
			}, new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					isOK = false;
				}
			});
		}
		Util.setLocationUnderMouse(ColorButton.chooserDlg);
		ColorButton.chooserDlg.setVisible(true);
		return (ColorButton.isOK ? chooser.getColor() : color);
	}

}
