package dssp.hashidate.shape.helper;

import dssp.brailleLib.BrailleDict;
import dssp.brailleLib.BrailleRenderer;
import dssp.brailleLib.BrailleTranslater;
import dssp.brailleLib.Util;
import dssp.hashidate.config.Config;

public class BrailleToolkit
{
	private static BrailleRenderer renderer = null;
	private static BrailleTranslater formulaTranslater = null;
	private static BrailleTranslater textTranslater = null;
	private static BrailleFormula formula = null;

	/**
	 * 数式変換を取得する
	 *
	 * @return
	 */
	public static BrailleTranslater getFormulaTranslater()
	{
		if (null == formulaTranslater)
		{
			BrailleDict dict = Config.getConfig(Config.BRAILLE.DICT_FORMULA);
			formulaTranslater = BrailleTranslater.newInstance(dict);
		}
		return formulaTranslater;
	}

	/**
	 * テキスト変換を取得する
	 *
	 * @return
	 */
	public static BrailleTranslater getTextTranslater()
	{
		if (null == textTranslater)
		{
			BrailleDict dict = Config.getConfig(Config.BRAILLE.DICT_TEXT);
			textTranslater = BrailleTranslater.newInstance(dict);
		}
		return textTranslater;
	}

	/**
	 * 点字描画を取得する
	 *
	 * @return
	 */
	public static BrailleRenderer getRenderer()
	{
		if (null == renderer)
		{
			renderer = BrailleRenderer.newInstance();

			int size = Config.getConfig(Config.BRAILLE.SPACE_X);
			size = Util.mmToPixel(0, size);
			renderer.setDotSpaceX(size);

			size = Config.getConfig(Config.BRAILLE.SPACE_Y);
			size = Util.mmToPixel(0, size);
			renderer.setDotSpaceY(size);

			size = Config.getConfig(Config.BRAILLE.BOX_SPACE);
			size = Util.mmToPixel(0, size);
			renderer.setBoxSpace(size);

			size = Config.getConfig(Config.BRAILLE.LINE_SPACE);
			size = Util.mmToPixel(0, size);
			renderer.setLineSpace(size);
		}
		return renderer;
	}

	/**
	 * 点字数式変換を取得するう
	 *
	 * @return
	 */
	public static BrailleFormula getFormula()
	{
		if (null == formula)
		{
			formula = BrailleFormula.newInstance(BrailleToolkit.getFormulaTranslater());
		}

		return formula;
	}

}
