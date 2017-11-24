package sx.compras

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Producto

@ToString(includeNames=true,includePackage=false, excludes = ['lastUpdated', 'dateCreated','id','version','lista'])
@EqualsAndHashCode(includeFields = true,includes = ['producto'])
class ListaDePreciosPorProveedorDet {

    String id

    Producto producto

    Currency moneda = Currency.getInstance('MXN')

    BigDecimal precio = 0.0

    BigDecimal neto = 0.0

    BigDecimal descuento1 = 0.0

    BigDecimal descuento2 = 0.0

    BigDecimal descuento3 = 0.0

    BigDecimal descuento4 = 0.0

    BigDecimal descuentoFinanciero = 0.0

    BigDecimal precioAnterior = 0.0

    Date dateCreated

    Date lastUpdated


    static constraints = {
        moneda maxSize:5
    }

    static mapping ={
        id generator:'uuid'
    }

    static belongsTo =[lista:ListaDePreciosPorProveedor]




}
