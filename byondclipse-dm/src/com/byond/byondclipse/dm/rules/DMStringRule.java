package com.byond.byondclipse.dm.rules;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class DMStringRule implements IPredicateRule
{
	IToken fToken;

	public DMStringRule(final IToken token)
	{
		this.fToken																= token;
		assert token != null;
	}

	private static boolean isQuote(final int c)									{ return ((char) c) == '\"'; }

	public IToken doEvaluate(final ICharacterScanner scanner, final boolean resume)
	{
		int c;

		if (!resume)
		{
			if (!isQuote(scanner.read()))
			{
				scanner.unread();

				return Token.UNDEFINED;
			}
		}

		boolean escapeCharUsed													= false;
		int depth																= 0;

		while ((c = scanner.read()) != ICharacterScanner.EOF)
		{
			if (depth <= 0 && !escapeCharUsed && (isQuote(c) || c == '\n'))		{ return this.fToken; }

			if (!escapeCharUsed && (c == '[' || c == ']'))
			{
				if (c == ']')													{ depth = depth - 1; }
				else															{ depth = depth + 1; }
			}

			if (!escapeCharUsed && c == '\\')									{ escapeCharUsed = true; }
			else if (c != '\r')													{ escapeCharUsed = false; }
		}

		scanner.unread();

		return Token.UNDEFINED;
	}

	@Override public IToken evaluate(final ICharacterScanner scanner)			{ return this.evaluate(scanner, false); }

	@Override public IToken evaluate(final ICharacterScanner scanner, final boolean resume)
	{
		final int c																= scanner.read();

		scanner.unread();

		if (isQuote(c))															{ return this.doEvaluate(scanner, resume); }
		else																	{ return Token.UNDEFINED; }
	}

	@Override public IToken getSuccessToken()									{ return this.fToken; }
}