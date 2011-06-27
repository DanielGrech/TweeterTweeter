package com.DGSD.TweeterTweeter.Utils;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView;

public class Tokenizer implements MultiAutoCompleteTextView.Tokenizer {
	//private static final String TAG = Tokenizer.class.getSimpleName();
	
	@Override
	public int findTokenEnd(CharSequence text, int cursor) {
		int i = cursor;
		int len = text.length();

		while (i < len) {
			if (text.charAt(i) == ' ' || text.charAt(i) == '@') {
				return i;
			} else {
				i++;
			}
		}
		return len;
	}

	@Override
	public int findTokenStart(CharSequence text, int cursor) {
		int i = cursor;

		while (i > 0 && text.charAt(i - 1) != '@') {
			i--;
		}

		return i;
	}

	@Override
	public CharSequence terminateToken(CharSequence text) {
		int i = text.length();

		while (i > 0 && text.charAt(i - 1) == ' ') {
			i--;
		}

		if (i > 0 && text.charAt(0) == '@') {
			return text;
		} else {
			if (text instanceof Spanned) {
				SpannableString sp = new SpannableString(text+" ");
				TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
						Object.class, sp, 0);
				return sp;
			} else {
				return text + " ";
			}
		}
	}

}
