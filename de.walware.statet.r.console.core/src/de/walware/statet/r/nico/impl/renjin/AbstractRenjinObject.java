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

import de.walware.rj.data.RCharacterStore;

import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;
import r.lang.AtomicVector;
import r.lang.SEXP;
import r.lang.StringVector;
import r.lang.Symbols;
import r.lang.Vector;


/**
 * 
 */
public abstract class AbstractRenjinObject implements RObject {
	
	protected final SEXP exp;
	
	public AbstractRenjinObject(SEXP exp) {
		this.exp = exp;
	}

	public String getRClassName() {
		Vector clz = (Vector)exp.getAttribute(Symbols.CLASS);
		if(clz.length() > 0) {
			return clz.getElementAsString(0);
		}
		return null;
	}

	public int getLength() {
		return exp.length();
	}

	public RStore getData() {
		return null;
	}

	public String getName(int idx) {
		return exp.getName(idx);
	}
	
	public RObject get(int idx) {
		return RenjinObjects.wrap(exp.getElementAsSEXP(idx));
	}

	public RList getAttributes() {
		return new RenjinList(exp.getAttributes());
	}
	
	public RCharacterStore getNames() {
		AtomicVector names = exp.getNames();
		if(names.length() == 0) {
			return new RCharacterDataImpl(0);
		} else {
			return new RCharacterDataImpl( ((StringVector)names).toArray() );
		}
	}
}
