package dssp.hashidate.misc;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import dssp.brailleLib.Util;

/**
 * JTextComponentにUNDO/REDO機能を追加する
 *
 * @author yagi
 *
 */
public class UndoFactory extends KeyAdapter implements UndoableEditListener
{
	private static Map<JTextComponent, UndoFactory> undoMap = Util.newHashMap();
	private UndoManager undo = new UndoManager();
	private int undoKey = KeyEvent.VK_Z;
	private int redoKey = KeyEvent.VK_Y;

	/**
	 * JTextComponentにUNDO/REDO機能を追加する<br/>
	 * ・Ctrl+z UNDO<br/>
	 * ・Ctrl+y REDO
	 *
	 * @param comp 追加するJTextComponent
	 */
	public static void add(JTextComponent comp)
	{
		UndoFactory obj = new UndoFactory();
		comp.addKeyListener(obj);
		comp.getDocument().addUndoableEditListener(obj);

		undoMap.put(comp, obj);
	}

	/**
	 * JTextComponentにUNDO/REDO機能を追加する<br/>
	 * ・Ctrl+指定したキーでUNDO/REDOを行う
	 *
	 * @param comp 追加するJTextComponent
	 * @param undoKey UNDO用のキー
	 * @param redoKey REDO用のキー
	 */
	public static void add(JTextComponent comp, int undoKey, int redoKey)
	{
		UndoFactory obj = new UndoFactory(undoKey, redoKey);
		comp.addKeyListener(obj);
		comp.getDocument().addUndoableEditListener(obj);
	}

	/**
	 * UNDOをコードから実行する
	 *
	 * @param comp UNDO/REDO機能を追加したJTextComponent
	 */
	public static void undo(JTextComponent comp)
	{
		UndoFactory obj = undoMap.get(comp);
		if (null != obj)
		{
			obj.undo.undo();
		}
	}

	/**
	 * REDOをコードから実行する
	 *
	 * @param comp UNDO/REDO機能を追加したJTextComponent
	 */
	public static void redo(JTextComponent comp)
	{
		UndoFactory obj = undoMap.get(comp);
		if (null != obj)
		{
			obj.undo.redo();
		}
	}

	private UndoFactory()
	{
	}

	private UndoFactory(int undoKey, int redoKey)
	{
		if (0 != undoKey)
		{
			this.undoKey = undoKey;
		}
		if (0 != redoKey)
		{
			this.redoKey = redoKey;
		}
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		int key = arg0.getKeyCode();
		if (key == undoKey)
		{
			if (arg0.isControlDown() && undo.canUndo())
			{
				undo.undo();
				arg0.consume();
			}
		}
		else if (key == redoKey)
		{
			if (arg0.isControlDown() && undo.canRedo())
			{
				undo.redo();
				arg0.consume();
			}
		}
	}

	@Override
	public void undoableEditHappened(UndoableEditEvent arg0)
	{
		undo.addEdit(arg0.getEdit());
	}
}
