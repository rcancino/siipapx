package sx.pos.server

class UrlMappings {

    static mappings = {
        delete "/$controller/$id(.$format)?"(action:"delete")
        get "/$controller(.$format)?"(action:"index")
        get "/$controller/$id(.$format)?"(action:"show")
        post "/$controller(.$format)?"(action:"save")
        put "/$controller/$id(.$format)?"(action:"update")
        patch "/$controller/$id(.$format)?"(action:"patch")

        "/api/config"(resource: "appConfig")

        // Catalogos principales
        "/api/sucursales"(resources: "sucursal")
        "/api/sucursales/otrosAlmacenes"(controller: "sucursal", action: 'otrosAlmacenes', method: 'GET')
        "/api/lineas"(resources: "linea")
        "/api/marcas"(resources: "marca")
        "/api/clases"(resources: "clase")
        "/api/productos"(resources: "producto")
        "/api/proveedores"(resources: "proveedor"){
            "/productos"(resources:'proveedorProducto')
        }
        "/api/clientes"(resources: "cliente")
        "/api/clientes/actualizarCfdiMail/$id"(controller: "cliente", action: 'actualizarCfdiMail', method: 'PUT')

        // SAT
        "/api/sat/bancos"(resources: "SatBanco")
        "/api/sat/cuentas"(resources:"SatCuenta")

        // Tesoreria
        "/api/tesoreria/bancos"(resources: "banco")
        "/api/tesoreria/cuentas"(resources: "cuentaDeBanco")

        //Comprobantes fiscales de proveedores CFDI's
        "/api/cfdis"(resources: "cfdi")
        "/api/cfdis/mostrarXml/$id?"(controller:"cfdi", action:"mostrarXml")
        "/api/cfdis/print/$id"(controller: "cfdi", action: 'print', method: 'GET')

        // Compras
        "/api/compras"(resources: "compra"){
            "/partidas"(resources:"compraDet")
        }
        "/api/compras/print/$ID"(controller: 'compra', action: 'print', method: 'GET')
        "/api/listaDePreciosPorProveedor"(resources: "listaDePreciosPorProveedor")
        "/api/compras/recepciones"(resources: "recepcionDeCompra") {
            "/partidas"(resource: "recepcionDeCompraDet")
        }
        "/api/compras/recepciones/buscarCompra"(controller: 'recepcionDeCompra', action: 'buscarCompra', method: 'GET')
        "/api/compras/devolucionCompra"(resources: "devolucionDeCompra")


        // Ventas
        "/api/ventas"(resources:"venta")
        "/api/ventas/pendientes/$id"( controller: 'venta', action: 'pendientes')
        "/api/ventas/facturados/$id"( controller: 'venta', action: 'facturados')
        "/api/ventas/findManiobra"( controller: 'venta', action: 'findManiobra')
        "/api/ventas/mandarFacturar/$id"( controller: 'venta', action: 'mandarFacturar')
        "/api/ventas/mandarFacturarConAutorizacion"( controller: 'venta', action: 'mandarFacturarConAutorizacion', method: 'POST')
        "/api/ventas/asignarEnvio/$id"( controller: 'venta', action: 'asignarEnvio', method: 'PUT')
        "/api/ventas/generarSolicitudAutomatica/$id"( controller: 'venta', action: 'generarSolicitudAutomatica')
        "/api/ventas/facturar/$id"( controller: 'venta', action: 'facturar')
        "/api/ventas/cobradas/$id"( controller: 'venta', action: 'cobradas')
        "/api/ventas/timbrar/$id"( controller: 'venta', action: 'timbrar')
        "/api/ventas/cancelar/$id"( controller: 'venta', action: 'cancelar')
        "/api/ventas/print/$id"(controller: "venta", action: 'print', method: 'GET')

        "/api/tesoreria/solicitudes"(resources:"solicitudDeDeposito")
        "/api/tesoreria/solicitudes/pendientes/$id"( controller: 'solicitudDeDeposito', action: 'pendientes')
        "/api/tesoreria/corteCobranza"(resources:"corteCobranza")
        "/api/tesoreria/fondoFijo"(resources:"fondoFijo")
        "/api/tesoreria/morralla"(resources:"morralla")
        "/api/tesoreria/reporteDeAarqueoCaja"(controller: 'cobro', action: 'reporteDeAarqueoCaja', method: 'GET')

        // CXC
        "/api/cxc/cobro"(resources: "cobro")
        "/api/cxc/cobro/cobroContado"(controller: 'cobro', action: 'cobroContado')
        "/api/cxc/cobro/cambioDeCheque"(controller: 'cobro', action: 'cambioDeCheque')
        "/api/cxc/cobro/buscarDisponibles/$id"(controller: 'cobro', action: 'buscarDisponibles')
        "/api/notasDeCargo"(resources: "notaDeCargo")
        "/api/cuentasPorCobrar"(resources: 'cuentaPorCobrar')
        "/api/cuentasPorCobrar/pendientesCod/$id"( controller: 'cuentaPorCobrar', action: 'pendientesCod')


        //Existencias
        "/api/existencias"(resources: "existencia"){
            collection {
                "/sucursal"(controller: 'existencias', action: 'existenciasPorSucursal', method: 'GET')
            }
        }
        "/api/existencias/$producto/$year/$month"(controller: 'existencia', action: 'buscarExistencias')

        //Inventario
        "/api/inventario"(resources: "inventario")
        "/api/inventario/movimientos"(resources: "movimientoDeAlmacen")
        "/api/inventario/movimientos/print"(controller: "movimientoDeAlmacen", action: 'print', method: 'GET')
        "/api/inventario/transformaciones"(resources: "transformacion")
        "/api/inventario/transformaciones/print"(controller: "transformacion", action: 'print', method: 'GET')
        "/api/inventario/devoluciones"(resources: "devolucionDeVenta")
        "/api/inventario/devoluciones/buscarVenta"(controller: 'devolucionDeVenta', action: 'buscarVenta', method: 'GET')
        // Decs
        "/api/inventario/decs"(resources: "devolucionDeCompra")
        "/api/inventario/decs/buscarCom"(controller: 'devolucionDeCompra', action: 'buscarCom', method: 'GET')

        // Sols
        "/api/inventario/sols"(resources: "solicitudDeTraslado")
        "/api/inventario/sols/print"(controller: "solicitudDeTraslado", action: 'print', method: 'GET')
        "/api/inventario/sols/atender/$id"(controller: "solicitudDeTraslado", action: 'atender', method: 'PUT')

        // Traslados
        "/api/inventario/traslados"(resources: "traslado")
        "/api/inventario/traslados/print"(controller: "traslado", action: 'print', method: 'GET')
        "/api/inventario/traslados/printCfdi"(controller: "traslado", action: 'printCfdi', method: 'GET')
        "/api/inventario/traslados/salida/$id"(controller: "traslado", action: 'salida', method: 'PUT')
        "/api/inventario/traslados/timbrar/$id"(controller: "traslado", action: 'timbrar', method: 'PUT')
        "/api/inventario/traslados/entrada/$id"(controller: "traslado", action: 'entrada', method: 'PUT')

        // Kardex
        "/api/inventario/kardex"(controller: "inventario", action: "kardex" )
        "/api/inventario/printKardex"(controller: "inventario", action: "printKardex", method: 'GET' )
        "/api/inventario/saveInventario"(controller: "inventario", action: "saveInventario" , method: 'POST')

        // Sectores
        "/api/inventario/sectores"(resources: "sector")

        // Conteos
        "/api/inventario/conteos"(resources: "conteo")
        "/api/inventario/conteos/generarConteo"(controller: "conteo", action: 'generarConteo', method: 'POST')
        "/api/inventario/conteos/generarExistencias"(controller: "conteo", action: ' generarExistencias', method: 'GET')
        "/api/inventario/conteos/limpiarExistencias"(controller: "conteo", action: ' limpiarExistencias', method: 'GET')

        // Embarques
        "/api/embarques/facturistas"(resources: 'facturistaDeEmbarque')
        "/api/embarques/transportes"(resources: 'transporte')
        "/api/embarques/choferes"(resources: "chofer")
        "/api/embarques/embarques"(resources: "embarque")
        "/api/embarques/embarques/buscarDocumento"(controller: 'embarque', action: 'buscarDocumento', method: 'GET')
        "/api/embarques/embarques/registrarSalida/$id"(controller: 'embarque', action: 'registrarSalida', method: 'PUT')
        "/api/embarques/embarques/registrarRegreso/$id"(controller: 'embarque', action: 'registrarRegreso', method: 'PUT')
        "/api/embarques/embarques/print"(controller: "embarque", action: 'print', method: 'GET')
        "/api/embarques/embarques/reporteDeEntregasPorChofer"(controller: "embarque", action: 'reporteDeEntregasPorChofer', method: 'GET')
        "/api/embarques/embarques/documentosEnTransito"(controller: "embarque", action: 'documentosEnTransito', method: 'GET')
        "/api/embarques/embarques/enviosPendientes"(controller: "embarque", action: 'enviosPendientes', method: 'GET')
        "/api/embarques/embarques/buscarVenta"(controller: 'embarque', action: 'buscarVenta', method: 'GET')
        "/api/embarques/embarques/buscarPartidasDeVenta"(controller: 'embarque', action: 'buscarPartidasDeVenta', method: 'GET')
        "/api/embarques/embarques/buscarTrasladosPendientes"(controller: 'embarque', action: 'buscarTrasladosPendientes', method: 'GET')
        "/api/embarques/embarques/buscarDevolucionesPendientes"(controller: 'embarque', action: 'buscarDevolucionesPendientes', method: 'GET')
        "/api/embarques/embarques/asignarFacturas"(controller: 'embarque', action: 'asignarFacturas', method: 'PUT')
        "/api/embarques/envios"(resources: 'envio')


        "/api/report"(controller: 'reporte', action: 'run', method: 'GET')
        "/api/report/ventasDiarias"(controller: 'ventas', action: 'ventasDiarias', method: 'GET')
        "/api/report/cobranzaCod"(controller: 'ventas', action: 'cobranzaCod', method: 'GET')
        "/api/report/cobranzaEfectivo"(controller: 'ventas', action: 'cobranzaEfectivo', method: 'GET')
        "/api/report/cobranzaContado"(controller: 'ventas', action: 'cobranzaContado', method: 'GET')
        "/api/report/facturasCanceladas"(controller: 'ventas', action: 'facturasCanceladas', method: 'GET')
        "/api/report/aplicacionSaldos"(controller: 'ventas', action: 'aplicacionDeSaldos', method: 'GET')
        "/api/report/disponiblesSucursal"(controller: 'ventas', action: 'disponiblesSucursal', method: 'GET')
        "/api/report/facturasPendientesCod"(controller: 'ventas', action: 'facturasPendientesCod', method: 'GET')
        "/api/report/facturasPendientesCodEmbarques"(controller: 'ventas', action: 'facturasPendientesCodEmbarques', method: 'GET')
        "/api/report/ventasDiariasCheques"(controller: 'ventas', action: 'ventasDiariasCheques', method: 'GET')

        // Security
        "/api/security/users"(resources: "user")
        "/api/security/roles"(resources: "role")
        "/api/security/users/findByNip"( controller:'user', action: 'findByNip', method: 'GET')

        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
