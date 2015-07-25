package com.byond.byondclipse.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class BEO
{
	public static byte[] getBytesFromFile(final File file) throws IOException
	{
		try (InputStream in = new BufferedInputStream(new FileInputStream(file)))
		{
			return getBytesFromInputStream(in);
		}
	}

	public static byte[] getBytesFromInputStream(final InputStream in) throws IOException
	{
		final ByteArrayOutputStream baos					= new ByteArrayOutputStream();
		final byte[] buffer									= new byte[1024];
		int bytesRead;

		while ((bytesRead = in.read(buffer)) != -1)			{ baos.write(buffer, 0, bytesRead); }

		return baos.toByteArray();
	}

	public static String toString(final byte[] bytes)
	{
		return new String(bytes);
	}

	public static byte[] toBytes(final String str)
	{
		try													{ return str.getBytes("UTF-8"); }
		catch (final UnsupportedEncodingException e)		{ throw new RuntimeException(e); }
	}

	private static void parseVariable(String line, final Set<String> variables)
	{
		line												= line.trim();

		if (!line.isEmpty())
		{
			final StringBuilder sb							= new StringBuilder();
			int pos											= -1;
			boolean skipUntilSeperator						= false;
			boolean inString								= false;
			int listDepth									= 0;
			int depth										= 0;
			boolean skipNext = false;
			char c;
			String tmp;

			while (true)
			{
				try
				{
					c											= line.charAt(++pos);

					if (c == '\"' && depth == 0)				{ inString = !inString; }

					if (inString && !skipNext)
					{
						if (c == '\\')							{ skipNext = true; }
						else if (c == '[')						{ depth = depth + 1; }
						else if (c == ']')						{ depth = depth - 1; }
					}

					if (!inString && !skipNext)
					{
						if (c == '(')							{ listDepth = listDepth + 1; }
						else if (c == ')')						{ listDepth = listDepth - 1; }
					}

					if (!inString && depth == 0 && listDepth == 0)
					{
						if (c == '=')
						{
							skipUntilSeperator					= true;

							continue;
						}
						else if (c == ',')
						{
							skipUntilSeperator					= false;

							tmp									= sb.toString().trim();

							if (tmp.contains("/"))				{ tmp = tmp.substring(tmp.lastIndexOf("/") + 1); }

							if (!tmp.isEmpty())					{ variables.add(tmp); }

							sb.delete(0, sb.length());

							continue;
						}
					}

					if (!skipUntilSeperator)
					{
						sb.append(c);
					}
				}
				catch (final IndexOutOfBoundsException e)
				{
					tmp											= sb.toString().trim();

					if (tmp.contains("/"))						{ tmp = tmp.substring(tmp.lastIndexOf("/") + 1); }

					if (!tmp.isEmpty())							{ variables.add(tmp); }

					break;
				}
			}
		}
	}

	public static Set<String> getVariablesFromBody(final String body)
	{
		final Set<String> variables							= new TreeSet<>(Collections.reverseOrder());

		int targetDepth										= -1;
		boolean isVar										= false;
		int depth;

		for (String line : body.replace("\r", "").split("\n"))
		{
			depth											= 0;

			while (!line.isEmpty() && line.charAt(0) == '\t')
			{
				depth										= depth + 1;
				line										= line.substring(1);
			}

			if (!line.isEmpty())
			{
				if (isVar)
				{
					if (depth == targetDepth)				{ parseVariable(line, variables); }
					else									{ isVar = false; }
				}

				if (!isVar)
				{
					if (line.equals("var") || line.equals("var/"))
					{
						isVar								= true;
						targetDepth							= depth + 1;
					}
					else if (line.startsWith("var/"))		{ parseVariable(line.substring(4), variables); }
				}
			}
		}

		return variables;
	}
}