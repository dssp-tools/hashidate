/**
 *
 */
package dssp.hashidate.shape.helper;

import java.util.EnumMap;
import java.util.List;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dssp.brailleLib.BrailleInfo;
import dssp.brailleLib.BrailleInfo.TABLE;
import dssp.brailleLib.BrailleTranslater;
import dssp.brailleLib.Util;
import dssp.brailleLib.XmlUtil;

/**
 * 点字の数式を扱う
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class BrailleFormula
{
	private final BrailleTranslater translater;
	private List<BrailleInfo> formula = null;
	private static final List<TagBase> tagList = Util.newArrayList();
	private static BrailleInfo SIJIFU;
	private static BrailleInfo KUGIRI;

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

	private BrailleFormula()
	{
		this.translater = null;
		SIJIFU = null;
		KUGIRI = null;
	}

	private BrailleFormula(BrailleTranslater translater)
	{
		this.translater = translater;
		this.translater.setMode(BrailleTranslater.MODE.FORMULA);
		SIJIFU = translater.getDict().getBrailleInfo("数式指示符");
		KUGIRI = translater.getDict().getBrailleInfo("区切り符号");
	}

	/**
	 * インスタンスを生成する
	 *
	 * @param dict 辞書
	 * @return インスタンス
	 */
	public static BrailleFormula newInstance(BrailleTranslater translater)
	{
		BrailleFormula obj = new BrailleFormula(translater);

		return obj;
	}

	/**
	 * MathMLの1つの数式を点字に翻訳する
	 * MathMLのテキストからDocumentを生成して、fromMathMLを呼ぶ
	 *
	 * @param mathML MathML
	 * @param inLine true=文章中の数式 false=数式単独
	 * @param formula 数式の点字のリスト
	 * @return 墨字の数<br/>負数=失敗した場合
	 */
	public int fromMathML(String mathML, boolean inLine, List<BrailleInfo> formula)
	{
		int nBraille = -1;
		try
		{
			String text = FormulaHandler.getInstance().translatetAlias(mathML);

			Document doc = XmlUtil.parse(text);
			nBraille = this.fromMathML(doc, inLine, formula);
		}
		catch (Exception ex)
		{
			Util.logException(ex);
		}

		return nBraille;
	}

	/**
	 * MathMLの1つの数式を点字に翻訳する
	 *
	 * @param doc MathMLのDocument
	 * @param inLine true=文章中の数式 false=数式単独
	 * @param formula 数式の点字のリスト
	 * @return 墨字の数<br/>負数=失敗した場合
	 */
	public int fromMathML(Document doc, boolean inLine, List<BrailleInfo> formula)
	{
		if (null == formula)
		{
			return -1;
		}

		this.formula = formula;
		this.formula.clear();

		Element tagMath = doc.getDocumentElement();

		math obj = new math(inLine);
		int nBraille = obj.parseMath(this.translater, this.formula, tagMath);

		return nBraille;
	}

	/**
	 * 数字後の"a-j"に指示付を前置する
	 *
	 * @param formula codeListを追加するリスト
	 * @param codeList textに対応するリスト
	 * @return codeList
	 */
	private static List<BrailleInfo> addSijifu(List<BrailleInfo> formula, List<BrailleInfo> codeList)
	{
		if (0 == codeList.size())
		{
			return codeList;
		}

		// 文頭のチェック
		if (0 < formula.size())
		{
			String text = codeList.get(0).getSumiji();
			if (text.matches("[a-j].*"))
			{
				BrailleInfo prev = formula.get(formula.size()-1);
				String prevSumiji = prev.getSumiji();
				try
				{
					Integer.parseInt(prevSumiji);
					codeList.add(0, SIJIFU);
				}
				catch (NumberFormatException ex)
				{
				}
			}
		}

		// 文中のチェック
		if (1 < codeList.size())
		{
			for (int i = (codeList.size()-1); i > 1; i--)
			{
				BrailleInfo buf = codeList.get(i);
				if (buf.getSumiji().matches("[a-j].*"))
				{
					BrailleInfo prev = codeList.get(i-1);
					try
					{
						Integer.parseInt(prev.getSumiji());
						codeList.add(i, SIJIFU);
					}
					catch (NumberFormatException ex)
					{

					}
				}
			}
		}

		return codeList;
	}

	private static final String BLOCK_OPEN = "ブロック化カッコ開始";
	private static final String BLOCK_CLOSE = "ブロック化カッコ終了";

	private static List<BrailleInfo> addBlock(BrailleTranslater translater, List<BrailleInfo> row)
	{
		BrailleInfo first = row.get(0);
		BrailleInfo last = row.get(row.size()-1);

		if (false == (first.getSumiji().equals(BLOCK_OPEN) && last.getSumiji().equals(BLOCK_CLOSE))
				&& false == (first.getSumiji().equals("(") && last.getSumiji().equals(")"))
				&& false == (first.getSumiji().equals("[") && last.getSumiji().equals("]"))
				&& false == (first.getSumiji().equals("{") && last.getSumiji().equals("}"))
				&& false == (first.getSumiji().equals("|") && last.getSumiji().equals("|"))
				)
		{
			BrailleInfo block = translater.getDict().getBrailleInfo(BLOCK_OPEN);
			row.add(0, block);

			block = translater.getDict().getBrailleInfo(BLOCK_CLOSE);
			row.add(block);
		}

		return row;
	}

	private static List<BrailleInfo> addKugiri(BrailleTranslater translater, List<BrailleInfo> parent, List<BrailleInfo> child)
	{
		if (0 == child.size())
		{
			return child;
		}

		// 前文字のチェック
		if (0 < parent.size())
		{
			BrailleInfo prev = parent.get(parent.size() - 1);
			BrailleInfo post = child.get(0);
			if (prev.needCheck(BrailleInfo.CHECK.POSTCHECK) && post.needCheck(BrailleInfo.CHECK.PRECHECK))
			{
				child.add(0, KUGIRI);
			}
			else if (needToAddKugiri(parent, post))
			{
				child.add(0, KUGIRI);
			}
		}

		// 内部のチェック
		BrailleInfo post = child.get(child.size()-1);
		int index = 0;
		int oomojiCount = 0;
		for (int i = (child.size()-2) ; i >= 0 ; i--)
		{
			BrailleInfo prev = child.get(i);
			if (post.needCheck(BrailleInfo.CHECK.PRECHECK))
			{
				index = i+1;
				// 区切り符号確認が必要な文字が前にある
				if (prev.needCheck(BrailleInfo.CHECK.POSTCHECK))
				{
					child.add(index, KUGIRI);
					oomojiCount = 0;
					index = -1;
				}
			}

			// 前に大文字が２個以上続いている場合のチェック
			if (prev.haveExtra(BrailleInfo.EXTRA.OOMOJIFU))
			{
				oomojiCount = Math.min(2, oomojiCount+1);
				if (2 == oomojiCount && 0 < index)
				{
					child.add(index, KUGIRI);
					oomojiCount = 0;
					index = -1;
				}
			}
			else
			{
				oomojiCount = 0;
				index = -1;
			}

			post = prev;
		}

		return child;
	}

	private static List<BrailleInfo> addKuguri(BrailleTranslater translater, List<BrailleInfo> parent, BrailleInfo post)
	{
		List<BrailleInfo> list = Util.newArrayList();
		// 前文字のチェック
		if (0 < parent.size())
		{
			BrailleInfo prev = parent.get(parent.size() - 1);
			if (prev.needCheck(BrailleInfo.CHECK.POSTCHECK) && post.needCheck(BrailleInfo.CHECK.PRECHECK))
			{
				list.add(KUGIRI);
			}
			else if (needToAddKugiri(parent, post))
			{
				list.add(KUGIRI);
			}
		}
		list.add(post);

		return list;
	}

	/**
	 * 関数の前に２文字以上の大文字があるかどうかを取得する
	 *
	 * @param prevList 前の式
	 * @param post 区切り符号確認が必要なBrailleInfo
	 * @return
	 */
	private static boolean needToAddKugiri(List<BrailleInfo> prevList, BrailleInfo post)
	{
		if (2 > prevList.size())
		{
			return false;
		}

		boolean ret = true;
		for (int i = 0; i < 2; i++)
		{
			BrailleInfo prev = prevList.get(prevList.size() - 1 - i);
			ret &= prev.haveExtra(BrailleInfo.EXTRA.OOMOJIFU);
		}

		return ret;
	}

	// 翻訳用クラス
	// 各クラス名はMathMLのタグ名と一致しなければならない

	private static abstract class TagBase
	{
		protected int parse(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			int nBraille = 0;

			String tagName = elm.getTagName();
			for (TagBase tagObj: tagList)
			{
				String className = tagObj.getClass().getSimpleName();
				if (tagName.equals(className))
				{
					nBraille += tagObj.parseTag(translater, formula, elm);
					break;
				}
			}

			return nBraille;
		}

		protected int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			String text = XmlUtil.getNodeText(elm);
			if (null == text)
			{
				return 0;
			}

			text = text.trim();
			List<BrailleInfo> list = Util.newArrayList();
			int nBraille = translater.braileFromSumiji(text, list, true, true);

			addSijifu(formula, list);
			addKugiri(translater, formula, list);

			formula.addAll(list);

			return nBraille;
		}

		protected int parseChildren(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			int nBraille = 0;

			NodeList children = elm.getChildNodes();
			int nChild = children.getLength();
			for (int i = 0; i < nChild; i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					nBraille += parse(translater, formula, (Element)node);
				}
			}

			return nBraille;
		}

		protected int parseAttr(BrailleTranslater translater, List<BrailleInfo> formula, Element elm, String attrName)
		{
			String attr = elm.getAttribute(attrName).trim();
			if (attr.isEmpty())
			{
				return 0;
			}

			List<BrailleInfo> list = Util.newArrayList();
			int nBraille = translater.braileFromSumiji(attr, list, true, true);

			addSijifu(formula, list);
			addKugiri(translater, formula, list);
			formula.addAll(list);

			return nBraille;
		}
	}

	private static class math extends TagBase
	{
		private final boolean inLine;
		enum END {ONE_BOX, TWO_BOX};
		static END end = END.ONE_BOX;

		/**
		 *
		 * @param inLine true=文中の数式 false=数式単独
		 */
		math(boolean inLine)
		{
			this.inLine = inLine;
		}

		protected int parseMath(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			int nBraille = this.parseChildren(translater, formula, elm);

			if (0 < formula.size())
			{
				BrailleInfo space = translater.getDict().newBrailleInfo();
				BrailleInfo first = formula.get(0);
				if (this.inLine)
				{
					// 日本語文中に数式を書く場合
					if (first.haveExtra(BrailleInfo.EXTRA.SUUFU) || first.isExtraOf(BrailleInfo.EXTRA.SUUFU))
					{
						// 数式が数符から始まる場合は、1マスあけ
						formula.add(0, space);
					}
					else
					{
						// 数式が数符から始まらない場合は、1マスあけて数式指示符を前置する
						formula.add(0, space);

						formula.add(1, SIJIFU);
					}

					// 数式の終わり
					switch(end)
					{
					case ONE_BOX:	// 1マスあけ
						formula.add(space);
						break;
					case TWO_BOX:	// 2マスあけ
						formula.add(space);
						formula.add(space);
						break;
					}
				}
				else
				{
					// 数式単独
					if (null == first)
					{
						Util.logError("null");
					}
//					if (false == (first.haveExtra(BrailleInfo.EXTRA.SUUFU) || first.isExtraOf(BrailleInfo.EXTRA.SUUFU)))
//					{
//						// 数式が数符から始まらない場合は、数式指示付を前置する
//						// 外字符から始まる場合は、数式指示符と重なるので前置しない
//						formula.add(0, SIJIFU);
//					}
					// 外字符から始まる場合は、数式指示符と重なるので前置しない
					if (false == first.isExtraOf(BrailleInfo.EXTRA.GAIJIFU))
					{
						formula.add(0, SIJIFU);
					}
				}
			}

			return nBraille;
		}
	}

	private static class semantics extends TagBase
	{
		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			List<BrailleInfo> row = Util.newArrayList();

			int nBraille = parseChildren(translater, row, elm);

			formula.addAll(row);

			return nBraille;
		}
	}

	private static class mi extends TagBase
	{
	}

	private static class mn extends TagBase
	{
	}

	private static class mo extends TagBase
	{
		private static final String ATTR_NEWLINE = "newline";
		private static final String LINEBREAK = "linebreak";
		private static final String ATTR_FORM = "form";
		private static final String PREFIX = "prefix";
		private static final String POSTFIX = "postfix";

		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			int nBraille = 0;
			boolean flag = true;
			String val = elm.getAttribute(ATTR_NEWLINE);
			if (val.equals(LINEBREAK))
			{
				// 改行なので「行末つなぎ符」を置く
				BrailleInfo lineBreak = translater.getDict().getBrailleInfo("行末つなぎ符");
//				lineBreak.setLineBreak(true);

				String form = elm.getAttribute(ATTR_FORM);
				if (form.isEmpty() || form.equals(PREFIX))
				{
					formula.addAll(addKuguri(translater, formula, lineBreak));
				}

				nBraille = super.parseTag(translater, formula, elm);

				if (form.equals(POSTFIX))
				{
					formula.addAll(addKuguri(translater, formula, lineBreak));
				}

				formula.add(BrailleInfo.LINEBREAK);
			}
			else
			{
				val = XmlUtil.getNodeText(elm);
				if (null != val)
				{
					switch(val)
					{
					case "(":
					case "[":
					case "{":
						for (Node node = elm.getNextSibling(); null != node; node = node.getNextSibling())
						{
							if (node instanceof Element)
							{
								Element child = (Element)node;
								if (child.getTagName().equals("mtable"))
								{
									flag = false;
									break;
								}
							}
						}
						if (flag)
						{
							nBraille = super.parseTag(translater, formula, elm);
						}
						break;
					case ")":
					case "]":
					case "}":
						for (Node node = elm.getPreviousSibling(); null != node; node = node.getPreviousSibling())
						{
							if (node instanceof Element)
							{
								Element child = (Element)node;
								if (child.getTagName().equals("mtable"))
								{
									flag = false;
									break;
								}
							}
						}
						if (flag)
						{
							nBraille = super.parseTag(translater, formula, elm);
						}
						break;
					case "|":
						for (Node node = elm.getNextSibling(); null != node; node = node.getNextSibling())
						{
							if (node instanceof Element)
							{
								Element child = (Element)node;
								if (child.getTagName().equals("mtable"))
								{
									flag = false;
									break;
								}
							}
						}
						for (Node node = elm.getPreviousSibling(); null != node; node = node.getPreviousSibling())
						{
							if (node instanceof Element)
							{
								Element child = (Element)node;
								if (child.getTagName().equals("mtable"))
								{
									flag = false;
									break;
								}
							}
						}
						if (flag)
						{
							nBraille = super.parseTag(translater, formula, elm);
						}
						break;
					default:
						nBraille = super.parseTag(translater, formula, elm);
					}
				}
			}

			return nBraille;
		}
	}

	private static class mtext extends TagBase
	{
	}

	private static class mspace extends TagBase
	{
		private static final String ATTR_LINEBREAK = "linebreak";
		private static final String NEWLINE = "newline";

		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			String val = elm.getAttribute(ATTR_LINEBREAK);
			switch(val)
			{
			case NEWLINE:	// 改行の処理
				BrailleInfo lineBreak = null;
				if (0 < formula.size())
				{
					BrailleInfo prev = formula.get(formula.size()-1);

					if (prev.getSumiji().equals(","))
					{
						// 最後がコンマなので空白の改行指示をする
//						lineBreak = translater.getDict().getLineBreak();
//						lineBreak = BrailleInfo.LINEBREAK;
					}
					else
					{
						lineBreak = translater.getDict().getBrailleInfo("行末つなぎ符");
//						lineBreak.setLineBreak(true);
					}
				}

				if (null == lineBreak)
				{
					formula.addAll(addKuguri(translater, formula, BrailleInfo.LINEBREAK));
				}
				else
				{
					formula.addAll(addKuguri(translater, formula, lineBreak));
					formula.add(BrailleInfo.LINEBREAK);
				}
				break;
			default:
//				BrailleInfo space = translater.getDict().getSpace();
				BrailleInfo space = BrailleInfo.SPACE;
				formula.addAll(addKuguri(translater, formula, space));
			}

			return 0;
		}
	}

	private static class ms extends TagBase
	{
	}

	private static class mrow extends TagBase
	{
		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			List<BrailleInfo> row = Util.newArrayList();

			int nBraille = parseChildren(translater, row, elm);

			if (0 < formula.size() && 1 < nBraille)
			{
				addBlock(translater, row);
			}

			addSijifu(formula, row);
			addKugiri(translater, formula, row);

			formula.addAll(row);

			return nBraille;
		}
	}

	private static class mfrac extends TagBase
	{
		private static enum PART {MOTHER, CHILD};
		private PART part = PART.MOTHER;

		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			int nBraille = 0;

			this.part = PART.MOTHER;
			List<BrailleInfo> codeList = Util.newArrayList();

			NodeList children = elm.getChildNodes();
			int nChild = children.getLength();
			for (int i = 0; i < nChild; i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					switch(this.part)
					{
					case MOTHER:	// 分母
						nBraille += this.parse(translater, codeList, (Element)node);

						BrailleInfo bar = translater.getDict().getBrailleInfo("分数線");
						codeList.add(bar);

						this.part = PART.CHILD;
						break;
					case CHILD:		// 分子
						nBraille += this.parse(translater, codeList, (Element)node);
					}
				}
			}

			BrailleInfo block = translater.getDict().getBrailleInfo("分数開始");
			codeList.add(0, block);

			block = translater.getDict().getBrailleInfo("分数終了");
			codeList.add(block);

			formula.addAll(addKugiri(translater, formula, codeList));

			return nBraille;
		}
	}

	private static class msqrt extends TagBase
	{
		private final static String ROOT = "√";

		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			BrailleInfo root = translater.getDict().getBrailleInfo(ROOT);
			formula.addAll(addKuguri(translater, formula, root));

			List<BrailleInfo> codeList = Util.newArrayList();

			int nBraille = super.parseTag(translater, codeList, elm);
			if (0 == nBraille)
			{
				nBraille = parseChildren(translater, codeList, elm);
			}

			if (1 < nBraille)
			{
				addBlock(translater, codeList);
			}

			addKugiri(translater, formula, codeList);
			formula.addAll(codeList);

			return nBraille;
		}
	}

	private static class mroot extends TagBase
	{
		private static final String ROOT = "累乗根";

		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			int nBraille = 0;
			int index = 0;

			// ベースの文字
			List<BrailleInfo> base = Util.newArrayList();
			NodeList children = elm.getChildNodes();
			int nBase = 0;
			for (int i = 0; i < children.getLength(); i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					nBase = this.parse(translater, base, (Element)node);
					nBraille += nBase;
					index = i+1;
					break;
				}
			}
			if (1 < nBase)
			{
				addBlock(translater, base);
			}

			// 添字
			List<BrailleInfo> multi = Util.newArrayList();
			int nMulti = 0;
			for (int i = index; i < children.getLength(); i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					nMulti = this.parse(translater, multi, (Element)node);
					nBraille += nMulti;
					break;
				}
			}
			if (1 < nMulti)
			{
				addBlock(translater, multi);
			}

			BrailleInfo root = translater.getDict().getBrailleInfo(ROOT);

			formula.addAll(addKugiri(translater, formula, multi));
			formula.addAll(addKuguri(translater, formula, root));
			formula.addAll(addKugiri(translater, formula, base));

			return nBraille;
		}
	}

	private final static String ATTR_OPEN = "open";
	private final static String ATTR_CLOSE = "close";

	private static class mfenced extends TagBase
	{

		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			int nBraille = 0;
			boolean flag = true;
			NodeList nodeList = elm.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				Node node = nodeList.item(i);
				if (node instanceof Element)
				{
					Element child = (Element)node;
					if (child.getTagName().equals("mtable"))
					{
						flag = false;
						break;
					}
				}
			}

			if (flag)
			{
				List<BrailleInfo> open = Util.newArrayList();
				List<BrailleInfo> close = Util.newArrayList();

				int nOpen = parseAttr(translater, open, elm, ATTR_OPEN);
				int nClose = parseAttr(translater, close, elm, ATTR_CLOSE);

				List<BrailleInfo> list = Util.newArrayList();
				nBraille = parseChildren(translater, list, elm);

				formula.addAll(addKugiri(translater, formula, open));
				formula.addAll(addKugiri(translater, formula, list));
				formula.addAll(addKugiri(translater, formula, close));

				nBraille += nOpen + nClose;
			}
			else
			{
				List<BrailleInfo> list = Util.newArrayList();
				nBraille = parseChildren(translater, list, elm);
				formula.addAll(addKugiri(translater, formula, list));
			}
			return nBraille;
		}
	}

	private static class menclose extends TagBase
	{
	}

	private static final String SUB = "右下添字";
	private static final String SUP = "右肩添字";

	/**
	 * 下がり文字に変換する
	 *
	 * @param translater
	 * @param infoList
	 * @return
	 */
	private static List<BrailleInfo> checkSagari(BrailleTranslater translater, List<BrailleInfo> infoList)
	{
		try
		{
			// 下がり文字
			StringBuilder buf = new StringBuilder();
			for (BrailleInfo braille: infoList)
			{
				if (false == braille.isExtraOf(BrailleInfo.EXTRA.SUUFU))
				{
					buf.append(braille.getSumiji());
				}
			}
			String subText = buf.toString();
			Integer.parseInt(subText);

			List<BrailleInfo> list = translater.getDict().getSagari(subText);
			if (null != list)
			{
				return list;
			}
			else
			{
				return infoList;
			}
		}
		catch (NumberFormatException ex)
		{
			return infoList;
		}

	}

	private static List<BrailleInfo> preSub;
	private static int nPreSub = 0;

	private static class msub extends TagBase
	{
		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			int nBraille = 0;
			int index = 0;

			// ベースの文字
			List<BrailleInfo> base = Util.newArrayList();
			NodeList children = elm.getChildNodes();
			for (int i = 0; i < children.getLength(); i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					nBraille += this.parse(translater, base, (Element)node);
					index = i+1;
					break;
				}
			}

			StringBuilder buf = new StringBuilder();
			for (BrailleInfo braille: base)
			{
				String sumiji = braille.getSumiji();
				if (null != sumiji)
				{
					buf.append(sumiji);
				}
			}
			String baseText = buf.toString();

			// 下添字
			List<BrailleInfo> subscript = Util.newArrayList();
			int nSub = 0;
			for (int i = index; i < children.getLength(); i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					nSub += this.parse(translater, subscript, (Element)node);
					index = i+1;
					break;
				}
			}
			nBraille += nSub;

			if (0 == base.size())
			{
				preSub = subscript;
				nPreSub = nSub;
				return 0;
			}

			buf.delete(0, buf.length());
			for (BrailleInfo braille: subscript)
			{
				if (false == braille.isExtraOf(BrailleInfo.EXTRA.SUUFU))
				{
					String val = braille.getSumiji();
					if (null != val && false == val.isEmpty())
					{
						buf.append(val);
					}
				}
			}
			String subText = buf.toString();

			// 下がり文字の確認
			boolean checkSagari = false;
			switch(baseText)
			{
			case "log":
				checkSagari = true;
				break;
			case "∫":	// 定積分では下がり文字にしない
				break;
			default:
				try
				{
					int num = Integer.parseInt(subText);
					if (0 <= num && 10 > num)
					{
						checkSagari = true;
					}
				}
				catch (NumberFormatException ex)
				{
				}
			}

			//			if (null != preSub && 0 < preSub.size())
			//			{
			//				addTo(translater, formula, KUGIRI);
			//			}
			addKugiri(translater, formula, base);
			formula.addAll(base);

			List<BrailleInfo> ret = null;
			if (checkSagari)
			{
				ret = checkSagari(translater, subscript);
			}
			if (null != ret && ret != subscript)
			{
				subscript = ret;
			}
			else
			{
				if (null != preSub && 0 < preSub.size())
				{
					// 順列・組み合わせ
//					subscript.add(0, translater.getDict().getSpace());
					subscript.add(0, BrailleInfo.SPACE);
					subscript.addAll(0, preSub);
					nSub += nPreSub;

					preSub = null;
					nPreSub = 0;
				}
				else
				{
					// 添字記号
					BrailleInfo info = translater.getDict().getBrailleInfo(SUB);
					if (null == info)
					{
						return 0;
					}
					formula.addAll(addKuguri(translater, formula, info));
				}
			}

			if (1 < nSub)
			{
				addBlock(translater, subscript);
			}

			addKugiri(translater, formula, subscript);
			formula.addAll(subscript);

			return nBraille;
		}
	}

	private static class msup extends TagBase
	{
		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			int nBraille = 0;
			int index = 0;

			// ベースの文字
			List<BrailleInfo> base = Util.newArrayList();
			NodeList children = elm.getChildNodes();
			for (int i = 0; i < children.getLength(); i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					nBraille += this.parse(translater, base, (Element)node);
					index = i+1;
					break;
				}
			}

			addSijifu(formula, base);
			addKugiri(translater, formula, base);
			formula.addAll(base);

			// 添字
			List<BrailleInfo> superscirpt = Util.newArrayList();
			int nSup = 0;
			String accent = null;
			for (int i = index; i < children.getLength(); i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					nSup += this.parse(translater, superscirpt, (Element)node);
					accent = node.getTextContent().replaceAll("[\\n\\s]", "");
					break;
				}
			}
			nBraille += nSup;

			switch(accent)
			{
			case "2":
				superscirpt.clear();
				superscirpt.add(translater.getDict().getBrailleInfo("平方"));
				break;
			case "3":
				superscirpt.clear();
				superscirpt.add(translater.getDict().getBrailleInfo("立方"));
				break;
			case "-1":
				superscirpt.clear();
				superscirpt.add(translater.getDict().getBrailleInfo("逆数"));
				break;
			default:
				// 添字記号
				BrailleInfo info = translater.getDict().getBrailleInfo(SUP);
				if (null == info)
				{
					return 0;
				}
				formula.addAll(addKuguri(translater, formula, info));
			}
			if (1 < nSup)
			{
				addBlock(translater, superscirpt);
			}
			addKugiri(translater, formula, superscirpt);
			formula.addAll(superscirpt);

			return nBraille;
		}
	}

	private static class msubsup extends TagBase
	{
		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			int nBraille = 0;
			int index = 0;

			// ベースの文字
			List<BrailleInfo> base = Util.newArrayList();
			StringBuilder val = new StringBuilder();
			NodeList children = elm.getChildNodes();
			for (int i = 0; i < children.getLength(); i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					nBraille += this.parse(translater, base, (Element)node);
					index = i+1;
					break;
				}
			}
			for (BrailleInfo info: base)
			{
				String sumiji = info.getSumiji();
				if (null != sumiji && false == sumiji.isEmpty())
				{
					val.append(sumiji);
				}
			}

			addKugiri(translater, formula, base);
			formula.addAll(base);

			// 下添字
			List<BrailleInfo> subscript = Util.newArrayList();
			int nSub = 0;
			for (int i = index; i < children.getLength(); i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					nSub += this.parse(translater, subscript, (Element)node);
					index = i+1;
					break;
				}
			}
			nBraille += nSub;

			// 下がり文字の確認
			boolean checkSagari = false;
			switch(val.toString())
			{
			case "∫":	// 定積分では下がり文字にしない
				break;
			default:
				StringBuilder buf = new StringBuilder();
				for (BrailleInfo braille: subscript)
				{
					if (false == braille.isExtraOf(BrailleInfo.EXTRA.SUUFU))
					{
						buf.append(braille.getSumiji());
					}
				}
				String subText = buf.toString();
				try
				{
					int num = Integer.parseInt(subText);
					if (0 <= num && 10 > num)
					{
						checkSagari = true;
					}
				}
				catch (NumberFormatException ex)
				{
				}
			}
			List<BrailleInfo> ret = null;
			if (checkSagari)
			{
				ret = checkSagari(translater, subscript);
			}
			if (null != ret && ret != subscript)
			{
				subscript = ret;
			}
			else
			{
				// 添字記号
				BrailleInfo info = translater.getDict().getBrailleInfo(SUB);
				if (null == info)
				{
					return 0;
				}
				formula.addAll(addKuguri(translater, formula, info));
			}
			if (1 < nSub)
			{
				addBlock(translater, subscript);
			}
			addKugiri(translater, formula, subscript);
			formula.addAll(subscript);

			// 上添字
			List<BrailleInfo> superscript = Util.newArrayList();
			int nSup = 0;
			String accent = null;
			for (int i = index; i < children.getLength(); i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					nSup += this.parse(translater, superscript, (Element)node);
					accent = node.getTextContent().replaceAll("[\\n\\s]", "");
					break;
				}
			}

			switch(val.substring(val.length()-1))
			{
			case "∫":	// 積分
			case "∑":	// 和
			case "]":	// 定積分
				superscript.add(0, BrailleInfo.SPACE);
				superscript.add(BrailleInfo.SPACE);
				addKugiri(translater, formula, superscript);
				formula.addAll(superscript);
				break;
			default:
				switch(accent)
				{
				case "2":
					superscript.clear();
					superscript.add(translater.getDict().getBrailleInfo("平方"));
					break;
				case "3":
					superscript.clear();
					superscript.add(translater.getDict().getBrailleInfo("立方"));
					break;
				case "-1":
					superscript.clear();
					superscript.add(translater.getDict().getBrailleInfo("逆数"));
					break;
				default:
					// 添字記号
					BrailleInfo info = translater.getDict().getBrailleInfo(SUP);
					if (null == info)
					{
						return 0;
					}
					addKuguri(translater, formula, info);
					if (1 < nSup)
					{
						addBlock(translater, superscript);
					}
				}
				addKugiri(translater, formula, superscript);
				formula.addAll(superscript);
			}

			return nBraille;
		}
	}

	private static class munder extends TagBase
	{
		private static enum INDEX {VALUE, UNDER};
		private INDEX index = INDEX.VALUE;

		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			int nBraille = 0;
			int nUnder = 0;

			List<BrailleInfo> codeList = Util.newArrayList();
			List<BrailleInfo> under = Util.newArrayList();
			String val = null;

			NodeList children = elm.getChildNodes();
			int nChild = children.getLength();
			this.index = INDEX.VALUE;
			for (int i = 0; i < nChild; i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					switch(this.index)
					{
					case VALUE:
						val = node.getTextContent();
						nBraille += this.parse(translater, codeList, (Element)node);
						this.index = INDEX.UNDER;
						break;
					case UNDER:
						nUnder = this.parse(translater, under, (Element)node);
						nBraille += nUnder;
					}
				}
			}
			addSijifu(formula, codeList);

			// 添字記号
			BrailleInfo subFlag = translater.getDict().getBrailleInfo(SUB);
			if (0 < nUnder)
			{
				switch(val)
				{
				case "lim":	// 極限
					formula.addAll(addKugiri(translater, formula, codeList));
					formula.addAll(addKuguri(translater, formula, subFlag));
					for (int i = 0; i < under.size(); i++)
					{
						BrailleInfo base = under.get(i);
						if (base.getSumiji().equals("→"))
						{
//							under.set(i, translater.getDict().getSpace());
							under.set(i, BrailleInfo.SPACE);
						}
					}
					if (1 < nUnder)
					{
						addBlock(translater, under);
					}
					addKugiri(translater, formula, under);
					formula.addAll(under);
					break;
				default:
					StringBuilder buf = new StringBuilder();
					for (BrailleInfo braille: under)
					{
						if (false == braille.isExtra())
						{
							buf.append(braille.getSumiji());
						}
					}
					switch(buf.toString())
					{
					case "¯":	// 下線
						BrailleInfo open = translater.getDict().getBrailleInfo("第３指示符開始");
						BrailleInfo close = translater.getDict().getBrailleInfo("第３指示符終了");

						formula.addAll(addKuguri(translater, formula, open));
						formula.addAll(addKugiri(translater, formula, codeList));
						formula.addAll(addKuguri(translater, formula, close));
						break;
					default:	// その他は右下で
						if (1 < nBraille)
						{
							addBlock(translater, codeList);
						}
						formula.addAll(addKugiri(translater, formula, codeList));
						formula.addAll(addKuguri(translater, formula, subFlag));
						if (1 < nUnder)
						{
							addBlock(translater, under);
						}
						addKugiri(translater, formula, under);
						formula.addAll(under);
					}
				}
			}

			return nBraille;
		}
	}

	private static class mover extends TagBase
	{
		private static enum INDEX {VALUE, OVER};
		private INDEX index = INDEX.VALUE;

		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			int nBraille = 0;

			List<BrailleInfo> codeList = Util.newArrayList();
			List<BrailleInfo> over = Util.newArrayList();

			NodeList children = elm.getChildNodes();
			int nChild = children.getLength();
			this.index = INDEX.VALUE;
			for (int i = 0; i < nChild; i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					switch(this.index)
					{
					case VALUE:
						nBraille += this.parse(translater, codeList, (Element)node);
						if (1 < nBraille)
						{
							addBlock(translater, codeList);
						}
						this.index = INDEX.OVER;
						break;
					case OVER:
						String text = XmlUtil.getNodeText(node);
						switch(text)
						{
						case "→":	// ベクトル
							over.add(translater.getDict().getBrailleInfo("ベクトル"));
							break;
						case "・":
						case "･":
						case "·":
							over.add(translater.getDict().getBrailleInfo("上のドット"));
							break;
						default:
							nBraille += this.parse(translater, over, (Element)node);
						}

					}
				}
			}
			addSijifu(formula, codeList);
			formula.addAll(addKugiri(translater, formula, codeList));
			if (0 < over.size())
			{
				formula.addAll(addKugiri(translater, formula, over));
			}
			return nBraille;
		}
	}

	private static class munderover extends TagBase
	{
		private static enum INDEX {VALUE, UNDER, OVER};
		private INDEX index = INDEX.VALUE;

		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			int nBraille = 0;

			List<BrailleInfo> codeList = Util.newArrayList();
			List<BrailleInfo> under = Util.newArrayList();
			List<BrailleInfo> over = Util.newArrayList();

			NodeList children = elm.getChildNodes();
			int nChild = children.getLength();
			this.index = INDEX.VALUE;
			for (int i = 0; i < nChild; i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					switch(this.index)
					{
					case VALUE:
						nBraille += this.parse(translater, codeList, (Element)node);
						if (1 < nBraille)
						{
							addBlock(translater, codeList);
						}
						this.index = INDEX.UNDER;
						break;
					case UNDER:
						nBraille += this.parse(translater, under, (Element)node);
						this.index = INDEX.OVER;
						break;
					case OVER:
						nBraille += this.parse(translater, over, (Element)node);
					}
				}
			}
			addSijifu(formula, codeList);
			addKugiri(translater, formula, codeList);
			formula.addAll(codeList);
			if (0 < under.size())
			{
				addKugiri(translater, formula, under);
				formula.addAll(under);
//				formula.add(translater.getDict().getSpace());
				formula.add(BrailleInfo.SPACE);
			}
			if (0 < over.size())
			{
				addKugiri(translater, formula, over);
				formula.addAll(over);
//				formula.add(translater.getDict().getSpace());
				formula.add(BrailleInfo.SPACE);
			}
			return nBraille;
		}
	}

	private static class mmultiscripts extends TagBase
	{
		private static enum INDEX {VALUE, SUB, SUP};
		private INDEX index = INDEX.VALUE;

		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			int nBraille = 0;
			int nSub = 0;
			int nSup = 0;

			BrailleInfo subFlag = translater.getDict().getBrailleInfo(SUB);
			BrailleInfo supFlag = translater.getDict().getBrailleInfo(SUP);

			List<BrailleInfo> codeList = Util.newArrayList();
			List<BrailleInfo> sub = Util.newArrayList();
			List<BrailleInfo> sup = Util.newArrayList();

			NodeList children = elm.getChildNodes();
			int nChild = children.getLength();
			this.index = INDEX.VALUE;
			for (int i = 0; i < nChild; i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					switch(this.index)
					{
					case VALUE:
						nBraille += this.parse(translater, codeList, (Element)node);
						if (1 < nBraille)
						{
							addBlock(translater, codeList);
						}
						this.index = INDEX.SUB;

						addSijifu(formula, codeList);
						formula.addAll(addKugiri(translater, formula, codeList));

						break;
					case SUB:
						sub.clear();
						nSub = this.parse(translater, sub, (Element)node);
						nBraille += nSub;
						this.index = INDEX.SUP;
						break;
					case SUP:
						sup.clear();
						nSup = this.parse(translater, sup, (Element)node);
						nBraille += nSup;
						this.index = INDEX.SUB;

						if (0 < sub.size())
						{
							formula.addAll(addKuguri(translater, formula, subFlag));
							if (1 < nSub)
							{
								addBlock(translater, sub);
							}
							addKugiri(translater, formula, sub);
							formula.addAll(sub);
						}
						if (0 < sup.size())
						{
							formula.addAll(addKuguri(translater, formula, supFlag));
							if (1 < nSup)
							{
								addBlock(translater, sup);
							}
							addKugiri(translater, formula, sup);
							formula.addAll(sup);
						}

						nSub = nSup = 0;
					}
				}
			}
			return nBraille;
		}
	}

	private static enum MATRIX
	{
		LU("行列左上"),
		LM("行列左中"),
		LD("行列左下"),
		RU("行列右上"),
		RM("行列右中"),
		RD("行列右下");

		private final String key;

		MATRIX(String key)
		{
			this.key = key;
		}

		public String getKey()
		{
			return this.key;
		}
	}

	private static Stack<String> tableOpenStack = Util.newStack();
	private static Stack<String> tableCloseStack = Util.newStack();

	private static class mtable extends TagBase
	{
		private void addOpen(BrailleTranslater translater, List<BrailleInfo> formula, String open, int nRow)
		{
			BrailleInfo lm = translater.getDict().getBrailleInfo(MATRIX.LM.getKey());
			if (1 == nRow)
			{
				BrailleInfo lo = translater.getDict().getBrailleInfo(open);
				if (null == lo)
				{
					return;
				}
				for (int i = 0; i < formula.size(); i++)
				{
					BrailleInfo info = formula.get(i);
					if (info.equals(lm))
					{
						formula.set(i, lo);
						break;
					}
				}
			}
			else
			{
				BrailleInfo lu = translater.getDict().getBrailleInfo(MATRIX.LU.getKey());
				BrailleInfo ld = translater.getDict().getBrailleInfo(MATRIX.LD.getKey());
				if (null == lu || null == ld || null == lm)
				{
					return;
				}
				for (int i = 0; i < formula.size(); i++)
				{
					BrailleInfo info = formula.get(i);
					if (info.equals(lm))
					{
						formula.set(i, lu);
						break;
					}
				}
				for (int i = (formula.size()-1); i >= 0 ; i--)
				{
					BrailleInfo info = formula.get(i);
					if (info.equals(lm))
					{
						formula.set(i, ld);
						break;
					}
				}
			}
		}

		private void addClose(BrailleTranslater translater, List<BrailleInfo> formula, String close, int nRow)
		{
			BrailleInfo rm = translater.getDict().getBrailleInfo(MATRIX.RM.getKey());
			if (1 == nRow)
			{
				BrailleInfo rc = translater.getDict().getBrailleInfo(close);
				if (null == rc)
				{
					return;
				}
				for (int i = (formula.size()-1); i >= 0 ; i--)
				{
					BrailleInfo info = formula.get(i);
					if (info.equals(rm))
					{
						formula.set(i, rc);
						break;
					}
				}
			}
			else
			{
				BrailleInfo ru = translater.getDict().getBrailleInfo(MATRIX.RU.getKey());
				BrailleInfo rd = translater.getDict().getBrailleInfo(MATRIX.RD.getKey());
				if (null == ru || null == rd || null == rm)
				{
					return;
				}
				for (int i = 0; i < formula.size(); i++)
				{
					BrailleInfo info = formula.get(i);
					if (info.equals(rm))
					{
						formula.set(i, ru);
						break;
					}
				}
				for (int i = (formula.size()-1); i >= 0 ; i--)
				{
					BrailleInfo info = formula.get(i);
					if (info.equals(rm))
					{
						formula.set(i, rd);
						break;
					}
				}
			}
		}

		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			boolean flagOpen = false;
			boolean flagClose = false;

			// 行列のカッコの有無
			Element parent = (Element)elm.getParentNode();
			String parentName = parent.getTagName();
			String open = null;
			String close = null;
			if (parentName.equals("mfenced"))
			{
				open = parent.getAttribute(ATTR_OPEN);
				close = parent.getAttribute(ATTR_CLOSE);
			}
			else
			{
				for (Node prev = elm.getPreviousSibling(); null != prev; prev = prev.getPreviousSibling())
				{
					if (prev instanceof Element)
					{
						Element el = (Element) prev;
						if (el.getTagName().equals("mo") || el.getTagName().equals("mfencedleft"))
						{
							open = el.getTextContent().replace("\n", "").trim();
						}
					}
				}
				for (Node next = elm.getNextSibling(); null != next; next = next.getNextSibling())
				{
					if (next instanceof Element)
					{
						Element el = (Element) next;
						if (el.getTagName().equals("mo") || el.getTagName().equals("mfencedright"))
						{
							close = next.getTextContent().replace("\n", "").trim();
						}
					}
				}
			}
			if (null != open)
			{
				switch(open)
				{
				case "(":
				case "[":
				case "{":
					flagOpen = true;
					break;
				}
			}
			if (null != close)
			{
				switch(close)
				{
				case ")":
				case "]":
				case "}":
					flagClose = true;
					break;
				}
			}
			tableOpenStack.push(flagOpen ? new String(open) : "");
			tableCloseStack.push(flagClose ? new String(close): "");

			// 行数を数える
			int nRow = 0;
			NodeList children = elm.getChildNodes();
			for (int i = 0; i < children.getLength(); i++)
			{
				Node node = children.item(i);
				if (node instanceof Element)
				{
					Element el = (Element)node;
					if (el.getTagName().equals("mtr") || el.getTagName().equals("labeledmtr"))
					{
						nRow++;
					}
				}
			}

			List<BrailleInfo> table = Util.newArrayList();

			int nBraille = parseChildren(translater, table, elm);

//			if (1 < nBraille)
//			{
//				addBlock(translater, table);
//			}

			// 行列のカッコを追加
			open = tableOpenStack.pop();
			close = tableCloseStack.pop();

			if (false == open.isEmpty())
			{
				this.addOpen(translater, table, open, nRow);
			}

			if (false == close.isEmpty())
			{
				this.addClose(translater, table, close, nRow);
			}

			addSijifu(formula, table);

			addKugiri(translater, formula, table);

			EnumMap<BrailleInfo.TABLE_OPTION, Object> options = null;
			BrailleInfo.TABLE_OPTION[] names = {BrailleInfo.TABLE_OPTION.FRAME, BrailleInfo.TABLE_OPTION.ROWLINES, BrailleInfo.TABLE_OPTION.COLUMNLINES};
			for (BrailleInfo.TABLE_OPTION name: names)
			{
				String val = elm.getAttribute(name.getAttrName());
				if (false == val.isEmpty() && false == val.equals("none"))
				{
					options = new EnumMap<BrailleInfo.TABLE_OPTION, Object>(BrailleInfo.TABLE_OPTION.class);
					options.put(name, val);
				}
			}
			table.add(0, translater.getDict().getTable(TABLE.TABLE_OPEN, options));
			table.add(translater.getDict().getTable(TABLE.TABLE_CLOSE, null));

			formula.addAll(table);

			return nBraille;
		}
	}

	private static class mtr extends TagBase
	{
		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			List<BrailleInfo> row = Util.newArrayList();

			int nBraille = parseChildren(translater, row, elm);

			//			if (1 < nBraille)
			//			{
			//				addBlock(translater, row);
			//			}

			String sopen = tableOpenStack.peek();
			if (false == sopen.isEmpty())
			{
				BrailleInfo open = translater.getDict().getBrailleInfo(MATRIX.LM.getKey());
				row.add(0, open);
			}
			String sclose = tableCloseStack.peek();
			if (false == sclose.isEmpty())
			{
				BrailleInfo close = translater.getDict().getBrailleInfo(MATRIX.RM.getKey());
				row.add(close);
			}

			addSijifu(formula, row);

			addKugiri(translater, formula, row);

			row.add(0, translater.getDict().getTable(TABLE.ROW_START, null));
			row.add(translater.getDict().getTable(TABLE.ROW_END, null));

			formula.addAll(row);

			return nBraille;
		}
	}

	private static class mlabeledtr extends mtr
	{
	}

	private static class mtd extends TagBase
	{
		@Override
		public int parseTag(BrailleTranslater translater, List<BrailleInfo> formula, Element elm)
		{
			String attr = elm.getAttribute(BrailleInfo.TABLE_OPTION.ROWSPAN.getAttrName());
			int rowSpan = (attr.isEmpty() ? 1 : Integer.parseInt(attr));
			attr = elm.getAttribute(BrailleInfo.TABLE_OPTION.COLUMNSPAN.getAttrName());
			int columnSpan = (attr.isEmpty() ? 1 : Integer.parseInt(attr));

			List<BrailleInfo> cell = Util.newArrayList();

			int nBraille = parseChildren(translater, cell, elm);
			if (0 == nBraille)
			{
//				cell.add(translater.getDict().getSpace());
				cell.add(BrailleInfo.SPACE);
			}

			//			if (1 < nBraille)
			//			{
			//				BrailleInfo tmp = cell.get(0);
			//				boolean flag = true;
			//				for (TABLE key: TABLE.values())
			//				{
			//					if (tmp.haveTable(TABLE.TABLE_OPEN))
			//					{
			//						flag = false;
			//						break;
			//					}
			//				}
			//				if (flag)
			//				{
			//					addBlock(translater, cell);
			//				}
			//			}

			addSijifu(formula, cell);

			addKugiri(translater, formula, cell);

			EnumMap<BrailleInfo.TABLE_OPTION, Object> options = null;
			if (1 < rowSpan || 1 < columnSpan)
			{
				options = new EnumMap<BrailleInfo.TABLE_OPTION, Object>(BrailleInfo.TABLE_OPTION.class);
				if (1 < rowSpan)
				{
					options.put(BrailleInfo.TABLE_OPTION.ROWSPAN, rowSpan);
				}
				if (1 < columnSpan)
				{
					options.put(BrailleInfo.TABLE_OPTION.COLUMNSPAN, columnSpan);
				}
			}
			cell.add(0, translater.getDict().getTable(TABLE.CELL_START, options));
			cell.add(translater.getDict().getTable(TABLE.CELL_END, null));

			formula.addAll(cell);

			return nBraille;
		}
	}
}
