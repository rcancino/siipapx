package sx.tesoreria

/**
 * Entidad que agrupa los movimientos de bancos  agenos a las operaciones basicas de la empresa
 * como pueden ser Aclaraciones,Conciliaciones,Faltantes Sobrantes
 *
 * Created by rcancino on 06/04/17.
 */
class MovimientoDeTesoreria {

    String id

    static mapping = {
        id generator: 'uuid'
    }
}
