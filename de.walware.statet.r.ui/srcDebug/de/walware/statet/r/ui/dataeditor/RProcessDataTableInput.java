/*******************************************************************************
 * Copyright (c) 2010-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.ui.dataeditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

import de.walware.ecommons.FastList;
import de.walware.ecommons.ts.ITool;
import de.walware.ecommons.ts.IToolRunnable;

import de.walware.statet.nico.core.IToolLifeListener;
import de.walware.statet.nico.core.NicoCore;
import de.walware.statet.nico.core.runtime.ToolProcess;

import de.walware.statet.r.core.model.RElementName;


public class RProcessDataTableInput implements IRDataTableInput {
	
	
	private final RElementName fElementName;
	
	private final String fFullName;
	private final String fLastName;
	
	private final ToolProcess fProcess;
	private IToolLifeListener fProcessListener;
	
	private final FastList<IRDataTableInput.StateListener> fListeners = new FastList<IRDataTableInput.StateListener>(IRDataTableInput.StateListener.class);
	
	
	public RProcessDataTableInput(RElementName name, final ToolProcess process) {
		if (process == null) {
			throw new NullPointerException("process");
		}
		if (name == null) {
			throw new NullPointerException("name");
		}
		
		fElementName = name;
		fFullName = RElementName.createDisplayName(name, RElementName.DISPLAY_NS_PREFIX | RElementName.DISPLAY_EXACT);
		
		while (name.getNextSegment() != null) {
			name = name.getNextSegment();
		}
		fLastName = name.getDisplayName();
		
		fProcess = process;
	}
	
	
	public RElementName getElementName() {
		return fElementName;
	}
	
	public String getFullName() {
		return fFullName;
	}
	
	public String getLastName() {
		return fLastName;
	}
	
	public ToolProcess getProcess() {
		return fProcess;
	}
	
	public void run(final IToolRunnable runnable) throws CoreException {
		final IStatus status = fProcess.getQueue().add(runnable);
		if (status.getSeverity() >= IStatus.ERROR) {
			throw new CoreException(status);
		}
	}
	
	
	public boolean isAvailable() {
		return !fProcess.isTerminated();
	}
	
	public void addStateListener(final StateListener listener) {
		synchronized (fListeners) {
			fListeners.add(listener);
			if (fListeners.size() > 0 && fProcessListener == null) {
				fProcessListener = new IToolLifeListener() {
					public void toolStarted(final ToolProcess process) {
					}
					public void toolTerminated(final ToolProcess process) {
						if (fProcess == process) {
							final IRDataTableInput.StateListener[] listeners;
							synchronized (fListeners) {
								if (fProcessListener != null) {
									NicoCore.removeToolLifeListener(fProcessListener);
									fProcessListener = null;
								}
								listeners = fListeners.toArray();
							}
							for (final IRDataTableInput.StateListener listener : listeners) {
								listener.tableUnavailable();
							}
						}
					}
				};
				NicoCore.addToolLifeListener(fProcessListener);
				if (fProcess.isTerminated()) {
					NicoCore.removeToolLifeListener(fProcessListener);
					fProcessListener = null;
				}
			}
		}
	}
	
	public void removeStateListener(final StateListener listener) {
		synchronized (fListeners) {
			fListeners.remove(listener);
			if (fListeners.size() == 0 && fProcessListener != null) {
				NicoCore.removeToolLifeListener(fProcessListener);
				fProcessListener = null;
			}
		}
	}
	
	
	@Override
	public int hashCode() {
		return fLastName.hashCode();
	}
	
	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof RProcessDataTableInput)) {
			return false;
		}
		final RProcessDataTableInput other = (RProcessDataTableInput) obj;
		return (this == other || (
				fProcess.equals(other.fProcess)
				&& fFullName.equals(other.fFullName) ));
	}
	
	@Override
	public String toString() {
		return getClass().getName() + "(" + fFullName //$NON-NLS-1$
				+ " in " + fProcess.getLabel(ITool.LONG_LABEL) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	
}
