package com.byond.byondclipse.project.builder;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;

import com.byond.byondclipse.core.BEConfiguration;
import com.byond.byondclipse.core.BEConstants;
import com.byond.byondclipse.core.BEO;

public class DMBuilder extends IncrementalProjectBuilder implements BEConstants
{
	class SampleResourceVisitor implements IResourceVisitor
	{
		private final StringBuilder dme									= new StringBuilder();
		private final Set<String> paths									= new TreeSet<String>();

		public SampleResourceVisitor()									{ this.dme.append("// BEGIN_INTERNALS\r\n// END_INTERNALS\r\n// BEGIN_FILE_DIR\r\n#define FILE_DIR .\r\n// END_FILE_DIR\r\n// BEGIN_PREFERENCES\r\n// END_PREFERENCES\r\n// BEGIN_INCLUDE\r\n"); }

		@Override public boolean visit(final IResource resource)
		{
			if (resource.getName().endsWith(".dm") || resource.getName().endsWith(".dmf") || resource.getName().endsWith(".dmm") || resource.getName().endsWith(".dms"))
			{
				final String path = resource.getLocation().makeRelativeTo(DMBuilder.this.getProject().getLocation()).toString();

				if (!path.contains("/"))
				{
					this.paths.add(path);
				}
			}

			// return true to continue visiting children.
			return true;
		}

		public void finish()
		{
			for (final String path : this.paths)
			{
				this.dme.append("#include \"" + path + "\"\r\n");
			}

			this.dme.append("// END_INCLUDE");
		}

		public StringBuilder getDME()									{ return this.dme; }
	}

	public static final String BUILDER_ID								= "byondclipse-project.dmBuilder";
	private static final String MARKER_TYPE								= "byondclipse-project.dmProblem";

	private void addMarker(final IFile file, final String message, int lineNumber, final int severity)
	{
		try
		{
			final IMarker marker										= file.createMarker(MARKER_TYPE);

			marker.setAttribute(IMarker.MESSAGE, message);
			marker.setAttribute(IMarker.SEVERITY, severity);

			if (lineNumber == -1)										{ lineNumber = 1; }

			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		}
		catch (final CoreException e)									{ /* no problem */ }
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("rawtypes")
	@Override protected IProject[] build(final int kind, final Map args, final IProgressMonitor monitor) throws CoreException
	{
		final DMNature dmNature											= (DMNature) this.getProject().getNature(DMNature.NATURE_ID);

		if (!dmNature.isSkipNextBuild())
		{
			if (dmNature.getDmWorkspaceJob() != null)
			{
				if (dmNature.getDmProcess() == null)					{ return null; }
				else
				{
					dmNature.getDmWorkspaceJob().cancel();

					try													{ dmNature.getDmWorkspaceJob().join(); }
					catch (final InterruptedException e)				{ /* no problem */ }
				}
			}

			this.doClean(monitor);

			final WorkspaceJob job										= new WorkspaceJob("DM Compiler")
			{
				private boolean cancelled								= false;

				@Override public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException
				{
					try													{ DMBuilder.this.fullBuild(monitor); }
					finally
					{
						dmNature.setDmWorkspaceJob(null);
						dmNature.setSkipNextBuild(true);

//						// This needs to be scheduled to execute 1 second afterward because otherwise
//						// changes in resources triggers another build.
//						new WorkspaceJob("DM Compiler - Abort")
//						{
//							@Override public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException
//							{
//
//								return Status.OK_STATUS;
//							}
//						}.schedule(1000);
					}

					return this.cancelled ? Status.CANCEL_STATUS : Status.OK_STATUS;
				}

				@Override protected void canceling()
				{
					this.cancelled										= true;

					try													{ DMBuilder.this.doClean(null); }
					catch (final CoreException e)						{ /* no problem */ }
				}
			};

			dmNature.setDmWorkspaceJob(job);

			job.schedule();
		}
		else
		{
			dmNature.setSkipNextBuild(false);
		}

		return null;
	}

	@Override protected void clean(final IProgressMonitor monitor) throws CoreException
	{
		this.doClean(monitor);
	}

	private void doClean(final IProgressMonitor monitor) throws CoreException
	{
		final DMNature dmNature											= (DMNature) this.getProject().getNature(DMNature.NATURE_ID);

		if (dmNature.getDmProcess() != null)
		{
			// Forcibly terminate the previous compile action.
			dmNature.getDmProcess().destroy();
			dmNature.setDmProcess(null);
		}

		// delete markers set and files created
		this.getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

	private void fullBuild(final IProgressMonitor monitor) throws CoreException
	{
		IFile file														= this.getProject().getFile(this.getProject().getName() + ".dme");

		if (!file.exists())												{ file.create(new ByteArrayInputStream(new byte[]{}), true, monitor); }

		final SampleResourceVisitor srv									= new SampleResourceVisitor();

		try																{ this.getProject().accept(srv); }
		catch (final CoreException e)									{ /* no problem */ }

		srv.finish();

		file.setContents(new ByteArrayInputStream(BEO.toBytes(srv.getDME().toString())), true, true, monitor);
		file.setDerived(true, monitor);

		try
		{
			final DMNature dmNature										= (DMNature) this.getProject().getNature(DMNature.NATURE_ID);

			synchronized (dmNature)
			{
				final ProcessBuilder pb									= new ProcessBuilder(BEConfiguration.getPathToBYOND() + S + "bin" + S + "dm.exe", file.getLocation().toOSString());

				// Delete .dmb and .rsc files.
				file													= this.getProject().getFile(this.getProject().getName() + ".dmb");

				if (file.exists())										{ file.delete(true, monitor); }

				file													= this.getProject().getFile(this.getProject().getName() + ".rsc");

				if (file.exists())										{ file.delete(true, monitor); }

				dmNature.setDmProcess(pb.start());

				final Process p											= dmNature.getDmProcess();
				final Pattern PATTERN_ERROR								= Pattern.compile("([^:]*):(\\d+):(\\w+):(.*)", Pattern.DOTALL);
				Matcher m;
				String line;
				int severity;
				String tmp;

				try (InputStream in = p.getInputStream())
				{
					try (BufferedReader r = new BufferedReader(new InputStreamReader(in)))
					{
						while ((line = r.readLine()) != null)
						{
							line										= line.trim();

							if (!line.equals("loading " + this.getProject().getName() + ".dme") && line.contains(":"))
							{
								m										= PATTERN_ERROR.matcher(line);

								if (m.find())
								{
									tmp									= m.group(3).trim();

									if (tmp.equals("error"))			{ severity = IMarker.SEVERITY_ERROR; }
									else if (tmp.equals("warning"))		{ severity = IMarker.SEVERITY_WARNING; }
									else								{ severity = IMarker.SEVERITY_INFO; }

									this.addMarker(this.getProject().getFile(m.group(1).replace(S, "/")), m.group(4).trim(), Integer.parseInt(m.group(2)), severity);
								}
							}
						}
					}
				}

				p.waitFor();

				dmNature.setDmProcess(null);

				// Mark .dmb and .rsc files as hidden.
				file													= this.getProject().getFile(this.getProject().getName() + ".dmb");

				file.refreshLocal(IResource.DEPTH_ZERO, monitor);

				if (file.exists())
				{
					file.setHidden(true);
					file.setDerived(true, monitor);
				}

				file													= this.getProject().getFile(this.getProject().getName() + ".rsc");

				file.refreshLocal(IResource.DEPTH_ZERO, monitor);

				if (file.exists())
				{
					file.setHidden(true);
					file.setDerived(true, monitor);
				}

				// Mark .int file as hidden.
				file													= this.getProject().getFile(this.getProject().getName() + ".int");

				file.refreshLocal(IResource.DEPTH_ZERO, monitor);

				if (file.exists())
				{
					file.setHidden(true);
					file.setDerived(true, monitor);
				}
			}
		}
		catch (final OperationCanceledException e)						{ this.doClean(monitor); }
		catch (final Exception e)										{ e.printStackTrace(); }
		finally
		{
			// Refresh the workspace to reflect the changes to the files above.
			final WorkspaceJob job										= new WorkspaceJob("Workspace Refresh")
			{
				@Override public IStatus runInWorkspace(final IProgressMonitor monitor) throws CoreException
				{
					DMBuilder.this.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);

					return Status.OK_STATUS;
				}
			};

			job.schedule();

			try															{ job.join(); }
			catch (final Exception e)									{ /* no problem */ }
		}
	}
}