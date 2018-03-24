package sx.integracion

import groovy.sql.Sql
import groovy.util.logging.Slf4j
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.object.SqlUpdate
import org.springframework.stereotype.Component
import sx.core.Sucursal
import sx.cxc.Cobro


@Component
@Slf4j
class CuentasPorCobrarIntegration implements  Integracion{

    static String SQL = "select count(*) as registros, ifNull(sum(importe),0) as control from cobro where date(fecha) = ? and sucursal_id = ?"

    def validar(Date fecha, Sucursal sucursal) {
        IntegracionLog integracionLog = preparaBitacora(Cobro.class, sucursal, fecha)
        analisisSucursal(integracionLog)
        .analisisOficinas(integracionLog)
        return integracionLog
    }

    def analisisSucursal(IntegracionLog integracionLog){
        Map res = getRows(integracionLog.sucursal, SQL, integracionLog.fecha, integracionLog.sucursal.id).get(0)
        integracionLog.control = res.control
        integracionLog.registros = res.registros
        return this
    }

    def analisisOficinas(IntegracionLog integracionLog){
        Map res = getLocalRows(integracionLog.sucursal, SQL, [integracionLog.fecha, integracionLog.sucursal.id]).get(0)
        integracionLog.registrosOficinas = res.registros
        integracionLog.controlOficinas = res.control
    }

    def validacionIndivicual(Sucursal sucursal, Date fecha ) {
        def closure = { row -> println 'Validando registro: ' +row };
        Sql db = getSql(sucursal)
        try{
            db.eachRow("select * from cobro where date(fecha)=? and sucursal_id = ? ", closure)
            JdbcTemplate template = new JdbcTemplate(dataSource)
            SqlUpdate update = new SqlUpdate(dataSource,"")
            template.inser
        }catch (Exception ex) {
            ex.printStackTrace()
        }
    }

}
