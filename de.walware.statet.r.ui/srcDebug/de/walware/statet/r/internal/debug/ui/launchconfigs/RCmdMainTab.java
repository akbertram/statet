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

package de.walware.statet.r.internal.debug.ui.launchconfigs;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewersObservables;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.walware.eclipsecommons.AbstractSettingsModelObject;
import de.walware.eclipsecommons.FileUtil;
import de.walware.eclipsecommons.ui.SharedMessages;
import de.walware.eclipsecommons.ui.databinding.LaunchConfigTabWithDbc;
import de.walware.eclipsecommons.ui.dialogs.ChooseResourceComposite;
import de.walware.eclipsecommons.ui.util.LayoutUtil;

import de.walware.statet.base.ui.StatetImages;
import de.walware.statet.base.ui.debug.HelpRequestor;
import de.walware.statet.base.ui.debug.InputArgumentsComposite;
import de.walware.statet.base.ui.debug.LaunchConfigUtil;
import de.walware.statet.base.ui.util.ExceptionHandler;
import de.walware.statet.r.core.renv.REnvConfiguration;
import de.walware.statet.r.debug.ui.launchconfigs.REnvTab;
import de.walware.statet.r.internal.debug.ui.RLaunchingMessages;


/**
 *
 */
public class RCmdMainTab extends LaunchConfigTabWithDbc {
	
	private static final String ATTR_ROOT = "de.walware.statet.r.debug/RCmd/"; //$NON-NLS-1$
	public static final String ATTR_CMD = ATTR_ROOT+"arguments.cmd"; //$NON-NLS-1$
	public static final String ATTR_OPTIONS = ATTR_ROOT+"arguments.options"; //$NON-NLS-1$
	public static final String ATTR_RESOURCE = ATTR_ROOT+"arguments.resource"; //$NON-NLS-1$

	
	private static class Cmd extends AbstractSettingsModelObject {
		
		public final static int PACKAGE = 1;
		public final static int DOC = 2;
		public final static int CUSTOM = 3;
		
		private String fName;
		private String fCommand;
		private int fType;
		
		public Cmd(String name, String command, int type) {
			fName = name;
			fCommand = command;
			fType = type;
		}

		public String getName() {
			return fName;
		}

		public int getType() {
			return fType;
		}
		
		public void setCommand(String command) {
			fCommand = command.trim();
		}
		
		public String getCommand() {
			return fCommand;
		}
	}
	

	private Cmd[] fCommands;
	private Cmd fCustomCommand;
	
	private ComboViewer fCmdCombo;
	private Text fCmdText;
	private Button fHelpButton;
	private InputArgumentsComposite fArgumentsControl;
	private ChooseResourceComposite fResourceControl;
	
	private WritableValue fCmdValue;
	private WritableValue fArgumentsValue;
	private WritableValue fResourceValue;
	
	boolean fWithHelp = false;
	private ILaunchConfigurationTab fREnvTab;
	private ILaunchConfiguration fConfigCache;
	
	
	public RCmdMainTab() {
		super();
		createCommands();
	}
	
	private void createCommands() {
		List<Cmd> commands = new ArrayList<Cmd>();
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdCheck_name, "CMD check", Cmd.PACKAGE)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdBuild_name, "CMD build", Cmd.PACKAGE)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdInstall_name, "CMD INSTALL", Cmd.PACKAGE)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdRemove_name, "CMD REMOVE", Cmd.PACKAGE)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdRdconv_name, "CMD Rdconv", Cmd.DOC)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdRd2dvi_name, "CMD Rd2dvi", Cmd.DOC)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdRd2txt_name, "CMD Rd2txt", Cmd.DOC)); //$NON-NLS-1$
		commands.add(new Cmd(RLaunchingMessages.RCmd_CmdSd2Rd_name, "CMD Sd2Rd", Cmd.DOC)); //$NON-NLS-1$
		fCustomCommand = new Cmd(RLaunchingMessages.RCmd_CmdOther_name, "", Cmd.CUSTOM); //$NON-NLS-1$
		commands.add(fCustomCommand);
		
		fCommands = commands.toArray(new Cmd[commands.size()]);
		resetCommands();
	}
	
	private void resetCommands() {
		fCustomCommand.fCommand = "CMD "; //$NON-NLS-1$
	}
	
	public String getName() {
		return RLaunchingMessages.RCmd_MainTab_name;
	}
	
	public Image getImage() {
		return StatetImages.getImage(StatetImages.LAUNCHCONFIG_MAIN);
	}
	
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainComposite.setLayout(GridLayoutFactory.swtDefaults().create());
		
		Group group;
		group = new Group(mainComposite, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setText(RLaunchingMessages.RCmd_MainTab_Cmd_label);
		createCommandControls(group);

		Label note = new Label(mainComposite, SWT.WRAP);
		note.setText(SharedMessages.Note_label + ": " + fArgumentsControl.getNoteText()); //$NON-NLS-1$
		note.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true));

		Dialog.applyDialogFont(parent);
		initBindings();
	}
		
	private void createCommandControls(Composite container) {
		for (ILaunchConfigurationTab tab : getLaunchConfigurationDialog().getTabs()) {
			if (tab instanceof REnvTab) {
				fREnvTab = tab;
				break;
			}
		}
		fWithHelp = (fREnvTab != null) && (getLaunchConfigurationDialog() instanceof TrayDialog);

		container.setLayout(LayoutUtil.applyGroupDefaults(new GridLayout(), 3));

		String[] names = new String[fCommands.length];
		for (int i = 0; i < fCommands.length; i++) {
			names[i] = fCommands[i].getName();
		}
		fCmdCombo = new ComboViewer(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		fCmdCombo.setContentProvider(new ArrayContentProvider());
		fCmdCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				Cmd cmd = (Cmd) element;
				return cmd.getName();
			}
		});
		fCmdCombo.setInput(fCommands);
		fCmdCombo.getCombo().setVisibleItemCount(names.length);
		fCmdCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		fCmdText = new Text(container, SWT.BORDER | SWT.SINGLE);
		fCmdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, fWithHelp ? 1 : 2, 1));

		if (fWithHelp) {
			fHelpButton = new Button(container, SWT.PUSH);
			fHelpButton.setText(RLaunchingMessages.RCmd_MainTab_RunHelp_label);
			fHelpButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					queryHelp();
				}
			});
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
			gd.widthHint = LayoutUtil.hintWidth(fHelpButton);
			fHelpButton.setLayoutData(gd);
		}

		LayoutUtil.addSmallFiller(container);
		fArgumentsControl = new InputArgumentsComposite(container);
		fArgumentsControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

		fResourceControl = new ChooseResourceComposite(container,
				ChooseResourceComposite.STYLE_LABEL | ChooseResourceComposite.STYLE_TEXT,
				ChooseResourceComposite.MODE_FILE | ChooseResourceComposite.MODE_OPEN, 
				""); //$NON-NLS-1$
		fResourceControl.showInsertVariable(true);
		fResourceControl.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
	}
	
	protected void addBindings(DataBindingContext dbc, Realm realm) {
		fCmdValue = new WritableValue(realm, Cmd.class);
		fArgumentsValue = new WritableValue(realm, String.class);
		fResourceValue = new WritableValue(realm, String.class);

		IObservableValue cmdSelection = ViewersObservables.observeSingleSelection(fCmdCombo);
		dbc.bindValue(cmdSelection, fCmdValue, null, null);
		IValidator cmdValidator = new IValidator() {
			public IStatus validate(Object value) {
				String s = (String) value;
				if (s == null || s.trim().length() == 0) {
					return ValidationStatus.warning("No CMD specified.");
				}
				return ValidationStatus.ok();
			}
		};
		dbc.bindValue(SWTObservables.observeText(fCmdText, SWT.Modify),
				BeansObservables.observeDetailValue(realm, cmdSelection, "command", String.class), //$NON-NLS-1$
				new UpdateValueStrategy().setAfterGetValidator(cmdValidator),
				new UpdateValueStrategy().setBeforeSetValidator(cmdValidator) );
		dbc.bindValue(SWTObservables.observeText(fArgumentsControl.getTextControl(), SWT.Modify),
				fArgumentsValue, null, null);
		
		fResourceControl.getValidator().setOnLateResolve(IStatus.WARNING);
		fResourceControl.getValidator().setOnEmpty(IStatus.OK);
		final Binding resourceBinding = dbc.bindValue(fResourceControl.createObservable(), fResourceValue, 
				new UpdateValueStrategy().setAfterGetValidator(
						new SavableErrorValidator(fResourceControl.getValidator())), null);
		cmdSelection.addValueChangeListener(new IValueChangeListener() {
			public void handleValueChange(ValueChangeEvent event) {
				Cmd cmd = (Cmd) event.diff.getNewValue();
				if (cmd != null) {
					fCmdText.setEditable(cmd.getType() == Cmd.CUSTOM);
					String label;
					int mode = 0;
					switch (cmd.getType()) {
					case Cmd.PACKAGE:
						label = RLaunchingMessages.RCmd_Resource_Package_label;
						mode = ChooseResourceComposite.MODE_DIRECTORY;
						break;
					case Cmd.DOC:
						label = RLaunchingMessages.RCmd_Resource_Doc_label;
						mode = ChooseResourceComposite.MODE_FILE;
						break;
					case Cmd.CUSTOM:
					default:
						label = RLaunchingMessages.RCmd_Resource_Other_label;
						mode = ChooseResourceComposite.MODE_FILE | ChooseResourceComposite.MODE_DIRECTORY;
						break;
					}
					fResourceControl.setResourceLabel(label);
					fResourceControl.setMode(mode | ChooseResourceComposite.MODE_OPEN);
					resourceBinding.validateTargetToModel();
				}
			} });
	}
	
	
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_CMD, fCommands[0].getCommand());
		configuration.setAttribute(ATTR_OPTIONS, ""); //$NON-NLS-1$
		configuration.setAttribute(ATTR_RESOURCE, "${resource_loc}"); //$NON-NLS-1$
	}

	@Override
	public void doInitialize(ILaunchConfiguration configuration) {
		resetCommands();
		Cmd cmd = null;
		try {
			String command = configuration.getAttribute(ATTR_CMD, ""); //$NON-NLS-1$
			for (Cmd candidate : fCommands) {
				if (candidate.getCommand().equals(command)) {
					cmd = candidate;
					break;
				}
			}
			if (cmd == null) {
				fCustomCommand.setCommand(command);
				cmd = fCustomCommand;
			}
		} catch (CoreException e) {
			cmd = fCommands[0];
			logReadingError(e);
		}
		fCmdValue.setValue(cmd);
		
		String options = null;
		try {
			options = configuration.getAttribute(ATTR_OPTIONS, ""); //$NON-NLS-1$
			
		} catch (CoreException e) {
			options = ""; //$NON-NLS-1$
			logReadingError(e);
		}
		fArgumentsValue.setValue(options);
		
		String resource = null;
		try {
			resource = configuration.getAttribute(ATTR_RESOURCE, ""); //$NON-NLS-1$
		} catch (CoreException e) {
			resource = ""; //$NON-NLS-1$
		}
		fResourceValue.setValue(resource);
		
		checkHelp(configuration);
	}
	
	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		checkHelp(workingCopy);
		super.activated(workingCopy);
	}
	
	private void checkHelp(ILaunchConfiguration configuration) {
		fConfigCache = configuration;
		if (fWithHelp) {
			fHelpButton.setEnabled(fREnvTab.isValid(fConfigCache));
		}
	}
	
	@Override
	public void doSave(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_CMD, ((Cmd) fCmdValue.getValue()).getCommand());
		configuration.setAttribute(ATTR_OPTIONS, (String) fArgumentsValue.getValue());
		configuration.setAttribute(ATTR_RESOURCE, (String) fResourceValue.getValue());
	}
	
	
	private void queryHelp() {
		if (!fWithHelp) {
			return;
		}
		try {
			List<String> cmdLine = new ArrayList<String>();
			ILaunchConfigurationDialog dialog = getLaunchConfigurationDialog();
			
			// r env
			REnvConfiguration renv = REnvTab.getREnv(fConfigCache);
			IPath r = FileUtil.expandToLocalPath(renv.getRHome(), "bin/R"); //$NON-NLS-1$
			cmdLine.add(r.toOSString());
	
			String cmd = ((Cmd) fCmdValue.getValue()).getCommand().trim();
			if (cmd.length() > 0) {
				cmdLine.addAll(Arrays.asList(cmd.split(" "))); //$NON-NLS-1$
			}
			cmdLine.add("--help"); //$NON-NLS-1$
			HelpRequestor helper = new HelpRequestor(cmdLine, (TrayDialog) dialog);
			
			helper.getProcessBuilder().environment();
			Map<String, String> envp = helper.getProcessBuilder().environment();
			LaunchConfigUtil.configureEnvironment(fConfigCache, envp);
			envp.putAll(renv.getEnvironmentsVariables());

			dialog.run(true, true, helper);
			updateLaunchConfigurationDialog();
		}
		catch(CoreException e) {
			ExceptionHandler.handle(e, RLaunchingMessages.RCmd_MainTab_error_CannotRunHelp_message);
		} catch (InvocationTargetException e) {
			ExceptionHandler.handle(e, getShell(), RLaunchingMessages.RCmd_MainTab_error_WhileRunningHelp_message);
		} catch (InterruptedException e) {
			Thread.interrupted();
		}
	}
	
	@Override
	public void dispose() {
		if (fWithHelp) {
			HelpRequestor.closeHelpTray((TrayDialog) getLaunchConfigurationDialog());
		}
		super.dispose();
	}
}