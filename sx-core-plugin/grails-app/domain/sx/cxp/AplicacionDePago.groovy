package sx.cxp

/**
 * Created by rcancino on 25/04/17.
 */
class AplicacionDePago extends  AplicacionCxP{

    String id

    static  mapping={
        id generator:'uuid'
    }
}
