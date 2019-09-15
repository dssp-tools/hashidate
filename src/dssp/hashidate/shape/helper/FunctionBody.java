package dssp.hashidate.shape.helper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dssp.brailleLib.Util;
import dssp.brailleLib.XmlUtil;

public class FunctionBody implements Cloneable
{
	static interface ELEMENT
	{
	}

	static interface ClonableELEMENT extends ELEMENT, Cloneable
	{
		public ELEMENT clone();
	}

	static class VALUE implements ClonableELEMENT
	{
		double value;
		VALUE()
		{

		}
		VALUE(double value)
		{
			this.value = value;
		}

		@Override
		public VALUE clone()
		{
			try
			{
				VALUE obj = (VALUE) super.clone();
				obj.value = this.value;
				return obj;
			}
			catch (CloneNotSupportedException e)
			{
				Util.logException(e);
			}
			return null;
		}

		public String toString()
		{
			return Double.toString(value);
		}
	}

	static class FUNCTION implements ClonableELEMENT
	{
		String name;
		Method method;
		int nArg;

		FUNCTION(String name, Method method, int nArg)
		{
			this.name = name;
			this.method = method;
			this.nArg = nArg;
		}

		@Override
		public FUNCTION clone()
		{
			try
			{
				FUNCTION obj = (FUNCTION) super.clone();
				obj.name = new String(this.name);
				obj.method = this.method;
				obj.nArg = this.nArg;
				return obj;
			}
			catch (CloneNotSupportedException e)
			{
				Util.logException(e);
			}
			return null;
		}

		public String toString()
		{
			return String.format("%s[%d]", this.name, this.nArg);
		}
	}

	static class ESTACK implements ClonableELEMENT
	{
		Stack<ELEMENT> stack = Util.newStack();

		ESTACK()
		{
		}

		@Override
		public ESTACK clone()
		{
			try
			{
				ESTACK obj = (ESTACK) super.clone();
				for (ELEMENT e: this.stack)
				{
					if (e instanceof ClonableELEMENT)
					{
						obj.stack.add(((ClonableELEMENT)e).clone());
					}
					else
					{
						obj.stack.add(e);
					}
				}
				return obj;
			}
			catch (CloneNotSupportedException e1)
			{
				Util.logException(e1);
			}
			return null;
		}

		public String toString()
		{
			StringBuilder text = new StringBuilder();
			for (ELEMENT elm: this.stack)
			{
				if (0 < text.length())
				{
					text.append(",");
				}
				text.append(elm.toString());
			}
			return String.format("[%s]", text.toString());
		}
	}

	static List<FUNCTION> fList = Util.newArrayList();
	static
	{
		Method[] mlist = Math.class.getMethods();
		for (Method method: mlist)
		{
			Class<?>[] atypes = method.getParameterTypes();
			if (0 == atypes.length)
			{
				continue;
			}
			boolean flag = true;
			for (Class<?> type: atypes)
			{
				if (false == type.getSimpleName().equals("double"))
				{
					flag = false;
					break;
				}
			}
			if (flag)
			{
				FUNCTION f = new FUNCTION(method.getName(), method, atypes.length);
				fList.add(f);
			}
		}
	}

	static enum OPERAND implements ELEMENT
	{
		VAR_X,
		MULTI("*", 2, true),
		DIV("/", 2, true),
		ADD("+", 2, false),
		SUB("-", 2, false),
		MOD("%", 2, true),
		LN,
		LOG1("log1", 2, false),
		POW("pow", 2, false),
		SQRT("sqrt", 1, false),
		CBRT("cbrt", 1, false);

		private String operator = "";
		private int nVar = 1;
		boolean hasPriviledge = false;

		OPERAND()
		{
		}
		OPERAND(String operator, int nVar, boolean hasPriviledge)
		{
			this.operator = operator;
			this.nVar = nVar;
			this.hasPriviledge = hasPriviledge;
		}

		public String toString()
		{
			if (operator.isEmpty())
			{
				return this.name();
			}
			else
			{
				return this.operator;
			}
		}

		public int nVar()
		{
			return this.nVar;
		}
	}

	private Stack<ELEMENT> stack = Util.newStack();
	private static Map<String, Double> constTable = null;

	@Override
	public FunctionBody clone()
	{
		try
		{
			FunctionBody obj = (FunctionBody) super.clone();
			obj.stack = Util.newStack();
			for (ELEMENT e: this.stack)
			{
				if (e instanceof ClonableELEMENT)
				{
					obj.stack.add(((ClonableELEMENT)e).clone());
				}
				else
				{
					obj.stack.add(e);
				}
			}
			obj.text = new String(this.text);

			return obj;
		}
		catch (CloneNotSupportedException e1)
		{
			Util.logException(e1);
		}
		return null;
	}

	public double calc(double x)
	{
		@SuppressWarnings("unchecked")
		Stack<ELEMENT> impl = (Stack<ELEMENT>) stack.clone();

		Stack<VALUE> valStack = Util.newStack();

		while (false == impl.isEmpty())
		{
			ELEMENT op = impl.pop();
			operate(valStack, op, x);
		}

		VALUE val = valStack.pop();
		return val.value;
	}

	private void operate(Stack<VALUE> valStack, ELEMENT elm, double x)
	{
		if (elm instanceof VALUE)
		{
			valStack.push(((VALUE)elm).clone());
		}
		else if (elm instanceof OPERAND)
		{
			OPERAND op = (OPERAND)elm;
			VALUE left, right;
			switch(op)
			{
			case MULTI:
				left = valStack.pop();
				right = valStack.pop();
				left.value = left.value * right.value;
				valStack.push(left);
				break;
			case DIV:
				left = valStack.pop();
				right = valStack.pop();
				left.value = left.value / right.value;
				valStack.push(left);
				break;
			case ADD:
				left = valStack.pop();
				if (valStack.isEmpty())
				{
					left.value = left.value;
				}
				else
				{
					right = valStack.pop();
					if (right instanceof VALUE)
					{
						left.value = left.value + right.value;
					}
					else
					{
						valStack.push(right);
						left.value = left.value;
					}
				}
				valStack.push(left);
				break;
			case SUB:
				left = valStack.pop();
				if (valStack.isEmpty())
				{
					left.value = -left.value;
				}
				else
				{
					right = valStack.pop();
					if (right instanceof VALUE)
					{
						left.value = left.value - right.value;
					}
					else
					{
						valStack.push(right);
						left.value = -left.value;
					}
				}
				valStack.push(left);
				break;
			case MOD:
				left = valStack.pop();
				right = valStack.pop();
				left.value = left.value % right.value;
				valStack.push(left);
				break;
			case LN:
				left = valStack.pop();
				left.value = Math.log(left.value);
				valStack.push(left);
				break;
			case LOG1:
				right = valStack.pop();
				left = valStack.pop();
				left.value = Math.log(left.value)/Math.log(right.value);
				valStack.push(left);
				break;
			case POW:
				right = valStack.pop();
				left = valStack.pop();
				left.value = Math.pow(right.value, left.value);
				valStack.push(left);
				break;
			case SQRT:
				left = valStack.pop();
				left.value = Math.sqrt(left.value);
				valStack.push(left);
				break;
			case CBRT:
				left = valStack.pop();
				left.value = Math.cbrt(left.value);
				valStack.push(left);
				break;
			case VAR_X:
				left = new VALUE(x);
				valStack.push(left);
				break;
			}
		}
		else if (elm instanceof FUNCTION)
		{
			FUNCTION func = (FUNCTION) elm;
			VALUE left, right;
			try
			{
				switch(func.nArg)
				{
				case 1:
					left = valStack.pop();
					left.value = (Double) func.method.invoke(Math.class, left.value);
					valStack.push(left);
					break;
				case 2:
					right = valStack.pop();
					left = valStack.pop();
					left.value = (Double) func.method.invoke(Math.class, left.value, right.value);
					valStack.push(left);
					break;
				}
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				Util.logException(e);
			}
		}
	}

	protected String text;
	public String getText()
	{
		return this.text;
	}

	// builder

	public static FunctionBody build(String mathML, Map<String, Double> constTable)
	{
		FunctionBody.constTable = constTable;

		// 小数点値の置き換え
		mathML = mathML.replaceAll("[\r\n]", "");
		{
			String text = mathML.substring(0);
			Pattern p = Pattern.compile("(<[a-zA-Z]+>\\d*</[a-zA-Z]+>)?\\s*<mo>\\.</mo>\\s*(<[a-zA-Z]+>\\d*</[a-zA-Z]+>)?");
			Matcher m = p.matcher(text);
			while (m.find())
			{
				String t = m.group();
				String[] vals = t.split("(<[a-zA^Z/]+>)");
				StringBuilder buf = new StringBuilder();
				for (String v: vals)
				{
					buf.append(v);
				}
				String sval = buf.toString().replaceAll("\\s", "");
				mathML = mathML.replaceAll(t, "<mtext>" + sval + "</mtext>");
			}
		}

		{
			String text = mathML.substring(0);
			mathML = text.replaceAll("<mtext[\\sa-zA-Z=\"\']*>e</mtext>", String.format("<mtext>%f</mtext>",Math.E));
		}

		try
		{
			Document doc = XmlUtil.parse(mathML);
			Element tagMath = doc.getDocumentElement();

			FunctionBody func = new FunctionBody();
			func.text = mathML;
			math obj = new math();
			obj.build(func, tagMath);

			return func;
		}
		catch (Exception e)
		{
			Util.logException(e);
			return null;
		}
	}

	private static final List<BuildBase> tagList = Util.newArrayList();

	static
	{
		tagList.add(new semantics());
		tagList.add(new mi());
		tagList.add(new mn());
		tagList.add(new mo());
		tagList.add(new mtext());
		tagList.add(new mspace());
		tagList.add(new ms());
		tagList.add(new mrow());
		tagList.add(new mfrac());
		tagList.add(new msqrt());
		tagList.add(new mroot());
		tagList.add(new mfenced());
		tagList.add(new menclose());
		tagList.add(new msub());
		tagList.add(new msup());
		tagList.add(new msubsup());
		tagList.add(new munder());
		tagList.add(new mover());
		tagList.add(new munderover());
		tagList.add(new mmultiscripts());
		tagList.add(new mtable());
		tagList.add(new mtr());
		tagList.add(new mlabeledtr());
		tagList.add(new mtd());
	}

	private static Stack<ELEMENT> estack;
	private static Stack<Stack<ELEMENT>> pstack = Util.newStack();
	private static void pushStack()
	{
		ESTACK tmp = new ESTACK();
		estack.push(tmp);
		pstack.push(estack);
		estack = tmp.stack;
	}

	private static void popStack()
	{
		estack = pstack.pop();
	}


	/**
	 * @author yagi
	 *
	 */
	private static abstract class BuildBase
	{
		protected void parse(Element elm)
		{
			String tagName = elm.getTagName();
			for (BuildBase tagObj: tagList)
			{
				String className = tagObj.getClass().getSimpleName();
				if (tagName.equals(className))
				{
					tagObj.parseTag(this, elm);
					break;
				}
			}

			return;
		}

		protected void parseTag(BuildBase parent, Element elm) throws IllegalArgumentException
		{
			String text = XmlUtil.getNodeText(elm);
			if (null == text)
			{
				return;
			}

			text = text.trim();
			if (text.isEmpty())
			{
				return;
			}
			boolean pushed = false;
			for (OPERAND op: OPERAND.values())
			{
				if (op.toString().equals(text))
				{
					if (parent instanceof msub)
					{
//						if (op == OPERAND.log || op == OPERAND.ln)
						if (op == OPERAND.LN)
						{
							estack.add(0, OPERAND.LOG1);
							pushed = true;
						}
					}
					if (false == pushed)
					{
						if (1 == op.nVar())
						{
							estack.push(op);
						}
						else
						{
							if (op.hasPriviledge)
							{
								ELEMENT first = estack.get(0);
								if (first instanceof OPERAND)
								{
									if (((OPERAND) first).hasPriviledge)
									{
										estack.add(0, op);
									}
									else
									{
										ELEMENT tmp = estack.pop();
										estack.push(op);
										estack.push(tmp);
									}
								}
								else
								{
									estack.add(0, op);
								}
							}
							else
							{
								estack.add(0, op);
							}
						}
					}
					pushed = true;
					break;
				}
			}
			if (false == pushed)
			{
				for (FUNCTION func: fList)
				{
					if (func.name.equals(text))
					{
						if (1 == func.nArg)
						{
							estack.push(func);
						}
						else
						{
							estack.add(0, func);
						}
						pushed = true;
						break;
					}
				}
			}
			if (false == pushed)
			{
				if (text.equals("x"))
				{
					estack.push(OPERAND.VAR_X);
				}
				else if (text.matches("[\\(\\)\\{\\}\\[\\]]+"))
				{
				}
				else if (text.matches("[+-]?[0-9]+\\.?[0-9]*"))
				{
					Double v = Double.parseDouble(text);
					VALUE val = new VALUE(v);
					estack.push(val);
				}
				else if (null != constTable && constTable.containsKey(text))
				{
					double val = constTable.get(text);
					estack.push(new VALUE(val));
				}
				else
				{
					throw new IllegalArgumentException(String.format("Unknown identifier: %s", text));
				}
			}

			return;
		}

//		protected void parseAttr(Element elm, String attrName)
//		{
//			String attr = elm.getAttribute(attrName).trim();
//			if (attr.isEmpty())
//			{
//				return;
//			}
//
//			return;
//		}

	}

	private static class ParentBase extends BuildBase
	{
		@Override
		protected void parseTag(BuildBase parent, Element elm) throws IllegalArgumentException
		{
			NodeList children = elm.getChildNodes();
			int nChild = children.getLength();
			for (int i = 0; i < nChild; i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					parse((Element)node);
				}
			}

			return;
		}
	}

	private static class math extends ParentBase
	{
		private void build(FunctionBody func, Element elm)
		{
			estack = Util.newStack();
			this.parseTag(this, elm);
			Util.logInfo("function = " + estack.toString());
			func.stack = Util.newStack();
			this.expand(func, estack);
		}

		private void expand(FunctionBody func, Stack<ELEMENT> stack)
		{
			for (ELEMENT elm: stack)
			{
				if (elm instanceof ESTACK)
				{
					ESTACK tmp = (ESTACK) elm;
					this.expand(func, tmp.stack);
				}
				else
				{
					func.stack.add(elm);
				}
			}
		}
	}

	private static class semantics extends ParentBase
	{
		@Override
		protected void parseTag(BuildBase parent, Element elm) throws IllegalArgumentException
		{
//			pushStack();
			super.parseTag(parent, elm);
//			popStack();
		}
	}

	private static class mi extends BuildBase
	{
	}

	private static class mn extends BuildBase
	{
	}

	private static class mo extends BuildBase
	{
		@Override
		protected void parseTag(BuildBase parent, Element elm) throws IllegalArgumentException
		{
			String text = XmlUtil.getNodeText(elm);
			switch(text)
			{
			case "(":
			case "[":
			case "{":
				pushStack();
				break;
			case ")":
			case "]":
			case "}":
				popStack();
				break;
			case "+":
				if (estack.isEmpty())
				{
					super.parseTag(parent, elm);
					estack.push(new VALUE(0));
				}
				else
				{
					ELEMENT pre = estack.peek();
					if (pre instanceof OPERAND && pre != OPERAND.VAR_X)
					{
						super.parseTag(parent, elm);
						estack.push(new VALUE(0));
					}
					else
					{
						super.parseTag(parent, elm);
					}
				}
				break;
			case "-":
				if (estack.isEmpty())
				{
					super.parseTag(parent, elm);
					estack.push(new VALUE(0));
				}
				else
				{
					ELEMENT pre = estack.peek();
					if (pre instanceof OPERAND && pre != OPERAND.VAR_X)
					{
						super.parseTag(parent, elm);
						estack.push(new VALUE(0));
					}
					else
					{
						super.parseTag(parent, elm);
					}
				}
				break;
			default:
				super.parseTag(parent, elm);
			}
		}
	}

	private static class mtext extends BuildBase
	{
	}

	private static class mspace extends BuildBase
	{
	}

	private static class ms extends BuildBase
	{
	}

	private static class mrow extends ParentBase
	{
		@Override
		protected void parseTag(BuildBase parent, Element elm) throws IllegalArgumentException
		{
			pushStack();
			super.parseTag(parent, elm);
			popStack();
		}
	}

	private static class mfrac extends ParentBase
	{
		@Override
		protected void parseTag(BuildBase parent, Element elm) throws IllegalArgumentException
		{
			super.parseTag(parent, elm);

			estack.add(0, OPERAND.DIV);

			return;
		}
	}

	private static class msqrt extends ParentBase
	{
		@Override
		protected void parseTag(BuildBase parent, Element elm) throws IllegalArgumentException
		{
			super.parseTag(parent, elm);
			ELEMENT base = estack.pop();
			estack.push(OPERAND.SQRT);
			estack.push(base);
//			estack.push(new VALUE(0.5));
//			estack.add(0, OPERAND.pow);
		}
	}

	private static class mroot extends ParentBase
	{
		@Override
		protected void parseTag(BuildBase parent, Element elm) throws IllegalArgumentException
		{
			NodeList children = elm.getChildNodes();
			super.parseTag(parent, elm);
			if (1 == children.getLength())
			{
				estack.push(new VALUE(0.5));
				estack.add(0, OPERAND.POW);
			}
			else
			{
				ELEMENT index = estack.pop();
				ELEMENT base = estack.pop();
				ESTACK temp = new ESTACK();
				if (index instanceof VALUE)
				{
					VALUE val = (VALUE) index;
					if (2 == val.value)
					{
						temp.stack.push(OPERAND.SQRT);
						temp.stack.push(base);
					}
					else if (3 == val.value)
					{
						temp.stack.push(OPERAND.CBRT);
						temp.stack.push(base);
					}
					else
					{
						temp.stack.push(OPERAND.POW);
						temp.stack.push(base);
						temp.stack.push(OPERAND.DIV);
						temp.stack.push(new VALUE(1));
						temp.stack.push(index);
					}
				}
				else
				{
					temp.stack.push(OPERAND.POW);
					temp.stack.push(base);
					temp.stack.push(OPERAND.DIV);
					temp.stack.push(new VALUE(1));
					temp.stack.push(index);
				}
				estack.push(temp);
			}
//			ELEMENT first = estack.get(0);
//			if (first instanceof OPERAND)
//			{
//				if (((OPERAND) first).hasPriviledge)
//				{
//					if (1 == children.getLength())
//					{
//						estack.push(new VALUE(0.5));
//						estack.add(0, OPERAND.pow);
//					}
//					else
//					{
//						ELEMENT index = estack.pop();
//						ESTACK temp = new ESTACK();
//						temp.stack.push(OPERAND.div);
//						temp.stack.push(new VALUE(1));
//						temp.stack.push(index);
//						estack.push(temp);
//						estack.add(0, OPERAND.pow);
//					}
//				}
//				else
//				{
//					if (1 == children.getLength())
//					{
//						ELEMENT tmp = estack.pop();
//						estack.push(OPERAND.pow);
//						estack.push(tmp);
//						estack.push(new VALUE(0.5));
//					}
//					else
//					{
//						ELEMENT index = estack.pop();
//						ELEMENT tmp = estack.pop();
//						estack.push(OPERAND.pow);
//						estack.push(tmp);;
//						ESTACK temp = new ESTACK();
//						temp.stack.push(OPERAND.div);
//						temp.stack.push(new VALUE(1));
//						temp.stack.push(index);
//						estack.push(temp);
//					}
//				}
//			}
//			else
//			{
//				if (1 == children.getLength())
//				{
//					estack.push(new VALUE(0.5));
//					estack.add(0, OPERAND.pow);
//				}
//				else
//				{
//					ELEMENT index = estack.pop();
//					ESTACK temp = new ESTACK();
//					temp.stack.push(OPERAND.div);
//					temp.stack.push(new VALUE(1));
//					temp.stack.push(index);
//					estack.push(temp);
//					estack.add(0, OPERAND.pow);
//				}
//			}
		}
	}

	private static class mfenced extends ParentBase
	{
	}

	private static class menclose extends ParentBase
	{
	}

	private static class msub extends ParentBase
	{
	}

	private static class msup extends ParentBase
	{
		@Override
		protected void parseTag(BuildBase parent, Element elm) throws IllegalArgumentException
		{
			super.parseTag(parent, elm);
			ESTACK temp = new ESTACK();
			ELEMENT index = estack.pop();
			ELEMENT base = estack.pop();
			temp.stack.push(OPERAND.POW);
			temp.stack.push(base);
			temp.stack.push(index);
			estack.push(temp);
		}
	}

	private static class msubsup extends ParentBase
	{
	}

	private static class munder extends ParentBase
	{
	}

	private static class mover extends ParentBase
	{
	}

	private static class munderover extends ParentBase
	{
	}

	private static class mmultiscripts extends ParentBase
	{
	}

	private static class mtable extends ParentBase
	{
	}

	private static class mtr extends ParentBase
	{
	}

	private static class mlabeledtr extends mtr
	{
	}

	private static class mtd extends ParentBase
	{
	}
}
