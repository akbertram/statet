/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ltk.AstInfo;
import de.walware.ecommons.ltk.GenericFragmentSourceUnit;
import de.walware.ecommons.ltk.IModelManager;
import de.walware.ecommons.ltk.ISourceUnitModelInfo;
import de.walware.ecommons.ltk.LTK;
import de.walware.ecommons.ltk.SourceContent;
import de.walware.ecommons.ltk.SourceDocumentRunnable;
import de.walware.ecommons.ltk.WorkingContext;
import de.walware.ecommons.ltk.ast.IAstNode;
import de.walware.ecommons.text.ISourceFragment;

import de.walware.statet.r.core.IRCoreAccess;
import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.model.IManagableRUnit;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.IRSourceUnit;
import de.walware.statet.r.core.model.RModel;
import de.walware.statet.r.core.renv.IREnv;
import de.walware.statet.r.core.rsource.ast.RAstInfo;


public class RFragmentSourceUnit extends GenericFragmentSourceUnit implements IRSourceUnit, IManagableRUnit {
	
	
	private RAstInfo fAst;
	private IRModelInfo fModelInfo;
	private final Object fModelLock = new Object();
	
	
	public RFragmentSourceUnit(final String id, final ISourceFragment fragment) {
		super(id, fragment);
	}
	
	
	public WorkingContext getWorkingContext() {
		return LTK.EDITOR_CONTEXT;
	}
	
	public String getModelTypeId() {
		return RModel.TYPE_ID;
	}
	
	
	@Override
	protected void register() {
		super.register();
		RCore.getRModelManager().registerDependentUnit(this);
	}
	
	@Override
	protected void unregister() {
		super.unregister();
		RCore.getRModelManager().deregisterDependentUnit(this);
	}
	
	public void reconcileRModel(final int reconcileLevel, final IProgressMonitor monitor) {
		RCore.getRModelManager().reconcile(this, reconcileLevel, true, monitor);
	}
	
	@Override
	public AstInfo<? extends IAstNode> getAstInfo(final String type, final boolean ensureSync, final IProgressMonitor monitor) {
		if (type == null || type == RModel.TYPE_ID) {
			if (ensureSync) {
				RCore.getRModelManager().reconcile(this, IModelManager.AST, false, monitor);
			}
			return fAst;
		}
		return null;
	}
	
	@Override
	public ISourceUnitModelInfo getModelInfo(final String type, final int syncLevel, final IProgressMonitor monitor) {
		if (type == null || type == RModel.TYPE_ID) {
			if (syncLevel > IModelManager.NONE) {
				RCore.getRModelManager().reconcile(this, syncLevel, false, monitor);
			}
			return fModelInfo;
		}
		return null;
	}
	
	public void syncExec(final SourceDocumentRunnable runnable) throws InvocationTargetException {
		runnable.run();
	}
	
	public IRCoreAccess getRCoreAccess() {
		return RCore.getWorkbenchAccess();
	}
	
	public IREnv getREnv() {
		IREnv rEnv = (IREnv) getFragment().getAdapter(IREnv.class);
		if (rEnv != null) {
			return rEnv;
		}
		return RCore.getREnvManager().getDefault();
	}
	
	
	public Object getModelLockObject() {
		return fModelLock;
	}
	
	public SourceContent getParseContent(final IProgressMonitor monitor) {
		return getContent(monitor);
	}
	
	public void setRAst(final RAstInfo ast) {
		fAst = ast;
	}
	
	public RAstInfo getCurrentRAst() {
		return fAst;
	}
	
	public void setRModel(final IRModelInfo model) {
		fModelInfo = model;
	}
	
	public IRModelInfo getCurrentRModel() {
		return fModelInfo;
	}
	
}
