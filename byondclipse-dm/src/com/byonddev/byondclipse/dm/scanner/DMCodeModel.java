package com.byonddev.byondclipse.dm.scanner;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.byonddev.byondclipse.dm.contentassist.DMContentAssistProcessor;

public class DMCodeModel
{
	private final Map<String, List<DMEntityModel>> entities							= new LinkedHashMap<>();

	public void clear(final String file)
	{
		synchronized (this.entities)												{ this.entities.remove(file); }
	}

	public List<DMEntityModel> getEntitiesByPath(final String path)
	{
		synchronized (this.entities)
		{
			final List<DMEntityModel> res											= new Vector<>();

			for (final List<DMEntityModel> deml : this.entities.values())
			{
				for (final DMEntityModel dem : deml)
				{
					if (dem.getPath().isEmpty() || dem.getPath().startsWith(path))	{ res.add(dem); }
				}
			}

			return res;
		}
	}

	public void scanAll()
	{
		synchronized (this.entities)
		{
			this.entities.clear();

			this.scanBuiltin();
		}
	}

	private void scanBuiltin()
	{
		Document doc																= null;

		try
		{
			try (InputStream in = new BufferedInputStream((InputStream) DMContentAssistProcessor.class.getResource("/META-INF/stddef.xml").getContent()))
			{
				final DocumentBuilderFactory factory								= DocumentBuilderFactory.newInstance();
				final DocumentBuilder builder										= factory.newDocumentBuilder();

				doc																	= builder.parse(in);
			}
		}
		catch (final Exception e)													{ /* no problem */ }

		if (doc != null)
		{
			final NodeList nl														= doc.getElementsByTagName("entry");
			final int nll															= nl.getLength();
			Element elem;
			boolean isField;

			for (int i = 0; i < nll; i = i + 1)
			{
				elem																= (Element) nl.item(i);

				isField																= elem.getAttribute("path").contains("/var/") || elem.getAttribute("path").endsWith("/var");

				this.putEntity(new DMEntityModel(isField ? DMEntityType.VAR : DMEntityType.PROC, elem.getAttribute("name"), elem.getAttribute("displayName"), elem.getAttribute("path"), "__builtin__", -1, elem.getTextContent()));
			}
		}
	}

	private void putEntity(final DMEntityModel dem)
	{
		if (!this.entities.containsKey(dem.getFile()))									{ this.entities.put(dem.getFile(), new Vector<DMEntityModel>()); }

		this.entities.get(dem.getFile()).add(dem);
	}

	public void scan(final String file)
	{
		synchronized (this.entities)
		{
			this.entities.remove(file);
		}
	}
}