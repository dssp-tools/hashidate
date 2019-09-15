package dssp.hashidate.config;

import java.awt.Dimension;
import java.util.StringTokenizer;

import dssp.brailleLib.Util;

/**
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class PageInfo implements Cloneable, Comparable<PageInfo>
{
	private final String name;
	private final Dimension size;

	public PageInfo(String name, int w, int h)
	{
		this.name = name;
		this.size = new Dimension(w,h);
	}

	public PageInfo(String name, String text)
	{
		if (null == text)
		{
			throw new IllegalArgumentException("Null argument.");
		}
		StringTokenizer st = new StringTokenizer(text, "x");
		if (2 != st.countTokens())
		{
			throw new IllegalArgumentException(String.format("%s is illegal format.", text));
		}

		int w = Integer.parseInt(st.nextToken());
		int h = Integer.parseInt(st.nextToken());

		this.name = name;
		this.size = new Dimension(w,h);
	}

	public PageInfo clone()
	{
		PageInfo info = null;
		try
		{
			info = (PageInfo) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			Util.logException(e);
		}

		return info;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (null == obj || false == (obj instanceof PageInfo))
		{
			return false;
		}
		PageInfo info = (PageInfo) obj;
		boolean flag = (0 == this.compareTo(info) ? true: false);

		return flag;
	}

	@Override
	public int compareTo(PageInfo o)
	{
		int ret = this.name.compareTo(o.name);
		if (0 == ret)
		{
			ret = (this.size.width > o.size.width ? 1 : (this.size.width < o.size.width ? -1 : 0));
			if (0 == ret)
			{
				ret = (this.size.height > o.size.height ? 1 : (this.size.height < o.size.height ? -1 : 0));
			}
		}

		return ret;
	}

	public String getName()
	{
		return this.name;
	}

	String getSizeText()
	{
		return String.format("%dx%d", this.size.width, this.size.height);
	}

	public Dimension getSize()
	{
		return this.size;
	}

	public int getWidth()
	{
		return this.size.width;
	}

	public int getHeight()
	{
		return this.size.height;
	}
}
