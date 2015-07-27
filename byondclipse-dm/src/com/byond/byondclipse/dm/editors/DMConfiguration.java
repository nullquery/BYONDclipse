package com.byond.byondclipse.dm.editors;

import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Shell;

import com.byond.byondclipse.dm.contentassist.DMContentAssistProcessor;

@SuppressWarnings({ "deprecation", "restriction" })
public class DMConfiguration extends SourceViewerConfiguration
{
	private DMDoubleClickStrategy doubleClickStrategy;
	private DMScanner scanner;
	private DMStringScanner stringScanner;
	private final ColorManager colorManager;

	public DMConfiguration(final ColorManager colorManager)										{ this.colorManager = colorManager; }

	@Override public String[] getConfiguredContentTypes(final ISourceViewer sourceViewer)
	{
		return new String[] { IDocument.DEFAULT_CONTENT_TYPE, DMPartitionScanner.DM_COMMENT };
	}

	@Override public ITextDoubleClickStrategy getDoubleClickStrategy(final ISourceViewer sourceViewer, final String contentType)
	{
		if (this.doubleClickStrategy == null)													{ this.doubleClickStrategy = new DMDoubleClickStrategy(); }

		return this.doubleClickStrategy;
	}

	protected DMScanner getDMScanner()
	{
		if (this.scanner == null)
		{
			this.scanner																		= new DMScanner(this.colorManager);
			this.scanner.setDefaultReturnToken(new Token(new TextAttribute(this.colorManager.getColor(IDMColorConstants.DEFAULT))));
		}

		return this.scanner;
	}

	protected DMStringScanner getDMStringScanner()
	{
		if (this.stringScanner == null)
		{
			this.stringScanner																	= new DMStringScanner(this.colorManager);
			this.stringScanner.setDefaultReturnToken(new Token(new TextAttribute(this.colorManager.getColor(IDMColorConstants.STRING))));
		}

		return this.stringScanner;
	}

	@Override public IPresentationReconciler getPresentationReconciler(final ISourceViewer sourceViewer)
	{
		final PresentationReconciler reconciler													= new PresentationReconciler();

		DefaultDamagerRepairer dr;

		dr																						= new RuleBasedDamagerRepairer(this.getDMScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr																						= new RuleBasedDamagerRepairer(this.getDMStringScanner());
		reconciler.setDamager(dr, DMPartitionScanner.DM_STRING);
		reconciler.setRepairer(dr, DMPartitionScanner.DM_STRING);

		NonRuleBasedDamagerRepairer ndr;

		ndr																						= new NonRuleBasedDamagerRepairer(new TextAttribute(this.colorManager.getColor(IDMColorConstants.DM_COMMENT)));
		reconciler.setDamager(ndr, DMPartitionScanner.DM_COMMENT);
		reconciler.setRepairer(ndr, DMPartitionScanner.DM_COMMENT);

		return reconciler;
	}

	@Override public IContentAssistant getContentAssistant(final ISourceViewer sourceViewer)
	{
		// Create content assistant
		final ContentAssistant assistant														= new ContentAssistant();

		assistant.enableAutoActivation(true);
		assistant.setAutoActivationDelay(500);
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(this.getInformationControlCreator(sourceViewer));

		// Create content assistant processor
		final IContentAssistProcessor processor													= new DMContentAssistProcessor();

		// Set this processor for each supported content type
		assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(processor, DMPartitionScanner.DM_STRING);

		// Return the content assistant
		return assistant;
	}

	@Override public IInformationControlCreator getInformationControlCreator(final ISourceViewer sourceViewer)
	{
		return new IInformationControlCreator()
		{
			@Override public IInformationControl createInformationControl(final Shell parent)	{ return new DefaultInformationControl(parent, new HTMLTextPresenter()); }
		};
	}
}