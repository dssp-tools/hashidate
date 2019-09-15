package dssp.hashidate.misc;

/*
 * 名前とオブジェクトの組
 */
public class Pair
{
	private String name;
	private Object value;
	public static final String SEPARATOR = "=";

	public Pair(String name, Object value)
	{
		this.name = name;
		this.value = value;
	}

	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public Object getValue()
	{
		return value;
	}
	public void setValue(Object value)
	{
		this.value = value;
	}

	public String toString()
	{
		return String.format("%s%s%s", this.name.toString(), SEPARATOR, (null == this.value ? "": this.value.toString()));
	}

	public static Pair fromString(String text)
	{
		if (null == text)
		{
			return null;
		}
		String[] tokens = text.split(SEPARATOR);
		switch(tokens.length)
		{
		case 0:
			return null;
		case 1:
			return new Pair(tokens[0].trim(), null);
		default:
			StringBuilder val = new StringBuilder();
			for (int i = 1; i < tokens.length; i++)
			{
				if (1 < i)
				{
					val.append(SEPARATOR);
				}
				val.append(tokens[i]);
			}
			return new Pair(tokens[0].trim(), val.toString());
		}
	}
}
