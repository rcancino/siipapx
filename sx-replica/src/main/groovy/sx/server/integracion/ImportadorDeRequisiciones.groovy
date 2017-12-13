package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.cxp.CuentaPorPagar
import sx.tesoreria.Requisicion
import sx.tesoreria.RequisicionDet

/**
 * Created by rcancino on 27/09/16.
 */
@Component
class ImportadorDeRequisiciones implements Importador, SW2Lookup{

    @Autowired
    ImportadorDeCuentasPorPagar importadorDeCxP

    def importar(f1){
        return importar(f1,f1)
    }

    def importar(Long sw2){
        logger.info('Importando requisicion ' + sw2)
        String select = QUERY + " where requisicion_id = ? "
        def row = findRegistro(select, [sw2])
        build(row)
    }

    def importar(Date f1 , Date f2){

        logger.info("Importando requisiciones del ${f1.format('dd/MM/yyyy')} al ${f2.format('dd/MM/yyyy')}" )

        List importados = []
        String select = QUERY + " where fecha between date(?) and date(?) and origen in (?,?)"

        def registros=  leerRegistros(select,[f1,f2,'COMPRAS','GASTOS'])

            registros.each { row ->
            println("Buscando los queryssssss")
            logger.info('Importando requisicion: ' + row.sw2)
            build(row)
        }
        def message = "Requisiciones importadas: ${importados.size()}"
        logger.info(message)
        return message
    }

    def build(def row){

        println "Estoy importando desde el build"
        def requisicion = Requisicion.where{ sw2 == row.sw2}.find()
        if(!requisicion){
            requisicion = new Requisicion()
        }
        try{
            bindData(requisicion,row)
            requisicion.proveedor = buscarProveedor(row.proveedor_id)
            importarPartidas(requisicion,row.sw2)
            requisicion.save failOnError:true, flush:true
        }catch (Exception ex) {
            String msg = ExceptionUtils.getRootCauseMessage(ex)

            logger.error("Error importando requisicion ${row.sw2} error: ${msg}")
        }

        return requisicion
    }



    def importarPartidas(Requisicion requisicion,sw2){

        println "Estoy importando las partidas"

        if(requisicion.partidas){

            println ">>>>Partidas: >>>>>>>>>"+requisicion.partidas.size()

            requisicion.partidas.clear()
        }



        List partidas = leerRegistros(QUERY_PARTIDAS,[sw2])
        println "<<<<<<Partidas: <<<<<<<<<<  " +partidas.size()
        def a =0
        partidas.each{ row ->

            a=a+1
            logger.info("Importando partida: " + row  +" Count >>>>  " +a)

            RequisicionDet det = new  RequisicionDet()

            CuentaPorPagar cxp  = CuentaPorPagar.where{sw2 == row.cxp_id}.find()
            if(!cxp){
                cxp = importadorDeCxP.importar(row.cxp_id);
            }
            det.cxp = cxp
            det.aPagar =  row.total
            det.comentario = row.comentario
            requisicion.addToPartidas(det)
        }
    }

    static String QUERY  = """
        select
        p.proveedor_id,
        r.afavor,
        r.origen as tipo,
        r.moneda,
        if(FORMADEPAGO = 0,'TRANSFERENCIA','CHEQUE')  as formaDePago ,
        r.tc as tipoDeCambio,
        r.total,
        r.comentario,
        r.fecha,
        r.fechaDePago,
        r.requisicion_id as sw2
        from sw_trequisicion r  join sx_proveedores p on(r.proveedor_id = p.proveedor_id)

    """
    static String QUERY_PARTIDAS ="""
        select
            cxp_id,
            total,
            comentario
            from sw_trequisiciondet  
            where requisicion_id = ?
    """
}
