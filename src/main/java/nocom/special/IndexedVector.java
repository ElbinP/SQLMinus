package nocom.special;

import java.util.Vector;

/**
 * A class that implements something similar to a history for components like
 * textfields or textareas.
 */
public class IndexedVector extends Vector<String> {

	private int selectedIndex;

	public IndexedVector() {
		clearHistory();
	}

	public void clearHistory() {
		removeAllElements();
		insertElementAt("", 0);
		selectedIndex = 0;
	}

	/*
	 * The item at index 0 is the current item or the working copy. The items from
	 * index 1 actually represent the history, the item at index 1 being the newest.
	 */
	public void insertString(String str) {
		if (str.trim().length() > 0) {
			String temp;
			for (int i = 1; i < size(); i++) {
				if (elementAt(i).toString().equals(str)) {
					temp = elementAt(i);
					removeElementAt(i);
					insertElementAt(temp, 1);
					removeElementAt(0);
					insertElementAt(temp, 0);
					selectedIndex = 1;
					// printDebugInfo();
					return;
				}
			}
			insertElementAt(str, 1);
			removeElementAt(0);
			insertElementAt(str, 0);
			selectedIndex = 1;
		}
	}

	public String getPrevious(String currentItem) throws VectorIndexOutOfBoundsException {
		setCurrentItem(currentItem);
		if (selectedIndex < (size() - 1)) {
			selectedIndex++;
		} else {
			throw new VectorIndexOutOfBoundsException("No previous item");
		}
		// printDebugInfo();
		return elementAt(selectedIndex).toString();
	}

	public String getNext(String currentItem) throws VectorIndexOutOfBoundsException {
		setCurrentItem(currentItem);
		if (selectedIndex > 0) {
			selectedIndex--;
		} else {
			throw new VectorIndexOutOfBoundsException("No next item");
		}
		// printDebugInfo();
		return elementAt(selectedIndex).toString();
	}

	/***
	 * Remove the entry at the specified index. Resets the selected index to the
	 * working copy at 0
	 * 
	 * @param index
	 */
	public void deleteStringAt(int index) {
		removeElementAt(index);
		selectedIndex = 0;
	}

	public boolean canGetPrevious() {
		return selectedIndex < (size() - 1);
	}

	public boolean canGetNext() {
		return selectedIndex > 0;
	}

	private void setCurrentItem(String currentItem) {
		if (!elementAt(selectedIndex).equals(currentItem)) {
			removeElementAt(0);
			insertElementAt(currentItem, 0);
			selectedIndex = 0;
		}
	}

	private void printDebugInfo() {
		System.out.println("----------------------");
		System.out.println("Selected Index=" + selectedIndex);
		for (int i = 0; i < size(); i++) {
			System.out.println(i + " = " + elementAt(i));
		}
		System.out.println("----------------------");
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(int index) throws VectorIndexOutOfBoundsException {
		if (index > size() - 1) {
			throw new VectorIndexOutOfBoundsException("index " + index + "greater than size of vector " + size());
		}
		selectedIndex = index;
	}

}
