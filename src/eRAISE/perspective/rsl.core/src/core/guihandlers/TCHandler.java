/**
 * 
 */
package core.guihandlers;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import core.Console;
import core.PluginLog;



/**
 * @author Marieta V. Fasie
 * 	marietafasie at gmail dot com
 *
 */
public class TCHandler extends AbstractHandler {

	private static PluginLog log = PluginLog.getInstance();
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		Console.getInstance().clear();
		
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	    if (window != null)
	    {
	    	//get the selections in the Project explorer
	    	log.debug("Get the selections in the Project explorer");
	        IStructuredSelection selection = (IStructuredSelection) window.getSelectionService().getSelection("org.eclipse.ui.navigator.ProjectExplorer");
	        final Object[] selectedElements = selection.toArray();
	        
			BusyIndicator.showWhile(null, new Runnable() {

				@Override
				public void run() {
					//go through each open selected project 
					log.debug(selectedElements.length + " selected elements");
					for(int index = 0; index < selectedElements.length; index++){
	        	
						Object element = selectedElements[index];	 
						log.debug("Selected item: "+element.toString());
						typeCheckObject(element);
					}
				}
			});
	    }
		return null;
	}

	/**
	 * Recursive method to go through a project or 
	 * a folder. If an RSL file is met the type checker
	 *  is called
	 * 
	 * @param project IProject or IFolder 
	 */
	public void typeCheckObject(Object element) {
		IResource[] members;
		
		try {
			if(element instanceof IProject ){ //if it is a project go deeper
				if(((IProject)element).isOpen()){ //and it is open
					log.debug("Recursive call on: "+((IProject)element).getFullPath().toOSString());
					members =((IProject) element).members();
					for(int index = 0; index < members.length; index++)
						this.typeCheckObject( members[index] );
				}
			}
			else 
				if(element instanceof IFolder){ //if it is a folder go deeper
					members = ((IFolder) element).members();
					log.debug("Recursive call on: "+ ((IFolder)element).getFullPath().toOSString());
					for(int index = 0; index < members.length; index++)
						this.typeCheckObject( members[index] );
				}
				else					
					if(element instanceof IFile){//if it is a file
						IFile ifile = (IFile) element;
						if(ifile.getFileExtension().equals("rsl")){
							log.debug("Calling typeCheckAndPrint on:"+ ((IFile) element)+ifile.getFullPath());
							//call the one that handles single type check
							TypeCheckActiveFile.typecheckAndPrint(ifile);
						}
					}
					else //only projects, folders and rsl files are of interest
						return;
		
		} catch (CoreException e) {
			log.error(e.getMessage(), e);
		}
	}

}
