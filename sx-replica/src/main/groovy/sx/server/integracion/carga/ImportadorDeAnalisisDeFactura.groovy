package sx.server.integracion.carga

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.compras.RecepcionDeCompraDet
import sx.cxp.AnalisisDeFactura
import sx.cxp.AnalisisDeFacturaDet
import sx.cxp.CuentaPorPagar
import sx.server.integracion.Importador
import sx.server.integracion.ImportadorDeCuentasPorPagar
import sx.server.integracion.ImportadorDeRecepcionDeCompras
import sx.server.integracion.SW2Lookup

/**
 * Created by rcancino on 04/11/16.
 */
@Component
class ImportadorDeAnalisisDeFactura implements Importador, SW2Lookup {

    @Autowired
    ImportadorDeCuentasPorPagar importadorDeCuentasPorPagar

    @Autowired
    ImportadorDeRecepcionDeCompras importadorDeRecepcionDeCompras

    def importar(fecha){
        logger.info("Importando analisis de facturas del : ${fecha.format('dd/MM/yyyy')}" )
        def ids = leerRegistros("select analisis_id from SX_ANALISIS where date(fecha) = ? ",[fecha.format('yyyy-MM-dd')])
        logger.info('Registros: ' + ids.size())
        ids.each { r ->
            importar(r.analisis_id)
        }
    }

    def importar(Long sw2){
        logger.info('Importando analisis de factura  ' + sw2)
        String select = QUERY + " where analisis_id = ? "
        def row = findRegistro(select, [sw2])
        build(row)
    }


    def build(def row){
        def analisis = AnalisisDeFactura.where{ sw2 == row.sw2}.find()
        if(!analisis){


            analisis = new AnalisisDeFactura()
            bindData(analisis,row)
            analisis.factura = buscarFactura(row.cxp_id)
            importarPartidas(analisis)
            analisis = analisis.save failOnError:true, flush:true
            return analisis

            try{

            }catch (Exception ex){
                logger.error(ExceptionUtils.getRootCauseMessage(ex))
            }
        }

    }

    def importarPartidas(AnalisisDeFactura analisis){
        List partidas = leerRegistros(QUERY_PARTIDAS,[analisis.sw2])
        partidas.each{ row ->
            AnalisisDeFacturaDet det = new  AnalisisDeFacturaDet()
            det.entrada = buscarEntrada(row.entrada_id)
            bindData(det,row)
            analisis.addToPartidas(det)
        }
    }

    CuentaPorPagar buscarFactura(def siipapId){
        def factura = CuentaPorPagar.where {sw2 == siipapId}.find()
        if(!factura){
            factura = importadorDeCuentasPorPagar.importar(siipapId)
        }
        assert factura, "No existe la factura con ID en SW2  $siipapId es probable que no se ha importado"
        return factura;
    }

    RecepcionDeCompraDet buscarEntrada(def siipapId) {
        RecepcionDeCompraDet entrada = RecepcionDeCompraDet.where {sw2 == siipapId}.find()
        if(!entrada) {
            // Buscar la recepcion de compra
            def row = findRegistro("select recepcion_id from SX_INVENTARIO_COM where inventario_id = ? ", [siipapId])
            //throw new RuntimeException("No se ha importado la recepcion de compra: " + row.recepcion_id)
            importadorDeRecepcionDeCompras.importar(row.recepcion_id)
            entrada = RecepcionDeCompraDet.where {sw2 == siipapId}.find()
            assert entrada, "No existe recepcionDeCompraDet   $siipapId y la recepcion de compra $recepcionId es probable que no se ha importado"
        }

        return entrada
    }



    static String QUERY = """
        select
            a.analisis_id as sw2,
            a.cxp_id,
            a.fecha,
            a.importe,
            a.comentario
        from SX_ANALISIS a
    """

    static String QUERY_PARTIDAS = """
        select
            a.id as sw2,
            a.entrada_id,
            a.cantidad,
            a.precio as precioDeLista,
            a.desc1,
            a.desc2,
            a.desc3,
            a.desc4,
            a.costo as costoUnitario,
            a.importe
        from SX_ANALISISDET a
        where a.analisis_id = ?
    """
}
