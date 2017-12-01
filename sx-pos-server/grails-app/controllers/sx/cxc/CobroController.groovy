package sx.cxc

import grails.rest.RestfulController
import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional
import groovy.transform.ToString

import sx.core.Venta
import sx.tesoreria.Banco
import sx.core.Sucursal
import sx.core.Cliente

@Secured("hasRole('ROLE_CXC_USER')")
class CobroController extends RestfulController{

    def cobroService

    def ventaService

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

    protected Cobro saveResource(Cobro resource) {
        println 'Salvando cobro: ' + resource
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
        // println 'Cambio de cheque : '+ cambio
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
        // def cobros = Cobro.where { cliente == cliente && (importe - aplicado) > 0}.list(params)
        def cobros = Cobro.findAll(' from Cobro c where c.cliente = ? and c.importe - c.aplicado > 0', cliente)
        respond cobros
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
