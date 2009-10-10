package org.andrewhitchcock.auntrosie;

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
		    String languageTarget = doc.getText(new Range(location, location + 2));
		    
		    Blip myBlip;
		    List<Blip> blips = blip.getChildren();
		    if (blips.isEmpty()) {
		      myBlip = blip.createChild();
		    } else {
		      myBlip = blips.get(0);
		    }
		    
		    TextView myDoc = myBlip.getDocument();
		    myDoc.delete();
		    
		    for (Annotation a : doc.getAnnotations()) {
		      myDoc.append(a.toString() + "\n");
		    }
		    myDoc.append("\n");
		    myDoc.append("languageTarget: " + languageTarget);
		    myDoc.append("\n");
		    myDoc.append(doc.getText());
		  }
		}
	}

	@Override
	public void registerForEvents() {
		// TODO Auto-generated method stub

	}

}
