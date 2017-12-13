package sx.server.integracion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.cxp.AplicacionCxP
import sx.cxp.AplicacionDePago
import sx.tesoreria.MovimientoDeCuenta
import sx.tesoreria.PagoDeRequisicion
import sx.tesoreria.Requisicion

/**
 * Created by rcancino on 04/11/16.
 */
@Component
class ImportadorDePagosCxP implements Importador, SW2Lookup {

    @Autowired
    ImportadorDeRequisiciones importadorDeRequisiciones

    @Autowired
    ImportadorDeMovimientosDeCuenta importadorDeMovimientosDeCuenta


    def importar(Long requisicionId){
        def pago = PagoDeRequisicion.where {requisicion.sw2 == requisicionId}.find()
        if(pago){
            logger.info("Pago de la requisicion ${requisicionId} ya importado")
            return null
        }

        Requisicion requisicion  = Requisicion.where {sw2 == requisicionId}.find()
        if(!requisicion){
            requisicion = importadorDeRequisiciones.importar(requisicionId)
        }
        String select = QUERY + " where a.requisicion_id = ? "
        def row = findRegistro(select, [requisicionId])
        build(row, requisicion)
    }


    def build(def row, Requisicion requisicion){
        logger.info('Importando pago: ' + row)
        MovimientoDeCuenta egreso = MovimientoDeCuenta.where{ sw2 == row.cargoabono_id}.find()
        if(!egreso){
            egreso = importadorDeMovimientosDeCuenta.importar(row.cargoabono_id)
        }
        PagoDeRequisicion pago = new PagoDeRequisicion()
        pago.proveedor = requisicion.proveedor
        bindData(pago,row)
        pago.egreso = egreso
        pago.requisicion = requisicion
        importarAplicaciones(pago)
        pago = pago.save failOnError:true, flush:true
        return pago
    }

    def importarAplicaciones(PagoDeRequisicion pago){
        List partidas = leerRegistros(APLICACIONES,[pago.sw2])
        partidas.each{ row ->
            AplicacionDePago aplicacion = new AplicacionDePago()
            aplicacion.abono = pago
            aplicacion.cxp = buscarCuentaPorPagar(row.cargo_id)
            bindData(aplicacion,row)
            pago.addToAplicaciones(aplicacion)
        }
    }

    static String QUERY = """
        select 
            a.documento as documento,
            a.fecha,
            a.total,
            b.cargoabono_id,
            a.comentario,
            a.cxp_id as sw2
            from sx_cxp a 
            join sw_trequisicion b on(a.requisicion_id = b.requisicion_id)

    """

    static String APLICACIONES = """
        select 
            aplicacion_id as sw2,
            comentario,
            fecha,
            importe,
            cargo_id
        from sx_cxp_aplicaciones where abono_id = ?
    """

    
}
