package sx.cxc

import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured

import groovy.transform.ToString

import sx.core.Venta
import sx.core.VentaService
import sx.reportes.PorFechaCommand
import sx.reports.ReportService
import sx.tesoreria.Banco
import sx.core.Sucursal
import sx.core.Cliente

@Secured("hasRole('ROLE_CXC_USER')")
class CobroController extends RestfulController{

    CobroService cobroService

    VentaService ventaService

    ReportService reportService

    static responseFormats = ['json']

    CobroController() {
        super(Cobro)
    }

    @Override
    protected List listAllResources(Map params) {
        def query = Cobro.where {}
        params.max = 200
        params.sort = params.sort ?:'lastUpdated'
        params.order = params.order ?:'desc'
        return query.list(params)
    }

    /**
     * Regresa las ventas de contado pendientes de factura
     */
    def ventasFacturables(PorFacturarCommand command){
        log.debug('Buscando ventas pendientes de facturar {}', params)
        log.debug('Buscando ventas pendientes de facturar command {}', command)

        if (command == null){
            notFound()
            return
        }

        params.sort = 'lastUpdated'
        params.order = 'asc'
        params.max = 200

        def query = Venta.where {sucursal ==  command.sucursal && cuentaPorCobrar == null && facturar != null}
        if(command.tipo == 'CRE'){
            query = query.where {tipo == command.tipo}
        } else {
            query = query.where {tipo != 'CRE'} // Contado y COD
        }
        def list = query.list(params)
        log.debug('Facturas pendientes de tipo {}: {}', command.tipo, list.size())
        respond list
    }

    protected Cobro saveResource(Cobro resource) {
        def username = getPrincipal().username
        if(resource.id == null) {
            def serie = resource.sucursal.clave
            resource.createUser = username
        }
        resource.updateUser = username
        resource.aplicaciones.each {
            it.createUser = username
            it.updateUser = username
        }
        return super.saveResource(resource)
    }

    @Transactional
    def cobroContado(CobroContado res) {
        if (res == null) {
            notFound()
            return
        }
        def venta = res.venta
        if(venta.cuentaPorCobrar == null) {
            venta = ventaService.generarCuentaPorCobrar(res.venta)
        }
        def cxc  = venta.cuentaPorCobrar
        if(cxc.saldo > 0 )
            cxc = cobroService.generarCobroDeContado(cxc, res.cobros)
        respond res.venta
    }

    @Transactional
    def cambioDeCheque(CambioDeCheque cambio) {
        if (cambio == null) {
            notFound()
            return
        }
        def cobro = new Cobro()
        cobro.sucursal = cambio.sucursal
        cobro.cliente = Cliente.where { clave == '1'}.find()
        cobro.tipo = 'CON'
        cobro.fecha = new Date()
        cobro.formaDePago = 'CHEQUE'
        cobro.importe = cambio.importe
        cobro.primeraAplicacion = new Date()

        def cheque = new CobroCheque()
        cheque.bancoOrigen = cambio.banco
        cheque.numeroDeCuenta = cambio.cuenta
        cheque.numero = cambio.numero
        cheque.emisor = cambio.emisor
        cheque.nombre = cambio.nombre
        cheque.cambioPorEfectivo = true
        cobro.cheque = cheque
        cobro.save flush:true, failOnError: true
        respond cobro

    }

    def buscarDisponibles(Cliente cliente) {
        if (cliente == null) {
            notFound()
            return
        }
        params.max = 20
        params.sort = 'fecha'
        params.order = 'asc'
        def cobros = Cobro.findAll(' from Cobro c where c.cliente = ? and c.importe - c.aplicado > 0', cliente)
        respond cobros
    }

    def reporteDeAarqueoCaja(PorFechaCommand command) {
        //log.debug(' Arqueo: {}', command)
        Map repParams = [:];
        repParams['FECHA'] = command.fecha.format('yyyy/MM/dd')
        repParams['SUCURSAL'] = params['SUCURSAL']
        def pdf = reportService.run('CajaArqueo', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "CajaArqueo.pdf")
    }

    def reporteDeFichas(PorFechaCommand command) {
        Map repParams = [:];
        repParams['SUCURSAL'] = params['SUCURSAL']
        repParams['FECHA'] = command.fecha
        repParams['ORIGEN'] = params.origen
        def pdf = reportService.run('RelacionDeFichasCaja', repParams)
        render (file: pdf.toByteArray(), contentType: 'application/pdf', filename: "CajaArqueo.pdf")

    }

}

public class CobroContado {

    Venta venta
    List<Cobro> cobros
}

@ToString(includeNames=true,includePackage=false)
public class CambioDeCheque {

    Sucursal sucursal

    Date fecha

    BigDecimal importe

    String nombre

    String emisor

    Banco banco

    Long numero

    String cuenta

    String comentario

    static constraints = {
        comentario nullable: true
    }
}

@ToString(includeNames=true,includePackage=false)
public class PorFacturarCommand {
    String tipo
    Sucursal sucursal


}
