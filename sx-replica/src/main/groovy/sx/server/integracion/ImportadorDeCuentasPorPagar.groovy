package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.stereotype.Component
import sx.core.Proveedor
import sx.cxp.CuentaPorPagar

/**
 * Created by rcancino on 28/09/16.
 */
@Component
class ImportadorDeCuentasPorPagar implements Importador, SW2Lookup{
    
    def importar(f1){
        return importar(f1,f1)
    }

    def importar(Long sw2){
        logger.info('Importando cxp ' + sw2)
        String select = QUERY + " and cxp_id = ? "
        def row = findRegistro(select, [sw2])
        build(row)
    }

    def importar(Range<Date> range){
        range.each{ dia ->
            importar(dia)
        }
    }

    def importar(Date f1 ){

        logger.info("Importando cuentas por pagar del dia: ${f1.format('dd/MM/yyyy')}" )

        List importados = []
        String select = QUERY + " where fecha = ? "

        leerRegistros(select,[f1]).each { row ->
            logger.info('Importando cxp: ' + row.sw2)
            build(row)
        }
        def message = "CuentaPorPagares importadas: ${importados.size()}"
        logger.info(message)
        return message
    }

    def build(def row){
        def cxp = CuentaPorPagar.where{ sw2 == row.sw2}.find()
        if(!cxp){
            cxp = new CuentaPorPagar()
            cxp.proveedor = buscarProveedor(row.proveedor_id)//Proveedor.where {sw2 == row.proveedor_id}.find()
        }
        bindData(cxp,row)
        try{
            cxp.save failOnError:true, flush:true
            return cxp
        }catch (Exception ex){logger.error(ExceptionUtils.getRootCauseMessage(ex))}
    }

    

    static String QUERY  = """
        select
        c.cxp_id as sw2,
        c.proveedor_id as proveedor_id,
        'COMPRAS' as tipo,
        p.rfc,
        c.documento,
        c.fecha,
        vto as vencimiento,
        c.revision,
        c.vtodf as descuentofVto,
        c.moneda,
        c.tc as tipoDeCambio,
        c.importe + c.flete as importe,
        c.impuesto + c.flete_iva as impuesto,
        c.total,
        c.flete_ret as retencionIva,
        .04 as retencionIvaTasa,
        c.descuentof ,
        c.comentario,
        c.anticipo
        from sx_cxp c
        join sx_proveedores p on (c.proveedor_id = p.proveedor_id)
        where tipo = 'FACTURA'

    """


    
}
