package sx.cxc

import com.luxsoft.utils.Periodo
import grails.compiler.GrailsCompileStatic
import grails.rest.RestfulController

@GrailsCompileStatic
class ComisionController extends RestfulController<Comision>{

    static responseFormats = ['json']

    ComisionController(){
        super(Comision);
    }

    @Override
    protected List<Comision> listAllResources(Map params) {
        println 'Buscando comisiones con: ' + params
        if(params.fechaInicial) {
            Periodo periodo = new Periodo()
            periodo.properties = params
            return Comision.where{ fechaIni == periodo.fechaInicial && fechaFin == periodo.fechaFinal}.list([max: 100])
        }
        return super.listAllResources(params)
    }
}
