package dssp.hashidate.misc;

import java.util.EmptyStackException;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;

import dssp.brailleLib.Util;
import dssp.hashidate.ObjectManager;
import dssp.hashidate.shape.DesignObject;

/**
 * UNDO/REDO機能を追加する
 *
 * @author yagi
 *
 */
public class ObjectUndoManager
{
	enum COMMAND {READY, ADD, CHANGE, DELETE, ZORDER, GROUP, UNGROUP};

	private class UndoInfo
	{
		private COMMAND com;
		private SortedMap<Integer, DesignObject> objMap = Util.newTreeMap();
		private int newIndex;

		@SuppressWarnings("unused")
		private UndoInfo()
		{
		}

		UndoInfo(COMMAND com)
		{
			this.com = com;
		}

		UndoInfo(COMMAND com, int index, DesignObject obj)
		{
			this(com);
			this.set(index, obj);
		}

		boolean hasObject()
		{
			return (0 < this.objMap.size());
		}

		void set(int index, DesignObject obj)
		{
			this.objMap.put(index, obj);
		}

		void unset(int index)
		{
			if (this.objMap.containsKey(index))
			{
				this.objMap.remove(index);
			}
		}

		void setNewIndex(int index)
		{
			this.newIndex = index;
		}

		int getNewIndex()
		{
			return this.newIndex;
		}

		void unsetAll()
		{
			this.objMap.clear();
		}

		DesignObject object(int index)
		{
			return this.objMap.get(index);
		}

		Set<Integer> indexes()
		{
			return this.objMap.keySet();
		}

		void setCom(COMMAND command)
		{
			this.com = command;
		}
	}

	private static ObjectUndoManager instance = null;
	private ObjectManager manager = null;
	private UndoInfo readyInfo = null;
	private Stack<UndoInfo> undoStack = Util.newStack();
	private Stack<UndoInfo> redoStack = Util.newStack();

	private ObjectUndoManager()
	{
	}

	public static ObjectUndoManager init(ObjectManager manager)
	{
		if (null == instance)
		{
			instance = new ObjectUndoManager();
		}
		instance.manager = manager;
		return instance;
	}

	public static ObjectUndoManager getInstance()
	{
		return instance;
	}

	/**
	 * 編集するオブジェクトの情報
	 *
	 * @author yagi
	 *
	 */
	public static class EditInfo
	{
		SortedMap<Integer, DesignObject> objList = Util.newTreeMap();
		public EditInfo()
		{

		}

		public EditInfo(int index, DesignObject obj)
		{
			this.objList.put(index, obj);
		}

		public void add(int index, DesignObject obj)
		{
			this.objList.put(index, obj);
		}

		public int count()
		{
			return this.objList.size();
		}

		public Set<Integer> indexes()
		{
			return this.objList.keySet();
		}

		public DesignObject getObject(int index)
		{
			return this.objList.get(index);
		}
	}

	/**
	 * 新しい操作：追加<br/>
	 * 番号が負の場合はなにもしない
	 *
	 * @param editInfo 追加したオブジェクトの情報
	 */
	public void add(EditInfo editInfo)
	{
		if (0 == editInfo.count())
		{
			return;
		}
		this.readyInfo = new UndoInfo(COMMAND.ADD);
		for (int index: editInfo.indexes())
		{
			this.readyInfo.set(index, editInfo.getObject(index));
		}
		this.fixEdit();
	}

	/**
	 * 新しい操作/継続操作：変更<br/>
	 */
	public void change()
	{
		this.readyInfo.setCom(COMMAND.CHANGE);
	}

	/**
	 * 新しい操作：Zオーダを変える
	 *
	 * @param index 新しい番号
	 * @param obj 変更したオブジェクト
	 */
	public void changeZorder(int index, DesignObject obj)
	{
		this.readyInfo.setCom(COMMAND.ZORDER);
		this.readyInfo.setNewIndex(index);
		this.fixEdit();
		this.readyInfo = new UndoInfo(COMMAND.READY, index, obj);
	}

	/**
	 * 新しい操作の準備<br/>
	 * 番号が負の場合はなにもしない
	 *
	 * @param index 変更したオブジェクトの番号
	 * @param oldObj 変更前のオブジェクトのクローン
	 */
	public void readyEdit(int index, DesignObject oldObj)
	{
		if (0 > index)
		{
			return;
		}
		if (null == this.readyInfo)
		{
			this.readyInfo = new UndoInfo(COMMAND.READY, index, oldObj);
		}
		else
		{
			this.readyInfo.set(index, oldObj);
		}
	}

	public void relaxEdit(int index)
	{
		if (0 > index)
		{
			return;
		}
		if (null == this.readyInfo)
		{
			return;
		}
		this.readyInfo.unset(index);
	}

	public void relaxAll()
	{
		if (null != this.readyInfo)
		{
			this.readyInfo.unsetAll();
		}
	}

	private void releaseReadyInfo()
	{
		this.readyInfo = null;
	}

	/**
	 * 新しい操作の確定：変更<br/>
	 * 番号が負の場合はなにもしない
	 */
	public void fixEdit()
	{
		if (null != this.readyInfo && COMMAND.READY != this.readyInfo.com)
		{
			this.undoStack.push(this.readyInfo);
			this.redoStack.clear();
			this.releaseReadyInfo();
		}
	}

	/**
	 * 新しい操作の中止：変更
	 */
	public void cancelEdit()
	{
		this.releaseReadyInfo();
	}

	/**
	 * 新しい操作：削除<br/>
	 */
	public void delete()
	{
		if (null == this.readyInfo || false == this.readyInfo.hasObject())
		{
			return;
		}
		this.readyInfo.setCom(COMMAND.DELETE);
		this.fixEdit();
	}

	/**
	 * 新しい操作：グループ化
	 *
	 * @param info グループオブジェクトの情報
	 */
	public void group(EditInfo info)
	{
		if (null == this.readyInfo || false == this.readyInfo.hasObject())
		{
			return;
		}
		this.readyInfo.setCom(COMMAND.DELETE);
		this.fixEdit();
		this.readyInfo = new UndoInfo(COMMAND.GROUP);
		for (int index: info.indexes())
		{
			this.readyInfo.set(index, info.getObject(index));
		}
		this.fixEdit();
	}

	/**
	 * 新しい操作：グループ解除
	 *
	 * @param info グループオブジェクトの情報
	 */
	public void ungroup(EditInfo info)
	{
		if (null == this.readyInfo || false == this.readyInfo.hasObject())
		{
			return;
		}
		this.readyInfo.setCom(COMMAND.DELETE);
		this.fixEdit();
		this.readyInfo = new UndoInfo(COMMAND.UNGROUP);
		for (int index: info.indexes())
		{
			this.readyInfo.set(index, info.getObject(index));
		}
		this.fixEdit();
	}

	/**
	 * 追加の取り消し
	 *
	 * @param info
	 */
	private void undoAdd(UndoInfo info)
	{
		// 追加と逆順に消す
		List<Integer> list = Util.newArrayList(info.indexes());
		for (ListIterator<Integer> it = list.listIterator(list.size()); it.hasPrevious();)
		{
			int index = it.previous();
			DesignObject obj = this.manager.delObject(index);
			info.set(index, obj);
		}
	}

	/**
	 * 追加のやり直し
	 *
	 * @param info
	 */
	private void redoAdd(UndoInfo info)
	{
		for (int index: info.indexes())
		{
			this.manager.insertObject(index, info.object(index));
		}
	}

	/**
	 * 削除の取り消し
	 * @param info
	 */
	private void undoDel(UndoInfo info)
	{
		for (int index: info.indexes())
		{
			this.manager.insertObject(index, info.object(index));
		}
	}

	/**
	 * 削除のやり直し
	 *
	 * @param info
	 */
	private void redoDel(UndoInfo info)
	{
		List<Integer> list = Util.newArrayList(info.indexes());
		for (ListIterator<Integer> it = list.listIterator(list.size()); it.hasPrevious();)
		{
			int index = it.previous();
			DesignObject obj = this.manager.delObject(index);
			info.set(index, obj);
		}
	}

	/**
	 * 変更の取り消し
	 *
	 * @param info
	 */
	private void undoChange(UndoInfo info)
	{
		for( int index: info.indexes())
		{
			DesignObject obj = this.manager.setObject(index, info.object(index));
			info.set(index, obj);
		}
	}

	/**
	 * 変更のやり直し
	 *
	 * @param info
	 */
	private void redoChange(UndoInfo info)
	{
		for (int index: info.indexes())
		{
			DesignObject obj = this.manager.setObject(index, info.object(index));
			info.set(index, obj);
		}
	}

	/**
	 * Zオーダの変更の取り消し
	 *
	 * @param info
	 */
	private void undoZorder(UndoInfo info)
	{
		int newIndex = info.getNewIndex();
		for (int index: info.indexes())
		{
			this.manager.delObject(newIndex);
			this.manager.insertObject(index, info.object(index));
			break;
		}
	}

	/**
	 * Zオーダの変更のやり直し
	 *
	 * @param info
	 */
	private void redoZorder(UndoInfo info)
	{
		int newIndex = info.getNewIndex();
		for (int index: info.indexes())
		{
			this.manager.delObject(index);
			this.manager.insertObject(newIndex, info.object(index));
			break;
		}
	}

	/**
	 * グループ化の取り消し
	 *
	 * @param groupInfo グループオブジェクト情報
	 * @param partsInfo 部品オブジェクト情報
	 */
	private void undoGroup(UndoInfo groupInfo, UndoInfo partsInfo)
	{
		this.undoAdd(groupInfo);
		this.undoDel(partsInfo);
	}

	/**
	 * グループ化のやり直し
	 *
	 * @param partsInfo 部品オブジェクト情報
	 * @param groupInfo グループオブジェクト情報
	 */
	private void redoGroup(UndoInfo groupInfo, UndoInfo partsInfo)
	{
		this.redoDel(partsInfo);
		this.redoAdd(groupInfo);
	}

	/**
	 * グループ解除の取り消し
	 *
	 * @param partsInfo 部品オブジェクト情報
	 * @param groupInfo グループオブジェクト情報
	 */
	private void undoUnGroup( UndoInfo partsInfo, UndoInfo groupInfo)
	{
		this.undoAdd(partsInfo);
		this.undoDel(groupInfo);
	}

	/**
	 * グループ解除のやり直し
	 *
	 * @param groupInfo グループオブジェクト情報
	 * @param partsInfo 部品オブジェクト情報
	 */
	private void redoUnGroup(UndoInfo partsInfo, UndoInfo groupInfo)
	{
		this.redoDel(groupInfo);
		this.redoAdd(partsInfo);
	}

	/**
	 * 取り消し
	 */
	public void undo()
	{
		try
		{
			UndoInfo info = this.undoStack.pop();
			if (null == info || null == info.com)
			{
				Util.error("No command");
			}
			UndoInfo secondInfo = null;
			switch(info.com)
			{
			case READY:
				break;
			case ADD:
				undoAdd(info);
				break;
			case DELETE:
				undoDel(info);
				break;
			case CHANGE:
				undoChange(info);
				break;
			case ZORDER:
				undoZorder(info);
				break;
			case GROUP:
				secondInfo = this.undoStack.pop();
				undoGroup(info, secondInfo);
				break;
			case UNGROUP:
				secondInfo = this.undoStack.pop();
				undoUnGroup(info, secondInfo);
				break;
			}
			if (null != secondInfo)
			{
				this.redoStack.push(secondInfo);
			}
			this.redoStack.push(info);
		}
		catch (EmptyStackException e)
		{
		}
	}

	/**
	 * やり直し
	 */
	public void redo()
	{
		try
		{
			UndoInfo info = this.redoStack.pop();
			UndoInfo secondInfo = null;
			switch(info.com)
			{
			case READY:
				break;
			case ADD:
				redoAdd(info);
				break;
			case DELETE:
				redoDel(info);
				break;
			case CHANGE:
				redoChange(info);
				break;
			case ZORDER:
				redoZorder(info);
				break;
			case GROUP:
				secondInfo = this.redoStack.pop();
				redoGroup(info, secondInfo);
				break;
			case UNGROUP:
				secondInfo = this.redoStack.pop();
				redoUnGroup(info, secondInfo);
				break;
			}
			if (null != secondInfo)
			{
				this.undoStack.push(secondInfo);
			}
			this.undoStack.push(info);
		}
		catch (EmptyStackException e)
		{
		}
	}
}
