package sx.cx

import grails.compiler.GrailsCompileStatic
import grails.rest.RestfulController

import sx.cxc.ChequeDevuelto
import sx.cxc.ChequeDevueltoService
import sx.reports.ReportService

@GrailsCompileStatic
class ChequeDevueltoController extends RestfulController<ChequeDevuelto> {

    ChequeDevueltoService chequeDevueltoService

    ReportService reportService

    ChequeDevueltoController() {
        super(ChequeDevuelto);
    }

    @Override
    protected ChequeDevuelto saveResource(ChequeDevuelto resource) {
        return this.chequeDevueltoService.save(resource)
    }

    @Override
    protected ChequeDevuelto updateResource(ChequeDevuelto resource) {
        return this.chequeDevueltoService.update(resource)
    }
}
