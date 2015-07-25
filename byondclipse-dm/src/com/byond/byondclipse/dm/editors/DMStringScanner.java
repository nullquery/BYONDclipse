package com.byond.byondclipse.dm.editors;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;

public class DMStringScanner extends RuleBasedScanner
{
	public DMStringScanner(final ColorManager manager)
	{
		final IToken opToken							= new Token(new TextAttribute(manager.getColor(IDMColorConstants.STRINGOP)));

		final IRule[] rules								= new IRule[2];
		// Add generic whitespace rule.
		rules[0]										= new WhitespaceRule(new DMWhitespaceDetector());
		// Add keyword detection
		rules[1]										= new SingleLineRule("[", "]", opToken);

		this.setRules(rules);
	}
}