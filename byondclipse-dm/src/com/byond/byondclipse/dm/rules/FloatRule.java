package com.byond.byondclipse.dm.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class FloatRule implements IRule
{
	IToken fToken;

	public FloatRule(final IToken token)
	{
		this.fToken										= token;
		assert token != null;
	}

	private boolean isDigit(final int c)				{ return c >= 0x30 && c <= 0x39; } // [0-9]

	@Override public IToken evaluate(final ICharacterScanner scanner)
	{
		int cnt											= 0;

		int c											= scanner.read();
		cnt												= cnt + 1;

		if (this.isDigit(c))
		{
			do
			{
				c										= scanner.read();
				cnt										= cnt + 1;
			}
			while (this.isDigit(c));
		}

		scanner.unread();
		cnt												= cnt - 1;

		// either we start with . or continue with .
		c												= scanner.read();
		cnt												= cnt + 1;

		if (c == '.')
		{
			final int cnt2								= cnt;
			do
			{
				c										= scanner.read();
				cnt										= cnt + 1;
			}
			while (this.isDigit(c));

			scanner.unread();
			cnt											= cnt - 1;

			// we have not read any digits after the .
			// so we reject this as a number
			if (cnt == cnt2)
			{
				// spit out the whole thing.
				while (cnt > 0)
				{
					scanner.unread();
					cnt = cnt - 1;
				}

				return Token.UNDEFINED;
			}

			// we have seen a number of the format
			// [0-9]+.[0-9]+ or .[0-9]+
			return this.fToken;
		}

		scanner.unread();
		cnt												= cnt - 1;

		return (cnt > 0 ? this.fToken : Token.UNDEFINED);
	}
}