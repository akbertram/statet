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

package de.walware.statet.r.nico.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.walware.ecommons.ts.IToolRunnable;

import de.walware.statet.nico.core.runtime.SubmitType;

import r.lang.Symbol;

import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.services.FunctionCall;
import de.walware.rj.services.RGraphicCreator;
import de.walware.rj.services.RPlatform;
import r.lang.Context;
import r.lang.ExpressionVector;
import r.lang.SEXP;
import r.parser.RParser;

import de.walware.statet.r.console.core.RProcess;
import de.walware.statet.r.console.core.RWorkspace;
import de.walware.statet.r.nico.AbstractRController;
import de.walware.statet.r.nico.RWorkspaceConfig;
import de.walware.statet.r.nico.impl.renjin.RenjinObjects;


/**
 * 
 */
public class RenjinController extends AbstractRController {

	private Context context;
	
	/**
	 * @param process
	 * @param initData
	 */
	public RenjinController(RProcess process, RWorkspaceConfig workspaceConfig, Map<String, Object> initData) {
		super(process, initData);
				
		
		fWorkspaceData = new RWorkspace(this, null,
			workspaceConfig );
	}

	public RPlatform getPlatform() {
		return new RPlatform(System.getProperty("os.name"), java.io.File.separator, 
				java.io.File.pathSeparator, "Renjin");
	}

	public void evalVoid(String expression, IProgressMonitor monitor)
			throws CoreException {
		
		eval(expression);
		
	}
	
	@Override
	public RWorkspace getWorkspaceData() {
		// TODO Auto-generated method stub
		return super.getWorkspaceData();
	}

	private SEXP eval(String expression) {
		ExpressionVector source = RParser.parseSource(expression + "\n");
		return source.evaluate(context, context.getGlobalEnvironment());
	}

	public RObject evalData(String expression, IProgressMonitor monitor)
			throws CoreException {
		return RenjinObjects.wrap(eval(expression));
	}

	public RObject evalData(String expression, String factoryId, int options,
			int depth, IProgressMonitor monitor) throws CoreException {
		return RenjinObjects.wrap(eval(expression));
	}

	public RObject evalData(RReference reference, IProgressMonitor monitor)
			throws CoreException {
	
		throw new UnsupportedOperationException();
	}

	public RObject evalData(RReference reference, String factoryId,
			int options, int depth, IProgressMonitor monitor)
			throws CoreException {
		throw new UnsupportedOperationException();
	}

	public void assignData(String expression, RObject data,
			IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
		
	}

	public void uploadFile(InputStream in, long length, String fileName,
			int options, IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
		
	}

	public void downloadFile(OutputStream out, String fileName, int options,
			IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		
		throw new UnsupportedOperationException();
	}

	public byte[] downloadFile(String fileName, int options,
			IProgressMonitor monitor) throws CoreException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public FunctionCall createFunctionCall(String name) throws CoreException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public RGraphicCreator createRGraphicCreator(int options)
			throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	protected IToolRunnable createStartRunnable() {
		return new StartRunnable() {
			@Override
			public String getLabel() {
				return "Initializing Renjin Context...";
			}
		};
	}

	@Override
	protected void startToolL(IProgressMonitor monitor) throws CoreException {
		try {
			context = Context.newTopLevelContext();
			context.getGlobals().setStdOut(new PrintWriter(new ToolWriter()));
			context.init();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}		
	}

	@Override
	protected void killTool(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean isToolAlive() {
		return true;
	}

	@Override
	protected void doSubmitL(IProgressMonitor monitor) throws CoreException {
		SEXP result = eval(fCurrentInput);
		if(!context.getGlobals().isInvisible()) {
			r.lang.FunctionCall.newCall(Symbol.get("print"), result).evaluate(context, context.getEnvironment());
		}
	}
	
	private class ToolWriter extends Writer {

		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void flush() throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
			fDefaultOutputStream.append(new String(cbuf, off, cbuf.length-off), SubmitType.CONSOLE, 0);
		}
		
	}
}
