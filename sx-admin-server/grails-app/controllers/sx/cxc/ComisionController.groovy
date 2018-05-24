package sx.cxc

import com.luxsoft.utils.Periodo
import grails.compiler.GrailsCompileStatic
import grails.rest.RestfulController

@GrailsCompileStatic
class ComisionController extends RestfulController<Comision>{

    static responseFormats = ['json']

    ComisionService comisionService

    ComisionController(){
        super(Comision);
    }

    @Override
    protected List<Comision> listAllResources(Map params) {
        if(params.fechaInicial) {
            Periodo periodo = new Periodo()
            periodo.properties = params
            return Comision.where{ fechaIni == periodo.fechaInicial && fechaFin == periodo.fechaFinal}.list()
        }
        return super.listAllResources(params)
    }

    def generarComisiones(GenerarComisionesCommand command) {
        List res = comisionService.generarComisionesCobrador(command.fechaInicial, command.fechaFinal)
        respond res

    }
}

class GenerarComisionesCommand {
    Date fechaInicial
    Date fechaFinal
    String tipo
}
