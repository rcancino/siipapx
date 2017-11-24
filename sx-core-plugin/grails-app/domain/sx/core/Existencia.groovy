package sx.core

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includes = 'producto,cantidad,disponible', includeNames = true, includePackage = false)
@EqualsAndHashCode(includes = 'sucursal, producto, anio, mes')
class Existencia {

  String  id

  Sucursal    sucursal

  Producto    producto

  Long    anio     = 0

  Long    mes  = 0

  Boolean nacional     = true

  BigDecimal  kilos    = 0

  BigDecimal  pedidosPendiente     = 0

  BigDecimal  entradaCompra    = 0

  BigDecimal  devolucionCompra     = 0

  BigDecimal  venta    = 0

  BigDecimal  devolucionVenta  = 0

  BigDecimal  movimientoAlmacen    = 0

  BigDecimal  traslado     = 0

  BigDecimal  transformacion   = 0

  BigDecimal  cantidad     = 0
  BigDecimal disponible = 0

  String sw2


  static constraints = {
    pedidosPendiente nullable: true
    entradaCompra nullable: true
    devolucionCompra nullable: true
    venta nullable: true
    devolucionVenta nullable: true
    movimientoAlmacen nullable: true
    traslado nullable: true
    transformacion nullable: true
    sw2 nullable: true
  }

  static mapping={
    id generator:'uuid'
    sucursal index: 'SUCURSAL_IDX'
    anio index: 'YEAR_IDX'
    mes index: 'MES_IDX'
    disponible formula:'cantidad - pedidos_Pendiente'
  }
}
