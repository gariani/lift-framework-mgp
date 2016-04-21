package code.snippet

import scala.xml.NodeSeq

/**
  * Created by daniel on 12/01/16.
  */
object Util {

  def mensagemErro(msg: String): NodeSeq = {
    <div class="alert alert-danger alert-dismissible" role="alert">
      <button type="button" class="close" data-dismiss="alert" aria-label="Close">
        <span aria-hidden="true">
          &times;
        </span>
      </button>{msg}
    </div>
  }

  def formataNum(i: Int): String = {
    i.toString.length match {
      case 1 => "0" + i.toString
      case _ => i.toString
    }
  }



}
