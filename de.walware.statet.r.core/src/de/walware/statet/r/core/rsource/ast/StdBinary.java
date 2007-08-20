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

package de.walware.statet.r.core.rsource.ast;

import de.walware.eclipsecommons.ltk.ast.CommonAstVisitor;
import de.walware.eclipsecommons.ltk.ast.IAstNode;


/**
 *
 */
abstract class StdBinary extends RAstNode {
	
	
	final Expression fLeftExpr = new Expression();
	final Expression fRightExpr = new Expression();


	@Override
	public final boolean hasChildren() {
		return true;
	}

	@Override
	public final int getChildCount() {
		return 2;
	}
	
	@Override
	public final RAstNode getChild(int index) {
		switch (index) {
		case 0:
			return fLeftExpr.node;
		case 1:
			return fRightExpr.node;
		default:
			throw new IndexOutOfBoundsException();
		}
	}

	@Override
	public final RAstNode[] getChildren() {
		return new RAstNode[] { fLeftExpr.node, fRightExpr.node };
	}
	
	public final RAstNode getLeftChild() {
		return fLeftExpr.node;
	}
	
	public final RAstNode getRightChild() {
		return fRightExpr.node;
	}

	@Override
	public final int getChildIndex(IAstNode child) {
		if (fLeftExpr.node == child) {
			return 0;
		}
		if (fRightExpr.node == child) {
			return 1;
		}
		return -1;
	}

	@Override
	public final void acceptInChildren(RAstVisitor visitor) {
		fLeftExpr.node.accept(visitor);
		fRightExpr.node.accept(visitor);
	}
	
	public final void acceptInChildren(CommonAstVisitor visitor) {
		fLeftExpr.node.accept(visitor);
		fRightExpr.node.accept(visitor);
	}

	
	@Override
	final Expression getExpr(RAstNode child) {
		if (fRightExpr.node == child) {
			return fRightExpr;
		}
		if (fLeftExpr.node == child) {
			return fLeftExpr;
		}
		return null;
	}
	
	@Override
	final Expression getLeftExpr() {
		return fLeftExpr;
	}

	@Override
	final Expression getRightExpr() {
		return fRightExpr;
	}

	final void updateStartOffset() {
		fStartOffset = fLeftExpr.node.fStartOffset;
	}
	
	@Override
	final void updateStopOffset() {
		fStopOffset = fRightExpr.node.fStopOffset;
	}

}
