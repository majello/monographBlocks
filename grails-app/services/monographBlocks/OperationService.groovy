package monographBlocks

import org.codehaus.groovy.grails.commons.ApplicationHolder
import grails.util.GrailsNameUtils
import bulldogApi.*

class OperationService {
	
	static transactional = true
	
	def apiService
	def metaService
	def grailsApplication
	
	def defaultService = "GenericOperation"
	
	def all(pattern='.*') {
		def r = [:]
		apiService.all().findAll {
			(it ==~ pattern) 
		}.each {
			r += [(it):list(it)] 
		}
		return r
	}
	
	def list(entity) {
		def r = []
		r += metaService.hasService(entity)?.metaClass?.methods.findAll {
			it.name.endsWith "Op"
		}.collect {
			it.name - "Op"
		}
		r += metaService.hasService("${apiService.defaultService}Operation")?.metaClass?.methods.findAll {
			it.name.endsWith "Op"
		}.collect {
			it.name - "Op"
		} 
		return r
	}
	
	def resolve(entity, operation) {
		def mthd = metaService.getMethod(entity,operation+"Op")
		if (!mthd)
			mthd = metaService.getMethod("${apiService.defaultService}",operation+"Op")
		if (!mthd)
			mthd = metaService.getMethod(defaultService,operation+"Op")
		return mthd
	}
	
	def getJsonFilter(outFormat) {
		return grailsApplication.mainContext.getBean(outFormat+"JsonFilter")
	}
	
	def resolveJsonParameter(entity,outFormat) {
		def svc 
		def mthd
		svc = metaService.hasService(entity)
		mthd = metaService.getMethod(entity,outFormat+"JsonParams")
		if (!mthd) {
			grailsApplication.serviceClasses.each { sc ->
				if (!mthd && sc.name.endsWith("Json")) {
					mthd = metaService.getMethod(sc.name,outFormat+"JsonParams")
				}
			}
		}
		if (!mthd) mthd = metaService.getMethod("${apiService.defaultService}",outFormat+"JsonParams")
		if (!mthd) mthd = metaService.getMethod(defaultService,outFormat+"JsonParams")
		return mthd
	}
	
	def resolveJson(entity,outFormat) {
		def svc 
		def mthd
		svc = metaService.hasService(entity)
		mthd = metaService.getMethod(entity,outFormat+"Json")?:metaService.getMethod(entity,"defaultJson")
		if (!mthd) {
			grailsApplication.serviceClasses.each { sc ->
				if (!mthd && sc.name.endsWith("Json")) {
					mthd = metaService.getMethod(sc.name,outFormat+"Json")
				}
			}
		}
		if (!mthd) mthd = metaService.getMethod("${apiService.defaultService}",outFormat+"Json")?:metaService.getMethod("${apiService.defaultService}","defaultJson")
		if (!mthd) mthd = metaService.getMethod(defaultService,outFormat+"Json")?:metaService.getMethod(defaultService,"defaultJson")
		return mthd
	}
	
	def getBlueprint(entity,name,dflt) {
		def bp
		def svc = metaService.hasService(entity)
		if (svc && svc.hasProperty("${name}Blueprint")) bp = svc."${name}Blueprint"
		svc = metaService.hasService(apiService.defaultService)
		if (!bp && svc.hasProperty("${name}Blueprint")) bp = svc."${name}Blueprint"
		def config = grailsApplication.config.blueprints[entity]
		if (!bp) bp = config[name]
		config = grailsApplication.config.sharedBlueprints
		if (!bp) bp = config[name]
		if (!bp) bp = config[dflt]
		if (!bp) bp = [:]
		// check for defaultsettings
		bp.pageLayout = bp.pageLayout?:"${dflt}"
		bp.pageTemplate = bp.pageTemplate?:"${dflt}Page"
		bp.listLayout = bp.listLayout?:"${dflt}List"
		bp.listElementLayout = bp.listElementLayout?:"${dflt}ListElement"
		bp.pageLayout = bp.pageLayout?:"${dflt}PageLayout"
		bp.metaView = bp.metaView?:"api"
		bp.blocks = bp.blocks?:[]
		bp.name = name
		// bp.repeat = bp.repeat?:true
		
		return bp
	}
	
	def getBlock(entity,name) {
		def bp
		def svc
		if (entity) { 
			svc = metaService.hasService(entity)
			if (svc && svc.hasProperty("${name}Block")) bp = svc."${name}Block"
		}
		svc = metaService.hasService(apiService.defaultService)
		if (!bp && svc.hasProperty("${name}Block")) bp = svc."${name}Block"
		def config
		if (entity) { 
			config = grailsApplication.config.blocks[entity]
			if (!bp) bp = config[name]
		}
		config = grailsApplication.config.sharedBlocks
		if (!bp) bp = config[name]
		// check for defaultsettings
		
		return bp
	}
}
