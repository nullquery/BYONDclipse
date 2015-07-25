package com.byond.byondclipse.dmm;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MapFile
{
	private static final Pattern PATTERN_MODEL				= Pattern.compile("^\"([^\"]*)\"\\s*=\\s*\\((.*)\\)$", Pattern.MULTILINE);
	private static final Pattern PATTERN_GRID				= Pattern.compile("\\(\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,(\\d+)\\s*\\)\\s*=\\s*\\{\"(.*?)\"\\}", Pattern.DOTALL);

	public static MapFile parse(final String str)
	{
		return new MapFile(str);
	}

	private MapFile(final String str)
	{
		try
		{
			final Map<String, String> models				= new LinkedHashMap<>();
			final Map<Coordinates, String> grid				= new LinkedHashMap<>();

			Matcher m;

			m = PATTERN_MODEL.matcher(str);

			while (m.find())
			{
				models.put(m.group(1), m.group(2));
			}

			System.out.println(models);

			m = PATTERN_GRID.matcher(str);

			while (m.find())
			{
				grid.put(new Coordinates(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3))), m.group(4));
			}

			System.out.println(grid);
		}
		catch (final Exception e)
		{
			e.printStackTrace();
		}
	}
}