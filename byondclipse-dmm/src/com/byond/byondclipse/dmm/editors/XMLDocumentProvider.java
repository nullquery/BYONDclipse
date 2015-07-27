package com.byond.byondclipse.dmm.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

import com.byond.byondclipse.dmm.MapFile;

public class XMLDocumentProvider extends FileDocumentProvider {

	@Override
	protected IDocument createDocument(final Object element) throws CoreException
	{
		final IDocument document = super.createDocument(element);

		if (document != null)
		{
			MapFile.parse(document.get());

			final IDocumentPartitioner partitioner =
				new FastPartitioner(
					new XMLPartitionScanner(),
					new String[] {
						XMLPartitionScanner.XML_TAG,
						XMLPartitionScanner.XML_COMMENT });
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
}