package nocom.special;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.ImageIcon;

public class ImageReader {

	/**
	 * Load an image from a jar
	 * 
	 * @param jar
	 *            the name of the jarfile
	 * @param image
	 *            the name of the image
	 */
	public static Image getImage(String jar, String image) throws IOException {
		JarFile jarFile = new JarFile(jar);
		JarEntry jarEntry = jarFile.getJarEntry(image);
		InputStream jarStream = jarFile.getInputStream(jarEntry);
		byte[] b = new byte[(int) jarEntry.getSize()];
		for (int i = 0; i < b.length; i++) {
			byte[] c = new byte[1];
			jarStream.read(c, 0, c.length);
			b[i] = c[0];
		}
		jarStream.close();
		return Toolkit.getDefaultToolkit().createImage(b, 0, b.length);
	}

	/**
	 * Load an image
	 * 
	 * @param image
	 *            the name of the image
	 * @param appClass
	 *            the image will be loaded by the classloader that loaded
	 *            <tt>appClass</tt>
	 * @return the image. If <tt>image</tt> was not found a FileNotFoundException
	 *         will be thrown
	 */
	public static Image getImage(Class appClass, String image) throws FileNotFoundException {
		URL url = appClass.getResource(image);
		try {
			return new ImageIcon(url).getImage();
		} catch (NullPointerException ne) {
			throw new FileNotFoundException(image + " not found");
		}
	}

}
