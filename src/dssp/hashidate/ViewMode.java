/**
 *
 */
package dssp.hashidate;

/**
 * 表示モード
 *
 * @author DSSP/Minoru Yagi
 *
 */
public final class ViewMode
{
	public static enum MODE
	{
		/**
		 * 墨字モード
		 */
		SUMIJI,
		/**
		 * 点字モード
		 */
		BRAILLE
	};
	private static MODE mode = MODE.SUMIJI;

	public static MODE getMode()
	{
		return ViewMode.mode;
	}

	public static void setMode(MODE mode)
	{
		ViewMode.mode = mode;
	}
}
