/**
 * 
 */
package core;


import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;


/**
 * @author Marieta V. Fasie
 * 	marietafasie at gmail dot com
 *
 */
public class ResourceHandler {

	private static String RSLML_CM = "/rslml.cm";
	
	private static PluginLog log = PluginLog.getInstance();

	private static String LATEX_PATH = "resources/latex_content.txt";

	public static void move(IProject srcProject,IProject destProject, String filename1, String filename2, IPath path){
		//check if an SML project exists
		log.debug("----Moving files: "+filename1+" and "+ filename2);
		log.debug(" from "+srcProject.getName()+" to "+destProject.getName());
		
		try {
			if (destProject.exists()){
				if( !destProject.isOpen() ){
					destProject.open(null);
				}
			}
			else{//sml project does not exist
				log.debug("Project does not exist, thus we create it");
				addProject(destProject);
			}
		} catch (CoreException e) {
			log.error(e.getMessage(), e);
		}
		 
		//at this point an SML project exists
	
	    //move files from rsl project to sml project
		log.debug("Calling moveFile method");
		
	    moveFile(filename1,srcProject,destProject,path);
	    moveFile(filename2,srcProject,destProject,path);
	    log.debug("----Moving files finished");
	}

	private static void moveFile(String fileName, IProject srcProject,
			IProject destProject, IPath path) {
		
		log.debug("----Enter moveFile");
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		
		if(!destProject.exists() || !srcProject.exists()){
			return;
		}
		 //refresh something after creation
        try {
			srcProject.refreshLocal(IResource.DEPTH_INFINITE, null);
			destProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e1) {
			log.error(e1.getMessage(), e1);
		}
		
		String fullPath="";
		
		//create the entire folder structure
		for(int i = 0; i < path.segmentCount(); i++){	
			String pathSeg = path.segment(i);
			fullPath += IPath.SEPARATOR + pathSeg;
			IFolder folder = destProject.getFolder(fullPath);
			if( !folder.exists() ){
				try {
					folder.create(false, true, null);
				} catch (CoreException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		
		
		//move the file
		IPath destFilePath = (destProject.getFullPath().append(path)).append(fileName);
		IFile ifile = srcProject.getFile(path.toString()+IPath.SEPARATOR+fileName);
		
		//check if the file already exists there
		//due to older versions
		
		IFile olderifile = root.getFile(destFilePath);
		if(olderifile.exists()){
			//delete file
			try {
				olderifile.delete(false, null);
			} catch (CoreException e) {
				log.error(e.getMessage(), e);
			}
		}
		
		log.debug("Move file");
		try {
			ifile.move(destFilePath, false, null);
			destProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			log.error(e.getMessage(), e);
		}
		
		 //refresh something after creation
        try {
			srcProject.refreshLocal(IResource.DEPTH_INFINITE, null);
			destProject.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e1) {
			log.error(e1.getMessage(), e1);
		}
		
        log.debug("----Exit moveFile");
	}

	/**
	 * Adds a project to the workspace with a 
	 * 'src' subfolder
	 * 
	 * @param project The handler to the projects that needs to be creates
	 */
	public static void addProject(IProject project) {
		log.debug("----Enter addProject");
		
		if(project.exists())
			return;
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		String projectName = project.getName();
		
		//create project
		log.debug("Create project at "+project.getFullPath());
		URI projectLocation;
		
		try {
			String rootLocation =  root.getLocationURI().toString();
			rootLocation = URLEncoder.encode(rootLocation, "UTF-8").replace("+", "%20");
			String name = URLEncoder.encode(projectName, "UTF-8").replace("+", "%20");
    	   
			projectLocation = new URI(rootLocation+ IPath.SEPARATOR+ name);
		} catch (URISyntaxException | UnsupportedEncodingException e1) {
			log.error(e1.getLocalizedMessage(), e1);
			return;
		}
		
		log.debug("Workspace uri: "+root.getLocationURI());
		//if projectLocation is the same as
		 if (projectLocation != null && root.getLocationURI().equals(projectLocation)) {
             projectLocation = null;
         }
		
		IProjectDescription description = workspace.newProjectDescription(projectName);	
		description.setLocationURI(project.getLocationURI());
				
		try {
			log.debug("Create project");
			//project.create(description, new ProgressMonitor(null, "Creating associated SML project", null, 0, 100));
			project.create(description, null);
			
			if (!project.isOpen()) {
				project.open(null);
			}
			
			//create src folder
			log.debug("create src folder");
			final IFolder srcFolder = project.getFolder(new Path("src"));
			if( !srcFolder.exists() )
				srcFolder.create(true, true, null);
            
            //refresh something after creation
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
			
		} catch (CoreException e) {
			log.error(e.getLocalizedMessage(), e);
		}
		log.debug("---Exit addProject");
	}

	public static void editFile(IFile smlIFile, IPath absolutePath) {
		log.debug("----Enter editFile");
		InputStream rslIS;
		log.debug("Editing file: "+smlIFile.getName()+" with absolutePath: "+absolutePath);
		
		try {
			rslIS = smlIFile.getContents();
			
			Scanner scanneraux = new Scanner(rslIS, "UTF-8");
			Scanner scanner = scanneraux.useDelimiter("\n"); 
			
			String completeContent = ""; 
			while (scanner.hasNext()){
				String line = scanner.next()+"\n";
				if(line.contains("use ")){
					log.debug("Line contains 'use'");
					//add path instead of old file name ex "X_.sml" will become "D:/somelocation/X_.sml"
					int indx1 = line.indexOf("\"");
					int indx2 = line.indexOf("\"", indx1+1);
					int end = line.length();
					String newline = (line.substring(0, indx1+1)).concat(absolutePath.toString()).concat(line.substring(indx2, end));
					
					log.debug("new line " + newline);
					completeContent += newline;
				}
				else
					if(line.contains("CM.autoload ") && !line.contains("$")){ //   "/usr/share/rsltc/sml/rslml.cm";
						log.debug("Line contains \"CM.autoload\" and no $");
						int indx1 = line.indexOf("\"");
						int indx2 = line.indexOf("\"", indx1+1);
						int end = line.length();
						
						// get rslml.cm location
						//String rslmlLocation = findPluginResource("rsl.core",RSLML_CM);
						String rslmlLocation = System.getProperty("rslml");
						if(rslmlLocation == null){
							Activator.getDefault().setBinariesPath();
							rslmlLocation = System.getProperty("rslml");
						}
						rslmlLocation += RSLML_CM;
						String rslmlUnixLoca = rslmlLocation.replace("\\", "/");
						String newline = (line.substring(0, indx1+1)).concat(rslmlUnixLoca).concat(line.substring(indx2, end));
						
						log.debug("New line " + newline);
						
						completeContent += newline;						
					}
					else
						completeContent += line;
				
				}
			
			scanneraux.close();
			scanner.close();
			
			FileWriter fw = new FileWriter(smlIFile.getLocation().toString());
			fw.write(completeContent);
			
			log.debug("Writing complete ");
			fw.close();
			rslIS.close();
			
		} catch (CoreException | IOException e) {
			log.error(e.getMessage(), e);
		}
		log.debug("---Exit editFile");
	}

	public static void addFile(String fileName, Path path) {
		log.debug("---Enter addFile");
		 IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path.append(fileName));
		 if(file.exists())
			 return;

		//get the file with the absolute path 
		try {
			String pathToRes ="platform:/plugin/rsl.core/"+LATEX_PATH; 
			URL url = new URL(pathToRes);
			InputStream inputStream = url.openConnection().getInputStream();
			
			file.create(inputStream, false, new NullProgressMonitor());
			
		} catch (CoreException | IOException e) {
			log.error(e.getMessage(), e);
		}
		log.debug("---Exit addFile");
		
	}
	
	public static void clearMarkers(IFile file){
		//delete all markers of a file
		try {
			file.deleteMarkers(null, true, IResource.DEPTH_INFINITE);
			log.debug("Delete markers on file: "+ file.getName());
		} catch (CoreException e) {
			log.error(e.getMessage(), e);
		}
	}
}
