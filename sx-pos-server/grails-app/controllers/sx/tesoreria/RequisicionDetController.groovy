package sx.tesoreria

import grails.rest.RestfulController

class RequisicionDetController extends  RestfulController{

    static responseFormats = ['json']

    RequisicionDetController(){
        super(RequisicionDet)
    }

    @Override
    protected List listAllResources(Map params) {
        def query = RequisicionDet.where {}
        if(params.requisicionId){
            query = query.where{requisicion.id == params.requisicionId}
            return query.list()
        }
        return query.list(params)
    }


}
