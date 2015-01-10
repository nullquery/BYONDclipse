package com.byonddev.byondclipse.dmm.editors;

import org.eclipse.ui.editors.text.TextEditor;

public class DMMEditor extends TextEditor {

	private ColorManager colorManager;

	public DMMEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new XMLConfiguration(colorManager));
		setDocumentProvider(new XMLDocumentProvider());
	}
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}

}
