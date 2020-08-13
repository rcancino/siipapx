import com.luxsoft.CustomAuditLogListener
import com.luxsoft.cfdix.v33.CfdiCadenaBuilder33
import com.luxsoft.cfdix.v33.CfdiSellador33
import com.luxsoft.cfdix.v33.NotaBuilder
import com.luxsoft.cfdix.v33.NotaDeCargoBuilder
import com.luxsoft.cfdix.v33.ReciboDePagoBuilder
import com.luxsoft.cfdix.v33.NotaDeCargoPdfGenerator
import org.springframework.web.servlet.i18n.FixedLocaleResolver
import sx.security.UserInfoClaimpProvider

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
    reciboDePagoBuilder(ReciboDePagoBuilder){
        sellador = ref('cfdiSellador')
    }

    notaDeCargoPdfGenerator(NotaDeCargoPdfGenerator){
        cfdiLocationService = ref('cfdiLocationService')
    } 

    localeResolver(FixedLocaleResolver, Locale.US){
        defaultLocale = new Locale('es', 'MX')
        Locale.setDefault(defaultLocale)
    }

    userInfoClaimpProvider(UserInfoClaimpProvider) {}
}
