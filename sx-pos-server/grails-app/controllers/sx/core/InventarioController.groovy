package sx.core


import grails.rest.*
import grails.converters.*
import sx.core.Sucursal
import sx.core.Producto

class InventarioController extends RestfulController {

    static responseFormats = ['json', 'xml']
    InventarioController() {
        super(Inventario)
    }

    def kardex(String clave ){

        def inicio= new Date(params.fechaIni)

       def fin= new Date(params.fechaFin)


        println "Ejecutando el kardex para la clave "+clave +"  "+inicio+" - "+fin

        println params

        Producto producto=Producto.where{clave==clave}.find()

        def inventarios= Inventario.where{producto==producto && fecha>= inicio && fecha<= fin}.list()

        inventarios.each {inv ->
            println inv.producto.clave+" - - - "+inv.cantidad+" "+inv.fecha
        }

        //respond inventarios:inventarios, inventarioCount:100
        respond inventarios:inventarios, inventarioCount:100
    }

    def saveInventario(){

        println "Salvando El inventario"

        Inventario inventario= new Inventario()

        Sucursal sucursal=Sucursal.where{nombre == params.sucursal}.find()

        Producto producto= Producto.where{clave== params.clave}.find()

        def fecha=new Date(params.fecha)

        inventario.sucursal=sucursal
        inventario.producto=producto
        inventario.documento = new Long(params.documento)
        inventario.tipo = params.tipo
        inventario.tipoVenta = params.tipoVenta
        inventario.cantidad = new BigDecimal(params.cantidad)
        inventario.kilos = new BigDecimal(params.kilos)
        inventario.fecha= fecha

        inventario.save failOnError:true, flush:true

        respond inventario, view: 'show'



    }

}
