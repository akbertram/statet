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

import de.walware.rj.data.defaultImpl.RPromise;

import r.parser.RParser;

import de.walware.rj.data.defaultImpl.RMissing;

import r.lang.Vector;

import de.walware.rj.data.RObject;
import r.lang.AtomicVector;
import r.lang.Environment;
import r.lang.Function;
import r.lang.FunctionCall;
import r.lang.ListVector;
import r.lang.Null;
import r.lang.Promise;
import r.lang.SEXP;
import r.lang.Symbol;
import r.lang.Symbols;


/**
 * 
 */
public class RenjinObjects {
	
	public static RObject wrap(SEXP exp) {
		
		if(exp instanceof Null) {
			return RenjinNull.INSTANCE;
			
		} else if(exp instanceof AtomicVector) {
			if(exp.getAttribute(Symbols.DIM) == Null.INSTANCE) {
				return new RenjinVector((Vector) exp);
			} else {
				return new RenjinArray((Vector)exp);
			}
		} else if(exp.inherits(RObject.CLASSNAME_DATAFRAME)) {
			return new RenjinDataframe((Vector)exp);
		
		} else if(exp instanceof ListVector) {
			return new RenjinList(exp);
			
		} else if(exp instanceof Environment) {
			return new RenjinEnvironment((Environment) exp);
		
//		/**
//		 * Constant indicating an S4 object. An R object is of this type if the R
//		 * command <code>isS4</code> returns true. This is criterion has priority
//		 * above the criteria for the other data types. If an S4 object represents 
//		 * also a simple data type, this data is accessible by its data slot.
//		 * <p>
//		 * The object is an instance of {@link RS4Object}.</p>
//		 */
//		byte TYPE_S4OBJECT =        0x0a;

		} else if(exp instanceof FunctionCall) {
			return new RenjinFunctionCall(exp);
			
		} else if(exp instanceof Function) {
			return new RenjinFunction(exp);
//		/**
//		 * Constant indicating a reference to a R object.
//		 * <p>
//		 * The object is an instance of {@link RReference}.</p>
//		 */
//		byte TYPE_REFERENCE =       0x0e;

		} else if(exp == Symbol.MISSING_ARG) {
			return RMissing.INSTANCE;
			
		} else if(exp instanceof Promise) {
			return RPromise.INSTANCE;
		} else {
			return new RenjinOther(exp);
		}
	}

}
