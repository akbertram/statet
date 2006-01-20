/*******************************************************************************
 * Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU General Public License v2.0
 * or newer, which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.rserve.internal.launchconfigs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;

import de.walware.statet.nico.console.NIConsole;
import de.walware.statet.nico.runtime.ToolProcess;
import de.walware.statet.nico.runtime.ToolRunner;
import de.walware.statet.r.nico.RConsole;
import de.walware.statet.r.rserve.RServeClientController;


public class RServeClientLaunchConfigDelegate implements ILaunchConfigurationDelegate {

	
	public void launch(ILaunchConfiguration configuration, String mode, 
			ILaunch launch, IProgressMonitor monitor) throws CoreException {

		try {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
//			monitor.beginTask("", 1000);
			if (monitor.isCanceled()) {
				return;
			}
			
			String name = configuration.getName() + " [" + configuration.getType().getName() + "]";
			final ConnectionConfig connectionConfig = new ConnectionConfig();
			connectionConfig.readFrom(configuration);
			
			ToolProcess process = new ToolProcess(launch, name);
			process.setController(new RServeClientController(process, connectionConfig));
			final NIConsole console = new RConsole(process);
	    	ConsolePlugin.getDefault().getConsoleManager().addConsoles(
	    			new IConsole[] { console });
	
	    	new ToolRunner().runInBackgroundThread(process);
	
	    	// open console
	    	Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						console.show(true);
					} 
					catch (CoreException e) {
					}
				}
	    	});
		}
		finally {
			monitor.done();
		}
	}
	
}
