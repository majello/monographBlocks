package monographBlocks

import org.codehaus.groovy.grails.commons.*

class JsonFilterArtefactHandler extends ArtefactHandlerAdapter {
	// the name for these artefacts in the application
	static public final String TYPE = "JsonFilter";

	public JsonFilterArtefactHandler() {
		super(TYPE, GrailsClass, DefaultGrailsClass, TYPE);
	}
}
