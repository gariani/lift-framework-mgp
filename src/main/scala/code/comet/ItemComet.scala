package code.comet

import code.model.Usuario
import net.liftweb.http.{SHtml, CometListener, CometActor}
import net.liftweb.util.Helpers

/**
  * Created by daniel on 24/04/16.
  */
class ItemComet extends CometActor with CometListener {

  private var items = Usuario.findAll()

  def registerWith = ItemsServer

  override def lowPriority = {
    case v: List[Usuario] => items = v; reRender()
  }

  def render = ".tr_content" #> items.map(u =>
    "#row [id]" #> Helpers.nextFuncName &
      "#id *" #> u.idUsuario &
        "#nome *" #> u.nome &
        "#email *" #> u.email &
        "#editar [onclick]" #> SHtml.ajaxInvoke(() => editar(u.idUsuario)) &
        "#deletar [onclick]" #> SHtml.ajaxInvoke(() => deletar(u.idUsuario)))


  def editar(e: Long) = {
    ItemsServer ! Excluir(e)
    println("teste editar" + e)
  }

  def deletar(e: Long) = {
    ItemsServer ! e
    println("teste deletar")
  }


}