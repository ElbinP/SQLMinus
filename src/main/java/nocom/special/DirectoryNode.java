package nocom.special;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

public class DirectoryNode extends DefaultMutableTreeNode {

	private File f;
	private boolean useFileHiding, checked = false;

	public DirectoryNode(File f, boolean useFileHiding) {
		super(f.getName());
		this.f = f;
		this.useFileHiding = useFileHiding;
		if (f.getName().equals("")) {
			setUserObject(f.toString());
		}
	}

	public URL getURL() throws MalformedURLException {
		return f.toURL();
	}

	public File getFile() {
		return f;
	}

	public File[] getFiles(File dir) {
		Vector files = new Vector();
		File[] names = dir.listFiles();
		File f;
		int nameCount = names == null ? 0 : names.length;
		for (int i = 0; i < nameCount; i++) {
			f = names[i];
			if (f.isFile() || f.isDirectory()) {
				if (!useFileHiding || !f.isHidden()) {
					files.addElement(f);
				}
			}
		}
		return (File[]) files.toArray(new File[files.size()]);
	}

	public boolean getUseFileHiding() {
		return useFileHiding;
	}

	public void setUseFileHiding(boolean flag) {
		useFileHiding = flag;
	}

	public boolean getIsChecked() {
		return checked;
	}

	public void setIsChecked(boolean flag) {
		checked = flag;
	}

	public void addDirectories() {
		checked = true;
		// File[]
		// file=FileSystemView.getFileSystemView().getFiles(getFile(),getUseFileHiding());
		File[] file = getFiles(getFile());
		for (int i = 0; i < file.length; i++) {
			if (file[i].isDirectory()) {
				DirectoryNode childnode = new DirectoryNode(file[i], getUseFileHiding());
				add(childnode);
			}
		}
	}

	public void addDirectoriesRecurseOnce() {
		checked = true;
		// File[]
		// file=FileSystemView.getFileSystemView().getFiles(getFile(),getUseFileHiding());
		File[] file = getFiles(getFile());
		for (int i = 0; i < file.length; i++) {
			if (file[i].isDirectory()) {
				DirectoryNode childnode = new DirectoryNode(file[i], getUseFileHiding());
				childnode.addDirectories();
				add(childnode);
			}
		}
	}

	public void addDirectoriesRecurse() {
		checked = true;
		// File[]
		// file=FileSystemView.getFileSystemView().getFiles(getFile(),getUseFileHiding());
		File[] file = getFiles(getFile());
		for (int i = 0; i < file.length; i++) {
			if (file[i].isDirectory()) {
				DirectoryNode childnode = new DirectoryNode(file[i], getUseFileHiding());
				childnode.addDirectoriesRecurse();
				add(childnode);
			}
		}
	}

}
