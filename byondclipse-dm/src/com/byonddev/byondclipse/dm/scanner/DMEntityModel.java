package com.byonddev.byondclipse.dm.scanner;

public class DMEntityModel
{
	private final DMEntityType type;
	private final String name;
	private final String displayName;
	private final String path;
	private final String file;
	private final int lineNumber;
	private final String infoText;

	protected DMEntityModel(final DMEntityType type, final String name, final String displayName, final String path, final String file, final int lineNumber, final String infoText)
	{
		this.type											= type;
		this.name											= name;
		this.displayName									= displayName;
		this.path											= path;
		this.file											= file;
		this.lineNumber										= lineNumber;
		this.infoText										= infoText;
	}

	public DMEntityType getType()							{ return this.type; }
	public String getName()									{ return this.name; }
	public String getDisplayName()							{ return this.displayName; }
	public String getPath()									{ return this.path; }
	public String getFile()									{ return this.file; }
	public int getLineNumber()								{ return this.lineNumber; }
	public String getInfoText()								{ return this.infoText; }
}