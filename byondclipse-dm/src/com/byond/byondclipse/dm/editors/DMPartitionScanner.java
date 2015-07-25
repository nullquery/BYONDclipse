package com.byond.byondclipse.dm.editors;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

import com.byond.byondclipse.dm.rules.DMStringRule;

public class DMPartitionScanner extends RuleBasedPartitionScanner
{
	public final static String DM_COMMENT						= "__dm_comment";
	public final static String DM_STRING						= "__dm_string";

	public DMPartitionScanner()
	{
		final IToken dmComment									= new Token(DM_COMMENT);
		final IToken dmString									= new Token(DM_STRING);

		final IPredicateRule[] rules							= new IPredicateRule[5];

		rules[0]												= new MultiLineRule("/*", "*/", dmComment);
		rules[1]												= new EndOfLineRule("//", dmComment, '\\', true);
		rules[2]												= new MultiLineRule("{\"", "\"}", dmString);
		rules[3]												= new DMStringRule(dmString);
		rules[4]												= new PatternRule("'", "'", dmString, '\\', true, true, true);

		this.setPredicateRules(rules);
	}
}