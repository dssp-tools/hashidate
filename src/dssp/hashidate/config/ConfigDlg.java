package dssp.hashidate.config;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;

import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

class ConfigDlg extends JDialog
{

	private final JPanel contentPanel = new JPanel();
	private JTabbedPane tabbedPane;
	private boolean isOK = false;

	/**
	 * Create the dialog.
	 */
	ConfigDlg()
	{
		setModal(true);
		setTitle("設定");
		setResizable(false);
		setBounds(100, 100, 526, 436);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			contentPanel.add(tabbedPane);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBorder(new EmptyBorder(0, 0, 5, 0));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			{
				JButton okButton = new JButton("更新する");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						isOK = true;
						setVisible(false);
					}
				});
				okButton.setPreferredSize(new Dimension(90, 21));
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
			}
			{
				JButton cancelButton = new JButton("更新しない");
				cancelButton.setPreferredSize(new Dimension(90, 21));
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						isOK = false;
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
				getRootPane().setDefaultButton(cancelButton);
			}
		}
	}

	void addPane(String title, JPanel panel)
	{
		this.tabbedPane.addTab(title, panel);
	}

	boolean showDialog()
	{
		this.setVisible(true);
		return this.isOK;
	}

}
