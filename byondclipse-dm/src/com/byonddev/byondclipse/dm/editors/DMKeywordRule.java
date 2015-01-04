package com.byonddev.byondclipse.dm.editors;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordRule;

public class DMKeywordRule extends WordRule
{
	private static final List<String> keywords							= Arrays.asList("arg", "as", "break", "const", "continue", "del", "do", "else", "for", "global", "goto", "if", "in", "new", "proc", "return", "set", "static", "step", "switch", "tmp", "to", "var", "verb", "while");

	public static List<String> getKeywords()							{ return Collections.unmodifiableList(keywords); }

	public DMKeywordRule(final IToken keywordToken, final IToken defaultToken, final boolean ignoreCase)
	{
		super(new DMKeywordDetector(), defaultToken, ignoreCase);

		for (final String keyword : keywords)							{ this.addWord(keyword, keywordToken); }
	}

	private static class DMKeywordDetector implements IWordDetector
	{
		@Override public boolean isWordPart(final char c)				{ return Character.isJavaIdentifierPart(c); }
		@Override public boolean isWordStart(final char c)				{ return Character.isJavaIdentifierStart(c); }
	}
}