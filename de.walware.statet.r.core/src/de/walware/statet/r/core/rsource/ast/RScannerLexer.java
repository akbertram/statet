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

import org.eclipse.core.runtime.IStatus;

import de.walware.eclipsecommons.ltk.text.SourceParseInput;

import de.walware.statet.r.core.rlang.RTerminal;
import de.walware.statet.r.core.rsource.RLexer;



class RScannerLexer extends RLexer {
	
	
	final class ScannerToken {
		RTerminal type;
		int offset;
		int length;
		String text;
		IStatus status;
	}
	
	
	protected final ScannerToken fNextToken;
	
	
	public RScannerLexer(SourceParseInput input) {
		super(input);
		fNextToken = new ScannerToken();
	}
	
	public ScannerToken getToken() {
		return fNextToken;
	}
	
	public void nextToken() {
		do {
			searchNext();
		} while (fNextToken.type == null);
	}

	
	@Override
	protected void createFix(RTerminal type) {
		fNextToken.type = type;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = null;
		fNextToken.status = STATUS_OK;
	}
	
	@Override
	protected void createSpecialToken(IStatus status) {
		fNextToken.type = RTerminal.SPECIAL;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = null;
		fNextToken.status = STATUS_OK;
	}
	
	@Override
	protected void createSymbolToken() {
		fNextToken.type = RTerminal.SYMBOL;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = null;
		fNextToken.status = STATUS_OK;
	}
	
	@Override
	protected void createStringToken(RTerminal type, IStatus status) {
		fNextToken.type = type;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = null;
		fNextToken.status = status;
	}

	@Override
	protected void createNumberToken(RTerminal type, IStatus status) {
		fNextToken.type = type;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = null;
		fNextToken.status = status;
	}
	
	@Override
	protected void createWhitespaceToken() {
		fNextToken.type = null;
	}
	
	@Override
	protected void createCommentToken() {
		fNextToken.type = RTerminal.COMMENT;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = null;
		fNextToken.status = STATUS_OK;
	}

	@Override
	protected void createLinebreakToken(String text) {
		fNextToken.type = RTerminal.LINEBREAK;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = text;
		fNextToken.status = STATUS_OK;
	}
	
	@Override
	protected void createUnknownToken(String text) {
		fNextToken.type = RTerminal.UNKNOWN;
		fNextToken.offset = fNextIndex;
		fNextToken.length = fNextNum;
		fNextToken.text = text;
		fNextToken.status = STATUS_OK;
	}
		
}