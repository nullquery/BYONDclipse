package com.byond.byondclipse.dm.editors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;

@SuppressWarnings("deprecation")
public class DMDocumentProvider extends FileDocumentProvider
{
	@Override protected IDocument createDocument(final Object element) throws CoreException
	{
		final IDocument document											= super.createDocument(element);

		if (document != null)
		{
			final String[] toBeProcessed									= new String[]
																			{
																				DMPartitionScanner.DM_COMMENT,
																				DMPartitionScanner.DM_STRING
																			};

			// Use of RuleBasedPartitioner in favor of FastPartitioner because otherwise single-line comments
			// ended with '\' will not be processed correctly when editing from below the original comment.
			final IDocumentPartitioner partitioner							= new RuleBasedPartitioner(new DMPartitionScanner(), toBeProcessed);
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}

		return document;
	}
}