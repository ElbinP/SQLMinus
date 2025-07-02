package nocom.special;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class LookAndFeelMenu extends JMenu implements ActionListener {

	private Vector rootComponents;
	private int mnemonic;
	private Color bg;
	private Vector<LookAndFeelMenu.MyMenuItem> menus;

	public LookAndFeelMenu(Component rootComponent, int mnemonic, Color bg) {
		this(new Component[] { rootComponent }, mnemonic, bg, true);
	}

	public LookAndFeelMenu(Component[] rootComponents, int mnemonic, Color bg) {
		this(rootComponents, mnemonic, bg, true);
	}

	public LookAndFeelMenu(Component[] rootComponents, int mnemonic, Color bg, boolean addDefaults) {
		super("LookAndFeel");
		setMnemonic(mnemonic);
		this.bg = bg;
		this.rootComponents = new Vector();
		for (int i = 0; i < rootComponents.length; i++) {
			this.rootComponents.add(rootComponents[i]);
		}
		menus = new Vector<>();
		if (addDefaults) {
			addDefaultMenus();
		}
	}

	public void addComponentToMonitor(Component component) {
		rootComponents.add(component);
	}

	public void removeComponentToMonitor(Component component) {
		for (int i = 0; i < rootComponents.size(); i++) {
			if (rootComponents.elementAt(i) == component)
				rootComponents.removeElementAt(i);
		}
	}

	public void addComponentsToMonitor(Component[] components) {
		for (int i = 0; i < components.length; i++) {
			rootComponents.add(components[i]);
		}
	}

	public void addLookAndFeelItem(String menuText, String className, int mnemonic) {
		JMenuItem item = new JMenuItem(menuText, mnemonic);
		item.addActionListener(this);
		add(item);
		item.setBackground(bg);
		menus.add(new LookAndFeelMenu.MyMenuItem(menuText, className));
	}

	private void addDefaultMenus() {
		addLookAndFeelItem("Windows", "com.sun.java.swing.plaf.windows.WindowsLookAndFeel", KeyEvent.VK_W);
		addLookAndFeelItem("Motif", "com.sun.java.swing.plaf.motif.MotifLookAndFeel", KeyEvent.VK_M);
		addLookAndFeelItem("Metal", "javax.swing.plaf.metal.MetalLookAndFeel", KeyEvent.VK_T);
		addLookAndFeelItem("Kunststoff", "com.incors.plaf.kunststoff.KunststoffLookAndFeel", KeyEvent.VK_K);
		addLookAndFeelItem("Nimbus", "javax.swing.plaf.nimbus.NimbusLookAndFeel", KeyEvent.VK_N);
	}

	public void actionPerformed(ActionEvent ae) {
		try {
			for (int i = 0; i < menus.size(); i++) {
				if (ae.getActionCommand().equals(menus.get(i).getMenuText())) {
					UIManager.setLookAndFeel(menus.get(i).getClassName());
					updateComponentTree();
					break;
				}
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	private void updateComponentTree() {
		for (int i = 0; i < rootComponents.size(); i++) {
			SwingUtilities.updateComponentTreeUI((Component) rootComponents.get(i));
		}
	}

	class MyMenuItem {
		private String menuText, className;

		public MyMenuItem(String menuText, String className) {
			this.menuText = menuText;
			this.className = className;
		}

		public String getMenuText() {
			return menuText;
		}

		public String getClassName() {
			return className;
		}
	}

}
