package monographBlocks

class GenericOperationService {
	
	static transactional = true
	
	def apiService
	def grailsApplication
	
	def createOp(params) {
		log.info "creating ${params.entity}"
		apiService.callbackService(params.entity,"createOp","before",params)
		def model = []
		def item = apiService.definition(params.entity,"${params.view}")
		log.info "${item}"
		model << item
		apiService.callbackService(params.entity,"createOp","after",[params:params,model:model])
		return model
	}
	
	def saveOp(params) {
		log.info "saveing ${params.id}"
		apiService.callbackService(params.entity,"saveOp","before",params)
		def model = []
		def ids = [params.id] + params.list('ids')
		ids?.each { id ->
			def item = apiService.create(params.entity,"${params.view}",true,params)
			if (item)
				model << item
		}
		apiService.callbackService(params.entity,"saveOp","after",[params:params,model:model])
		return model
	}
	
	def updateOp(params) {
		log.info "updateing ${params.id}"
		apiService.callbackService(params.entity,"updateOp","before",params)
		def model = []
		def ids = [params.id] + params.list('ids')
		ids?.each { id ->
			def item = apiService.update(params.entity,"${params.view}",true,id,params)
			if (item)
				model << item
		}
		apiService.callbackService(params.entity,"updateOp","after",[params:params,model:model])
		return model
	}
	
	def validateOp(params) {
		log.info "validating ${params.id}"
		apiService.callbackService(params.entity,"validateOp","before",params)
		def model = []
		def ids = [params.id] + params.list('ids')
		ids?.each { id ->
			def item = apiService.validate(params.entity,"${params.view}",true,id,params)
			if (item)
				model << item
		}
		apiService.callbackService(params.entity,"validateOp","after",[params:params,model:model])
		return model
	}
	
	def getOp(params) {
		log.info "getting ${params.id}"
		apiService.callbackService(params.entity,"getOp","before",params)
		def model = []
		def ids = [params.id] + params.list('ids')
		ids?.each { id ->
			def item = apiService.get(params.entity,"${params.view}",true,id)
			if (item)
				model << item
		}
		apiService.callbackService(params.entity,"getOp","after",[params:params,model:model])
		return model
	}
	
	def listOp(params) {
		log.info "listing ${params.view}"
		apiService.callbackService(params.entity,"listOp","before",params)
		def model = apiService.list(params.entity,"${params.view}",true,params)
		apiService.callbackService(params.entity,"listOp","after",[params:params,model:model])
		return model
	}
	
}
