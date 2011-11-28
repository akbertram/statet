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

import de.walware.rj.data.REnvironment;
import de.walware.rj.data.RObject;
import r.lang.Environment;


/**
 * 
 */
public class RenjinEnvironment extends AbstractRenjinObject implements REnvironment {

	private Environment environment;

	/**
	 * @param exp
	 */
	public RenjinEnvironment(Environment environment) {
		super(environment);
		this.environment = environment;
	}


	public RObject get(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public RObject[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public byte getRObjectType() {
		return RObject.TYPE_ENV;
	}

	public int getSpecialType() {
		return 0;
	}

	public String getEnvironmentName() {
		return environment.getName();
	}

	public long getHandle() {
		return System.identityHashCode(environment); 
	}
}
