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

import de.walware.rj.data.RArray;
import de.walware.rj.data.RCharacterStore;
import de.walware.rj.data.RIntegerStore;
import de.walware.rj.data.RStore;
import de.walware.rj.data.defaultImpl.RCharacterDataImpl;
import de.walware.rj.data.defaultImpl.RIntegerDataImpl;
import r.lang.IntVector;
import r.lang.StringVector;
import r.lang.Symbols;
import r.lang.Vector;


/**
 * 
 */
public class RenjinArray extends RenjinVector implements RArray<RStore> {

	/**
	 * @param vector
	 */
	public RenjinArray(Vector vector) {
		super(vector);
	}

	public RIntegerStore getDim() {
		return new RIntegerDataImpl( ((IntVector)exp.getAttribute(Symbols.DIM)).toIntArray());
	}

	public RCharacterStore getDimNames() {
		return null;
	}

	public RStore getNames(int dim) {
		Vector dimNames = (Vector) exp.getAttribute(Symbols.DIMNAMES);
		if(dimNames.length() > 0) {
			StringVector names = (StringVector)dimNames.getElementAsSEXP(dim);
			return new RCharacterDataImpl( names.toArray() );
		} else {
			return null;
		}
	}

	@Override
	public byte getRObjectType() {
		return TYPE_ARRAY;
	}
}
