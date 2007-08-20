/*******************************************************************************
 * Copyright (c) 2007 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.eclipsecommons.ltk;

import de.walware.eclipsecommons.ltk.ast.IAstNode;



/**
 *
 */
public class AstInfo<NodeT extends IAstNode> {
	
	
	public final int level;
	public final long stamp;
	public NodeT root;
	
	
	public AstInfo(int level, long stamp) {
		this.level = level;
		this.stamp = stamp;
	}
	
}
