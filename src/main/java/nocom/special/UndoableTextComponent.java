package nocom.special;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public interface UndoableTextComponent {

	public void undo() throws CannotUndoException;

	public void redo() throws CannotRedoException;

	public String getUndoPresentationName();

	public String getRedoPresentationName();

	public void discardAllEdits();

	public boolean canUndo();

	public boolean canRedo();

}
