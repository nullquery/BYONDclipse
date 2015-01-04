package com.byonddev.byondclipse.dm.contentassist;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ContextInformationValidator;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;

import com.byonddev.byondclipse.core.BEConstants;
import com.byonddev.byondclipse.core.BEO;
import com.byonddev.byondclipse.dm.editors.DMPartitionScanner;
import com.byonddev.byondclipse.dm.scanner.DMCodeModel;
import com.byonddev.byondclipse.dm.scanner.DMEntityModel;
import com.byonddev.byondclipse.dm.scanner.DMEntityType;

public class DMContentAssistProcessor implements IContentAssistProcessor, BEConstants
{
	@Override public ICompletionProposal[] computeCompletionProposals(final ITextViewer viewer, final int documentOffset)
	{
		// Retrieve current document
		final IDocument doc																									= viewer.getDocument();

		final List<ICompletionProposal> propList																			= new Vector<>();

		boolean inString																									= false;

		try																													{ inString = (doc.getContentType(documentOffset - 1).equals(DMPartitionScanner.DM_STRING)) && (doc.getContentType(documentOffset).equals(DMPartitionScanner.DM_STRING)); }
		catch (final BadLocationException e)																				{ /* no problem */ }

		if (inString)
		{
			// Determine if we're in a code block within the string ('dark blue' color) or in a string directly ('light blue' color).
			int caretPos																									= documentOffset;
			boolean found1																									= false;
			boolean found2																									= false;
			char c;

			while (true)
			{
				try
				{
					c																										= doc.getChar(--caretPos);

					if (found1)
					{
						if (c == '\\')																						{ found1 = false; }
						else																								{ break; }
					}
					else if (found2)
					{
						if (c == '\\')																						{ found2 = false; }
						else																								{ found1 = false; break; }
					}
					else
					{
						if (c == '[')																						{ found1 = true; }
						else if (c == ']')																					{ found2 = true; }
					}
				}
				catch (final BadLocationException e)																		{ break; }
			}

			inString																										= !found1;
		}

		// No proposals in strings.
		if (!inString)
		{
			final ProposalInfoModel pim																						= new ProposalInfoModel(doc, documentOffset);

			this.computeStructureProposals(pim, documentOffset, propList);
		}

		return propList.toArray(new ICompletionProposal[]{});
	}

	private CompletionProposal newCompletionProposal(final String proposal, final int documentOffset, final int qlen, final String imageID, final int offset)
	{
		return this.newCompletionProposal(proposal, proposal, documentOffset, qlen, imageID, offset);
	}

	private CompletionProposal newCompletionProposal(final String proposal, final String displayName, final int documentOffset, final int qlen, final String imageID, final int offset)
	{
		return this.newCompletionProposal(proposal, displayName, documentOffset, qlen, imageID, offset, "");
	}

	private CompletionProposal newCompletionProposal(final String proposal, final String displayName, final int documentOffset, final int qlen, final String imageID, final int offset, final String description)
	{
		final Image image																									= JavaUI.getSharedImages().getImage(imageID);
		final IContextInformation contextInformation																		= new ContextInformation(proposal, description);

		return new CompletionProposal(proposal, documentOffset - qlen, qlen, proposal.length() - (-offset), image, displayName, contextInformation, description);
	}

	private void computeStructureProposals(final ProposalInfoModel pim, final int documentOffset, final List<ICompletionProposal> propList)
	{
		final int qlen																										= pim.getPrefix().length();

		final DMCodeModel dcm																								= new DMCodeModel();

		dcm.scanAll();

		if (pim.getPosition() == ProposalPosition.ROOT || pim.getPosition() == ProposalPosition.TREE)
		{
			propList.add(this.newCompletionProposal("proc", documentOffset, qlen, ISharedImages.IMG_FIELD_DEFAULT, 0));
			propList.add(this.newCompletionProposal("verb", documentOffset, qlen, ISharedImages.IMG_FIELD_DEFAULT, 0));
			propList.add(this.newCompletionProposal("const", documentOffset, qlen, ISharedImages.IMG_FIELD_DEFAULT, 0));
			propList.add(this.newCompletionProposal("static", documentOffset, qlen, ISharedImages.IMG_FIELD_DEFAULT, 0));
		}

		if (pim.getPosition() == ProposalPosition.ROOT || pim.getPosition() == ProposalPosition.TREE || pim.getPosition() == ProposalPosition.BODY)
		{
			for (final DMEntityModel dem : dcm.getEntitiesByPath("/var"))
			{
				propList.add(this.newCompletionProposal(dem.getName(), dem.getDisplayName(), documentOffset, qlen, (dem.getType() == DMEntityType.VAR ? ISharedImages.IMG_FIELD_DEFAULT : ISharedImages.IMG_OBJS_DEFAULT), dem.getType() == DMEntityType.VAR ? 0 : -1, dem.getInfoText()));
			}
		}

		if (pim.getPosition() == ProposalPosition.BODY)
		{
			for (final DMEntityModel dem : dcm.getEntitiesByPath("/proc"))
			{
				propList.add(this.newCompletionProposal(dem.getName(), dem.getDisplayName(), documentOffset, qlen, (dem.getType() == DMEntityType.VAR ? ISharedImages.IMG_FIELD_DEFAULT : ISharedImages.IMG_OBJS_DEFAULT), dem.getType() == DMEntityType.VAR ? 0 : -1, dem.getInfoText()));
			}

			final StringBuilder sb																							= new StringBuilder();

			final List<DMEntityModel> deml																					= new Vector<>();

			for (final String pathComponent : pim.getPath().split("/"))
			{
				sb.append(pathComponent + "/");

				deml.clear();

				deml.addAll(dcm.getEntitiesByPath(sb.toString() + "proc/"));
				deml.addAll(dcm.getEntitiesByPath(sb.toString() + "var/"));

				if (sb.toString().equals("/obj/") || sb.toString().equals("/mob/"))
				{
					deml.addAll(dcm.getEntitiesByPath("/atom/movable/proc/"));
					deml.addAll(dcm.getEntitiesByPath("/atom/movable/var/"));
					deml.addAll(dcm.getEntitiesByPath("/atom/proc/"));
					deml.addAll(dcm.getEntitiesByPath("/atom/var/"));
				}

//				Collections.sort(deml);

				Collections.reverse(deml);

				for (final DMEntityModel dem : deml)
				{
					propList.add(0, this.newCompletionProposal("src." + dem.getName(), dem.getDisplayName(), documentOffset, qlen, (dem.getType() == DMEntityType.VAR ? ISharedImages.IMG_FIELD_DEFAULT : ISharedImages.IMG_OBJS_DEFAULT), dem.getType() == DMEntityType.VAR ? 0 : -1, dem.getInfoText()));
				}
			}

			if (!pim.getFunctionName().isEmpty() && pim.getFunctionName().contains("(") && pim.getFunctionName().contains(")"))
			{
				String tmp;

				final String functionName																					= pim.getFunctionName().substring(pim.getFunctionName().indexOf("(") + 1, pim.getFunctionName().lastIndexOf(")"));

				int pos																										= -1;
				boolean skip																								= false;
				boolean allowWhitespace																						= true;
				char c;

				sb.delete(0, sb.length());

				while (true)
				{
					try
					{
						c																									= functionName.charAt(++pos);

						if (c == ',')
						{
							skip																							= false;
							allowWhitespace																					= true;

							// Keep in sync with exception below
							tmp																								= sb.toString().trim();

							if (tmp.contains("/"))																			{ tmp = tmp.substring(tmp.lastIndexOf("/") + 1); }

							if (!tmp.isEmpty())																				{ propList.add(0, this.newCompletionProposal(tmp, documentOffset, qlen, ISharedImages.IMG_OBJS_LOCAL_VARIABLE, 0)); }
							// Keep in sync with exception below

							sb.delete(0, sb.length());
						}
						else if ((!allowWhitespace && (c == ' ' || c == '\t')) || c == '=')									{ skip = true; }
						else
						{
							allowWhitespace																					= false;

							if (!skip)																						{ sb.append(c); }
						}
					}
					catch (final IndexOutOfBoundsException e)
					{
						tmp																									= sb.toString().trim();

						if (tmp.contains("/"))																				{ tmp = tmp.substring(tmp.lastIndexOf("/") + 1); }

						if (!tmp.isEmpty())																					{ propList.add(0, this.newCompletionProposal(tmp, documentOffset, qlen, ISharedImages.IMG_OBJS_LOCAL_VARIABLE, 0)); }

						break;
					}
				}
			}

			// plop

			// Append local variables.
			if (!pim.getFunctionBody().isEmpty())
			{
				final Set<String> localVariables																			= BEO.getVariablesFromBody(pim.getFunctionBody());

				for (final String v : localVariables)																		{ propList.add(0, this.newCompletionProposal(v, documentOffset, qlen, ISharedImages.IMG_OBJS_LOCAL_VARIABLE, 0)); }
			}
		}

		if (!pim.getPrefix().isEmpty())
		{
			final Iterator<ICompletionProposal> it																			= propList.iterator();
			ICompletionProposal proposal;

			while (it.hasNext())
			{
				proposal																									= it.next();

				if (!proposal.getDisplayString().startsWith(pim.getPrefix()))												{ it.remove(); }
			}
		}
	}

	@Override public IContextInformation[] computeContextInformation(final ITextViewer viewer, final int documentOffset)	{ return new ContextInformation[0]; }
	@Override public char[] getCompletionProposalAutoActivationCharacters()													{ return null; }
	@Override public char[] getContextInformationAutoActivationCharacters()													{ return null; }
	@Override public IContextInformationValidator getContextInformationValidator()											{ return new ContextInformationValidator(this); }
	@Override public String getErrorMessage()																				{ return null; }
}