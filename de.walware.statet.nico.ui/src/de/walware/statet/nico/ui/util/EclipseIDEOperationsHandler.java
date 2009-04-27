/*******************************************************************************
 * Copyright (c) 2009 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.util;

import java.util.Map;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.IToolEventHandler;
import de.walware.statet.nico.core.runtime.IToolRunnableControllerAdapter;
import de.walware.statet.nico.core.runtime.ToolWorkspace;
import de.walware.statet.nico.core.util.ToolEventHandlerUtil;
import de.walware.statet.nico.ui.NicoUI;
import de.walware.statet.nico.ui.views.HistoryView;


/**
 * Handles in Eclipse IDE Platform:
 *  {@link IToolEventHandler#SHOW_HISTORY_ID} - shows the history view
 * 
 */
public class EclipseIDEOperationsHandler implements IToolEventHandler {
	
	
	public int handle(final String id, final IToolRunnableControllerAdapter tools, final Map<String, Object> data, final IProgressMonitor monitor) {
		if (id.equals(IToolEventHandler.SHOW_HISTORY_ID)) {
			final String pattern = ToolEventHandlerUtil.getCheckedData(data, "pattern", String.class, false); //$NON-NLS-1$
			final Display display = UIAccess.getDisplay();
			display.syncExec(new Runnable() {
				public void run() {
					try {
						final IWorkbenchPage page = NicoUI.getToolRegistry().findWorkbenchPage(tools.getProcess());
						final HistoryView view = (HistoryView) page.showView(NicoUI.HISTORY_VIEW_ID);
						if (pattern != null) {
							view.search(pattern, false);
						}
					}
					catch (final PartInitException e) {
						tools.handleStatus(new Status(IStatus.ERROR, NicoUI.PLUGIN_ID, -1,
								"An error occurred when trying open/activate the History view.", e), monitor);
					}
				}
			});
			return OK;
		}
		if (id.equals(IToolEventHandler.SHOW_FILE_ID)) {
			final IFileStore fileStore;
			final String filename = ToolEventHandlerUtil.getCheckedData(data, "filename", String.class, true); //$NON-NLS-1$
			final ToolWorkspace workspaceData = tools.getWorkspaceData();
			try {
				fileStore = workspaceData.toFileStore(filename);
			}
			catch (final CoreException e) {
				tools.handleStatus(new Status(IStatus.ERROR, NicoUI.PLUGIN_ID, -1, "Could not resolve", e), monitor);
				return ERROR;
			}
			final Display display = UIAccess.getDisplay();
			display.syncExec(new Runnable() {
				public void run() {
					final IWorkbenchPage page = NicoUI.getToolRegistry().findWorkbenchPage(tools.getProcess());
					try {
						IDE.openEditorOnFileStore(page, fileStore);
					}
					catch (final PartInitException e) {
						tools.handleStatus(new Status(IStatus.ERROR, NicoUI.PLUGIN_ID, -1,
								"An error occurred when trying open/activate the Editor.", e), monitor);
					}
				}
			});
			return OK;
		}
		throw new UnsupportedOperationException();
	}
	
}