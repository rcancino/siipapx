package sx.compras

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import sx.core.Proveedor

@ToString(includeNames=true,includePackage=false, excludes = ['lastUpdated', 'dateCreated','id','version','partidas'])
@EqualsAndHashCode(includeFields = true,includes = ['id'])
class ListaDePreciosPorProveedor {

    String id

    Proveedor proveedor

    Date fechaInicial

    Date fechaFinal

    String descripcion

    BigDecimal descuentoFinanciero = 0.0



    String linea = 'TODAS'

    Long sw2

    List partidas

    Date dateCreated

    Date lastUpdated

    static constraints = {
        descripcion nullable:true
        sw2 nullable:true
    }

    static hasMany =[partidas:ListaDePreciosPorProveedorDet]

    static mapping ={
        id generator:'uuid'
        partidas cascade: "all-delete-orphan"
        fechaInicial type:'date'
        fechaFinal type:'date'
    }


    static transients = ['vigente']

    Boolean isVigente(){
        return isVigente(new Date())
    }

    Boolean isVigente(Date fecha){
        return (fechaInicial.compareTo(fecha) <= 0 && fechaFinal.compareTo(fecha) >= 0)
    }

    /*def beforeValidate(){
        if(partidas){
           partidas.each{
               if(it.producto){
                   it.clave = it.producto.clave
                   it.descripcion = it.producto.descripcion
                   it.unidad = it.producto.unidad
               }
           }
        }

    }*/

}
