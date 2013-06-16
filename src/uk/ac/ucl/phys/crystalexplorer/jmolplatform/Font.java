package uk.ac.ucl.phys.crystalexplorer.jmolplatform;

import org.jmol.util.JmolFont;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

/**
 * methods required by Jmol that access java.awt.Font
 * 
 * private to org.jmol.awt
 * 
 */

class Font {
	private final float myScaleFactor;
	
	Font(final float scaleFactor) {
		myScaleFactor = scaleFactor;
	}

	Object newFont(String fontFace, boolean isBold, boolean isItalic,
			float fontSize) {
		int style = Typeface.NORMAL | (isBold ? Typeface.BOLD : 0)
				| (isItalic ? Typeface.ITALIC : 0);
		Typeface typeface = Typeface.create(fontFace, style);
		Paint paint = new Paint();
		paint.setColor(Color.WHITE);
		paint.setTypeface(typeface);
		paint.setTextSize(fontSize * myScaleFactor);
		return paint;
	}

	Object getFontMetrics(JmolFont font, Object graphics) {
		return ((Paint) font.font).getFontMetricsInt();
	}

	int getAscent(Object metrics) {
		return Math.abs(((Paint.FontMetricsInt) metrics).ascent);
	}

	int getDescent(Object metrics) {
		return Math.abs(((Paint.FontMetricsInt) metrics).descent);
	}

	int stringWidth(JmolFont font, String text) {
		return (int)((Paint) font.font).measureText(text);
	}
}
