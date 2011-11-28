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

import r.lang.Null;


/**
 * 
 */
public class RenjinNull extends AbstractRenjinObject {

	public static RenjinNull INSTANCE = new RenjinNull();
	
	private RenjinNull() {
		super(Null.INSTANCE);
	}

	public byte getRObjectType() {
		return TYPE_NULL;
	}
}
