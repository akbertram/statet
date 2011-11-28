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

import de.walware.rj.data.RList;
import de.walware.rj.data.RObject;
import r.lang.SEXP;



/**
 * 
 */
public class RenjinList extends AbstractRenjinObject implements RList {

	/**
	 * @param exp
	 */
	public RenjinList(SEXP exp) {
		super(exp);
	}

	public String getName(int idx) {
		return exp.getName(idx);
	}


	public RObject get(String name) {
		return RenjinObjects.wrap(
				exp.getElementAsSEXP(
						exp.getIndexByName(name)));
	}

	public RObject[] toArray() {
		throw new UnsupportedOperationException();
	}

	public byte getRObjectType() {
		return TYPE_LIST;
	}
}
