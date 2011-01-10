package monographBlocks

import grails.util.GrailsNameUtils

class OperationTagLib {

	def operationService
	def grailsApplication

	private fieldWrapper = { attrs, action ->
		def model = attrs.model?:pageScope.variables["item"]
		def property = attrs.property
		if (model && property) {
			def meta = model?.element?.meta[property]
			def data = model?.element?.data?."${property}"
			def errors = (model?.element?.errors)?(model?.element?.errors[property]):[]
			def options = model?.element?.options?.get(property)
			def show = (options?.show)?:true
			def edit = (options?.edit != null)?options.edit:true
			return action(model:model,meta:meta,data:data,errors:errors,options:options,show:show,edit:edit)
		}
	}

	private invokeTag(style,suffix,attrs) {
		def tagMethod
		def tagName = "meta${GrailsNameUtils.getClassNameRepresentation(style)}${GrailsNameUtils.getClassNameRepresentation(suffix)}"
		grailsApplication.tagLibClasses.each { tl ->
			tl.tagNames.each { tag ->
				if (!tagMethod && tag == tagName) {
					tagMethod = tag
				}
			}
		}
		if (tagMethod) {
			out << g."${tagMethod}"(attrs)
			// out << g.metaAutocompleteEdit(attrs)
		} else {
			log.info "no tag ${tagName} for style ${style}"
		}
	}

	def propertiesFromModel(model,deep=false) {
		def p = []
		if (model instanceof List) {
			if (deep) {

			} else {
				model[0]?.element?.meta?.each { el ->
					p << el.key
				}
			}
		} else if ( model instanceof Map ) {
			model.element?.meta?.each { el ->
				p << el.key
			}
		}
		return p
	}

	def propertiesFromBlock(block,model=[:]) {
		def plist = block.properties?.list?:propertiesFromModel(model)
		// log.info "from model: ${model}"
		def p = plist - block.properties?.exclude
		if (block.properties?.excludeLists) {
			def meta = (model instanceof Map)?model?.element?.meta:model[0]?.element?.meta
			meta.each { mi ->
				if (mi.value.many == true)
					p -= mi.key
			}
		}
		return p
	}

	def getItemFromProperty = { attrs ->
		def model = attrs.model?:pageScope["item"]
		def property = attrs.property
		def var = attrs.var
		if (property && var) {
			def data = model?.element?.data?."${property}"
			pageScope."${var}" = data
		}
	}

	def getProperties = { attrs ->
		def model = attrs.model?:pageScope["item"]
		def target = attrs.var?:"properties"
		if (target && model) {
			def propertyList = propertiesFromModel(model)
			pageScope."${target}" = propertyList
		} else
			out << "<!-- Not in block -->"
	}

	def hasMetaProperty = { attrs,body ->
		def model = attrs.model?:pageScope.variables["item"]
		def property = attrs.property
		if (model?.element?.meta?."${property}" != null) {
			out << body()
		}
	}

	def metaDomains = { attrs, body ->
		def model= attrs.model?:pageScope.variables["item"]
		def deep = attrs.deep?:false
		Set domains = []
		model.each { item ->
			domains << item.domain
			if (deep) {
				item.element.meta.each { prop,el ->
					// log.info "${prop} -> ${el}"
					if (el.referencedDomain) {
						// log.info "${el.referencedDomain}"
						domains << el.referencedDomain
					}
				}
			}
		}
		domains.each { dm -> 
			pageScope.domain = dm
			out << body()
		}
	}


	def renderBlocks = { attrs,body ->
		def model= attrs.model?:pageScope["item"]
		def blueprint = attrs.blueprint?:pageScope["blueprint"]
		if ((blueprint || attrs.block) && model) {
			def blockNames = attrs.blocks?:blueprint.blocks
			blockNames.each { blockName ->
				def block = (model instanceof Map)?operationService.getBlock(model.domain,blockName):operationService.getBlock(model[0]?.domain,blockName)
				if (block) {
					def propertyList = attrs.properties?:propertiesFromBlock(block,model)
					// log.info "block properties: ${propertyList}"
					def renderSingleBlock = { properties ->
						this.pageScope.properties = properties
						this.pageScope.blockName = blockName
						def templateName = block.template?:blockName
						if (block.layout) {
							// log.info "block ${blockName} with ${block.layout} and ${properties}"
							// log.info "renderBlocks: ${model}"
							out << applyLayout(name:block.layout,template:"/operation/blocks/${templateName}", model:[item:model,blockName:blockName,block:block,properties:properties,blueprint:blueprint])
						} else {
							try {
								out << render(template:"/operation/blocks/${templateName}", model:[item:model,blockName:blockName,block:block,properties:properties,blueprint:blueprint],plugin:"bulldogBlocks")
							} catch (Exception e) {
								out << render(template:"/operation/blocks/${templateName}", model:[item:model,blockName:blockName,block:block,properties:properties,blueprint:blueprint])
							}
						}
					}
					if (block.perProperty) {
						propertyList.each { prop ->
							renderSingleBlock([prop])
						}
					} else {
						renderSingleBlock(propertyList)
					}
				}
			}
		} else
			out << "<!-- Blueprint not found in pagescope -->"
	}

	def renderBlueprint = { attrs,body ->
		def model = attrs.model?:[]
		def itemName = attrs.item?:"item"
		def blueprint = attrs.blueprint?:pageScope.variables["blueprint"]
		def itemIndexName = "${itemName}Index"
		def itemClasses = "${itemName}Classes"
		// log.info "${blueprint}"
		if (blueprint && blueprint.repeat) {
			model.eachWithIndex { item,i ->
				def first = (i == 0)?"${itemName}First":""
				def last = (i == (model.size()-1))?"${itemName}Last":""
				def cls = [first,last].join(" ").trim()
				pageScope."$itemName" = item
				pageScope."$itemIndexName" = i
				pageScope."$itemClasses" = cls
				pageScope.blueprint = blueprint
				out << "<!-- ${itemName}: ${i}-->"
				// def bodyParams = ["${itemName}":item,"${itemIndexName}":i,"${itemClasses}":cls]
				out << body()
			}
		} else {
			this.pageScope."$itemName" = model
			out << body()
		}
	}

	def metaSelector = { attrs ->
		def model = attrs.model?:pageScope.variables["item"]
		// log.info "${model.id}"
		out << "<input type=\"checkbox\" name=\"${model.domain}-${model.id}\" class=\"selector\" >"
	}

	def metaLabel = { attrs ->
		def model = attrs.model?:pageScope.variables["item"]
		def property = attrs.property
		def meta = (model instanceof Map)?(model?.element?.meta[property]):(model[0]?.element?.meta[property])
		// log.info "${property}: ${(model[0]?.element?.meta[property])}"
		if (meta) {
			def msg = meta.message
			out << g.message(code:msg.code,default:msg.default)
		} else
			out << "<!-- meta not found -->"
	}

	def metaLinkEntity = { attrs ->
		def model = attrs.model?:pageScope.variables["item"]
		def cls = attrs.classes
		if (cls instanceof String) {
			cls = cls.tokenize(" ")
		}
		if (model && model instanceof Map && model["domain"]) {
			def title = attrs.title?:model.title
			def classes = cls?cls.join(" "):""
			def link = g.createLink(controller:"operation",action:"resolveHtml",id:model.id,params:[entity:model.domain,operation:"get"])
			out << "<a href=\"${link}\" class=\"${classes}\">"
			out << title
			out << "</a>"
		}
	}

	def metaCount = { attrs ->
		fieldWrapper(attrs) { params ->
			if (params.data instanceof List) {
				out << "<span class=\"fieldCount\">${params.data?.size()}</span>"
			}
		}
	}

	def metaSimpleListField = { attrs, body ->
		def model = attrs.model?:pageScope.variables["item"]
		def property = attrs.property
		if (property) {
			def meta = model?.element?.meta[property]
			def data = model?.element?.data[property]
			// def options = model?.element?.options?.get(property)
			if (data instanceof List) {
				data.each { i ->
					// log.info "${i.title}"
					out << "<span id=\"${i.id}\" class=\"listItem\">"
					out << g.metaLinkEntity([model:i])
					out << "</span>"
				}
			}
		}
	}

	def metaEdit = { attrs ->
		def model = attrs.model?:pageScope.variables["item"]
		def property = attrs.property
		if (model && property) {
			def meta = model?.element?.meta[property]
			def data = model?.element?.data?."${property}"
			def errors = (model?.element?.errors)?(model?.element?.errors[property]):[]
			def options = model?.element?.options?.get(property)
			def errclass = (errors)?" error":""
			def edit = (options?.edit != null)?options.edit:true
			def show = (options?.show != null)?options.show:true
			def style = (options?.style != null)?options.style:null
			// log.info "${meta.type} ${options}: ${edit} ${show} ${edit && show}"
			if (edit && show) {
				if (style != null) {
					invokeTag(style,"edit",attrs)
				} else {
					// log.info "${meta}"
					if (meta.many == false && meta.referencedDomain) {
						def dataAttr = data?"value=\"${data.title}\"":""
						out << "<input type=\"text\" name=\"${property}.id\" value=\"${data?data.id:'null'}\" />"
						out << "<input type=\"text\" name=\"${property}Content\" ${dataAttr} class=\"editable${errclass}\" />"
						// out << g.metaAutocompleteEdit(attrs)
					} else if (data instanceof List) {
					} else {
						def dataAttr = data?"value=\"${data}\"":""
						if (meta?.type == "String")
							out << "<input type=\"text\" name=\"${property}\" ${dataAttr} class=\"editable${errclass}\" />"
						if (meta?.type == "boolean")
							out << g.checkBox(name:property, value:data)
					}
				}
				if (errors) {
					errors.each { err ->
						out << "${err}"
					}
				}

			}
		}
	}

	def metaLink = { attrs ->
		fieldWrapper(attrs) { params ->
			if (params.data && params.data instanceof List) {
				out << g.link(class:"fieldSearch",controller:"operation",action:"resolveHtml",params:[entity:params.meta.referencedDomain,operation:"list",search:"[[${params.meta.referencedProperty},${params.model.id}]]"]) {
					out << "?"
				}
			}
			if (params.data && params.data instanceof Map) {
				out << metaLinkEntity([model:params.data,title:'>',classes:["fieldLink"]])
			}
		}
	}

	def metaField = { attrs,body ->
		def model = attrs.model?:pageScope.variables["item"]
		def property = attrs.property
		if (property == "~") {
			// we assume you mean a title link to this
			out << metaLinkEntity([model:model])
		} else if (property) {
			def meta = model?.element?.meta[property]
			def data = model?.element?.data?."${property}"
			def errors = (model?.element?.errors)?(model?.element?.errors[property]):[]
			// log.info "${model}"
			def options = model?.element?.options?.get(property)
			def show = (options?.show)?:true
			def style = (options?.style != null)?options.style:null
			// log.info "${property}(${model.view}}: ${meta}"
			if (show) {
				if (style != null) {
					invokeTag(style,"field",attrs)
				} else {
					if (data instanceof Map) {
						// we have a link here
						out << metaLinkEntity([model:data])
					} else if (data instanceof List) {
						// log.info "${data}"
						out << metaSimpleListField(attrs,body)
					} else {
						// some other field
						out << data
						if (errors) {
							errors.each { err ->
								out << "${err}"
							}
						}
					}
				}
			}
		} else {
			// we assume you want a link to "this"
			// log.info "catchall: ${model}"
			def operation = attrs.operation?:"get"
			if (model.domain) {
				out << g.link(controller:"operation",action:"resolveHtml",id:model.id,params:[entity:model.domain,operation:operation]) {
					if (body)
						out << body()
					else
						out << model.title
				}
			}
		}
	}

	def refreshLink = { attrs,body ->
		def model = attrs.model?:pageScope["item"]
		def defEntity = (model instanceof List)?(model[0].domain):model.domain
		def entity = attrs.entity?:defEntity
		def block = attrs.block?:pageScope["blockName"]
		def operation = attrs.operation
		def property = attrs.property
		def parameter = attrs.params
		if (model && operation && property) {
			def link = linkToFragment(operation:operation,entity:entity,block:block,property:property,params:parameter)
			out << link
		}
	}

	def linkToJson = { attrs ->
		def operation = attrs.operation
		def entity = attrs.entity
		if (operation && entity) {
			def parameter = attrs.params?:[:]
			def filter = attrs.filter?[outFormat:attrs.filter]:[:]
			String link = g.createLink(controller:"operation",action:"resolveJson",params:[operation:operation,entity:entity]+parameter+filter)
			out << link
		}
	}

	def linkToFragment = { attrs ->
		def operation = attrs.operation
		def entity = attrs.entity
		if (operation && entity) {
			def blueprint  = ((attrs.blueprint?:params.blueprint)?:pageScope["blueprint"])
			def blueprintName = (blueprint instanceof String)?blueprint:blueprint.name
			def block  = attrs.block?:pageScope["blockName"]
			def prop = attrs.property?[field:attrs.property]:[:]
			def parameter = attrs.params?:[:]
			out << g.createLink(controller:"operation",action:"resolveFragment",params:[operation:operation,blueprint:blueprintName,block:block,entity:entity]+prop+parameter)
		}
	}

	def linkToOperation = { attrs ->
		def operation = attrs.operation
		def entity = attrs.entity
		if (operation && entity) {
			def blueprint  = ((attrs.blueprint?:params.blueprint)?:pageScope["blueprint"])
			def blueprintName = (blueprint instanceof String)?blueprint:blueprint.name
			def prop = attrs.property?[field:attrs.property]:[:]
			def parameter = attrs.params?:[:]
			out << g.createLink(controller:"operation",action:"resolveHtml",params:[operation:operation,blueprint:blueprintName,entity:entity]+prop+parameter)
		}
	}

}
