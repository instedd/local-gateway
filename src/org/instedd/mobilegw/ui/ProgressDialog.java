package org.instedd.mobilegw.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import org.instedd.mobilegw.ProgressListener;

public class ProgressDialog extends JDialog implements ProgressListener
{
	private static final long serialVersionUID = 1L;
	private JLabel statusLabel;
	private JButton okButton;
	private JButton cancelButton;
	private boolean isCanceled = false;
	private JLabel icon;
	private static ImageIcon ajaxLoaderIcon = new ImageIcon(ProgressDialog.class.getResource("ajax-loader.gif"));
	private static ImageIcon successIcon = new ImageIcon(ProgressDialog.class.getResource("success.gif"));
	private static ImageIcon errorIcon = new ImageIcon(ProgressDialog.class.getResource("error.gif"));
	private Runnable cancelListener;
	

	public ProgressDialog(Window owner)
	{
		super(owner);
		initialize();
	}
	
	private void initialize()
	{
		setSize(400, 150);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setLocationRelativeTo(getOwner());
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(5, 5, 5, 5);
		
		c.weightx = 0;
		c.ipadx = 15;
		add(icon = new JLabel(ajaxLoaderIcon), c);
		
		c.weightx = 1;
		c.weighty = 1;
		c.fill = GridBagConstraints.VERTICAL;
		add(statusLabel = new JLabel(""), c);

		c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		c.weighty = 0;
		c.weightx = 1;
		c.gridwidth = 2;
		add(cancelButton = new JButton(new CancelAction()), c);
		add(okButton = new JButton(new OkAction()), c);
		okButton.setVisible(false);
	}

	@Override
	public void completed(boolean successful)
	{
		if (isCanceled)
			setVisible(false);
		
		cancelButton.setVisible(false);
		okButton.setVisible(true);
		
		if (successful)
			icon.setIcon(successIcon);
		else
			icon.setIcon(errorIcon);
	}

	@Override
	public boolean isCanceled()
	{
		return isCanceled;
	}

	@Override
	public void start(String title)
	{
		setTitle(title);
		new Thread(new Runnable() {
			public void run()
			{
				setVisible(true);		
			} 
		}).start();
	}

	@Override
	public void statusChange(String status)
	{
		statusLabel.setText(status);
	}
	
	@Override
	public void setCancelListener(Runnable cancelListener)
	{
		this.cancelListener = cancelListener;
	}
	
	@Override
	public void end()
	{
		if (isCanceled)
			setVisible(false);
	}
	
	@SuppressWarnings("serial")
	private class CancelAction extends AbstractAction
	{
		public CancelAction()
		{
			this.putValue(NAME, "Cancel");
		}
		@Override
		public void actionPerformed(ActionEvent e)
		{
			isCanceled = true;
			putValue(NAME, "Canceling...");
			setEnabled(false);
			if (cancelListener != null)
				cancelListener.run();
		}
	}
	
	@SuppressWarnings("serial")
	private class OkAction extends AbstractAction
	{
		public OkAction()
		{
			this.putValue(NAME, "Ok");
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			setVisible(false);
		}	
	}
}