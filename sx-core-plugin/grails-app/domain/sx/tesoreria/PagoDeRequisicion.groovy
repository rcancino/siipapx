package sx.tesoreria

import sx.cxp.AbonoCxP

/**
 * Created by rcancino on 25/04/17.
 */
class PagoDeRequisicion extends AbonoCxP {

    String id

    MovimientoDeCuenta egreso

    Requisicion requisicion

    static mapping = {
        id generator: 'uuid'
    }

}
