/*******************************************************************************
 * Copyright (c) 2009-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.core.sourcemodel;

import java.util.Iterator;
import java.util.List;

import de.walware.ecommons.ltk.IModelElement;
import de.walware.ecommons.ltk.ISourceStructElement;

import de.walware.statet.r.core.model.IRLangSourceElement;
import de.walware.statet.r.core.model.IRModelInfo;
import de.walware.statet.r.core.model.RDocuLink;
import de.walware.statet.r.core.rsource.ast.DocuComment;
import de.walware.statet.r.core.rsource.ast.DocuTag;
import de.walware.statet.r.core.rsource.ast.NodeType;
import de.walware.statet.r.core.rsource.ast.RAstNode;
import de.walware.statet.r.internal.core.sourcemodel.RSourceElementByElementAccess.RClass;
import de.walware.statet.r.internal.core.sourcemodel.RSourceElementByElementAccess.RMethod;
import de.walware.statet.r.internal.core.sourcemodel.RSourceElementByElementAccess.RVariable;


public class RoxygenAnalyzer implements IModelElement.Filter {
	
	
	private IRoxygenAnalyzeContext fContext;
	
	private List<RAstNode> fComments;
	
	private Iterator<RAstNode> fCommentsIterator;
	private DocuComment fNextComment;
	private int fNextCommentRefOffset;
	
	
	public RoxygenAnalyzer() {
	}
	
	
	public void updateModel(IRoxygenAnalyzeContext context) {
		fContext = context;
		IRModelInfo model = context.getModelInfo();
		fComments = model.getAst().root.getComments();
		if (fComments == null || fComments.isEmpty()) {
			return;
		}
		fCommentsIterator = fComments.iterator();
		if (!nextDocuComment()) {
			return;
		}
		final ISourceStructElement sourceElement = model.getSourceElement();
		if (sourceElement instanceof IRLangSourceElement) {
			include(sourceElement);
		}
		if (fNextComment != null) {
			checkElement(null);
		}
	}
	
	private boolean nextDocuComment() {
		while (fCommentsIterator.hasNext()) {
			final RAstNode next = fCommentsIterator.next();
			if (next.getNodeType() == NodeType.DOCU_AGGREGATION) {
				fNextComment = (DocuComment) next;
				fNextCommentRefOffset = fNextComment.getSubsequentNodeOffset();
				if (fNextCommentRefOffset != Integer.MIN_VALUE) {
					return true;
				}
				else {
					checkElement(null);
				}
			}
		}
		fNextComment = null;
		fNextCommentRefOffset = Integer.MAX_VALUE;
		return false;
	}
	
	
	public boolean include(final IModelElement element) {
		final IRLangSourceElement rElement = (IRLangSourceElement) element;
		if (fNextComment == null) {
			return true;
		}
		final int offset = rElement.getSourceRange().getOffset();
		while (fNextCommentRefOffset < offset) {
			checkElement(null);
			nextDocuComment();
		}
		if (fNextCommentRefOffset == offset) {
			if (rElement instanceof RClass) {
				final RClass rClass = (RClass) rElement;
				final RDocuLink link = new RDocuLink(rClass, fNextComment);
				fNextComment.addAttachment(link);
				rClass.fDocu = fNextComment;
				checkElement(rClass);
				nextDocuComment();
			}
			else if (rElement instanceof RMethod) {
				final RMethod rMethod = (RMethod) rElement;
				final RDocuLink link = new RDocuLink(rMethod, fNextComment);
				fNextComment.addAttachment(link);
				rMethod.fDocu = fNextComment;
				checkElement(rMethod);
				nextDocuComment();
			}
			else if (rElement instanceof RVariable) {
				final RVariable rVariable = (RVariable) rElement;
				final RDocuLink link = new RDocuLink(rVariable, fNextComment);
				fNextComment.addAttachment(link);
				rVariable.fDocu = fNextComment;
				checkElement(rVariable);
				nextDocuComment();
			}
		}
		
		if (fNextCommentRefOffset < offset+rElement.getSourceRange().getLength()) {
			return rElement.hasSourceChildren(this);
		}
		return false;
	}
	
	private void checkElement(final IRLangSourceElement element) {
		final List<DocuTag> tags = fNextComment.getTags();
		for (final DocuTag tag : tags) {
			final RoxygenTagType tagType = RoxygenTagType.TYPES.get(tag.getText());
			if (tagType != null) {
				tagType.analyze(fContext, tag, element);
			}
		}
	}
	
}
