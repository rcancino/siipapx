import com.luxsoft.CustomAuditLogListener
import com.luxsoft.cfdix.v33.CfdiCadenaBuilder33
import com.luxsoft.cfdix.v33.CfdiSellador33
import com.luxsoft.cfdix.v33.NotaBuilder
import com.luxsoft.cfdix.v33.NotaDeCargoBuilder

// Place your Spring DSL code here
beans = {

    customAuditLogListener(CustomAuditLogListener) {
        dataSource = ref('dataSource')
    }

    cfdiCadenaBuilder(CfdiCadenaBuilder33){}

    cfdiSellador(CfdiSellador33){
        cadenaBuilder = ref('cfdiCadenaBuilder')
    }

    notaBuilder(NotaBuilder){
        sellador = ref('cfdiSellador')
    }
    notaDeCargoBuilder(NotaDeCargoBuilder){
        sellador = ref('cfdiSellador')
    }
}
