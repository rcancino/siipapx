package sx.cxc

import grails.rest.RestfulController
import grails.transaction.Transactional

import static org.springframework.http.HttpStatus.NO_CONTENT

class NotaDeCreditoDetController extends RestfulController<NotaDeCreditoDet>{

    static responseFormats = ['json']

    NotaDeCreditoDetController(){
        super(NotaDeCreditoDet)
    }

    @Override
    protected List<NotaDeCreditoDet> listAllResources(Map params) {
        def query = NotaDeCreditoDet.where {}
        if(params.notaDeCreditoId){
            query = query.where{nota.id == params.notaDeCreditoId}
            return query.list()
        }
        return query.list(params)
    }

    def delete(NotaDeCreditoDet det){
        if (det == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        NotaDeCredito nota = NotaDeCredito.where {id == det.nota.id}.find([fetch:[partidas:"join"]])
        log.info 'Eliminando concepto de nota ' + det + ' Partidas: '+ nota.partidas.size()
        nota.removeFromPartidas(det)
        nota.actualizarImportes()

        nota.save flush:true
        render status: NO_CONTENT

    }
}
