package sx.ventas.api.plugin

class UrlMappings {

    static mappings = {
        // Ventas
        "/api/ventas"(resources:"venta")
        "/api/ventas/pendientes/$id"( controller: 'venta', action: 'pendientes')
        "/api/ventas/findManiobra"( controller: 'venta', action: 'findManiobra')
        "/api/ventas/mandarFacturar/$id"( controller: 'venta', action: 'mandarFacturar')
        "/api/ventas/generarSolicitudAutomatica/$id"( controller: 'venta', action: 'generarSolicitudAutomatica')
        "/api/ventas/facturar/$id"( controller: 'venta', action: 'facturar')
        "/api/ventas/cobradas/$id"( controller: 'venta', action: 'cobradas')
        "/api/ventas/timbrar/$id"( controller: 'venta', action: 'timbrar')
        "/api/ventas/print/$id"(controller: "venta", action: 'print', method: 'GET')
    }
}
