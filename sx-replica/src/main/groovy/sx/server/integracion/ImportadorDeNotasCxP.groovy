package sx.server.integracion

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.cxp.AplicacionCxP
import sx.cxp.AplicacionDeNota
import sx.cxp.CuentaPorPagar
import sx.cxp.NotaCxP

/**
 * Created by rcancino on 04/11/16.
 */
@Component
class ImportadorDeNotasCxP implements Importador, SW2Lookup {

    @Autowired
    ImportadorDeCuentasPorPagar importadorDeCxP

    def importar(Long sw2){
        def nota = NotaCxP.where {sw2 == sw2}.find()
        if(nota){
            logger.info("La nota de credito (CXP) con sw2: ${requisicionId} ya existe")
            return null
        }
        String select = QUERY + " where cxp_id = ? "
        def row = findRegistro(select, [sw2])
        build(row)
    }


    def build(def row){
        logger.info('Importando nota de credito (CXP): ' + row)

        NotaCxP nota = new NotaCxP()
        bindData(nota,row)
        nota.proveedor = buscarProveedor(row.proveedor_id)
        importarAplicaciones(nota)
        nota = nota.save failOnError:true, flush:true
        return nota
    }

    def importarAplicaciones(NotaCxP nota){
        List partidas = leerRegistros(APLICACIONES,[nota.sw2])
        partidas.each{ row ->
            AplicacionDeNota aplicacion = new AplicacionDeNota()
            aplicacion.abono = nota
            CuentaPorPagar cxp  = CuentaPorPagar.where{sw2 == row.cargo_id}.find()
            if(!cxp){
                cxp = importadorDeCxP.importar(row.cargo_id);
            }
            aplicacion.cxp = cxp
            bindData(aplicacion,row)
            nota.addToAplicaciones(aplicacion)
        }
    }



    static String QUERY = """
        select
            fecha, 
            documento,
            proveedor_id,
            concepto_nota as concepto,
            moneda,
            tc as tipoDeCambio,
            importe,
            impuesto,
            total,
            cxp_id as sw2
        from sx_cxp 
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
