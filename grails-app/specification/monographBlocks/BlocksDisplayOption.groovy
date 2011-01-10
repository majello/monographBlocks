package bulldogBlocks

import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

class BlocksDisplayOption  {
	def standard(object,property) {
		log.info "${property.propertyName}"
		def config = CH.config.displayOptions[property.propertyName]
		// println "${config}"
		if (config) {
			option += config
		}
		return option
	}

}