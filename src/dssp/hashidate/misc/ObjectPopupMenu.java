package dssp.hashidate.misc;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import dssp.hashidate.MainFrame;
import dssp.hashidate.shape.DesignObject;

public class ObjectPopupMenu extends JPopupMenu implements ActionListener
{
	static MainFrame mainFrame;
	DesignObject target;

	public ObjectPopupMenu()
	{
	}

	public ObjectPopupMenu(String label)
	{
		super(label);
	}

	public static void setMainFrame(MainFrame mainFrame)
	{
		ObjectPopupMenu.mainFrame = mainFrame;
	}

	public void actionPerformed(ActionEvent arg0)
	{
		if (null == target)
		{
			return;
		}
		this.setVisible(false);
		JMenuItem src = (JMenuItem) arg0.getSource();
		mainFrame.menuCalled(src.getText(), target, this.getLocation());
	}

	public void callback(DesignObject.StatusHint hint)
	{
		mainFrame.notifyHint(hint);
	}

	@Override
	public JMenuItem add(JMenuItem item)
	{
		super.add(item);
		item.addActionListener(this);
		return item;
	}

	public void show(DesignObject target, Point location)
	{
		this.target = target;
		this.setLocation(location);
		this.setVisible(true);
	}
}
