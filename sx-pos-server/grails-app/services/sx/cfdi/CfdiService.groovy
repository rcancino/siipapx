package sx.cfdi

import grails.transaction.Transactional
import lx.cfdi.v33.CfdiUtils
import lx.cfdi.v33.Comprobante
import sx.core.AppConfig

@Transactional
class CfdiService {

  private AppConfig config

  Cfdi generarCfdi(Comprobante comprobante, String tipo) {
    Cfdi cfdi = new Cfdi()
    cfdi.tipoDeComprobante = tipo
    cfdi.fecha = Date.parse( "yyyy-MM-dd'T'HH:mm:ss", comprobante.fecha,)
    cfdi.serie = comprobante.serie
    cfdi.folio = comprobante.folio
    cfdi.emisor = comprobante.emisor.nombre
    cfdi.emisorRfc = comprobante.emisor.getRfc()
    cfdi.receptor = comprobante.receptor.nombre
    cfdi.receptorRfc = comprobante.receptor.rfc
    cfdi.total = comprobante.total
    cfdi.fileName = getFileName(cfdi)
    // cfdi.url = "${getDirPath(cfdi)}/${getFileName(cfdi)}"
    try {
      // save(comprobante, getDirPath(cfdi), cfdi.fileName)
      saveXml(cfdi, CfdiUtils.toXmlByteArray(comprobante))
      cfdi.save failOnError: true, flush:true
      return cfdi
    }catch (Exception ex) {
      ex.printStackTrace()
      return null
    }

  }

  void saveXml(Cfdi cfdi, byte[] data){
    def date = cfdi.fecha
    String year = date[Calendar.YEAR]
    String month = date[Calendar.MONTH]+1
    String day = date[Calendar.DATE]
    def cfdiRootDir = new File(getConfig().cfdiLocation?: System.properties['user.home'])
    final FileTreeBuilder treeBuilder = new FileTreeBuilder(cfdiRootDir)
    treeBuilder{
      dir(cfdi.emisor){
        dir(year){
          dir(month){
            dir(day){
              File res = file(cfdi.fileName) {
                setBytes(data)
              }
              cfdi.url = res.toURI().toURL()
            }
          }
        }
      }
    }
  }

  def save(Comprobante comprobante, String dirPath, String fileName){

    File dir = new File(dirPath)
    if(!dir.exists()) {
      dir.mkdirs()
    }
    byte[] data = CfdiUtils.toXmlByteArray(comprobante)
    File xmlFile = new File(dir, fileName);
    FileOutputStream os = new FileOutputStream(xmlFile, false)
    os.write(data)
    os.flush()
    os.close()
  }


  private getDirPath(Cfdi cfdi) {
    String dirPath = "${getConfig().cfdiLocation}/${cfdi.emisor}/${cfdi.fecha.format('YYYY')}/${cfdi.fecha.format('MM')}"
    return dirPath
  }

  private getFileName(Cfdi cfdi){
    String name = "${cfdi.receptorRfc}-${cfdi.serie}-${cfdi.folio}.xml"
    return name
  }


  AppConfig getConfig() {
    if(!this.config){
      this.config = AppConfig.first()
    }
    return this.config
  }

}
