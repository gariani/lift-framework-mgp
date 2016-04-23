package code.lib

import java.text.MessageFormat

import code.snippet.editarPerfilUsuario
import net.liftweb.common.{Empty, Full}

import scala.xml.NodeSeq

/**
  * Created by daniel on 12/01/16.
  */
object Util {

  def mensagemErro(msg: String): NodeSeq = {
    <div id="alertaMensagem" class="alert alert-warning alert-dismissible fade in" role="alert">
      <button type="button" class="close" data-dismiss="alert" aria-label="Close">
        <span aria-hidden="true">
          &times;
        </span>
      </button> <h6>
      {msg}
    </h6>
    </div>
  }

  def mensagemSucesso(msg: String): NodeSeq = {
    <div id="sucessoaMensagem" class="alert alert-success alert-dismissible fade in" role="alert">
      <button type="button" class="close" data-dismiss="alert" aria-label="Close">
        <span aria-hidden="true">
          &times;
        </span>
      </button> <h6>
      {msg}
    </h6>
    </div>
  }

  def formataNum(i: Int): String = {
    i.toString.length match {
      case 1 => "0" + i.toString
      case _ => i.toString
    }
  }

  def validarEmail(e: String) = {
    val v = Option(e) map (_.trim) getOrElse ""
    val bool = v.isEmpty() || !v.matches("""(?i)\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,4}\b""")
    bool
  }

  def validarNome(n: String) = {
    var bool = (n.length > 100 || n.length < 5) && n.isEmpty
    bool
  }

  def validarSenha(s: String): Boolean = {
    var bool = (s.length < 5) && s.isEmpty
    bool
  }

  object MensagemUsuario {
    val REQUERIDO = "Campo obrigatório!"
    val REMOTE = "Por favor, corrija este campo."
    val EMAIL_INVALIDO = "Por favor, forneça um endereço eletrônico válido."
    val URL = "Por favor, forneça uma URL válida."
    val DATA = "Por favor, forneça uma data válida."
    val NUMERO = "Por favor, forneça um número válido."
    val DIGITOS = "Por favor, forneça somente dígitos."
    val MESMO_VALOR = "Por favor, forneça o mesmo valor novamente."
    val MAXIMO_VALOR = "Por favor, forneça não mais que %d caracteres."
    val MENOR_VALOR = "Por favor, forneça ao menos %d caracteres."
    val INTERVALO_VALOR = "Por favor, forneça um valor entre %d e %d caracteres de comprimento."
    val INTERVALO = "Por favor, forneça um valor entre %d e %d."
    val MAX = "Por favor, forneça um valor menor ou igual a %d."
    val MIN = "Por favor, forneça um valor maior ou igual a %d."
    val EMAIL_JA_USADO = "Endereço eletrônico já utilizado."
    val TAM_SENHA = "Por favor, forneça um valor maior ou igual a %d para senha."
    val ERRO_SALVAR_DADOS = "Ocorreu um erro ao salvar os dados. Entre em contato com o suporte."
    val DADOS_SALVOS_SUCESSO = "Dados atualizados com sucesso."
  }

  def intervaloHora = {
    (0 to 23).toList.map(i => (Util.formataNum(i), Util.formataNum(i)))
  }

  def intervaloMin = {
    (0 to 59).toList.map(i => (Util.formataNum(i), Util.formataNum(i)))
  }

  def removerFormatacao(telefone: String): String = {
    telefone.replaceAll("[^\\d.]", "")
  }

  def converterTelefone(tel: Option[String]) = {
    val t = tel match {
      case Some(t) => t
      case None => ""
    }
    formatarTelefone(t)
  }

  def formatarTelefone(tel: String): String = {
    if (!tel.isEmpty) {
      val foneFormat = new MessageFormat("({0}) {1}-{2}")
      val telefoneOpt = Option(tel)
      val formatado = telefoneOpt.map { s => Array(s.substring(0, 3), s.substring(3, 6), s.substring(6)) }
        .map(foneFormat.format).getOrElse("")
      formatado
    }
    else {
      tel
    }
  }

  def getDia(d: Int) = {
    intervaloDias(d)._1
  }

  def intervaloDias = {
    val inicio = (0 to 0).map(i => (Util.formataNum(i), ""));
    val intervalo = (1 to 31).map(i => (Util.formataNum(i), i.toString));
    inicio ++ intervalo
  }

  def intervaloMeses = {
    Map(-1 -> "", 0 -> "Janeiro", 1 -> "Fevereiro", 2 -> "Março", 3 -> "Abril",
      4 -> "Maio", 5 -> "Junho", 6 -> "Julho", 7 -> "Agosto", 8 -> "Setembro",
      9 -> "Outubro", 10 -> "Novembro", 11 -> "Dezembro").toList.map { case (i, j) => (Util.formataNum(i), j) }.sorted
  }

  def intervaloAnos = {
    val inicio = (0 to 0).map(i => (Util.formataNum(i), ""));
    val intervalo = (1900 to 2016).map(i => (Util.formataNum(i), i.toString));
    inicio ++ intervalo
  }

  def getSexo = {
    Map(0 -> "", 1 -> "Masculino", 2 -> "Feminino").toList.map { case (i, s) => (Util.formataNum(i), s) }
  }

  def getEstadoCivil = {
    Map(0 -> "", 1 -> "Solteiro", 2 -> "Casado", 3 -> "Divorciado").toList.map { case (i, ec) => (Util.formataNum(i), ec) }
  }


}
