package monographBlocks

import grails.converters.*
import org.codehaus.groovy.grails.web.sitemesh.*
import org.codehaus.groovy.grails.web.util.*
import org.codehaus.groovy.grails.web.pages.*
import javax.servlet.*

class OperationController {

	def operationService
	def blocksTemplateService

	def index = {
		// list available entities
		def elist = operationService.all()
		[entities:elist]
	}

	def listOperations = {
		// list available operations for an entity
		def entity = params.entity
	}

	def listBlueprints = {

	}

	def test = {
		log.info "${params}"
		render params
	}

	def resolveJson = {
		def op = operationService.resolve(params.entity,params.operation)
		if (op) {
			params.view = params.view?:"all"
			def rp = params
			params.outFormat?.tokenize("-").each {
				/*				def parser = operationService.resolveJsonParameter(params.entity,params.outFormat)
				 if (parser) rp = parser.method.invoke(parser.object,params) */
				def parser = operationService.getJsonFilter(it)
				if (parser) rp = parser.parameters(params)
			}
			def model = op.method.invoke(op.object,rp)
			def lastFormat
			rp.outFormat?.tokenize("-").each {
				def parser = operationService.getJsonFilter(it)
				if (parser) model = parser.result(model,params)
				/*				def parser = operationService.resolveJson(params.entity,rp.outFormat)
				 if (parser) model = parser.method.invoke(parser.object,[model:model,params:rp])
				 */
			}
			log.info "json request: ${rp}"
			if (lastFormat == "html")
				[json:model,entity:rp.entity,operation:rp.operation]
			else
				render model as JSON
		} else
			response.sendError(response.SC_NOT_FOUND,"Operation '${operation}' for entity '${entity}' is not available.")
	}

	def resolveFragment = {
		def defaultBlueprint = params.defaultBlueprint?:"html"
		// we resolve an operation against the api and chain to the appropriate view
		def blueprint = operationService.getBlueprint(params.entity,params.blueprint?:"${params.operation}",defaultBlueprint)
		def block = params.block
		def properties=params.properties
		def op = operationService.resolve(params.entity,params.operation)
		if (op) {
			params.view = (params.view?:blueprint.metaView)?:"api"
			def model = op.method.invoke(op.object,params)
			if (block) {
				render g.renderBlueprint(blueprint:blueprint,model:model) {
					g.renderBlocks(blueprint:blueprint,blocks:[block],properties:properties)
				}
			} else {
				if (blueprint.pageLayout) {
					render applyLayout(name:blueprint.pageLayout,template:"/operation/${blueprint.pageTemplate}",model:[content:model,blueprint:blueprint])
				} else
					render(template:"${blueprint.pageTemplate}",model:[content:model,blueprint:blueprint])
			}
		} else
			response.sendError(response.SC_NOT_FOUND,"Operation '${operation}' for entity '${entity}' is not available.")
	}

	def resolveHtml = {
		def defaultBlueprint = params.defaultBlueprint?:"html"
		// we resolve an operation against the api and chain to the appropriate view
		def blueprint = operationService.getBlueprint(params.entity,params.blueprint?:"${params.operation}",defaultBlueprint)
		def op = operationService.resolve(params.entity,params.operation)
		if (op) {
			params.view = (params.view?:blueprint.metaView)?:"api"
			def model = op.method.invoke(op.object,params)
			if (blueprint.pageLayout) {
				render applyLayout(name:blueprint.pageLayout,template:"/operation/${blueprint.pageTemplate}",model:[content:model,blueprint:blueprint])
			} else
				render(template:"${blueprint.pageTemplate}",model:[content:model,blueprint:blueprint])
		} else
			response.sendError(response.SC_NOT_FOUND,"Operation '${operation}' for entity '${entity}' is not available.")
	}

}
