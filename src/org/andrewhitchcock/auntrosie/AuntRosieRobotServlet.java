package org.andrewhitchcock.auntrosie;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.wave.api.AbstractRobot;
import com.google.wave.api.Annotation;
import com.google.wave.api.Blip;
import com.google.wave.api.BlipContentRefs;
import com.google.wave.api.Element;
import com.google.wave.api.ElementType;
import com.google.wave.api.Gadget;
import com.google.wave.api.Range;
import com.google.wave.api.Restriction;
import com.google.wave.api.event.DocumentChangedEvent;
import com.google.wave.api.event.OperationErrorEvent;

public class AuntRosieRobotServlet extends AbstractRobot {
  private static final Logger log = Logger.getLogger(AuntRosieRobotServlet.class.getName());

	private static final long serialVersionUID = -1546080376029476133L;

	private static final String myAddress = "aunt-rosie@appspot.com";
	private static final String buttonUrl = "http://aunt-rosie.appspot.com/translate-button.xml";
	
	@Override
	protected String getRobotName() {
		return "Aunt Rosie";
	}
	
	@Override
	protected String getRobotAvatarUrl() {
	  return "http://aunt-rosie.appspot.com/avatar.png";
	}
	
	@Override
	protected String getRobotProfilePageUrl() {
    return "http://andrewhitchcock.org/?post=322";
  }
	
	@Override
  public void onDocumentChanged(DocumentChangedEvent event) {
    Blip blip = event.getBlip();
    
    log.info("Received request");
    
    if (blip == null) {
      return;
    }
    
    log.info("Blip ID: " + blip.getBlipId());
    
    // Skip blips I created.
    if (myAddress.equals(blip.getCreator())) {
      return;
    }
    
    if (blip.getAnnotations() == null
        || blip.getAnnotations().get("lang") == null
        || blip.getAnnotations().get("lang").isEmpty()) {
      log.info("No lang annotations, skipping");
      return;
    }
    
    // Gadget.restrictByUrl
    BlipContentRefs buttonRef = blip.first(ElementType.GADGET, Restriction.of("url", buttonUrl));
    
    if (buttonRef.value() == null) {
      blip.at(1).insert("\n");
      blip.at(1).insert(new Gadget(buttonUrl));
      log.info("Inserted new gadget.");
      return;
    }
    
    Element buttonElement = buttonRef.value().asElement();
    String languageTarget = buttonElement.getProperty("lang");
    if (languageTarget == null || languageTarget.equals("none")) {
      log.info("No language target selected");
      return;
    }
    
    List<Annotation> annotationsToTranslate = new ArrayList<Annotation>();
    for (Annotation langAnnotations : blip.getAnnotations().get("lang")) {
      // Don't try to translate a language to itself
      if (! (langAnnotations.getValue().equals(languageTarget)
          || langAnnotations.getValue().equals("unknown"))) {
        annotationsToTranslate.add(langAnnotations);
      }
    }

    Blip myBlip = null;
    for (Blip child : blip.getChildBlips()) {
      if (child.getCreator().equals(myAddress)) {
        myBlip = child;
        break;
      }
    }
    if (myBlip == null) {
      myBlip = blip.reply();
    }
    
    myBlip.all().delete();
    /*
    myDoc.append(languageTarget + "\n");
    myDoc.append(doc.getAnnotations().size() + "\n");
    for (Annotation a : doc.getAnnotations()) {
      myDoc.append(a + "\n");
    }
    myDoc.append(doc.getText() + "\n");
    */
    
    for (Annotation a : annotationsToTranslate) {
      Range r = a.getRange();
      String translation = translateText(
          a.getValue(),
          languageTarget,
          blip.range(Math.max(0, r.getStart()), r.getEnd()).value().getText());
      myBlip.append(translation);
    }
    myBlip.appendMarkup("<br /><br /><br /><i>Translations powered by <a href=\"http://translate.google.com/#\">Google Translate</a></i>");
  }
  
  private String translateText(String from, String to, String text) {
    try {
      String encoded = URLEncoder.encode(text, "UTF-8");
      String key = "ABQIAAAAe-kCKwcl6Q0CmpfGzUgerhRQ2UqWlTnfxxg_2AxnXE1oQ7HidBQiKHUD7uJx-wEVmjhu1_n1rxbw8g";
      URL url = new URI("http://ajax.googleapis.com/ajax/services/language/translate?key=" + key + "&langpair=" + from + "%7C" + to + "&v=1.0&q=" + encoded).toURL();
      BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
      
      StringBuilder sb = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      
      JSONObject json = new JSONObject(new JSONTokener(sb.toString()));
      if (json.isNull("responseData")) {
        return json.getString("responseDetails");
      } else {
        return json.getJSONObject("responseData").getString("translatedText");
      }
    } catch (Exception e) {
      return e.toString();
    }
  }
  
  @Override
  public void onOperationError(OperationErrorEvent event) {
    log.info(event.toString());
  }

}
