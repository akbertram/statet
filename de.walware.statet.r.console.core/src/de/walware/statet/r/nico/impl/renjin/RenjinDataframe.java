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
import de.walware.rj.data.RDataFrame;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RStore;
import r.lang.Vector;


/**
 * 
 */
public class RenjinDataframe extends RenjinVector implements RDataFrame {

	/**
	 * @param exp
	 */
	public RenjinDataframe(Vector exp) {
		super(exp);
	}

	public RObject[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public byte getRObjectType() {
		return TYPE_DATAFRAME;
	}

	public String getName(int idx) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getColumnCount() {
		return exp.length();
	}

	public RCharacterStore getColumnNames() {
		return getNames();
	}

	public RStore getColumn(int idx) {
		// TODO Auto-generated method stub
		return null;
	}

	public RStore getColumn(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public int getRowCount() {
		return exp.getElementAsSEXP(0).length();
	}

	public RStore getRowNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public RObject get(String name) {
		// TODO Auto-generated method stub
		return null;
	}
}
