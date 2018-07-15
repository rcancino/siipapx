package sx.crm

import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.apache.commons.lang.exception.ExceptionUtils

import javax.sql.DataSource
import java.sql.SQLException

@Transactional
class BonificacionMCService {

    DataSource dataSource

    String SQL ="""
            SELECT 
        X.cliente_id
        ,X.clave
        ,X.nombre
        ,(X.neto) as neto
        ,(X.KILOS) as kilos
        ,x.ult_vta
        ,x.facs
        ,x.suc
        FROM (
        select a.cliente_id 
        ,(SELECT x.clave FROM cliente x where x.ID=a.CLIENTE_ID) as clave
        ,v.nombre
        ,sum(a.subtotal) as neto
        ,sum(v.kilos) as kilos
        ,count(*) as facs
        ,max(A.fecha) as ult_vta
        ,A.tipo
        ,(SELECT s.nombre FROM sucursal s where s.ID=a.sucursal_id) as suc 
        from cuenta_por_cobrar a  join venta v on(v.cuenta_por_cobrar_id=a.id)
        where a.fecha between '2018-04-01' and '2018-04-30' AND A.TIPO IN('CON','COD')
        and a.cliente_id not in('402880fc5e4ec411015e4ec8fbbb045c','402880fc5e4ec411015e4ec9349204ae','402880fc5e4ec411015e4ecc5dfc0554')
        group by A.CLIENTE_ID
        order by a.cliente_id,a.fecha desc
        ) AS X
        group by X.cliente_id
        order by sum(X.neto) desc
        limit 200

    """

    def generar(Integer ejercicio, Integer mes) {
        def found = BonificacionMC.where{ ejercicio == ejercicio && mes == mes}.find()
        if(!found) {
            def rows = getRows(SQL, [])
            rows.each { row ->
                BonificacionMC bono = new BonificacionMC()
                bono.properties == row
            }
            return rows
        } else {
            log.info('Bonificaciones ya generadas para el periodo {} / {}' , ejercicio, mes)
            return []
        }

    }

    def getRows(String sql, List params) {
        def db = getLocalSql()
        try {
            return db.rows(sql, params)
        }catch (SQLException e){
            e.printStackTrace()
            def c = ExceptionUtils.getRootCause(e)
            def message = ExceptionUtils.getRootCauseMessage(e)
            throw new RuntimeException(message,c)
        }finally {
            db.close()
        }
    }

    def getLocalSql(){
        Sql sql = new Sql(this.dataSource)
        return sql;
    }
}
