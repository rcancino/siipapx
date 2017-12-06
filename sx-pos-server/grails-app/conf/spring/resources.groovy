import com.luxsoft.CustomAuditLogListener

// Place your Spring DSL code here
beans = {
    customAuditLogListener(CustomAuditLogListener) {
        dataSource = ref('dataSource')
    }

}
