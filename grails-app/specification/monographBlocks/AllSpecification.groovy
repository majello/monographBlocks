package bulldogBlocks

import org.codehaus.groovy.grails.commons.ApplicationHolder as holder
import grails.util.GrailsNameUtils
import org.springframework.beans.factory.InitializingBean

class AllSpecification  {

	def get(entity,baseEntity,propertyName) {
		Set pl = []
		// log.info "All Spec: ${entity}"
		def entityName = (entity instanceof String)?entity:org.hibernate.Hibernate.getClass(entity).simpleName
		def dom = holder.application.domainClasses.find { it.name == entityName }
		if (dom) {
			pl += dom.properties.findAll{
				!propertyName || !(it.isOneToMany() || it.isManyToMany())
			}.collect { it.name }
			if (!propertyName && pl.contains("statusCode"))
				pl << "statusCode.successor" 
		} else {
			log.info "Could not find domainclass ${entityName}"
		}
		// log.info "Api: ${entity}/${baseEntity}.${propertyName}: ${pl}"
		return pl
	}

}