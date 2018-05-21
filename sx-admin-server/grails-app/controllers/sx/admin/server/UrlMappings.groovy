package sx.admin.server

class UrlMappings {

    static mappings = {
        delete "/$controller/$id(.$format)?"(action:"delete")
        get "/$controller(.$format)?"(action:"index")
        get "/$controller/$id(.$format)?"(action:"show")
        post "/$controller(.$format)?"(action:"save")
        put "/$controller/$id(.$format)?"(action:"update")
        patch "/$controller/$id(.$format)?"(action:"patch")

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
        "/api/clientes"(resources: "cliente"){
            "/credito"(resources: 'clienteCredito')
        }
        "/api/clientes/actualizarCfdiMail/$id"(controller: "cliente", action: 'actualizarCfdiMail', method: 'PUT')
        "/api/clientes/$id/facturas"(controller: 'cliente', action: 'facturas', method: 'GET')
        "/api/clientes/$id/cxc"(controller: 'cliente', action: 'cxc', method: 'GET')
        "/api/clientes/$id/cobros"(controller: 'cliente', action: 'cobros', method: 'GET')
        "/api/clientes/$id/notas"(controller: 'cliente', action: 'notas', method: 'GET')
        // "/api/clientes/$id/socios"(controller: 'cliente', action: 'socios', method: 'GET')
        "/api/clientes"(resources: "cliente"){
            "/socios"(resources: 'socio')
        }

        "/api/clientes/estadoDeCuenta"(controller: "cliente", action: 'estadoDeCuenta', method: 'GET')

        // SAT
        "/api/sat/bancos"(resources: "SatBanco")
        "/api/sat/cuentas"(resources:"SatCuenta")

        // Tesoreria
        "/api/tesoreria/bancos"(resources: "banco")
        "/api/tesoreria/cuentas"(resources: "cuentaDeBanco")
        "/api/tesoreria/fichas"(resources: "ficha"){
            "/cheques"( controller: 'ficha', action: 'cheques')
            "/ingreso"( controller: 'ficha', action: 'ingreso')
        }
        "/api/tesoreria/fichas/generar"(controller: "ficha", action: 'generar', method: 'GET')
        "/api/tesoreria/fichas/reporteDeRelacionDeFichas"(controller: "ficha", action: 'reporteDeRelacionDeFichas', method: 'GET')

        //Comprobantes fiscales de proveedores CFDI's
        "/api/cfdis"(resources: "cfdi")
        "/api/cfdis/mostrarXml/$id?"(controller:"cfdi", action:"mostrarXml")
        "/api/cfdis/print/$id"(controller: "cfdi", action: 'print', method: 'GET')
        "/api/cfdis/enviarEmail/$id?"(controller:"cfdi", action:"enviarEmail")

        // Compras
        "/api/compras"(resources: "compra")
        "/api/compras/print/$ID"(controller: 'compra', action: 'print', method: 'GET')

        // Ventas
        "/api/ventas"(resources:"venta")

        "/api/tesoreria/solicitudes"(resources:"solicitudDeDeposito")
        "/api/tesoreria/solicitudes/pendientes"( controller: 'solicitudDeDeposito', action: 'pendientes')
        "/api/tesoreria/solicitudes/autorizadas"( controller: 'solicitudDeDeposito', action: 'autorizadas')
        "/api/tesoreria/solicitudes/transito"( controller: 'solicitudDeDeposito', action: 'transito')
        "/api/tesoreria/solicitudes/canceladas"( controller: 'solicitudDeDeposito', action: 'canceladas')
        "/api/tesoreria/solicitudes/autorizar/$id"( controller: 'solicitudDeDeposito', action: 'autorizar')
        "/api/tesoreria/solicitudes/posponer/$id"( controller: 'solicitudDeDeposito', action: 'posponer')
        "/api/tesoreria/solicitudes/rechazar/$id"( controller: 'solicitudDeDeposito', action: 'rechazar')
        "/api/tesoreria/solicitudes/cancelar/$id"( controller: 'solicitudDeDeposito', action: 'cancelar')
        "/api/tesoreria/solicitudes/buscarDuplicada/$id"( controller: 'solicitudDeDeposito', action: 'buscarDuplicada')
        "/api/tesoreria/solicitudes/ingreso/$id"( controller: 'solicitudDeDeposito', action: 'ingreso', method: 'PUT')
        "/api/tesoreria/solicitudes/cobranzaContado"(controller: 'solicitudDeDeposito', action: 'cobranzaContado', method: 'GET')
        "/api/tesoreria/solicitudes/cobranzaCod"(controller: 'solicitudDeDeposito', action: 'cobranzaCod',  method: 'GET')
        "/api/tesoreria/solicitudes/disponibles"(controller: 'solicitudDeDeposito', action: 'disponibles',  method: 'GET')
        "/api/tesoreria/solicitudes/ventasDiarias"(controller: 'solicitudDeDeposito', action: 'ventasDiarias',  method: 'GET')

        "/api/tesoreria/cortesTarjeta"(resources: 'corteDeTarjeta')
        "/api/tesoreria/cortesTarjeta/pendientes"( controller: 'corteDeTarjeta', action: 'pendientes')
        "/api/tesoreria/cortesTarjeta/generarCortes"( controller: 'corteDeTarjeta', action: 'generarCortes', method: 'POST')
        "/api/tesoreria/cortesTarjeta/ajustarCobro"( controller: 'corteDeTarjeta', action: 'ajustarCobro', method: 'PUT')
        "/api/tesoreria/cortesTarjeta/aplicar/$id"( controller: 'corteDeTarjeta', action: 'aplicar', method: 'PUT')
        "/api/tesoreria/cortesTarjeta/cancelarAplicacion/$id"( controller: 'corteDeTarjeta', action: 'cancelarAplicacion', method: 'PUT')
        "/api/tesoreria/cortesTarjeta/reporteDeComisionesTarjeta"(controller: 'corteDeTarjeta', action: 'reporteDeComisionesTarjeta', method: 'GET')

        "/api/tesoreria/movimientosDeTesoreria"(resources: 'movimientoDeTesoreria')

        // CXC
        "/api/cxc/cobro"(resources: "cobro")
        "/api/cxc/cobro/cobrosMonetarios"(controller: "cobro", action: 'cobrosMonetarios', method: 'GET')
        "/api/cxc/cobro/disponibles"(controller: "cobro", action: 'disponibles', method: 'GET')
        "/api/cxc/cobro/aplicar/$id"(controller: "cobro", action: 'aplicar', method: 'PUT')
        "/api/cxc/cobro/saldar/$id"(controller: "cobro", action: 'saldar', method: 'PUT')
        "/api/cxc/cobro/reporteDeCobranza"(controller: "cobro", action: 'reporteDeCobranza', method: 'GET')
        "/api/cxc/cobro/reporteDeRelacionDePagos"(controller: "cobro", action: 'reporteDeRelacionDePagos', method: 'GET')

        "/api/cobradores"(resources:"cobrador")
        "/api/vendedores"(resources:"vendedor")
        "/api/despachos"(resources:"despachoDeCobranza")

        "/api/cxc/notas"(resources: "notaDeCredito")
        "/api/cxc/notas/buscarRmd"(controller: "notaDeCredito", action: 'buscarRmd', method: 'GET')
        "/api/cxc/notas/buscarFacturasPendientes"(controller: "notaDeCredito", action: 'buscarFacturasPendientes', method: 'GET')
        "/api/cxc/notas/generarConRmd/$id"(controller: 'notaDeCredito', action: 'generarConRmd', method: 'POST')
        "/api/cxc/notas/timbrar/$id"(controller: 'notaDeCredito', action: 'timbrar', method: 'POST')
        "/api/cxc/notas/aplicar/$id"(controller: 'notaDeCredito', action: 'aplicar', method: 'POST')
        "/api/cxc/notas/print/$id"(controller: 'notaDeCredito', action: 'print', method: 'GET')
        "/api/cxc/notas/reporteDeNotasDeCredito"(controller: 'notaDeCredito', action: 'reporteDeNotasDeCredito', method: 'GET')

        "/api/cxc/notasDeCargo"(resources: "notaDeCargo")
        "/api/cxc/notasDeCargo/timbrar/$id"(controller: 'notaDeCargo', action: 'timbrar', method: 'POST')
        "/api/cxc/notasDeCargo/print/$id"(controller: 'notaDeCargo', action: 'print', method: 'GET')
        "/api/cxc/notasDeCargo/reporteDeNotasDeCargo"(controller: 'notaDeCargo', action: 'reporteDeNotasDeCargo', method: 'GET')


        "/api/cuentasPorCobrar"(resources: 'cuentaPorCobrar')
        "/api/cuentasPorCobrar/antiguedad"(controller: 'cuentaPorCobrar', action: 'antiguedad', method: 'GET')
        "/api/cuentasPorCobrar/antiguedad/print"(controller: 'cuentaPorCobrar', action: 'printAntiguedad', method: 'GET')
        "/api/cuentasPorCobrar/antiguedad/reporteDeCobranzaCOD"(controller: 'cuentaPorCobrar', action: 'reporteDeCobranzaCOD', method: 'GET')
        "/api/cuentasPorCobrar/pendientes/$id"(controller: 'cuentaPorCobrar', action: 'pendientes', method: 'GET')
        "/api/cuentasPorCobrar/saldar/$id"(controller: 'cuentaPorCobrar', action: 'saldar', method: 'PUT')

        "/api/cxc/ventaCredito"(resources: 'ventaCredito')
        "/api/cxc/ventaCredito/generar"(controller: 'ventaCredito', action: 'generar', method: 'POST')
        "/api/cxc/ventaCredito/recalcular"(controller: 'ventaCredito', action: 'generar', method: 'POST')
        "/api/cxc/ventaCredito/batchUpdate"(controller: 'ventaCredito', action: 'batchUpdate', method: 'POST')
        "/api/cxc/ventaCredito/registrarRecepcionCxC"(controller: 'ventaCredito', action: 'registrarRecepcionCxC', method: 'PUT')
        "/api/cxc/ventaCredito/cancelarRecepcionCxC"(controller: 'ventaCredito', action: 'cancelarRecepcionCxC', method: 'PUT')
        "/api/cxc/ventaCredito/registrarRvisada"(controller: 'ventaCredito', action: 'registrarRvisada', method: 'PUT')
        "/api/cxc/ventaCredito/print"(controller: 'ventaCredito', action: 'print', method: 'GET')

        // Cheques devueltos
        "/api/cxc/cheques"(resources: "chequeDevuelto")

        //Existencias
        "/api/existencias"(resources: "existencia")
        "/api/existencias/$producto/$year/$month"(controller: 'existencia', action: 'buscarExistencias')

        // Security
        "/api/security/users"(resources: "user")
        "/api/security/roles"(resources: "role")
        "/api/security/users/findByNip"( controller:'user', action: 'findByNip', method: 'GET')

        "/"(controller: 'application', action:'index')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }
}
