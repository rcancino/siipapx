package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.compras.ListaDePreciosPorProveedor
import sx.compras.ListaDePreciosPorProveedorDet
import sx.core.Producto
import sx.core.Proveedor


/**
 * Created by rcancino on 29/09/16.
 */
@Component
class ImportadorDeListaDePrecios implements Importador {

  /*  @Autowired
    ImportadorDeProductos importadorDeProductos

    def importar(f1){
        return importar(f1,f1)
    }

    def importar(Date ini, Date fin){
        logger.info("Importando listad de precios del : ${ini.format('dd/MM/yyyy')} al ${fin.format('dd/MM/yyyy')}" )
        def ids = leerRegistros("select id from sx_lp_provs where fecha_fin between ? and ? ",[ini,fin])
        ids.each { r ->
            //println 'Importando : ' + id
            importar(r.id)
        }
    }

    def importar(Long sw2){
        logger.info('Importando lista ' + sw2)
        String select = QUERY + " where id = ? "
        def row = findRegistro(select, [sw2])
        build(row)
    }


    def build(def row){
        def lista = ListaDePreciosPorProveedor.where{ sw2 == row.sw2}.find()
        if(!lista){
            lista = new ListaDePreciosPorProveedor()
            lista.proveedor = Proveedor.where {sw2 == row.proveedor_id}.find()
        }
        bindData(lista,row)
        importarPartidas(lista)
        try{
            lista.save failOnError:true, flush:true
            return lista
        }catch (Exception ex){logger.error(ExceptionUtils.getRootCauseMessage(ex))}
    }

    def importarPartidas(ListaDePreciosPorProveedor lista){
        List partidas = leerRegistros(QUERY_PARTIDAS,[lista.sw2])
        if(lista.partidas) {
            lista.partidas.clear()

        }
        else
            lista.partidas = []
        logger.info('Limpiando partidas.... res: ' + lista.partidas.size())
        partidas.each{ row ->
            ListaDePreciosPorProveedorDet det = new  ListaDePreciosPorProveedorDet()
            Producto producto = Producto.where {sw2 == row.producto_id}.find()
            if(!producto){
                producto = importadorDeProductos.importar(row.producto_id)
            }
            det.producto = producto
            bindData(det,row)
            lista.addToPartidas(det)
        }
    }

*/

    static String QUERY  = """
        select
            id as sw2,
            descripcion,
            proveedor_id ,
            fecha_ini as fechaInicial,
            fecha_fin as fechaFinal,
            vigente
            from `sx_lp_provs` l
        """
    static String QUERY_PARTIDAS ="""
        select
            l.prod_id as producto_id,
            p.descripcion,
            l.precio_mon as moneda,
            l.precio,
            l.precio as precioAnterior,
            l.neto,
            l.desc1 as descuento1,
            l.desc2 as descuento2,
            l.desc3 as descuento3,
            l.desc4 as descuento4,
            l.desc_f as descuentoFinanciero
            from `sx_lp_provs_det` l join sx_productos p on(l.prod_id = p.producto_id)
            where lista_id = ?
        """
}
