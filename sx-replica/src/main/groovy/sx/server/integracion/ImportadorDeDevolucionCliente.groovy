package sx.server.integracion

import org.apache.commons.lang.exception.ExceptionUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import sx.core.Cliente
import sx.core.Sucursal
import sx.cxc.DevolucionCliente


/**
 * Created by Luis on 20/06/17.
 */

@Component
class ImportadorDeDevolucionCliente implements  Importador, SW2Lookup {

    @Autowired
    ImportadorDeClientes importadorDeClientes

    @Autowired
    ImportadorDeCuentasPorCobrar importadorDeCuentasPorCobrar

    def importar(Date ini, Date fin){

        println("Importando Cheques devueltos " )
        def ids = leerRegistros("select cargo_id from SX_VENTAS where fecha between ? and ? and tipo = ? ",[ini.format('yyyy-MM-dd'),fin.format('yyyy-MM-dd'), 'TES'])
        println('Registros: ' + ids.size())

        ids.each { r ->
            importar(r.cargo_id)
        }
        return 'OK'

    }

    def importar(){
        importar(new Date())
    }

    def importar(fecha){
        println("Importando Cheques devueltos del : ${fecha.format('dd/MM/yyyy')}" )
        def ids = leerRegistros("select cargo_id from SX_VENTAS where fecha = ? and tipo = ? ",[fecha.format('yyyy-MM-dd'), 'TES'])
        println('Registros: ' + ids.size())

        ids.each { r ->
            importar(r.cargo_id)
        }
        return 'OK'
    }

    def importar(String sw2){
        String select = QUERY + " where tipo = ? and cargo_id = ? "
        def row = findRegistro(select, ['TES',sw2])
        def devolucion = build(row)

        importadorDeCuentasPorCobrar.importar(devolucion.sw2)

    }

    def build(def row){
        println('Importando Devolucion al cliente con ROW: ' + row)
        def devolucion = DevolucionCliente.where{ sw2 == row.sw2}.find()
        if(!devolucion){
            devolucion = new DevolucionCliente()
        }
        bindData(devolucion,row)
        Cliente cliente = Cliente.where {sw2 == row.cliente_id}.find()
        if(!cliente){
            cliente = importadorDeClientes.importar(row.cliente_id)
        }


        Sucursal sucursal=buscarSucursal(row.sucursal_id)
        devolucion.sucursal=sucursal
        devolucion.cliente = cliente


        try{
            devolucion= devolucion.save failOnError:true, flush:true
            return devolucion
        }catch (Exception ex){
            println(ExceptionUtils.getRootCauseMessage(ex))
        }

    }


    static String QUERY="""
    SELECT
        cliente_id,
        sucursal_id,
        fecha,
        docto as documento,
        importe,
        impuesto,
        total,
        ifnull(FPAGO,'EFECTIVO') as formaDePago,
        moneda,
        TC as tipoDeCambio,
        comentario,
        CARGOS as cargo,
        CARGO_ID as sw2,
        ifnull(CREADO,fecha) as dateCreated,
        ifnull(MODIFICADO,fecha) as lastUpdated,
        ifnull(CREADO_USERID,'NA') as createUser,
        ifnull(MODIFICADO_USERID,'NA') as updateUser
    FROM sx_ventas

    """



}
