class MonographBlocksGrailsPlugin {
	// the plugin version
	def version = "0.1"
	// the version or versions of Grails the plugin is designed for
	def grailsVersion = "1.3.6 > *"
	// the other plugins this plugin depends on
	def dependsOn = [
		monographApi:"0.1 > *"
	]    // resources that are excluded from plugin packaging
	def pluginExcludes = [
		"grails-app/views/error.gsp"
	]

	// TODO Fill in these fields
	def author = "Stefan Marte"
	def authorEmail = ""
	def title = "Plugin summary/headline"
	def description = '''\\
Brief description of the plugin.
'''

	// URL to the plugin's documentation
	def documentation = "http://grails.org/plugin/monograph-blocks"

	def watchedResources = [
		"file:./grails-app/jsonFilter/**/*JsonFilter.groovy",
		"file:../../plugins/*/jsonFilter/**/*JsonFilter.groovy"
	]

	def doWithWebDescriptor = { xml ->
		// TODO Implement additions to web.xml (optional), this event occurs before
	}

	def doWithSpring = {
		application.jsonFilterClasses.each { GrailsClass cls ->
			"${cls.propertyName}"(cls.getClazz()) { bean ->
				bean.autowire = true
				bean.lazyInit = true
			}
		}
	}

	def doWithDynamicMethods = { ctx ->
		// TODO Implement registering dynamic methods to classes (optional)
	}

	def doWithApplicationContext = { applicationContext ->
		// TODO Implement post initialization spring config (optional)
	}

	def onChange = { event ->
		if (event.source) {
			println "Reload for JsonFilter triggered"
			Class changed = event.source
			GrailsClass cls = application.addArtefact(changed)
			def newBeans = beans {
				"${cls.propertyName}"(cls.getClazz()) { bean ->
					bean.autowire = true
					bean.lazyInit = true
				}
			}
			newBeans.registerBeans(applicationContext)
		}
	}

	def onConfigChange = { event ->
		// TODO Implement code that is executed when the project configuration changes.
		// The event is the same as for 'onChange'.
	}
}
