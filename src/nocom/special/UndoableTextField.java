package nocom.special;

import javax.swing.JTextField;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class UndoableTextField extends JTextField implements UndoableTextComponent {

	private UndoManager undoManager;

	public UndoableTextField() {
		undoManager = new UndoManager();
		getDocument().addUndoableEditListener(new UndoableEditListener() {
			public void undoableEditHappened(UndoableEditEvent e) {
				undoManager.addEdit(e.getEdit());
			}
		});
	}

	public void undo() throws CannotUndoException {
		undoManager.undo();
	}

	public void redo() throws CannotRedoException {
		undoManager.redo();
	}

	public String getUndoPresentationName() {
		return undoManager.getUndoPresentationName();
	}

	public String getRedoPresentationName() {
		return undoManager.getRedoPresentationName();
	}

	public void discardAllEdits() {
		undoManager.discardAllEdits();
	}

	public boolean canUndo() {
		return undoManager.canUndo();
	}

	public boolean canRedo() {
		return undoManager.canRedo();
	}

}
