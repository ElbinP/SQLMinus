package nocom.special;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomizedMouseAdapter extends MouseAdapter implements ActionListener {

	private JTextComponent text;
	private JPopupMenu popup;
	private boolean forceClear;
	private JMenuItem cut, copy, paste, selectAll, clear;

	/**
	 * A mouse adapter for JTextComponents that will show a typical popupmenu with
	 * cut,copy,paste,selectall and clear menus for a right click event on the text
	 * component. The popup will be displayed only if the event source is a JTextComponent.
	 * <p>
	 * The <code>forceClear</code> argument, if true, would enable the clear menu
	 * even when the JTextComponent is not editable.
	 */
	public CustomizedMouseAdapter(boolean forceClear) {
		super();
		this.forceClear = forceClear;

		cut = new JMenuItem("Cut");
		copy = new JMenuItem("Copy");
		paste = new JMenuItem("Paste");
		selectAll = new JMenuItem("Select All");
		clear = new JMenuItem("Clear");

		cut.addActionListener(this);
		copy.addActionListener(this);
		paste.addActionListener(this);
		selectAll.addActionListener(this);
		clear.addActionListener(this);

		popup = new JPopupMenu();
		popup.add(cut);
		popup.add(copy);
		popup.add(paste);
		popup.add(selectAll);
		popup.addSeparator();
		popup.add(clear);
	}

	/**
	 * The <code>text</code> parameter is for compatibility with older code, it is
	 * not used anymore.
	 */
	public CustomizedMouseAdapter(javax.swing.text.JTextComponent text) {
		this(false);
	}

	public JPopupMenu getPopupMenu() {
		return popup;
	}

	public void actionPerformed(ActionEvent ae) {
		if (text == null)
			return;
		String command = ae.getActionCommand();
		if (command.equals("Cut")) {
			text.cut();
		} else if (command.equals("Copy")) {
			text.copy();
		} else if (command.equals("Paste")) {
			text.paste();
		} else if (command.equals("Clear")) {
			text.setText("");
		} else if (command.equals("Select All")) {
			text.selectAll();
		}
		text = null;
	}

	public void mouseReleased(MouseEvent me) {
		Component eventSource = me.getComponent();
		if (eventSource instanceof JTextComponent) {
			text = (JTextComponent) eventSource;
			if (me.getModifiers() == me.BUTTON3_MASK && text.isEnabled()) {
				cut.setEnabled(text.isEditable());
				paste.setEnabled(text.isEditable());
				if (forceClear)
					clear.setEnabled(true);
				else
					clear.setEnabled(text.isEditable());
				popup.show(me.getComponent(), me.getX(), me.getY());
			}
		}
	}

}
