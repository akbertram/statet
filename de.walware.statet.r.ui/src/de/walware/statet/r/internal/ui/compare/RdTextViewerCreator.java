/*******************************************************************************
 * Copyright (c) 2008-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.r.internal.ui.compare;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;

import de.walware.ecommons.ltk.ui.compare.CompareTextViewer;
import de.walware.ecommons.ui.SharedUIResources;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.internal.ui.RUIPlugin;
import de.walware.statet.r.ui.editors.RdSourceViewerConfiguration;
import de.walware.statet.r.ui.editors.RdSourceViewerConfigurator;


public class RdTextViewerCreator implements IViewerCreator {
	
	public Viewer createViewer(final Composite parent, final CompareConfiguration config) {
		final RdSourceViewerConfigurator viewerConfigurator = new RdSourceViewerConfigurator(
				RCore.getWorkbenchAccess());
		viewerConfigurator.setConfiguration(new RdSourceViewerConfiguration(
				viewerConfigurator, RUIPlugin.getDefault().getEditorPreferenceStore(), SharedUIResources.getColors()));
		return new CompareTextViewer(parent, config, viewerConfigurator);
	}
	
}
