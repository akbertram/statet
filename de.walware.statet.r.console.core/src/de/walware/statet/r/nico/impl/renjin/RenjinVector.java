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

import r.lang.StringVector;

import r.lang.AtomicVector;

import de.walware.rj.data.defaultImpl.RCharacterDataImpl;

import r.lang.Vector;

import de.walware.rj.data.RCharacterStore;

import r.lang.SEXP;

import de.walware.rj.data.RStore;

import de.walware.rj.data.RVector;


/**
 * 
 */
public class RenjinVector extends AbstractRenjinObject implements RVector<RStore> {

	private final Vector vector;
	/**
	 * @param exp
	 */
	public RenjinVector(Vector vector) {
		super(vector);
		this.vector = vector;
	}

	public byte getRObjectType() {
		return TYPE_VECTOR;
	}
}
