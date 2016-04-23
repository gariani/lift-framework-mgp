package code.snippet


import java.sql.SQLException
import java.text.{MessageFormat, SimpleDateFormat}
import java.util.Calendar
import code.dao.UsuarioDAO
import code.lib.{Util, Validador}
import code.model.Usuario
import net.liftweb.common.{Empty, Logger, Full}
import net.liftweb.http.SHtml.{text}
import net.liftweb.http._
import net.liftweb.http.SHtml.ajaxSubmit
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.js.{JsCmds, JsCmd}
import org.joda.time.{DateTime}
import scala.xml.{NodeSeq}
import net.liftweb._
import util.Helpers._
import code.lib.Util._

/**
  * Created by daniel on 15/01/16.
  */
class SPerfilUsuario extends StatefulSnippet with Logger {

  private var id_usuario: Long = 0
  private var nome: String = ""
  private var email: String = ""
  private var cargo: String = ""
  private var senha: String = ""
  private var telefone: String = ""
  private var observacao: String = ""
  private var sexo: String = "0"
  private var estadoCivil: String = "0"
  private var mes_empresa: String = "0"
  private var ano_empresa: String = "0"
  private var dia_nasc: String = "0 "
  private var mes_nasc: String = "0"
  private var ano_nasc: String = "0"
  private lazy val meses = intervaloMeses
  private lazy val anos = intervaloAnos
  private lazy val dias = intervaloDias
  private lazy val lestadoCivil = getEstadoCivil
  private lazy val lsexo = getSexo

  private val definirUsuario = editarPerfilUsuario.is

  carregarDados

  def carregarDados = {
    var usuarioDAO = new UsuarioDAO
    val usuario = usuarioDAO.findByEmail(definirUsuario.getOrElse(""))
    usuario match {
      case Some(u) => {
        id_usuario = u.idUsuario
        nome = u.nome
        email = u.email
        senha = u.senha
        telefone = converterTelefone(u.telefone)
        observacao = definirObservacao(u.observacao)
        cargo = definiCargo(u.cargo)
        sexo = definirSexo(u.sexo)
        estadoCivil = definiEstadoCicil(u.estadoCivil)
        definirInicioEmpresa(u.inicioEmpresa)
        definirNascimento(u.nascimento)
      }
      case None => JsCmds.Noop
    }
  }

  def dispatch = {
    case "render" => render
  }

  def render = {
    "name=nome" #> SHtml.text(nome, nome = _) &
      "name=email" #> SHtml.text(email, email = _) &
      "name=telefone" #> SHtml.ajaxText(telefone, (v) => telefone = formatarTelefone(v)) &
      "name=cargo" #> SHtml.text(cargo, cargo = _) &
      "name=observacao" #> SHtml.textarea(observacao, observacao = _) &
      "#desde_mes" #> SHtml.ajaxSelect(meses, Full(mes_empresa), (m) => mes_empresa = m, "style" -> "width:130px;padding-right: 10px;padding-left: 10px;") &
      "#desde_ano" #> SHtml.ajaxSelect(anos, Full(ano_empresa), (a) => ano_empresa = a, "style" -> "width:90px;padding-right: 10px;padding-left: 10px;") &
      "#sexo" #> SHtml.ajaxSelect(lsexo, Full(sexo), (s) => sexo = s, "style" -> "width:130px;") &
      "#nasc_dia" #> SHtml.ajaxSelect(dias, Full(dia_nasc), (d) => dia_nasc = d, "style" -> "width:90px;") &
      "#nasc_mes" #> SHtml.ajaxSelect(meses, Full(mes_nasc), (m) => mes_nasc = m, "style" -> "width:130px;") &
      "#nasc_ano" #> SHtml.ajaxSelect(anos, Full(ano_nasc), (a) => ano_nasc = a, "style" -> "width:90px;") &
      "#civil" #> SHtml.ajaxSelect(lestadoCivil, Full(estadoCivil), (e) => estadoCivil = e, "style" -> "width:130px;") &
      "type=submit" #> ajaxSubmit("Atualizar", () => atualizar)
  }

  private def validarMesmoEmail: Boolean = {
    editarPerfilUsuario.is match {
      case Full(e) => email == e
      case Empty => false
      case _ => false
    }
  }

  private def atualizar: JsCmd = {
    if (validarNome(nome)) {
      SetHtml("perfil", mensagemErro(MensagemUsuario.INTERVALO_VALOR.format(5, 100)))
    }
    else if (validarEmail(email)) {
      SetHtml("perfil", mensagemErro(MensagemUsuario.EMAIL_INVALIDO))
    }
    else if (!validarMesmoEmail && !Usuario.isExistsEmail(email).isEmpty) {
      SetHtml("perfil", mensagemErro(MensagemUsuario.EMAIL_JA_USADO))
    }
    else {
      if (salvar) {
        SetHtml("perfil", mensagemSucesso(MensagemUsuario.DADOS_SALVOS_SUCESSO))
      }
      else {
        SetHtml("perfil", mensagemErro(MensagemUsuario.ERRO_SALVAR_DADOS))
      }
    }
  }

  private def formatarDataInicioEmpresa: Option[org.joda.time.DateTime] = {
    if ((mes_empresa == "-1") || (ano_empresa == "0") || (mes_empresa == "") || (ano_empresa == "")) {
      Some(null)
    }
    else {
      var formatar = new SimpleDateFormat("dd/MM/yyyy")

      var mesAjuste = (BigDecimal(mes_empresa) + 1).toString

      var parse = formatar.parse("01" + "/" + mesAjuste + "/" + ano_empresa)
      var c = Calendar.getInstance()
      c.setTime(parse)
      var data = new DateTime(c.getTime)
      Some(data)
    }
  }

  private def formatarDataNascimento: Option[DateTime] = {
    if ((dia_nasc == "0") || (mes_nasc == "-1") || (ano_nasc == "0") || (dia_nasc == "")
      || (mes_nasc == "") || (ano_nasc == "")) {
      Some(null)
    }
    else {
      var formatar = new SimpleDateFormat("dd/MM/yyyy")

      var mesAjuste = (BigDecimal(mes_nasc) + 1).toString

      var parse = formatar.parse(dia_nasc + "/" + mesAjuste + "/" + ano_nasc)
      var c = Calendar.getInstance()
      c.setTime(parse)
      var data = new DateTime(c.getTime)
      Some(data)
    }
  }

  private def salvar(): Boolean = {

    var inicioEmpresa = formatarDataInicioEmpresa
    var nascimento = formatarDataNascimento
    telefone = removerFormatacao(telefone)

    var u = new Usuario(id_usuario,
      email,
      nome,
      Some(cargo),
      Some(observacao),
      Some(telefone),
      senha,
      inicioEmpresa,
      nascimento,
      Some(sexo.toInt),
      Some(estadoCivil.toInt),
      DateTime.now,
      None)

    try {
      Usuario.save(u)
      true
    } catch {
      case _: SQLException | _: IndexOutOfBoundsException => println("Erro!")
        false
      case e: Throwable => println("Erro ao salvar!")
        false
    }


  }

  private def definirSexo(sexo: Option[Int]): String = {
    sexo match {
      case Some(s) => getSexo(s)._1
      case None => "0"
    }
  }

  private def definiEstadoCicil(estadoCivil: Option[Int]): String = {
    estadoCivil match {
      case Some(ec) => getEstadoCivil(ec)._1
      case None => "0"
    }
  }

  private def definirInicioEmpresa(inicioEmpresa: Option[DateTime]) = {
    inicioEmpresa match {
      case Some(hm) => {

        var data = new SimpleDateFormat("dd/MM/yyyy")
        var parse = data.parse(hm.toString("dd/MM/yyyy"))
        var c = Calendar.getInstance()

        c.setTime(parse)
        mes_empresa = Util.formataNum(c.get(Calendar.MONTH))
        ano_empresa = c.get(Calendar.YEAR).toString
      }
      case None => {
        mes_empresa = ""
        ano_empresa = ""
      }
    }
  }

  private def definirNascimento(nascimento: Option[DateTime]) = {
    nascimento match {
      case Some(hm) => {
        var data = new SimpleDateFormat("dd/MM/yyyy")
        var parse = data.parse(hm.toString("dd/MM/YYYY"))
        var c = Calendar.getInstance()
        c.setTime(parse)
        dia_nasc = Util.formataNum(c.get(Calendar.DAY_OF_MONTH))
        mes_nasc = Util.formataNum(c.get(Calendar.MONTH))
        ano_nasc = c.get(Calendar.YEAR).toString
      }
      case None => {
        dia_nasc = ""
        mes_nasc = ""
        ano_nasc = ""
      }
    }
  }

  private def definirObservacao(obs: Option[String]) = {
    obs match {
      case Some(o) => o
      case None => ""
    }
  }

  private def definiCargo(c: Option[String]) = {
    c match {
      case Some(o) => o
      case None => ""
    }
  }

}
