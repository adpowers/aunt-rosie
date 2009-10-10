package org.andrewhitchcock.auntrosie;

import java.util.ArrayList;
import java.util.List;

import com.google.wave.api.AbstractRobot;
import com.google.wave.api.Annotation;
import com.google.wave.api.Blip;
import com.google.wave.api.Event;
import com.google.wave.api.EventType;
import com.google.wave.api.Range;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.TextView;

public class AuntRosieRobotServlet extends AbstractRobot {

	private static final long serialVersionUID = -1546080376029476133L;

	@Override
	public String getRobotName() {
		return "Aunt Rosie";
	}

	@Override
	public void processEvents(RobotMessageBundle bundle) {
		for (Event e : bundle.getEvents()) {
		  if (e.getType() == EventType.DOCUMENT_CHANGED) {
		    Blip blip = e.getBlip();
		    
		    // Skip blips I created.
		    if (blip.getContributors().contains("aunt-rosie@appspot.com")) {
		      continue;
		    }
		    
		    TextView doc = blip.getDocument();
		    
		    String keyword = "/translate:";
		    int location = doc.getText().indexOf(keyword);
		    if (location == -1) {
		      continue; // nothing to translate
		    }
		    
		    location += keyword.length();
		    String languageTarget = doc.getText(new Range(location, location + 2)).toLowerCase();
		    
		    List<Annotation> annotationsToTranslate = new ArrayList<Annotation>();
		    for (Annotation langAnnotations : doc.getAnnotations("lang")) {
		      // Don't try to translate a language to itself
		      if (langAnnotations.getValue().equals(languageTarget)) {
		        continue;
		      }
		      
		      if (langAnnotations.getValue().equals("unknown")) {
		        continue;
		      }
		      
	        // Don't try to translate text we added (in case language detection fails).
		      boolean valid = true;
	        for (Annotation myAnnotations : doc.getAnnotations("aunt-rosie")) {
	           Range langRange = langAnnotations.getRange();
	           Range myRange = myAnnotations.getRange();
	           
	           if ((langRange.getStart() >= myRange.getStart() && langRange.getStart() <= myRange.getEnd())
	            || (langRange.getEnd() >= myRange.getStart() && langRange.getEnd() <= myRange.getEnd())) {
	             valid = false;
	             break;
	           }
	        }
	        if (valid) {
	          annotationsToTranslate.add(langAnnotations);
	        }
		    }
		    
		    
		    Blip myBlip;
		    List<Blip> blips = blip.getChildren();
		    if (blips.isEmpty()) {
		      myBlip = blip.createChild();
		    } else {
		      myBlip = blips.get(0);
		    }
		    
		    TextView myDoc = myBlip.getDocument();
		    myDoc.delete();
		    
		    myDoc.append("Text to translate:\n");
		    for (Annotation a : annotationsToTranslate) {
		      myDoc.append("\"" + doc.getText(a.getRange()) + "\"" + "\n\n");
		    }
		  }
		}
	}

	@Override
	public void registerForEvents() {
		// TODO Auto-generated method stub

	}

}
