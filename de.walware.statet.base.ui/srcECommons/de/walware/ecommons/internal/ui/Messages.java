/*******************************************************************************
 * Copyright (c) 2006-2008 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.internal.ui;

import org.eclipse.osgi.util.NLS;


public class Messages extends NLS {
	
	
	public static String CopyToClipboard_error_title;
	public static String CopyToClipboard_error_message;
	
	public static String SearchWorkspace_label;
	public static String BrowseFilesystem_label;
	public static String BrowseWorkspace_label;
	public static String BrowseFilesystem_ForFile_label;
	public static String BrowseWorkspace_ForFile_label;
	public static String BrowseFilesystem_ForDir_label;
	public static String BrowseWorkspace_ForDir_label;
	
	public static String ChooseResource_Task_description;
	public static String ResourceSelectionDialog_title;
	public static String ResourceSelectionDialog_message;
	
	public static String InsertVariable_label;
	
	public static String ConfigurationPage_error_message;
	
	public static String LaunchDelegate_LaunchingTask_label;
	public static String LaunchDelegate_RunningTask_label;
	public static String BackgroundResourceRefresher_Job_name;
	
	
	static {
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}
	private Messages() {}
	
}