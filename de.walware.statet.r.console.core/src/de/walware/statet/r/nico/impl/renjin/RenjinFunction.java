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

package de.walware.statet.r.nico.impl.renjin;

import r.lang.SEXP;

import de.walware.rj.data.RFunction;


/**
 * 
 */
public class RenjinFunction extends AbstractRenjinObject implements RFunction {

	/**
	 * @param exp
	 */
	public RenjinFunction(SEXP exp) {
		super(exp);
	}

	public byte getRObjectType() {
		return TYPE_FUNCTION;
	}

	public String getHeaderSource() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getBodySource() {
		// TODO Auto-generated method stub
		return null;
	}
}
