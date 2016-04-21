package code.snippet


import java.text.{SimpleDateFormat}
import java.util.Calendar
import code.dao.UsuarioDAO
import code.lib.Validador
import code.lib.session.SessionState
import code.model.Usuario
import net.liftweb.common.{Logger, Full, Empty}
import net.liftweb.http.SHtml.{text}
import net.liftweb.http._
import net.liftweb.http.SHtml.ajaxSubmit
import net.liftweb.http.js.{JsCmds, JsCmd}
import net.liftweb.util.{Helpers}
import org.joda.time.{DateTime}
import scala.xml.{NodeSeq}
import net.liftweb._
import util.Helpers._


/**
  * Created by daniel on 15/01/16.
  */
class PerfilUsuario extends StatefulSnippet with Logger {

  private var id_usuario: Long = 0
  private var nome: String = ""
  private var email: String = ""
  private var cargo: String = ""
  private var senha: String = ""
  private var telefone: Option[Long] = None
  private var observacao: String = ""
  private var sexo: String = "0"
  private var estadoCivil: String = "0"
  private var mes_empresa: String = "0"
  private var ano_empresa: String = "0"
  private var dia_nasc: String = "0"
  private var mes_nasc: String = "0"
  private var ano_nasc: String = "0"
  private lazy val meses = intervaloMeses
  private lazy val anos = intervaloAnos
  private lazy val dias = intervaloDias
  private lazy val lestadoCivil = getEstadoCivil
  private lazy val lsexo = getSexo

  carregarDados

  def carregarDados = {

    var usuarioDAO = new UsuarioDAO

    val usuario = usuarioDAO.findByEmail(SessionState.getLogin)

    usuario match {
      case Some(u) => {

        id_usuario = u.idUsuario
        nome = u.nome
        email = u.email
        telefone = u.telefone
        senha = u.senha

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
      "name=telefone" #> SHtml.text(telefone.get.toString, (v) => formatarTelefone(v)) &
      "name=cargo" #> SHtml.text(cargo, cargo = _) &
      "name=observacao" #> SHtml.textarea(observacao, observacao = _) &
      "#desde_mes" #> SHtml.ajaxSelect(meses, Full(mes_empresa), (m) => mes_empresa = m, "style" -> "width:130px;") &
      "#desde_ano" #> SHtml.ajaxSelect(anos, Full(ano_empresa), (a) => ano_empresa = a, "style" -> "width:90px;") &
      "#sexo" #> SHtml.ajaxSelect(lsexo, Full(sexo), (s) => sexo = s, "style" -> "width:130px;") &
      "#nasc_dia" #> SHtml.ajaxSelect(dias, Full(dia_nasc), (d) => dia_nasc = d, "style" -> "width:90px;") &
      "#nasc_mes" #> SHtml.ajaxSelect(meses, Full(mes_nasc), (m) => mes_nasc = m, "style" -> "width:130px;") &
      "#nasc_ano" #> SHtml.ajaxSelect(anos, Full(ano_nasc), (a) => ano_nasc = a, "style" -> "width:90px;") &
      "#civil" #> SHtml.ajaxSelect(lestadoCivil, Full(estadoCivil), (e) => estadoCivil = e, "style" -> "width:130px;") &
      "type=submit" #> ajaxSubmit("Atualizar", () => atualizar)
  }

  private def mensagemErro(msg: String): NodeSeq = {
    <div class="alert alert-danger alert-dismissible" role="alert">
      <button type="button" class="close" data-dismiss="alert" aria-label="Close">
        <span aria-hidden="true">
          &times;
        </span>
      </button>{msg}
    </div>
  }

  private def atualizar: JsCmd = {

    S.clearCurrentNotices

    if (!Validador.validarMinTamanhoNome(nome)) {
      S.error("perfilError", mensagemErro("Nome deve conter no mínimo 4 caracteres."))
    }
    else if (!Validador.validarMaxTamanhoNome(nome)) {
      S.error("perfilError", mensagemErro("Nome muito grande, há mais de 100 caracteres."))
    }
    else if (!Validador.validarEmail(email)) {
      S.error("perfilError", mensagemErro("Email incorreto"))
    }
    else {
      salvar
    }

    JsCmds.Noop
  }

  private def formatarTelefone(t: String): String = {
    t.isEmpty match {
      case true => telefone = None
        ""
      case false => t.matches("^\\d*$") match {
        case true => telefone = Some(t.toLong)
          t
        case false => telefone = None
          ""
      }
    }
  }

  private def formatarDataInicioEmpresa: Option[org.joda.time.DateTime] = {
    var formatar = new SimpleDateFormat("dd/MM/yyyy")
    var parse = formatar.parse("01" + "/" + mes_empresa + "/" + ano_empresa)
    var c = Calendar.getInstance()
    c.setTime(parse)
    var data = new DateTime(c.getTime)
    Some(data)
  }

  private def formatarDataNascimento: Option[DateTime] = {
    var formatar = new SimpleDateFormat("dd/MM/yyyy")
    var parse = formatar.parse(dia_nasc + "/" + mes_nasc + "/" + ano_nasc)
    var c = Calendar.getInstance()
    c.setTime(parse)
    var data = new DateTime(c.getTime)
    Some(data)
  }

  private def salvar() = {

    var inicioEmpresa = formatarDataInicioEmpresa
    var nascimento = formatarDataNascimento

    var u = new Usuario(id_usuario,
      email,
      nome,
      Some(cargo),
      Some(observacao),
      telefone,
      senha,
      inicioEmpresa,
      nascimento,
      Some(sexo.toInt),
      Some(estadoCivil.toInt),
      DateTime.now,
      None)

    Usuario.save(u)
  }

  private def definirSexo(sexo: Option[Int]): String = {
    sexo match {
      case Some(s) => getSexo(s)._1
      case None => ""
    }
  }

  private def definiEstadoCicil(estadoCivil: Option[Int]): String = {
    estadoCivil match {
      case Some(ec) => getEstadoCivil(ec)._1
      case None => ""
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

  private def intervaloMeses = {
    Map(-1 -> "", 0 -> "Janeiro", 1 -> "Fevereiro", 2 -> "Março", 3 -> "Abril", 4 -> "Maio", 5 -> "Junho", 6 -> "Julho", 7 -> "Agosto", 8 -> "Setembro", 9 -> "Outubro", 10 -> "Novembro", 11 -> "Dezembro")
      .toList.map { case (i, j) => (Util.formataNum(i), j) }.sorted
  }

  private def intervaloAnos = {
    val inicio = (0 to 0).map(i => (Util.formataNum(i), ""));
    val intervalo = (1900 to 2016).map(i => (Util.formataNum(i), i.toString));
    inicio ++ intervalo
  }

  private def getDia(d: Int) = {
    intervaloDias(d)._1
  }

  private def intervaloDias = {
    val inicio = (0 to 0).map(i => (Util.formataNum(i), ""));
    val intervalo = (1 to 31).map(i => (Util.formataNum(i), i.toString));
    inicio ++ intervalo
  }

  private def getSexo = {
    Map(0 -> "", 1 -> "Masculino", 2 -> "Feminino").toList.map { case (i, s) => (Util.formataNum(i), s) }
  }

  private def getEstadoCivil = {
    Map(0 -> "", 1 -> "Solteiro", 2 -> "Casado", 3 -> "Divorciado").toList.map { case (i, ec) => (Util.formataNum(i), ec) }
  }

}
