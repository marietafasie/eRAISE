/**
 * 
 */
package testcases.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import core.IRSLTestCasesListener;
import core.PluginLog;

import testcases.ui.RTestView;
import testcases.ui.TestCaseContentProvider;


/**
 * Class called when the test cases' execution is  
 * started and when their execution is finished
 * 
 * @author Marieta V. Fasie
 * 	marietafasie at gmail dot com
 *
 */
public class TestCasesListener implements IRSLTestCasesListener{
	
	private PluginLog log = PluginLog.getInstance();

	private final String RT_VIEW_ID = "rsl.testcases.testview";

	@Override
	public void testFinished(String message, IFile rslifile) {
		
		int dotPosition = rslifile.getName().lastIndexOf(".");
		String smlFileName = rslifile.getName().substring(0, dotPosition).concat(".sml");

		RSLTestFile rtf = null;
		TestCaseContentProvider content = TestCaseContentProvider.getInstance();
		
		String rslFileName = smlFileName.substring(0,smlFileName.length()-4); //X.sml vs X
		if(! message.contains("open "+rslFileName)){
			log.debug("Does not contain: open "+rslFileName);
			return;
		}
		
		int foundTests = 0;
		
		int pos = message.indexOf("open "+rslFileName);
		String submessage = message.substring(pos, message.length());
		BufferedReader r = new BufferedReader(new StringReader(submessage));
		String line1, line2, line3;
		try {
			line1 = r.readLine(); //read the line with "use X"
			line2 = r.readLine();
			
			while ( line1 != null && line2 != null &&
					(line3=r.readLine()) != null ) {
				//rules defined based on rsltc.el source code and the documentation at 
				//ftp://ftp.iist.unu.edu/pub/RAISE/dist/user_guide/ug.pdf
				if( line1.contains("val it = () : unit") && !line2.contains("val it = () : unit") 
						&& !line2.contains("Unexecuted expressions in") 
						&& !line2.contains("Complete expression coverage of") 
						&& !line2.contains("error(s)")
						&& line3.contains("val it = () : unit") )
				{
					
					foundTests++;
					if(foundTests == 1){
					
						//rtf = new RSLTestFile(ifile.getLocation().toString());
						rtf = new RSLTestFile(rslifile.getProject().getName()+"/"+rslifile.getProjectRelativePath().toString());
						RSLTestFile file = content.getModel().hasFile(rtf.getName()); 
						if( file != null){
							content.getModel().removeRSLFile(file);
							content.refreshView();
						}
						content.getModel().addRSLFile(rtf);
					
					}
					
					createModel(line2, rtf);
						
					content.refreshView();										
					
				}
				line1 = line2;
				line2 = line3;					
			}
		
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		
	}

	@Override
	public void testsStarted() {
		log.debug("Test started in testcases");
		//display test view
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(RT_VIEW_ID );
			RTestView rtv = new RTestView();
			rtv.getViewer().setInput(new RSLTestCaseModel());
			
		} catch (PartInitException e) {
			log.error(e.getMessage(), e);
		}
		
	}
	
	private void createModel(String line, RSLTestFile rtf){
		
		if(line.startsWith("[")){
			int pos = line.indexOf("]");
			String testName = line.substring(1, pos);
			String value = line.substring(pos+2, line.length());
			log.debug("Test name: "+testName+" Value: "+value);
			TestCase tc = new TestCase(testName, value);
			rtf.getTestCases().add(tc);
		}
		else{
			String value = line;
			log.debug("Value: "+value);
			TestCase tc = new TestCase("");
			tc.setValue(value);
			rtf.getTestCases().add(tc);
		}
	}


}
