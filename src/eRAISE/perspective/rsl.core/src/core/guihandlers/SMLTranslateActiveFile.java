/**
 * 
 */
package core.guihandlers;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import core.Console;
import core.ResourceHandler;
import core.PluginLog;
import core.SMLTranslator;



/**
 * @author Marieta V. Fasie
 * 	marietafasie at gmail dot com
 *
 */
public class SMLTranslateActiveFile extends AbstractHandler {

	private static PluginLog log = PluginLog.getInstance();
	
	private static Scanner scanner;

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		//Get the editor		
		IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();		
		IEditorInput input = null;
		
		try{
			input = editorPart.getEditorInput();
		}catch(Exception e){
			log.error(e.getMessage(), e);
			return null;
		}
	
		if (input instanceof FileEditorInput) {
		    final IFile ifile = ((FileEditorInput) input).getFile();
		    //clear console
		    Console.getInstance().clear();
		   
		    log.debug("Checking if file is null");
		    //call the type checker if ifile is a file
			if(ifile != null){
				
				log.debug("File "+ifile.getName()+" is not null");
				BusyIndicator.showWhile(null, new Runnable() {

					@Override
					public void run() {
						translateandprint(ifile);
					}
				});
			}
		}
				
		return null;
	}
	
	/**
	 * 
	 * @param ifile
	 * @return true if the SML files were successfully created
	 */
	public static boolean translateandprint(IFile ifile) {
		
		SMLTranslator sml = new SMLTranslator();
		//delete all markers of a file before type check
		ResourceHandler.clearMarkers(ifile);
		
		//Translate RSL file to SML
		log.debug("Calling translate on "+ifile.getName());
		String message = sml.translate(ifile);
		
		log.debug("SML translate finished ... now we need to move the SML files");
		//get the names of the newly created sml files
		Pattern SML_INFO_PATTERN = Pattern.compile("SML output is in files(\\s*)(.*sml)(\\s+)(.*)(\\s+)(.*sml)");
		
		scanner = new Scanner(message);
		
		Console console = Console.getInstance();
		console.print(message);
		
		while (scanner.hasNextLine()) {
			log.debug("Reading output line per line to se if it contains 'SML translation was correct'");
			String line = scanner.nextLine();
			// process the line
			Matcher matcher = SML_INFO_PATTERN.matcher(line); 
		
			//if the SML translation was correct
			if (matcher.matches()) {
				log.debug("SML translation was correct");
				String smlFileName1, smlFileName2;
			
				smlFileName1 = matcher.group(2); //ex.sml
				smlFileName2 = matcher.group(6); //ex_.sml
			
				log.debug("Two files created "+ smlFileName1+" "+smlFileName2);
			
				String smlProjectName = ifile.getProject().getName()+"SML";
				IProject smlProject = ResourcesPlugin.getWorkspace().getRoot().getProject(smlProjectName);
		    
				log.debug("Calling move sml file in project: "+smlProject.getName());
				ResourceHandler.move(ifile.getProject(),smlProject, smlFileName1, smlFileName2, ifile.getProjectRelativePath().removeLastSegments(1));
		    		    					    
				log.debug("----SML tranlsateandprint returns true");
				return true;
			}	 
		}
		
		log.debug("---SML tranlsateandprint returns false");
		return false;
	}

}
