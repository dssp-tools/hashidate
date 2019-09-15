package dssp.hashidate.misc;

import java.awt.Desktop;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import dssp.brailleLib.CheckInfo;
import dssp.brailleLib.Util;
import dssp.brailleLib.CheckInfo.SoftInfo;
import dssp.hashidate.MainFrame;

public class About
{
//	private static String URL_TEXT = "http://fvis.uh-oh.jp";
	private static final String URL_TEXT = "http://dssp.sakura.ne.jp";
	private static final String URL_CHECK = "http://dssp.sakura.ne.jp/software/info.php?name=hashidate";

	private static enum STATUS {
		CHECKING("バージョンを確認中です"),
		LATEST("最新です"),
		NOT_LATEST("新しいバージョンがダウンロードできます"),
		DISABLE("更新を確認できませんでした");

		String message;
		STATUS(String message)
		{
			this.message = message;
		}
	};
	private static STATUS status;

	public About()
	{
	}

	public static void check()
	{
		CheckInfo checkInfo = new CheckInfo();
		About.status = STATUS.CHECKING;
		checkInfo.check(URL_CHECK, new CheckInfo.CheckInfoListener()
		{
			@Override
			public void checked(SoftInfo info)
			{
				if (null == info)
				{
					About.status = STATUS.DISABLE;
					Util.logWarning(About.status.message);
					return;
				}
				String val = info.getValue("major");
				int major = Integer.parseInt(val);
				val = info.getValue("minor");
				int minor = Integer.parseInt(val);
				if (major > MainFrame.VERSION[0] || minor > MainFrame.VERSION[1])
				{
					Util.notify("橋立 " + MainFrame.VERSION_FORMAT + "がダウンロードできます\n[DSSPホームページ]\n %s", major, minor, URL_TEXT);
					About.status = STATUS.NOT_LATEST;
				}
				else
				{
					About.status = STATUS.LATEST;
				}
			}
		});
	}

	public static void show()
	{
		String ABOUT = "<html><body>";
		ABOUT += "橋立 " + MainFrame.VERSION_FORMAT + "<br/>";
		ABOUT += "障害学生支援プロジェクト<br/>";
		ABOUT += "Disabled-student Study Support Project (DSSP)<br/>";
		ABOUT += "<a href=%s>%s</a><br/>";
		ABOUT += About.status.message;
		ABOUT += "</body></html>";

		String message = String.format(ABOUT, MainFrame.VERSION[0], MainFrame.VERSION[1], URL_TEXT, URL_TEXT);

		JEditorPane area = new JEditorPane("text/html", message);
		area.setOpaque(false);
		area.setEditable(false);
		area.addHyperlinkListener(new HyperlinkListener()
		{
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e)
			{
				if (MouseEvent.MOUSE_CLICKED == e.getInputEvent().getID())
				{
					Desktop desktop = Desktop.getDesktop();
					URI uri;
					try
					{
						uri = new URI(e.getURL().toString());
						desktop.browse(uri);
					}
					catch (URISyntaxException | IOException e1)
					{
						Util.logException(e1);
					}
				}
			}
		}
		);
		JOptionPane pane = null;
		if (null == MainFrame.icon)
		{
			pane = new JOptionPane(area, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);
		}
		else
		{
			pane = new JOptionPane(area, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, MainFrame.icon);
		}
		JDialog dlg = pane.createDialog("About");

		PointerInfo info = MouseInfo.getPointerInfo();
		Point p = info.getLocation();
		p.x -= dlg.getWidth()/2;
		p.y -= dlg.getHeight()/2;
		dlg.setLocation(p);

		dlg.setVisible(true);;
	}
}
