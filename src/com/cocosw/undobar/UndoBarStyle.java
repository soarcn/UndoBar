package com.cocosw.undobar;

import com.cocosw.undobar.R.drawable;

public class UndoBarStyle {

	int iconRes;
	int titleRes;
	int bgRes = drawable.undobar;

	public UndoBarStyle(final int icon, final int title) {
		iconRes = icon;
		titleRes = title;
	}

	public UndoBarStyle(final int icon, final int title, final int bg) {
		iconRes = icon;
		titleRes = title;
		bgRes = bg;
	}

}
