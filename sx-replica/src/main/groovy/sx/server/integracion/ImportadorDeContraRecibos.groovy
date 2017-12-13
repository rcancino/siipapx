package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.cxp.ContraRecibo


/**
 * Created by rcancino on 04/11/16.
 */
@Component
class ImportadorDeContraRecibos implements Importador, SW2Lookup {

    @Autowired
    ImportadorDeCuentasPorPagar importadorDeCuentasPorPagar

    def importar(Date ini, Date fin){
        (ini..fin).each{
            importar(it)
        }
    }
    def importar(fecha){
        logger.info("Importando contra recibos del : ${fecha.format('dd/MM/yyyy')}" )
        def ids = leerRegistros("select recibo_id from SX_CXP_RECIBOS where fecha = ? ",[fecha.format('yyyy-MM-dd')])
        logger.info('Registros: ' + ids.size())
        ids.each { r ->
            importar(r.recibo_id)
        }
    }

    def importar(Long sw2){
        logger.info('Importando contrare recibo ' + sw2)
        String select = QUERY + " where recibo_id = ? "
        def row = findRegistro(select, [sw2])
        build(row)
    }


    def build(def row){
        def recibo = ContraRecibo.where{ sw2 == row.sw2}.find()
        if(!recibo){
            try{
                recibo = new ContraRecibo()
                bindData(recibo,row)
                recibo.proveedor = buscarProveedor(row.proveedor_id)
                recibo = recibo.save failOnError:true, flush:true
                return recibo
            }catch (Exception ex){
                logger.error(ExceptionUtils.getRootCauseMessage(ex))
            }
        }

    }




    static String QUERY = """
        select
            c.recibo_id as sw2,
            c.fecha,
            c.total,
            c.proveedor_id,
            c.comentario,
            c.recibo_id as folio
            from SX_CXP_RECIBOS c
    """


}
