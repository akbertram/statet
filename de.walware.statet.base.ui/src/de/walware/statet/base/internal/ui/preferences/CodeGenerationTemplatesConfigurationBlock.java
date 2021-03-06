/*******************************************************************************
 * Copyright (c) 2005-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.base.internal.ui.preferences;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateReaderWriter;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.statushandlers.StatusManager;

import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.sourceediting.ViewerSourceEditorAdapter;
import de.walware.ecommons.ltk.ui.templates.EditTemplateDialog;
import de.walware.ecommons.preferences.ui.ConfigurationBlock;
import de.walware.ecommons.preferences.ui.SettingsUpdater;
import de.walware.ecommons.templates.TemplateVariableProcessor;
import de.walware.ecommons.text.ui.TextViewerEditorColorUpdater;
import de.walware.ecommons.text.ui.TextViewerJFaceUpdater;
import de.walware.ecommons.ui.ISettingsChangedHandler;
import de.walware.ecommons.ui.dialogs.groups.CategorizedOptionButtonsGroup;
import de.walware.ecommons.ui.dialogs.groups.CategorizedOptionsGroup.CategorizedItem;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.ext.templates.ICodeGenerationTemplatesCategory;

import de.walware.statet.base.internal.ui.StatetUIPlugin;


/**
 * The page to configure the codegenerator templates.
 */
public class CodeGenerationTemplatesConfigurationBlock extends ConfigurationBlock {
	
	
	public static final String EXTENSION_POINT = "codeGenerationTemplatesCategory"; //$NON-NLS-1$
	public static final String ATT_ID = "id"; //$NON-NLS-1$
	public static final String ATT_NAME = "name"; //$NON-NLS-1$
	public static final String ATT_CLASS = "providerClass"; //$NON-NLS-1$
	
	private final static int IDX_EDIT = 0;
	private final static int IDX_IMPORT = 2;
	private final static int IDX_EXPORT = 3;
	private final static int IDX_EXPORTALL = 4;
	
	
	public class TemplateItem extends CategorizedItem {
		
		protected TemplatePersistenceData fData;
		
		public TemplateItem(final TemplatePersistenceData data) {
			super(data.getTemplate().getDescription());
			fData = data;
		}
	}
	
	private class Group extends CategorizedOptionButtonsGroup<TemplateItem> {
		
		public Group() {
			super(new String[] {
					Messages.CodeTemplates_EditButton_label,
					null,
					Messages.CodeTemplates_ImportButton_label,
					Messages.CodeTemplates_ExportButton_label,
					Messages.CodeTemplates_ExportAllButton_label,
			});
		}
		
		
		@Override
		protected void handleDoubleClick(final TemplateItem item, final IStructuredSelection rawSelection) {
			doEdit(item);
		}
		
		@Override
		public void handleSelection(final TemplateItem item, final IStructuredSelection rawSelection) {
			fButtonGroup.enableButton(IDX_EDIT, (item != null));
			fButtonGroup.enableButton(IDX_EXPORT, rawSelection.size() > 0);
			
			updateSourceViewerInput(item);
		}
		
		@Override
		public void handleButtonPressed(final int buttonIdx, final TemplateItem item, final IStructuredSelection rawSelection) {
			switch (buttonIdx) {
			case IDX_EDIT:
				if (item != null) {
					doEdit(item);
				}
				break;
			
			case IDX_EXPORT:
				final List<TemplatePersistenceData> datas = new ArrayList<TemplatePersistenceData>();
				for (final Iterator<?> iter = rawSelection.iterator(); iter.hasNext();) {
					final Object curr = iter.next();
					if (curr instanceof TemplateItem) {
						datas.add( ((TemplateItem) curr).fData );
					} else {
						final int catIndex = getIndexOfCategory(curr);
						for (int j = 0; j < fCategoryChilds[catIndex].length; j++) {
							datas.add(fCategoryChilds[catIndex][j].fData );
						}
					}
				}
				doExport(datas.toArray(new TemplatePersistenceData[datas.size()]));
				break;
			
			case IDX_EXPORTALL:
				doExport(fTemplatesStore.getAllTemplateData());
				break;
			
			case IDX_IMPORT:
				doImport();
				break;
			}
		}
		
	}
	
	
	protected IProject fProject;
	private String[] fCategoryIds;
	private ICodeGenerationTemplatesCategory[] fCategoryProvider;
	
	protected CodeGenerationTemplatesStore fTemplatesStore;
	
	protected Group fGroup;
	private SourceViewer fPatternViewer;
	private int fPatternViewerConfiguredCategory = -1;
	private TextViewerJFaceUpdater fPatternViewerUpdater = null;
	private SourceEditorViewerConfigurator fPatternConfigurator;
	private ISourceEditor fPatternEditor;
	
	private final TemplateVariableProcessor fPatternTemplateProcessor;
	private final TemplateVariableProcessor fEditTemplateProcessor;
	
	
	public CodeGenerationTemplatesConfigurationBlock(final IProject project) throws CoreException {
		fProject = project;
		fGroup = new Group();
		loadRegisteredTemplates();
		fGroup.generateListModel();
		
		fPatternTemplateProcessor = new TemplateVariableProcessor();
		fEditTemplateProcessor = new TemplateVariableProcessor();
	}
	
	private void loadRegisteredTemplates() {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();
		final IConfigurationElement[] elements = registry.getConfigurationElementsFor(StatetUIPlugin.PLUGIN_ID, EXTENSION_POINT);
		
		fCategoryIds = new String[elements.length];
		fCategoryProvider = new ICodeGenerationTemplatesCategory[elements.length];
		fGroup.fCategorys = new String[elements.length];
		fGroup.fCategoryChilds = new TemplateItem[elements.length][];
		final TemplateStore[] templates = new TemplateStore[elements.length];
		
		for (int i = 0; i < elements.length; i++) {
			fCategoryIds[i] = elements[i].getAttribute(ATT_ID);
			Assert.isLegal(fCategoryIds[i] != null);
			
			fGroup.fCategorys[i] = elements[i].getAttribute(ATT_NAME);
			if (fGroup.fCategorys[i] == null) {
				fGroup.fCategorys[i] = fCategoryIds[i];
			}
			
			try {
				fCategoryProvider[i] = (ICodeGenerationTemplatesCategory) elements[i].createExecutableExtension(ATT_CLASS);
			} catch (final CoreException e) {
				throw new IllegalArgumentException("Error occured when loading templateprovider of category with id "+fCategoryIds[i], e); //$NON-NLS-1$
			}
			
			templates[i] = fCategoryProvider[i].getTemplateStore();
		}
		
		fTemplatesStore = new CodeGenerationTemplatesStore(fProject, templates);
		try {
			fTemplatesStore.load();
		} catch (final IOException e) {
			StatetUIPlugin.logUnexpectedError(e);
		}
		for (int i = 0; i < fCategoryIds.length; i++) {
			final TemplatePersistenceData[] datas = fTemplatesStore.getTemplateData(i);
			
			fGroup.fCategoryChilds[i] = new TemplateItem[datas.length];
			for (int j = 0; j < fGroup.fCategoryChilds[i].length; j++) {
				fGroup.fCategoryChilds[i][j] = new TemplateItem(datas[j]);
			}
		}
	}
	
	private void reloadTemplateData() {
		for (int i = 0; i < fCategoryIds.length; i++) {
			for (int j = 0; j < fGroup.fCategoryChilds[i].length; j++) {
				fGroup.fCategoryChilds[i][j].fData = fTemplatesStore.getTemplateData(
						i, fGroup.fCategoryChilds[i][j].fData.getId());
			}
		}
	}
	
	private void updateTemplate(final TemplatePersistenceData data) {
		final TemplatePersistenceData[] datas = fTemplatesStore.getAllTemplateData();
		for (int i = 0; i < datas.length; i++) {
			final String id = datas[i].getId();
			if (id != null && id.equals(data.getId())) {
				datas[i].setTemplate(data.getTemplate());
				break;
			}
		}
	}
	
	@Override
	public void setUseProjectSpecificSettings(final boolean enable) {
		super.setUseProjectSpecificSettings(enable);
		
		if (enable) {
			UIAccess.getDisplay(getShell()).asyncExec(new Runnable() {
				public void run() {
					fGroup.reselect();
				}
			});
		}
	}
	
/* GUI ************************************************************************/
	
	@Override
	protected void createBlockArea(final Composite pageComposite) {
		final Label label = new Label(pageComposite, SWT.LEFT);
		label.setText(Messages.CodeTemplates_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		fGroup.createGroup(pageComposite, 1);
		fGroup.initFields();
		
		fPatternViewer = createViewer(pageComposite);
	}
	
	private SourceViewer createViewer(final Composite parent) {
		final Label label = new Label(parent, SWT.LEFT);
		label.setText(Messages.CodeTemplates_Preview_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final SourceViewer viewer = new SourceViewer(parent, null, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.setEditable(false);
		viewer.getTextWidget().setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
		new TextViewerEditorColorUpdater(viewer, EditorsUI.getPreferenceStore());
		
		final IDocument document = new Document();
		viewer.setDocument(document);
		
		final GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = new PixelConverter(viewer.getControl()).convertHeightInCharsToPixels(5);
		viewer.getControl().setLayoutData(data);
		
		fPatternEditor = new ViewerSourceEditorAdapter(viewer, null);
		new SettingsUpdater(new ISettingsChangedHandler() {
			public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
				if (fPatternConfigurator != null) {
					fPatternConfigurator.handleSettingsChanged(groupIds, options);
				}
			}
		}, viewer.getControl());
		
		return viewer;
	}
	
	protected void updateSourceViewerInput(final TemplateItem item) {
		if (fPatternViewer == null || !(UIAccess.isOkToUse(fPatternViewer.getControl())) ) {
			return;
		}
		
		if (item != null) {
			final TemplatePersistenceData data = item.fData;
			final ICodeGenerationTemplatesCategory category = fCategoryProvider[item.getCategoryIndex()];
			final Template template = data.getTemplate();
			
			final TemplateContextType type = category.getContextTypeRegistry().getContextType(template.getContextTypeId());
			fPatternTemplateProcessor.setContextType(type);
			
			if (item.getCategoryIndex() != fPatternViewerConfiguredCategory) {
				fPatternViewerConfiguredCategory = item.getCategoryIndex();
				
				if (fPatternViewerUpdater != null) {
					fPatternViewerUpdater.dispose();
					fPatternViewerUpdater = null;
				}
				if (fPatternConfigurator != null) {
					fPatternConfigurator.unconfigureTarget();
					fPatternConfigurator = null;
				}
				
				fPatternConfigurator = category.getEditTemplateDialogConfiguator(fPatternTemplateProcessor, fProject);
				fPatternConfigurator.setTarget(fPatternEditor);
				fPatternViewerUpdater = new TextViewerJFaceUpdater(fPatternViewer,
						fPatternConfigurator.getSourceViewerConfiguration().getPreferences() );
				
				final IDocument document = new Document(template.getPattern());
				fPatternConfigurator.getDocumentSetupParticipant().setup(document);
				fPatternViewer.setDocument(document);
			}
			else {
				fPatternViewer.getDocument().set(template.getPattern());
			}
			
		} else {
			fPatternViewer.getDocument().set(""); //$NON-NLS-1$
		}
	}
	
	
/* Execute Actions ************************************************************/
	
	public void doEdit(final TemplateItem item) {
		final EditTemplateDialog dialog = new EditTemplateDialog(
				getShell(), item.fData.getTemplate(), true, false,
				fCategoryProvider[item.getCategoryIndex()].getEditTemplateDialogConfiguator(fEditTemplateProcessor, fProject),
				fEditTemplateProcessor,
				fCategoryProvider[item.getCategoryIndex()].getContextTypeRegistry());
		if (dialog.open() == Window.OK) {
			// changed
			item.fData.setTemplate(dialog.getTemplate());
			fGroup.getStructuredViewer().refresh(item);
			fGroup.getStructuredViewer().setSelection(new StructuredSelection(item));
		}
	}
	
	public void doImport() {
		final FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
		dialog.setText(Messages.CodeTemplates_Import_title);
		dialog.setFilterExtensions(new String[] { Messages.CodeTemplates_Import_extension });
		final String path = dialog.open();
		
		if (path == null) {
			return;
		}
		
		try {
			final TemplateReaderWriter reader = new TemplateReaderWriter();
			final File file = new File(path);
			if (file.exists()) {
				final InputStream input = new BufferedInputStream(new FileInputStream(file));
				try {
					final TemplatePersistenceData[] datas = reader.read(input, null);
					for (int i= 0; i < datas.length; i++) {
						updateTemplate(datas[i]);
					}
				} finally {
					try {
						input.close();
					} catch (final IOException x) {
					}
				}
			}
			
			fGroup.getStructuredViewer().refresh();
			updateSourceViewerInput(fGroup.getSingleItem(fGroup.getSelectedItems()));
			
		} catch (final FileNotFoundException e) {
			openReadErrorDialog(e);
		} catch (final IOException e) {
			openReadErrorDialog(e);
		}
	}
	
	public void doExport(final TemplatePersistenceData[] templates) {
		final FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
		dialog.setText(NLS.bind(Messages.CodeTemplates_Export_title, String.valueOf(templates.length)));
		dialog.setFilterExtensions(new String[] { Messages.CodeTemplates_Export_extension });
		dialog.setFileName(Messages.CodeTemplates_Export_filename);
		final String path = dialog.open();
		
		if (path == null) {
			return;
		}
		
		final File file = new File(path);
		
		if (file.isHidden()) {
			MessageDialog.openError(getShell(),
					Messages.CodeTemplates_Export_Error_title,
					NLS.bind(Messages.CodeTemplates_Export_Error_Hidden_message, file.getAbsolutePath()) );
			return;
		}
		
		if (file.exists() && !file.canWrite()) {
			MessageDialog.openError(getShell(),
					Messages.CodeTemplates_Export_Error_title,
					NLS.bind(Messages.CodeTemplates_Export_Error_CanNotWrite_message, file.getAbsolutePath()) );
			return;
		}
		
		if (!file.exists() || confirmOverwrite(file)) {
			OutputStream output= null;
			try {
				output= new BufferedOutputStream(new FileOutputStream(file));
				final TemplateReaderWriter writer= new TemplateReaderWriter();
				writer.save(templates, output);
				output.close();
			} catch (final IOException e) {
				if (output != null) {
					try {
						output.close();
					} catch (final IOException e2) {
						// ignore
					}
				}
				openWriteErrorDialog(e);
			}
		}
	}
	
	private boolean confirmOverwrite(final File file) {
		return MessageDialog.openQuestion(getShell(),
			Messages.CodeTemplates_Export_Exists_title,
			NLS.bind(Messages.CodeTemplates_Export_Exists_message, file.getAbsolutePath()) );
	}
	
	
/* IConfigurationBlock ********************************************************/
	
	@Override
	public void performDefaults() {
		fTemplatesStore.restoreDefaults();
		reloadTemplateData();
		
		// refresh
		fGroup.getStructuredViewer().refresh();
		updateSourceViewerInput(fGroup.getSingleItem(fGroup.getSelectedItems()));
	}
	
	@Override
	public boolean performOk() {
		if (fProject != null) {
			final TemplatePersistenceData[] templateData = fTemplatesStore.getAllTemplateData();
			fTemplatesStore.setProjectSpecific(fUseProjectSettings);
			for (int i = 0; i < templateData.length; i++) {
				fTemplatesStore.setProjectSpecific(templateData[i].getId(), fUseProjectSettings);
			}
		}
		try {
			fTemplatesStore.save();
		} catch (final IOException e) {
			StatetUIPlugin.logUnexpectedError(e);
			openWriteErrorDialog(e);
		}
		return true;
	}
	
	@Override
	public void performCancel() {
		try {
			fTemplatesStore.revertChanges();
		} catch (final IOException e) {
			openReadErrorDialog(e);
		}
	}
	
	
/* Error Dialogs **************************************************************/
	
	private void openReadErrorDialog(final Exception e) {
		StatusManager.getManager().handle(new Status(IStatus.ERROR, StatetUIPlugin.PLUGIN_ID, 0,
				Messages.CodeTemplates_error_Read_message, e));
		MessageDialog.openError(getShell(),
				Messages.CodeTemplates_error_title, Messages.CodeTemplates_error_Read_message);
	}
	
	private void openWriteErrorDialog(final Exception e) {
		StatusManager.getManager().handle(new Status(IStatus.ERROR, StatetUIPlugin.PLUGIN_ID, 0,
				Messages.CodeTemplates_error_Write_message, e));
		MessageDialog.openError(getShell(),
				Messages.CodeTemplates_error_title, Messages.CodeTemplates_error_Write_message);
	}
	
}
