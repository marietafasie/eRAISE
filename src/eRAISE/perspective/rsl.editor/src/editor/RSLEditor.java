/**
 * 
 */
package editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;

import core.Console;
import core.TypeChecker;


import editor.config.ColorManager;
import editor.config.RSLConfiguration;
import editor.config.RSLDocumentProvider;


/**
 * @author Marieta V. Fasie
 * 	marietafasie at gmail dot com
 *
 */
public class RSLEditor extends TextEditor {

	private PluginLog log = PluginLog.getInstance();
	
	/**
	 *Stores the color manager 
	 */
	private ColorManager colorManager;
	

	/**
	 * Constructor that instantiates the color manager,
	 * the document provider and the source viewer
	 */
	public RSLEditor() {
		super();
		colorManager = new ColorManager();
		
		SourceViewerConfiguration viewerCongif = new RSLConfiguration(colorManager);
		setSourceViewerConfiguration(viewerCongif);
		
		FileDocumentProvider documentProvider = new RSLDocumentProvider();
		setDocumentProvider(documentProvider);
	}
	
	
	/**
	 * The method reacts only when
	 * something has changed 
	 */
	@Override
	public void doSave(IProgressMonitor monitor){
		super.doSave(monitor);		
		//Get the editor
		
		IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		
		IFile ifile = null;
		IEditorInput input = null;
		
		try{
			input = editorPart.getEditorInput();
			if (input instanceof FileEditorInput) {
				ifile = ((FileEditorInput) input).getFile();				
				//call the type checker if ifile is a file
				if(ifile != null){
					//clear markers first
					ifile.deleteMarkers(null, true, IResource.DEPTH_INFINITE);
					Console.getInstance().clear();
					TypeChecker tc = new TypeChecker();				
					tc.typeCheck(ifile);
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
			return;
		}
		
		
	}
	
	@Override
	public void doSaveAs(){
		super.doSaveAs();
		
		//Get the editor
		
		IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		
		IFile ifile = null;
		IEditorInput input = null;
		
		try{
			input = editorPart.getEditorInput();
			if (input instanceof FileEditorInput) {
				ifile = ((FileEditorInput) input).getFile();
				//call the type checker if ifile is a file
				if(ifile != null){
					//clear markers first
					ifile.deleteMarkers(null, true, IResource.DEPTH_INFINITE);
					Console.getInstance().clear();
					TypeChecker tc = new TypeChecker();						
					tc.typeCheck(ifile);
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
			return;
		}
		
	}

	
	/**
	 * Disposes the color manager and the rsl editor
	 */
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}
	

}
