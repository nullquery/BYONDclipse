package com.byond.byondclipse.dm.editors;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class DMWhitespaceDetector implements IWhitespaceDetector
{
	@Override public boolean isWhitespace(final char c)			{ return (c == ' ' || c == '\t' || c == '\n' || c == '\r'); }
}
