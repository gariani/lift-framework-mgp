package code.lib

/**
  * Created by daniel on 10/04/16.
  */
trait GenericSnippet[T] {

  protected def salvar()
  protected def validarValores(): Boolean


}
