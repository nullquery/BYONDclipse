package com.byonddev.byondclipse.dm.contentassist;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

public class ProposalInfoModel
{
	private final ProposalPosition position;
	private final String functionName;
	private final String functionBody;
	private final String prefix;
	private final String path;
	private final int depth;

	public ProposalInfoModel(final IDocument doc, final int documentOffset)
	{
		final StringBuilder sb										= new StringBuilder();
		int pos;
		char c;
		int depth, targetDepth;
		String tmp;

		pos															= documentOffset;

		// Go to the beginning of the current line.
		while (true)
		{
			while (true)
			{
				try
				{
					c												= doc.getChar(--pos);

					if (c == '\n')									{ break; }
					else											{ sb.append(c); }
				}
				catch (final BadLocationException e)				{ break; }
			}

			try
			{
				if (doc.getContentType(pos).equals(IDocument.DEFAULT_CONTENT_TYPE))
				{
					break;
				}
			}
			catch (final BadLocationException e)					{ break; }
		}

		final int startPos											= pos;

		ProposalPosition position									= null;
		String prefix												= sb.reverse().toString().trim();

		if (!prefix.isEmpty())
		{
			pos														= prefix.length();

			while (true)
			{
				try
				{
					c												= prefix.charAt(--pos);

					if (!(Character.isAlphabetic(c) || Character.isDigit(c)))
					{
						prefix										= prefix.substring(pos + 1);
						break;
					}
				}
				catch (final IndexOutOfBoundsException e)			{ break; }
			}
		}

		this.prefix													= prefix;

		sb.delete(0, sb.length());

		pos															= startPos;
		depth														= 0;

		// Count the amount of tabs and determine the current depth of the code block.
		while (true)
		{
			try
			{
				c													= doc.getChar(++pos);

				if (c == '\t')										{ depth = depth + 1; }
				else												{ break; }
			}
			catch (final BadLocationException e)					{ break; }
		}

		this.depth													= depth;

		// Move up the chain to the root node, storing the path as we go along.

		pos															= startPos;
		targetDepth													= depth - 1;
		depth														= 0;

		// List to contain the components of the path.
		final List<String> paths									= new Vector<>();

		// List to contain the positions of the components of the path. (related to above list)
		final List<Integer> positions								= new Vector<>();

		// List to contain the depths of the components of the path. (related to above list)
		final List<Integer> depths									= new Vector<>();

		while (true)
		{
			try
			{
				c													= doc.getChar(--pos);

				if (c == '\n')
				{
					// If we receive a newline we start over...
					depth											= 0;

					sb.delete(0, sb.length());
				}
				else
				{
					// Anything else we'll grab the entire line.

					// Load the current line in a StringBuilder.
					while (true)
					{
						try
						{
							c										= doc.getChar(--pos);

							if (c == '\n')
							{
								pos									= pos + 1;
								break;
							}
							else									{ sb.append(c); }
						}
						catch (final BadLocationException e)		{ break; }
					}

					// Reverse the string in the StringBuilder (because we go right to left)
					sb.reverse();

					// Determine the content type for the start position of the current line.
					tmp												= doc.getContentType(pos);

					// Determine the depth of the current line.
					depth											= 0;

					while (sb.length() > 0 && sb.charAt(0) == '\t')
					{
						depth										= depth + 1;

						sb.delete(0, 1);
					}

					if (tmp.equals(IDocument.DEFAULT_CONTENT_TYPE) && depth == targetDepth)
					{
						tmp											= sb.toString().trim();

						if (!tmp.isEmpty())
						{
							paths.add(tmp);
							positions.add(pos);
							depths.add(depth);

							targetDepth								= targetDepth - 1;
						}
					}
				}
			}
			catch (final BadLocationException e)					{ break; }
		}

		// Reverse paths and positions so that they are in chronological order.
		Collections.reverse(paths);
		Collections.reverse(positions);
		Collections.reverse(depths);

		// Generate the path string and determine the position of the function body.
		sb.delete(0, sb.length());

		pos															= -1;

		int i														= 0;

		main_loop:
		for (final String pathPartMain : paths)
		{
			for (final String pathPart : pathPartMain.split("/"))
			{
				if (!(pathPart.contains("(") || pathPart.contains(")") || pathPart.equals("var") || pathPart.startsWith("var/") || pathPart.equals("proc") || pathPart.startsWith("proc/")))
				{
					sb.append(pathPart + "/");
				}
				else
				{
					if (pathPart.contains("(") || pathPart.contains(")"))
					{
						pos											= positions.get(i);
						targetDepth									= depths.get(i) + 1;

						break main_loop;
					}
				}
			}

			i														= i + 1;
		}

		if (sb.length() > 0)
		{
			while (sb.charAt(0) == '/')								{ sb.delete(0, 1); }
			while (sb.charAt(sb.length() - 1) == '/')				{ sb.delete(sb.length() - 1, sb.length()); }

			sb.insert(0, "/");
		}

		this.path													= sb.toString();

		if (pos > -1)
		{
			sb.delete(0, sb.length());

			// Strip the function name from the function body (the above implementation includes the definition
			// of the proc/verb that is being executed)
			while (true)
			{
				try
				{
					c												= doc.getChar(++pos);

					if (c == '\n')									{ break; }
					else
					{
						sb.append(c);
					}
				}
				catch (final BadLocationException e)				{ break; }
			}

			tmp														= sb.toString();

			if (tmp.contains("("))
			{
				i													= tmp.indexOf("(");

				if (i == -1)										{ i = tmp.length(); }

				if (tmp.contains("/"))
				{
					i												= tmp.lastIndexOf("/", i);

					if (i != -1)									{ tmp = tmp.substring(tmp.lastIndexOf("/", i) + 1); }
				}
			}

			this.functionName										= tmp;

			final StringBuilder fsb									= new StringBuilder();

			sb.delete(0, sb.length());

			while (true)
			{
				try
				{
					c												= doc.getChar(++pos);

					if (c == '\n')
					{
						// If we receive a newline we start over...
						sb.delete(0, sb.length());

						fsb.append(c);
					}
					else
					{
						pos											= pos - 1;

						// Anything else we'll grab the entire line.

						// Load the current line in a StringBuilder.
						while (true)
						{
							try
							{
								c									= doc.getChar(++pos);

								if (c == '\n')
								{
									pos								= pos - 1;
									break;
								}
								else
								{
									sb.append(c);
								}
							}
							catch (final BadLocationException e)	{ break; }
						}

						// Determine the content type for the start position of the current line.
						tmp											= doc.getContentType(pos);

						// Determine the depth of the current line.
						depth										= 0;

						i											= 0;

						while (sb.length() > 0 && sb.charAt(i) == '\t')
						{
							depth									= depth + 1;

							i										= i + 1;
						}

						if (tmp.equals(IDocument.DEFAULT_CONTENT_TYPE) && depth < targetDepth)
						{
							tmp										= sb.toString().trim();

							if (!tmp.isEmpty())						{ break; }
						}

						fsb.append(sb);
					}
				}
				catch (final BadLocationException e)				{ break; }
			}

			this.functionBody										= fsb.toString();

			position												= ProposalPosition.BODY;
		}
		else
		{
			this.functionName										= "";
			this.functionBody										= "";

			position												= this.path.isEmpty() ? ProposalPosition.ROOT : ProposalPosition.TREE;
		}
		this.position												= position;
	}

	public ProposalPosition getPosition()							{ return this.position; }
	public String getFunctionName()									{ return this.functionName; }
	public String getFunctionBody()									{ return this.functionBody; }
	public String getPrefix()										{ return this.prefix; }
	public String getPath()											{ return this.path; }
	public int getDepth()											{ return this.depth; }
}