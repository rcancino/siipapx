package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Proveedor
import sx.cxp.ConceptoDeGasto
import sx.cxp.CuentaPorPagar
import sx.cxp.Gasto

/**
 * Created by rcancino on 28/09/16.
 */
@Component
class ImportadorDeGastos implements Importador, SW2Lookup{

    @Autowired
    ImportadorDeProveedores importadorDeProveedores;


    def importar(Long sw2){

        logger.info("Importando cuentas por pagar de gastos:  ${sw2}" )
        def row = findRegistro(QUERY,[sw2])
        return build(row)
    }

    def build(def row){
        def cxp = CuentaPorPagar.where{ sw2 == row.sw2}.find()
        if(!cxp){
            cxp = new CuentaPorPagar()
            Proveedor proveedor = Proveedor.where {sw2 == row.sw2 && tipo == 'GASTOS'}.find()
            if(!proveedor){
                proveedor = importadorDeProveedores.importar(row.proveedor_id, 'GASTOS')
            }
            cxp.proveedor = proveedor
        }
        bindData(cxp,row)
        importarGastos(cxp)
        try{
            cxp.save failOnError:true, flush:true
            return cxp
        }catch (Exception ex){logger.error(ExceptionUtils.getRootCauseMessage(ex))}
    }

    def importarGastos(CuentaPorPagar cxp){
        if(cxp.gastos) cxp.gastos.clear()
        def partidas = leerRegistros(GASTOS_QUERY,[cxp.sw2])
        partidas.each { row ->
            Gasto gasto = new Gasto()
            bindData(gasto,row)
            importarConceptos(gasto)
            cxp.addToGastos(gasto)

        }
    }

    def importarConceptos(Gasto gasto){
        if(gasto.partidas) gasto.partidas.clear()
        def partidas = leerRegistros(CONCEPTOS_QUERY,[gasto.sw2])
        partidas.each { row ->
            ConceptoDeGasto concepto = new ConceptoDeGasto()
            bindData(concepto,row)
            concepto.sucursal = buscarSucursal(row.sucursal_id)
            gasto.addToPartidas(concepto)
        }
    }

    

    static String QUERY  = """
        select 
        c.compra_id as sw2,
        p.proveedor_id,
        'GASTOS' as tipo,
        p.rfc,
        f.documento,
        f.fecha,
        f.vto as vencimiento,
        f.vto as revision,
        f.vto as descuentofVto,
        f.moneda,
        f.tc as tipoDeCambio,
        f.importe,
        f.impuesto,
        f.total,
        0.0 as retencionIva,
        0.0 as retencionIvaTasa,
        0.0 as descuentof,
        c.comentario,
        'false' as anticipo        
        from sw_facturas_gastos f
        join sw_gcompra c on(f.compra_id=c.compra_id)
        join sw_gproveedor p on (c.proveedor_id = p.proveedor_id)
        where c.compra_id = ? and c.tipo = 'NORMAL'
    """

    static String GASTOS_QUERY = """ 
        SELECT 
            g.gcompradet_id as sw2,
            g.cantidad,
            ifnull(g.comentario,' GASTO IMPORTADO') as comentario,
            g.importe,
            g.ret2_imp as retencionIsr,
            g.ret2 as retencionIsrTasa,
            g.ret1_impp as retencionIva,
            g.ret1 as retencionIvaTasa,
            g.impuesto as tasaIva,
            p.unidad,
            g.precio as valorUnitario
        FROM 
         sw_gcompradet g
         join sw_gproductoservicio p on(p.producto_id=g.producto_id)
         where g.COMPRA_ID=?
    
    """

    static String CONCEPTOS_QUERY ="""  
        SELECT 
            g.gcompradet_id as sw2_gasto,
            g.cantidad,
            g.comentario,
            p.descripcion as concepto,
            g.importe,
            g.sucursal_id
        FROM 
        sw_gcompradet g
        join sw_gproductoservicio p on(p.producto_id=g.producto_id)
        where g.gcompradet_id=?
        
    """


    
}
