package com.byond.byondclipse.dm.editors;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;

import com.byond.byondclipse.dm.rules.FloatRule;

public class DMScanner extends RuleBasedScanner
{
	public DMScanner(final ColorManager manager)
	{
		final IToken dmPreprocessor						= new Token(new TextAttribute(manager.getColor(IDMColorConstants.DM_PREPROCESSOR)));
		final IToken keyword							= new Token(new TextAttribute(manager.getColor(IDMColorConstants.KEYWORD)));
		final IToken defaultToken						= new Token(new TextAttribute(manager.getColor(IDMColorConstants.DEFAULT)));
		final IToken numberToken						= new Token(new TextAttribute(manager.getColor(IDMColorConstants.NUMBER)));

		final IRule[] rules								= new IRule[4];
		//Add rule for processing instructions
		rules[0]										= new EndOfLineRule("#", dmPreprocessor);
		// Add generic whitespace rule.
		rules[1]										= new WhitespaceRule(new DMWhitespaceDetector());
		// Add keyword detection
		rules[2]										= new DMKeywordRule(keyword, defaultToken, false);
		// Add number detection
		rules[3]										= new FloatRule(numberToken);

		this.setRules(rules);
	}
}