package sx.cxc


import grails.rest.*


class JuridicoController extends RestfulController<Juridico> {

    static responseFormats = ['json']

    JuridicoController() {
        super(Juridico)
    }

    @Override
    protected List<Juridico> listAllResources(Map params) {
        log.info('List: {}', params)
        return super.listAllResources(params)
    }

    @Override
    protected Juridico createResource() {
        Juridico juridico = new Juridico()
        bindData juridico, getObjectToBind()
        CuentaPorCobrar cxc = juridico.cxc
        juridico.saldo = cxc.saldo
        juridico.importe = cxc.total
        juridico.nombre = cxc.cliente.nombre
        // log.info('Jur: {}', juridico)
        return juridico
    }

    @Override
    protected Juridico saveResource(Juridico resource) {
        CuentaPorCobrar cxc = resource.cxc
        cxc.juridico = resource.traspaso
        cxc.save flush: true
        resource.save flush: true
        resource.save flush: true
    }
}
