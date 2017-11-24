package sx.contabilidad

import groovy.transform.EqualsAndHashCode

@EqualsAndHashCode(includes='clave')
class CuentaContable {

    String id

    String clave

    String descripcion

    String tipo

    CuentaContable padre

    SatCuenta cuentaSat

    boolean detalle=false

    boolean deResultado=false

    String naturaleza

    boolean presentacionContable=false

    boolean presentacionFiscal=false

    boolean presentacionFinanciera=false

    boolean presentacionPresupuestal=false

    Boolean suspendida = false

    Date dateCreated

    Date lastUpdated

    static hasMany = [subCuentas:CuentaContable]

    static constraints = {
        clave nullable:true,maxSize:100 , unique:true
        descripcion blank:false,maxSize:300
        tipo inList:['ACTIVO','PASIVO','CAPITAL','ORDEN']
        naturaleza inList:['DEUDORA','ACREEDORA']
        cuentaSat nullable:true
    }

    static mapping ={
        id generator:'uuid'
        subCuentas cascade: "all-delete-orphan", batchSize: 10

    }

    String toString(){
        return clave+" "+descripcion
    }


    static CuentaContable buscarPorClave(String clave){
        def found=CuentaContable.findByClave(clave)
        if(!found)

            throw new RuntimeException("No existe la cuenta contable: $clave")
        return found
    }
}
