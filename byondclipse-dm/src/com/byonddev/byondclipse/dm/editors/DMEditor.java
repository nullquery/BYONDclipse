package com.byonddev.byondclipse.dm.editors;

import org.eclipse.ui.editors.text.TextEditor;

public class DMEditor extends TextEditor
{
	private final ColorManager colorManager;

	public DMEditor()
	{
		super();
		this.colorManager								= new ColorManager();
		this.setSourceViewerConfiguration(new DMConfiguration(this.colorManager));
		this.setDocumentProvider(new DMDocumentProvider());
	}

	@Override public void dispose()
	{
		this.colorManager.dispose();
		super.dispose();
	}
}