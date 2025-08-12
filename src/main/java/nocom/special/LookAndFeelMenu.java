package nocom.special;

import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.DefaultEditorKit;

import org.misc.sqlminus.Constants;
import org.misc.sqlminus.SQLMinusPreferences;

public class LookAndFeelMenu extends JMenu implements ActionListener {

	private Vector rootComponents;
	private int mnemonic;
	private Color bg;
	private Vector<LookAndFeelMenu.MyMenuItem> menus;
	private final SQLMinusPreferences sqlMinusPreferences;

	public LookAndFeelMenu(Component rootComponent, int mnemonic, Color bg, SQLMinusPreferences sqlMinusPreferences) {
		this(new Component[] { rootComponent }, mnemonic, bg, true, sqlMinusPreferences);
	}

	public LookAndFeelMenu(Component[] rootComponents, int mnemonic, Color bg,
			SQLMinusPreferences sqlMinusPreferences) {
		this(rootComponents, mnemonic, bg, true, sqlMinusPreferences);
	}

	public LookAndFeelMenu(Component[] rootComponents, int mnemonic, Color bg, boolean addDefaults,
			SQLMinusPreferences sqlMinusPreferences) {
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
		this.sqlMinusPreferences = sqlMinusPreferences;
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
		addLookAndFeelItem("Pgs", "com.pagosoft.plaf.PgsLookAndFeel", KeyEvent.VK_P);
		addLookAndFeelItem("Aqua", "com.apple.laf.AquaLookAndFeel", KeyEvent.VK_A);
	}

	public void actionPerformed(ActionEvent ae) {
		try {
			for (int i = 0; i < menus.size(); i++) {
				if (ae.getActionCommand().equals(menus.get(i).getMenuText())) {
					String lookAndFeelClassName = menus.get(i).getClassName();
					setLookAndFeel(lookAndFeelClassName);
					sqlMinusPreferences.put(Constants.PreferencesKeys.LOOK_AND_FEEL_CLASS, lookAndFeelClassName);
					break;
				}
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	public void setSavedLookAndFeel() {
		try {
			String savedLookAndFeelClass = sqlMinusPreferences.get(Constants.PreferencesKeys.LOOK_AND_FEEL_CLASS,
					"javax.swing.plaf.nimbus.NimbusLookAndFeel");
			setLookAndFeel(savedLookAndFeelClass);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			System.err.println(e);
		}
	}

	private void setLookAndFeel(String lookAndFeelClassName) throws ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {
		UIManager.setLookAndFeel(lookAndFeelClassName);
		updateComponentTree();
		// Set cut, copy, paste commands to use CTRL or COMMAND as appropriate for the platform
		fixPlatformShortcuts();
		updateComponentTree();
	}
	
	private static void fixPlatformShortcuts() {
		// Set cut, copy, paste commands to use CTRL or COMMAND as appropriate for the platform
		int shortcutKey = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
		UIManager.put("TextField.focusInputMap", createPlatformInputMapFromExisting(new JTextField(), shortcutKey));
		UIManager.put("TextArea.focusInputMap", createPlatformInputMapFromExisting(new JTextArea(), shortcutKey));
		UIManager.put("TextPane.focusInputMap", createPlatformInputMapFromExisting(new JTextPane(), shortcutKey));
		UIManager.put("EditorPane.focusInputMap", createPlatformInputMapFromExisting(new JEditorPane(), shortcutKey));

	}

	private static InputMap createPlatformInputMapFromExisting(JComponent sample, int shortcutKey) {
	    // Get the current map so we preserve Delete, Backspace, navigation keys, etc.
	    InputMap existingMap = sample.getInputMap(JComponent.WHEN_FOCUSED);
	    InputMap newMap = new InputMap();

	    // Copy all existing bindings
	    KeyStroke[] keys = existingMap.allKeys();
	    if (keys != null) {
	        for (KeyStroke ks : keys) {
	            newMap.put(ks, existingMap.get(ks));
	        }
	    }

	    // Override only the shortcuts that need platform adaptation
	    newMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, shortcutKey), DefaultEditorKit.cutAction);
	    newMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, shortcutKey), DefaultEditorKit.copyAction);
	    newMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, shortcutKey), DefaultEditorKit.pasteAction);
	    newMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, shortcutKey), DefaultEditorKit.selectAllAction);

	    return newMap;
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
