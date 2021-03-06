package wizard;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


/**
 * Creates a new RSL project wizard 
 * 
 * @author Marieta V. Fasie
 * 	marietafasie at gmail dot com
 *
 */
public class NewRSLProjectWizard extends Wizard implements INewWizard {
	
	private static PluginLog log = PluginLog.getInstance();
	
	private IStructuredSelection intialSelection = null;
	
	//Represents the wizard first page
	private RSLProjectPage page1 = null; 

	public NewRSLProjectWizard() {
		
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		//stores the selection so we can have later access to it
		intialSelection = selection;

	}

	/**
	 * Method called when the user clicks the Finish button. 
	 * The operation will be executed in a separate thread 
	 * so that the UI stays responsive and so that the user  
	 * can choose to cancel it
	 */
	@Override
	public boolean performFinish() {
		//get the user input
		final String[] userInput = RSLProjectPage.getUserInput();
		
		//start a new thread using the wizard container and create a new RSL project
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException {
					//create the RSL project with the user information
					createRSLProject(userInput, monitor);
					
				}
			});
		} catch (InvocationTargetException e) {
			log.error(e.getMessage(), e);
			return false;
		} catch (InterruptedException e) {
			log.error(e.getMessage(), e);
			return false;
		}
		
		return true;
	}
	
	/**
	 * Creates the new RSL project 
	 * and structure 
	 * @param userInput Array containing the Project name and location
	 * @param monitor 
	 * @throws InterruptedException 
	 */
	public void createRSLProject(String[] userInput, IProgressMonitor monitor) throws InterruptedException {
		
		monitor.beginTask("Extracting user input", userInput.length);
		
		String location = userInput[1];
		String projectName = userInput[0];
		
		IProject project;
		IProjectDescription desc;
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		
		IPath path = new Path(location);
		
		//check if the new project is in workspace or not
		if(path.toString().equals(root.getLocation().toString()))
		{
			//creating a new project in workspace
			
			project = root.getProject( projectName );
			 
            desc = workspace.newProjectDescription(project.getName());
 
            desc.setLocationURI(null);
		}
		else{
			//creating a new project in a different location than the workspace
			if(monitor.isCanceled())
				throw new InterruptedException("Canceled by user");
			monitor.worked(1);
			
			//create the new RSL project
			
			//create handler to the project
			project = root.getProject(projectName);
			desc = workspace.newProjectDescription( project.getName() ); 
		    IPath path1 = new Path(location+"/"+projectName);
		    desc.setLocation(path1); 
		}
		
        try {
                project.create(desc, null);
                
                //project must be open in order to create new folders
				if (!project.isOpen()) {
					project.open(null);
				}
				
				//create src folder
				final IFolder srcFolder = project.getFolder(new Path("src"));
	            srcFolder.create(true, true, null);
	            
	            //refresh something after creation
	            project.refreshLocal(IResource.DEPTH_INFINITE, null);
	           
            } catch (CoreException e) {
                log.error(e.getMessage(), e);
       }

       
        
		monitor.done();
		
	}

	/**
	 * Add all existing pages to the wizard.
	 * This method creates the pages, adds them to the wizard 
	 * and calls the appropriate methods to initialize them
	 */
	public void addPages(){
		//Sets the wizard title
		setWindowTitle("New RSL project");
		
		//create a new wizard page
		String title = "Create a RSL project";
		String description = "Enter a project name";
		page1 = new RSLProjectPage(title, description);
		
		//add it to the wizard
		addPage(page1);
		
		//populate the wizard page if the user has already selected something in the workbench
		page1.init(intialSelection);
		
	}
}
