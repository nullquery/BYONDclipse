package com.byonddev.byondclipse.project.builder;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;

import com.byonddev.byondclipse.dm.scanner.DMCodeModel;

public class DMNature implements IProjectNature
{
	/**
	 * ID of this project nature
	 */
	public static final String NATURE_ID										= "byondclipse-project.dmNature";

	private IProject project;
	private final DMCodeModel codeModel											= new DMCodeModel();
	private boolean skipNextBuild												= false;
	private Process dmProcess;
	private WorkspaceJob dmWorkspaceJob;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	@Override public void configure() throws CoreException
	{
		final IProjectDescription desc											= this.project.getDescription();
		final ICommand[] commands												= desc.getBuildSpec();

		for (int i = 0; i < commands.length; ++i)
		{
			if (commands[i].getBuilderName().equals(DMBuilder.BUILDER_ID))		{ return; }
		}

		final ICommand[] newCommands											= new ICommand[commands.length + 1];
		System.arraycopy(commands, 0, newCommands, 0, commands.length);

		final ICommand command													= desc.newCommand();
		command.setBuilderName(DMBuilder.BUILDER_ID);

		newCommands[newCommands.length - 1] = command;
		desc.setBuildSpec(newCommands);

		this.project.setDescription(desc, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	@Override public void deconfigure() throws CoreException
	{
		final IProjectDescription description									= this.getProject().getDescription();
		final ICommand[] commands												= description.getBuildSpec();

		for (int i = 0; i < commands.length; ++i)
		{
			if (commands[i].getBuilderName().equals(DMBuilder.BUILDER_ID))
			{
				final ICommand[] newCommands									= new ICommand[commands.length - 1];

				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);

				description.setBuildSpec(newCommands);
				this.project.setDescription(description, null);

				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	@Override public IProject getProject()										{ return this.project; }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	@Override public void setProject(final IProject project)					{ this.project = project; }

	public DMCodeModel getCodeModel()											{ return this.codeModel; }

	public Process getDmProcess()												{ return this.dmProcess; }
	public void setDmProcess(final Process dmProcess)							{ this.dmProcess = dmProcess; }

	public WorkspaceJob getDmWorkspaceJob()										{ return this.dmWorkspaceJob; }
	public void setDmWorkspaceJob(final WorkspaceJob dmWorkspaceJob)			{ this.dmWorkspaceJob = dmWorkspaceJob; }

	public boolean isSkipNextBuild()											{ return this.skipNextBuild; }
	public void setSkipNextBuild(final boolean skipNextBuild)					{ this.skipNextBuild = skipNextBuild; }
}