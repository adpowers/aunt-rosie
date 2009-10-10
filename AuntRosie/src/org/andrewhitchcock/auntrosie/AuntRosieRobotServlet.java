package org.andrewhitchcock.auntrosie;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.wave.api.AbstractRobot;
import com.google.wave.api.Annotation;
import com.google.wave.api.Blip;
import com.google.wave.api.Event;
import com.google.wave.api.EventType;
import com.google.wave.api.RobotMessageBundle;
import com.google.wave.api.TextView;

public class AuntRosieRobotServlet extends AbstractRobot {

	private static final long serialVersionUID = -1546080376029476133L;

	private static final Pattern translatePattern = Pattern.compile("/translate:([A-Za-z-]{2,5})");
	
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
		    
		    Matcher matcher = translatePattern.matcher(doc.getText());
		    if (!matcher.matches()) {
		      continue; // nothing to translate
		    }
		    String languageTarget = matcher.group(1);
		    
		    List<Annotation> annotationsToTranslate = new ArrayList<Annotation>();
		    for (Annotation langAnnotations : doc.getAnnotations("lang")) {
		      // Don't try to translate a language to itself
		      if (! (langAnnotations.getValue().equals(languageTarget)
		          || langAnnotations.getValue().equals("unknown"))) {
		        annotationsToTranslate.add(langAnnotations);
		      }
		    }

        Blip myBlip = null;
        for (Blip child : blip.getChildren()) {
          if (child.getCreator().equals("aunt-rosie@appspot.com")) {
            myBlip = child;
            break;
          }
        }
        if (myBlip == null) {
          myBlip = blip.createChild();
        }
        
        TextView myDoc = myBlip.getDocument();
        myDoc.delete();
        
        myDoc.append(languageTarget + "\n");
        myDoc.append(doc.getAnnotations().size() + "\n");
		    for (Annotation a : doc.getAnnotations()) {
		      myDoc.append(a + "\n");
		    }
		    myDoc.append(doc.getText() + "\n");
		    for (Annotation a : annotationsToTranslate) {
		      String translation = translateText(a.getValue(), languageTarget, doc.getText(a.getRange()));
    		  myDoc.append(translation);
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
