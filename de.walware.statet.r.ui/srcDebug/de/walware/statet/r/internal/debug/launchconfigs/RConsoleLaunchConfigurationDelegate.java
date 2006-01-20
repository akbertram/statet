/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.debug.launchconfigs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.RefreshTab;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsUtil;
import org.eclipse.ui.externaltools.internal.program.launchConfigurations.BackgroundResourceRefresher;

import de.walware.statet.base.IStatetStatusConstants;
import de.walware.statet.r.internal.debug.RLaunchingMessages;
import de.walware.statet.r.ui.RUiPlugin;


public class RConsoleLaunchConfigurationDelegate implements
		ILaunchConfigurationDelegate {

	
	private static IWindowListener windowListener;

	/**
	 * A window listener that warns the user about any running programs when
	 * the workbench closes. Programs are killed when the VM exits.
	 */
	private class ProgramLaunchWindowListener implements IWindowListener {
		
		public void windowActivated(IWorkbenchWindow window) {
		}
		public void windowDeactivated(IWorkbenchWindow window) {
		}
		public void windowOpened(IWorkbenchWindow window) {
		}

		public void windowClosed(IWorkbenchWindow window) {
			
			IWorkbenchWindow windows[] = PlatformUI.getWorkbench().getWorkbenchWindows();
			if (windows.length > 1) {
				// There are more windows still open.
				return;
			}
			ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType programType = manager.getLaunchConfigurationType(IRConsoleConstants.ID_RCMD_LAUNCHCONFIG);
			if (programType == null) {
				return;
			}
			ILaunch launches[] = manager.getLaunches();
			ILaunchConfigurationType configType;
			ILaunchConfiguration config;
			for (int i = 0; i < launches.length; i++) {
				try {
					config = launches[i].getLaunchConfiguration();
					if (config == null) {
						continue;
					}
					configType = config.getType();
				} catch (CoreException e) {
					continue;
				}
				if (configType.equals(programType)) {
					if (!launches[i].isTerminated()) {
						MessageDialog.openWarning(window.getShell(), RLaunchingMessages.RConsoleLaunchDelegate_WorkbenchClosing_title, RLaunchingMessages.RConsoleLaunchDelegate_WorkbenchClosing_message); //$NON-NLS-1$ //$NON-NLS-2$
						break;
					}
				}
			}
		}
	}
	
	
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		try {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask(NLS.bind(RLaunchingMessages.RConsoleLaunchDelegate_Running, configuration.getName()), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
			if (monitor.isCanceled()) {
				return;
			}
		
			// resolve location
			IPath location = ExternalToolsUtil.getLocation(configuration);
			
			if (monitor.isCanceled()) {
				return;
			}
			
			// resolve working directory
			IPath workingDirectory = ExternalToolsUtil.getWorkingDirectory(configuration);
			
			if (monitor.isCanceled()) {
				return;
			}
			
			// resolve arguments
			String[] arguments = ExternalToolsUtil.getArguments(configuration);
			
			if (monitor.isCanceled()) {
				return;
			}
			
			int cmdLineLength = 1;
			if (arguments != null) {
				cmdLineLength += arguments.length;
			}
			String[] cmdLine = new String[cmdLineLength];
			cmdLine[0] = location.toOSString();
			if (arguments != null) {
				System.arraycopy(arguments, 0, cmdLine, 1, arguments.length);
			}
			
			File workingDir = null;
			if (workingDirectory != null) {
				workingDir = workingDirectory.toFile();
			}
			
			if (monitor.isCanceled()) {
				return;
			}
			
			String[] envp = DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);
			
			if (monitor.isCanceled()) {
				return;
			}
			
			if (windowListener == null) {
				windowListener = new ProgramLaunchWindowListener();
				PlatformUI.getWorkbench().addWindowListener(windowListener);
			}
			Process p = DebugPlugin.exec(cmdLine, workingDir, envp);
			IProcess process = null;
			
			// add process type to process attributes
			Map processAttributes = new HashMap();
			String programName = location.lastSegment();
			String extension = location.getFileExtension();
			if (extension != null) {
				programName = programName.substring(0, programName.length() - (extension.length() + 1));
			}
			programName = programName.toLowerCase();
			processAttributes.put(IProcess.ATTR_PROCESS_TYPE, programName);
			
			if (p != null) {
				process = DebugPlugin.newProcess(launch, p, location.toOSString(), processAttributes);
				if (process == null) {
					p.destroy();
					throw new CoreException(new Status(IStatus.ERROR, RUiPlugin.ID, IStatetStatusConstants.LAUNCHING_ERROR, RLaunchingMessages.RConsoleLaunchDelegate_error_ProcessHandle, null)); //$NON-NLS-1$
				}
				
			}
			process.setAttribute(IProcess.ATTR_CMDLINE, generateCommandLine(cmdLine));
			
			if (CommonTab.isLaunchInBackground(configuration)) {
				// refresh resources after process finishes
				if (RefreshTab.getRefreshScope(configuration) != null) {
					BackgroundResourceRefresher refresher = new BackgroundResourceRefresher(configuration, process);
					refresher.startBackgroundRefresh();
				}				
			} else {
				// wait for process to exit
				while (!process.isTerminated()) {
					try {
						if (monitor.isCanceled()) {
							process.terminate();
							break;
						}
						Thread.sleep(50);
					} catch (InterruptedException e) {
					}
				}
				
				// refresh resources
				RefreshTab.refreshResources(configuration, monitor);
			}
		}
		finally {
			monitor.done();
		}
	}
	
	
	private String generateCommandLine(String[] commandLine) {
		if (commandLine.length < 1)
			return ""; //$NON-NLS-1$
		StringBuffer buf= new StringBuffer();
		for (int i= 0; i < commandLine.length; i++) {
			buf.append(' ');
			char[] characters= commandLine[i].toCharArray();
			StringBuffer command= new StringBuffer();
			boolean containsSpace= false;
			for (int j = 0; j < characters.length; j++) {
				char character= characters[j];
				if (character == '\"') {
					command.append('\\');
				} else if (character == ' ') {
					containsSpace = true;
				}
				command.append(character);
			}
			if (containsSpace) {
				buf.append('\"');
				buf.append(command);
				buf.append('\"');
			} else {
				buf.append(command);
			}
		}	
		return buf.toString();
	}	

}
