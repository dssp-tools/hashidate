package dssp.hashidate.misc;

import java.util.ArrayList;
import java.util.List;

import dssp.brailleLib.Util;

public class PairList extends ArrayList<Pair>
{
	public static final String SEPARATOR = "|";

	public List<String> getNames()
	{
		List<String> names = Util.newArrayList();
		for (Pair pair: this)
		{
			names.add(pair.getName());
		}

		return names;
	}

	/**
	 * 名前に該当する値のリストを取得する
	 *
	 * @param name
	 * @return 値が無い場合は長さ0のリストを返す
	 */
	public List<Object> getValues(String name)
	{
		List<Object> list = Util.newArrayList();
		for (Pair pair: this)
		{
			if (pair.getName().equals(name))
			{
				list.add(pair.getValue());
			}
		}

		return list;
	}

	public void add(String name, Object value)
	{
		this.add(new Pair(name, value));
	}

	/**
	 * SEPARATORでPair.toString()をつなげて文字列にする
	 */
	@Override
	public String toString()
	{
		StringBuilder buf = new StringBuilder();
		for (Pair pair: this)
		{
			if (0 < buf.length())
			{
				buf.append(SEPARATOR);
			}
			buf.append(pair.toString());
		}

		return buf.toString();
	}

	public static PairList fromString(String text)
	{
		PairList list = new PairList();

		String[] pairs = text.split("\\" + SEPARATOR);
		Pair prev = null;
		for (String ptext: pairs)
		{
			if (null != prev && ptext.isEmpty())
			{
				Object v = prev.getValue();
				if (null == v)
				{
					prev.setValue(SEPARATOR);
				}
				else
				{
					prev.setValue(v.toString() + SEPARATOR);
				}
			}
			Pair p = Pair.fromString(ptext);
			if (null != p)
			{
				list.add(p);
			}

			prev = p;
		}

		return list;
	}
}
