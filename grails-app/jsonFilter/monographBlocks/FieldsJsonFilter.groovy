package bulldogBlocks

class FieldsJsonFilter  {
	
	def grailsApplication
	
	def parameters(params) {
		return params
	}
	
	def result(model,params) {
		def g = grailsApplication.mainContext.getBean('org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib')
		model.each { me ->
			me.element.html = [:]
			me.element.editor = [:]
			me.element?.data?.each { k,v ->
				// log.info "fieldFilter: ${k} -> ${v}"
				def html = g.metaField(model:me,property:k)
				me.element.html[k] = html
				def editor = g.metaEdit(model:me,property:k)
				me.element.editor[k] = editor
			}
		}
		return model
	}
}