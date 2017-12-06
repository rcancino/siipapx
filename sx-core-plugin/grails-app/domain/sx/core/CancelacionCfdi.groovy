package sx.core

import sx.cfdi.Cfdi

class CancelacionCfdi {

    String id
    String motivo
    //Cfdi cfdi
    URL acuse
    //byte[] message

    Date dateCreated
    Date lastUpdated

    String createUser
    String updateUser

    static belongsTo = [cfdi: Cfdi]

    static constraints = {
        motivo nullable:true
        acuse url:true
        //message maxSize:(1024 * 512)  // 50kb para almacenar el xml
        createUser nullable:true, maxSize: 100
        updateUser nullable:true, maxSize: 100
    }

    static  mapping={
        id generator:'uuid'
    }
}
