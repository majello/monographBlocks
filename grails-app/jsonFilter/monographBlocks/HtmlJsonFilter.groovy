package bulldogBlocks

class HtmlJsonFilter  {

	def parameters(params) {
		return params
	}
	
	def result(model,params) {
		model.each { le ->
			if (params.meta?:false == false) {
				le.element.remove('meta')
			}
			if (params.options?:false == false) {
				le.element.remove('options')
			}			
		}
		return model
	}
	
}