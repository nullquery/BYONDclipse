package com.byond.byondclipse.project.builder;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class AddRemoveDMNatureHandler extends AbstractHandler
{
	@Override public Object execute(final ExecutionEvent event) throws ExecutionException
	{
		final ISelection selection								= HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection)
		{
			Object element;
			IProject project;

			for (final Iterator<?> it = ((IStructuredSelection) selection).iterator(); it.hasNext();)
			{
				element											= it.next();
				project											= null;

				if (element instanceof IProject)				{ project = (IProject) element; }
				else if (element instanceof IAdaptable)			{ project = (IProject) ((IAdaptable) element).getAdapter(IProject.class); }

				if (project != null)
				{
					try											{ this.toggleNature(project); }
					catch (final CoreException e)				{ throw new ExecutionException("Failed to toggle nature", e); }
				}
			}
		}

		return null;
	}

	/**
	 * Toggles sample nature on a project
	 *
	 * @param project
	 *            to have sample nature added or removed
	 */
	private void toggleNature(final IProject project) throws CoreException
	{
		final IProjectDescription description					= project.getDescription();
		final String[] natures									= description.getNatureIds();

		for (int i = 0; i < natures.length; ++i)
		{
			if (DMNature.NATURE_ID.equals(natures[i]))
			{
				// Remove the nature
				final String[] newNatures						= new String[natures.length - 1];

				System.arraycopy(natures, 0, newNatures, 0, i);
				System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);

				description.setNatureIds(newNatures);

				project.setDescription(description, null);

				return;
			}
		}

		// Add the nature
		final String[] newNatures								= new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);

		newNatures[natures.length]								= DMNature.NATURE_ID;

		description.setNatureIds(newNatures);
		project.setDescription(description, null);
	}
}