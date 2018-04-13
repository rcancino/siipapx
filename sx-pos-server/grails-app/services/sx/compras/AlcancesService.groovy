package sx.compras

import groovy.sql.Sql

class AlcancesService {

    def dataSource

    private static SQL = """
        SELECT 
        z.id as proveedor
        ,z.nombre
        ,CASE WHEN '402880fc5e4ec411015e4ec64e70012e'='%' THEN "TODAS" ELSE (SELECT S.NOMBRE FROM sucursal S WHERE S.activa IS TRUE AND S.ID LIKE '402880fc5e4ec411015e4ec64e70012e' ) END AS sucursal
        ,L.linea
        ,C.clase
        ,M.marca
        ,P.clave
        ,P.descripcion
        ,P.kilos
        ,SUM(EXI) AS existencia
        ,(CASE WHEN SUM(VTA)=0 AND P.UNIDAD='MIL' THEN 0.100 WHEN SUM(VTA)=0 AND P.UNIDAD<>'MIL' THEN 1 ELSE SUM(VTA)  END)  AS venta
        ,ROUND((ROUND(TO_DAYS('2018-04-12')-TO_DAYS('2018-02-12'),0)),0)/30.4166 AS mesesPeriodo
        ,(CASE WHEN SUM(VTA)=0 AND P.UNIDAD='MIL' THEN 0.100 WHEN SUM(VTA)=0 AND P.UNIDAD<>'MIL' THEN 1 ELSE (SUM(VTA)/(ROUND((ROUND(TO_DAYS('2018-04-12')-TO_DAYS('2018-02-12'),0)),0)/30.4166)) END) AS promVta
        ,IFNULL(IFNULL(SUM(EXI),0)/IFNULL((CASE WHEN SUM(VTA)=0 AND P.UNIDAD='MIL' THEN 0.100 WHEN SUM(VTA)=0 AND P.UNIDAD<>'MIL' THEN 1 ELSE (SUM(VTA)/(ROUND((ROUND(TO_DAYS('2018-04-12')-TO_DAYS('2018-02-12'),0)),0)/30.4166)) END),0),0) AS alcanceTotal
        ,SUM(PEND) AS pedidoCompraPendte
        ,SUM(EXI)*P.KILOS/1000 AS existenciaEnToneladas
        ,(CASE WHEN SUM(VTA)=0 AND P.UNIDAD='MIL' THEN 0.100 WHEN SUM(VTA)=0 AND P.UNIDAD<>'MIL' THEN 1 ELSE  (SUM(VTA)/(ROUND((ROUND(TO_DAYS('2018-04-12')-TO_DAYS('2018-02-12'),0)),0)/30.4166)) END)*P.KILOS/1000 AS promVtaEnTonelada
        FROM (
        SELECT X.PRODUCTO_ID,X.CLAVE,SUM(X.CANTIDAD/(case when p.unidad ='MIL' then 1000 else 1 end)) AS EXI,0 AS VTA,0 AS PEND FROM EXISTENCIA X JOIN producto p ON(X.producto_id=p.ID) WHERE X.anio=YEAR('2018-04-12') AND  X.MES=MONTH('2018-04-12') AND   x.sucursal_id LIKE '402880fc5e4ec411015e4ec64e70012e' GROUP BY X.CLAVE,X.PRODUCTO_ID
        UNION
        SELECT X.PRODUCTO_ID,P.CLAVE,0,SUM((X.CANTIDAD*-1)/(case when p.unidad ='MIL' then 1000 else 1 end)) AS VTA,0 AS PEND FROM INVENTARIO X JOIN producto p ON(X.producto_id=p.ID) WHERE DATE(X.FECHA) BETWEEN '2018-01-12' AND '2018-04-12' AND X.TIPO IN('FAC','DEV') AND   X.sucursal_id LIKE '402880fc5e4ec411015e4ec64e70012e' GROUP BY P.CLAVE,X.PRODUCTO_ID
        UNION
        SELECT X.PRODUCTO_ID,P.CLAVE,0,0,SUM(((X.SOLICITADO-X.DEPURADO)/(case when P.unidad ='MIL' then 1000 else 1 end))-IFNULL((SELECT SUM(I.CANTIDAD/(case when Y.unidad ='MIL' then 1000 else 1 end)) FROM recepcion_de_compra_det I JOIN producto Y ON(I.producto_id=Y.ID) WHERE I.compra_det_id=X.ID AND I.inventario_id IS NOT NULL),0)) AS PENDTE
        from compra_det X JOIN producto P ON(X.producto_id=P.ID) WHERE X.SUCURSAL_ID LIKE '402880fc5e4ec411015e4ec64e70012e' GROUP BY X.PRODUCTO_ID
        ) AS A
        JOIN producto P ON(A.PRODUCTO_ID=P.ID)
        join LINEA L on(L.ID=p.LINEA_ID)
        JOIN CLASE C ON(C.ID=P.CLASE_ID)
        JOIN marca M ON(M.ID=P.MARCA_ID)
        LEFT JOIN proveedor z on(p.proveedor_favorito_id=z.id)
        WHERE P.ACTIVO IS TRUE AND P.INVENTARIABLE IS TRUE 
        GROUP BY P.CLAVE 
        ORDER BY LINEA,CLASE
    """

    def generar() {
        Sql sql = new Sql(dataSource)
        return sql.rows(SQL)
    }
}


