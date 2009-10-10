package org.andrewhitchcock.auntrosie;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONTokener;

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
		    
		    List<Annotation> annotationsToTranslate = new ArrayList<Annotation>();
		    for (Annotation langAnnotations : doc.getAnnotations("lang")) {
		      // Don't try to translate a language to itself
		      if (! (langAnnotations.getValue().equals(languageTarget)
		          || langAnnotations.getValue().equals("unknown"))) {
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
		    
		    for (Annotation a : annotationsToTranslate) {
    		  myDoc.append(translateText(a.getValue(), languageTarget, doc.getText(a.getRange())));
			  }		    
		  }
		}
	}
	
	private String translateText(String from, String to, String text) {
	  try {
	    String encoded = URLEncoder.encode(text, "UTF-8");
	    URL url = new URI("http://ajax.googleapis.com/ajax/services/language/translate?langpair=" + from + "%7C" + to + "&v=1.0&q=" + encoded).toURL();
	    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
	    
	    StringBuilder sb = new StringBuilder();
	    String line;
	    while ((line = reader.readLine()) != null) {
	      sb.append(line);
	    }
	    
	    JSONObject json = new JSONObject(new JSONTokener(sb.toString()));
	    return json.getJSONObject("responseData").getString("translatedText");
	  } catch (Exception e) {
	    return e.toString();
	  }
	}

	@Override
	public void registerForEvents() {
		// TODO Auto-generated method stub

	}

}
