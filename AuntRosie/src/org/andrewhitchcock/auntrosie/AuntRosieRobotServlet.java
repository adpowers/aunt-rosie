package org.andrewhitchcock.auntrosie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private static final String myAnnotationName = "aunt-rosie";
	
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
		    
		    List<Annotation> myAnnotations = doc.getAnnotations(myAnnotationName);
		    
        Blip myBlip;
        List<Blip> blips = blip.getChildren();
        if (blips.isEmpty()) {
          myBlip = blip.createChild();
        } else {
          myBlip = blips.get(0);
        }
		    
		    TextView myDoc = myBlip.getDocument();
		    myDoc.delete();
		    // delete my own annotations        
		    //doc.deleteAnnotations(myAnnotationName);
        for (Annotation a : myAnnotations) {
          myDoc.append(a + "\n");
          doc.delete(a.getRange());
        }
		    
		    List<Annotation> annotationsToTranslate = new ArrayList<Annotation>();
		    for (Annotation langAnnotations : doc.getAnnotations("lang")) {
		      // Don't try to translate a language to itself
		      if (! (langAnnotations.getValue().equals(languageTarget)
		          || langAnnotations.getValue().equals("unknown"))) {
		        annotationsToTranslate.add(langAnnotations);
		      }
		    }
		    
		    
		    for (Annotation a : annotationsToTranslate) {
		      String translatedText = "\n\nblah!" + doc.getText(a.getRange()) + "blah!";
		      int annotationStart = a.getRange().getEnd();
		      doc.insert(annotationStart, translatedText);
		      doc.setAnnotation(new Range(annotationStart, annotationStart + translatedText.length()), myAnnotationName, languageTarget);
		    }
		    
		    
		    
		    for (Annotation a : doc.getAnnotations()) {
		      myDoc.append(a + "\n");
		    }
		    
		    
		    myDoc.append("Text to translate:\n");
		    for (Annotation a : annotationsToTranslate) {
		      myDoc.append("\"" + doc.getText(a.getRange()) + "\"" + "\n\n");
		    }
		    
		  }
		}
	}
	
	private Map<Annotation, Annotation> getLangInputToMyAnnotations(List<Annotation> langAnnotations, List<Annotation> myAnnotations) {
	  Map<Annotation, Annotation> result = new HashMap<Annotation, Annotation>();
	  
	  if (langAnnotations.size() == 0) {
	    return result;
	  }
	  
	  for (int i = 0; i < langAnnotations.size() - 1; i++) {
	    int thisStart = langAnnotations.get(i).getRange().getStart();
	    int nextStart = langAnnotations.get(i+1).getRange().getStart();
	    
	    for (Annotation myAnnotation : myAnnotations) {
	      int myStart = myAnnotation.getRange().getStart();
	      
	      if (myStart < thisStart) {
	        continue;
	      } else if (myStart > thisStart && myStart < nextStart) {
	        result.put(langAnnotations.get(i), myAnnotation);
	      } else {
	        break;
	      }
	    }
	    
	    Annotation lastLangAnnotation = langAnnotations.get(langAnnotations.size() - 1);
	    int lastStart = lastLangAnnotation.getRange().getStart();
	    
	    for (Annotation myAnnotation : myAnnotations) {
	      if (lastStart > myAnnotation.getRange().getStart()) {
	        result.put(lastLangAnnotation, myAnnotation);
	        break;
	      }
	    }
	  }
	  
	  return result;
	}

	@Override
	public void registerForEvents() {
		// TODO Auto-generated method stub

	}

}
