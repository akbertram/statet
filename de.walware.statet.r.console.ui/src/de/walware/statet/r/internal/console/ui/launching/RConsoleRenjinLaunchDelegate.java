/*******************************************************************************
 * Copyright (c) 2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     alex - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.console.ui.launching;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ui.IWorkbenchPage;

import de.walware.ecommons.debug.ui.LaunchConfigUtil;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.ToolRunner;
import de.walware.statet.nico.ui.NicoUITools;
import de.walware.statet.nico.ui.console.NIConsoleColorAdapter;
import de.walware.statet.nico.ui.util.WorkbenchStatusHandler;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.ui.RConsole;
import de.walware.statet.r.console.ui.launching.RConsoleLaunching;
import de.walware.statet.r.core.renv.IREnvConfiguration;
import de.walware.statet.r.launching.core.RLaunching;
import de.walware.statet.r.launching.ui.REnvTab;
import de.walware.statet.r.nico.impl.RenjinController;


/**
 * 
 */
public class RConsoleRenjinLaunchDelegate {

	public void launch(final ILaunchConfiguration configuration, final String mode,
			final ILaunch launch, final IProgressMonitor monitor)
			throws CoreException {

		final IWorkbenchPage page = UIAccess.getActiveWorkbenchPage(false);
		final SubMonitor progress = SubMonitor.convert(monitor, 15);
		
		final long timestamp = System.currentTimeMillis();
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
		
		// r env
		final IREnvConfiguration renv = RLaunching.getREnvConfig(configuration, true);
//		renv.validate();
		
		// working directory
		final IFileStore workingDirectory = REnvTab.getWorkingDirectory(configuration);
		
		progress.worked(1);
		if (progress.isCanceled()) {
			return;
		}
//		
//		final ProcessBuilder builder = new ProcessBuilder();
//		builder.directory(workingDirectory.toLocalFile(EFS.NONE, null));
//		
//		// environment
//		final Map<String, String> envp = builder.environment();
//		LaunchConfigUtil.configureEnvironment(envp, configuration, renv.getEnvironmentsVariables());
//		
//		final List<String> cmdLine = builder.command();
//		cmdLine.addAll(0, renv.getExecCommand(Exec.TERM));
//		if (Platform.getOS().startsWith("win")) { //$NON-NLS-1$
//			cmdLine.add("--ess"); //$NON-NLS-1$
//		}
//		else {
//			cmdLine.add("--interactive");
//		}
//		if ("2".equals(envp.get("R_NETWORK"))) { //$NON-NLS-1$ //$NON-NLS-2$
//			cmdLine.add("--internet2"); //$NON-NLS-1$
//		}
//		
//		// arguments
//		cmdLine.addAll(Arrays.asList(
//				LaunchConfigUtil.getProcessArguments(configuration, RConsoleLaunching.ATTR_OPTIONS) ));
//		
//		progress.worked(1);
//		if (progress.isCanceled()) {
//			return;
//		}
//		
//		final String encoding = configuration.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING, ""); //$NON-NLS-1$
//		Charset charset;
//		try {
//			if (encoding.length() > 0) {
//				charset = Charset.forName(encoding);
//			}
//			else {
//				charset = Charset.defaultCharset();
//			}
//		} catch (final Exception e) {
//			throw new CoreException(new Status(IStatus.ERROR, RConsoleUIPlugin.PLUGIN_ID,
//					ICommonStatusConstants.LAUNCHCONFIG_ERROR,
//					NLS.bind(RConsoleMessages.LaunchDelegate_error_InvalidUnsupportedConsoleEncoding_message, encoding),
//					e ));
//		}
//		
//		progress.worked(1);
//		if (progress.isCanceled()) {
//			return;
//		}
//		
//		// create process
//		UnterminatedLaunchAlerter.registerLaunchType(RConsoleLaunching.R_CONSOLE_CONFIGURATION_TYPE_ID);
		
		final RProcess process = new RProcess(launch, renv,
				LaunchConfigUtil.createLaunchPrefix(configuration), renv.getName() + " / Renjin " + LaunchConfigUtil.createProcessTimestamp(timestamp), //$NON-NLS-1$
				null,
				workingDirectory.toString(),
				timestamp );
//		process.setAttribute(IProcess.ATTR_CMDLINE, LaunchConfigUtil.generateCommandLine(cmdLine));
//		
		final RenjinController controller = new RenjinController(process,null);
		process.init(controller);
		RConsoleLaunching.registerDefaultHandlerTo(controller);
		
		progress.worked(5);
		
		RConsoleLaunching.scheduleStartupSnippet(controller, configuration);
		
		final RConsole console = new RConsole(process, new NIConsoleColorAdapter());
		NicoUITools.startConsoleLazy(console, page,
				configuration.getAttribute(RConsoleLaunching.ATTR_PIN_CONSOLE, false));
		
		new ToolRunner().runInBackgroundThread(process, new WorkbenchStatusHandler());
		
		if (monitor != null) {
			monitor.done();
		}
	}

}
