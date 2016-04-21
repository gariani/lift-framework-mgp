package code.model

import java.sql.Date

import code.lib.Settings
import org.joda.time.DateTime
import scalikejdbc._

case class Usuario(idUsuario: Long,
                   email: String,
                   nome: String,
                   cargo: Option[String] = None,
                   observacao: Option[String] = None,
                   telefone: Option[Long],
                   senha: String,
                   inicioEmpresa: Option[DateTime],
                   nascimento: Option[DateTime],
                   sexo: Option[Int],
                   estadoCivil: Option[Int],
                   createdAt: DateTime,
                   deletedAt: Option[DateTime] = None) {
  def save()(implicit session: DBSession = Usuario.autoSession): Usuario = Usuario.save(this)(session)

  def destroy()(implicit session: DBSession = Usuario.autoSession): Unit = Usuario.destroy(idUsuario)(session)

  private val (u) = (Usuario.u)
}

object Usuario extends SQLSyntaxSupport[Usuario] with Settings {

  override val tableName = "usuario"

  override val columns = Seq("id_usuario", "email", "nome", "cargo", "observacao", "telefone", "senha", "inicio_empresa", "nascimento", "sexo", "estado_civil", "created_at", "deleted_at")

  def apply(u: SyntaxProvider[Usuario])(rs: WrappedResultSet): Usuario = apply(u.resultName)(rs)

  def apply(u: ResultName[Usuario])(rs: WrappedResultSet): Usuario = new Usuario(
    idUsuario = rs.get(u.idUsuario),
    email = rs.get(u.email),
    nome = rs.get(u.nome),
    cargo = rs.stringOpt(u.cargo),
    observacao = rs.stringOpt(u.observacao),
    telefone = rs.longOpt(u.telefone),
    senha = rs.string(u.senha),
    inicioEmpresa = rs.jodaDateTimeOpt(u.inicioEmpresa),
    nascimento = rs.jodaDateTimeOpt(u.nascimento),
    sexo = rs.intOpt(u.sexo),
    estadoCivil = rs.intOpt(u.estadoCivil),
    createdAt = rs.get(u.createdAt),
    deletedAt = rs.jodaDateTimeOpt(u.deletedAt)
  )

  val u = Usuario.syntax("u")

  //private val isNotDeleted = sqls.isNull(u.deletedAt)

  def findByEmail(email: String)(implicit session: DBSession = autoSession): Option[Usuario] = withSQL {
    select.from(Usuario as u).where.eq(u.email, email)
  }.map(Usuario(u)).single().apply()

  def findByLogin(email: String, senha: String)(implicit session: DBSession = autoSession): Option[String] = withSQL {
    select(u.email).from(Usuario as u).where.eq(u.email, email).and.eq(u.senha, senha)
  }.map(_.string(u.email)).single().apply()

  def findAllUsuarios()(implicit session: DBSession = autoSession): List[Usuario] = withSQL {
    select.from(Usuario as u)
  }.map(Usuario(u)).list().apply()

  def save(usuario: Usuario)(implicit session: DBSession = autoSession): Usuario = {
    print(usuario)
    withSQL {
      update(Usuario).set(
        Usuario.column.email -> usuario.email,
        Usuario.column.nome -> usuario.nome,
        Usuario.column.cargo -> usuario.cargo,
        Usuario.column.observacao -> usuario.observacao,
        Usuario.column.senha -> usuario.senha,
        Usuario.column.telefone -> usuario.telefone,
        Usuario.column.inicioEmpresa -> usuario.inicioEmpresa,
        Usuario.column.nascimento -> usuario.nascimento,
        Usuario.column.sexo -> usuario.sexo,
        Usuario.column.estadoCivil -> usuario.estadoCivil
      ).where.eq(Usuario.column.idUsuario, usuario.idUsuario).and.isNull(column.deletedAt)
    }.update.apply()
    usuario
  }

  def create(email: String, nome: String, cargo: Option[String] = None, observacao: Option[String] = None,
             telefone: Option[Long] = None, senha: String, inicioEmpresa: Option[DateTime], nascimento: Option[DateTime],
             sexo: Option[Int], estadoCivil: Option[Int], createdAt: DateTime = DateTime.now)
            (implicit session: DBSession = autoSession): Usuario = {

    val id = withSQL {
      insert.into(Usuario).namedValues(
        column.email -> email,
        column.nome -> nome,
        column.cargo -> cargo,
        column.observacao -> observacao,
        column.telefone -> telefone,
        column.senha -> senha,
        column.inicioEmpresa -> inicioEmpresa,
        column.nascimento -> nascimento,
        column.sexo -> sexo,
        column.estadoCivil -> estadoCivil,
        column.createdAt -> createdAt
      )
    }.updateAndReturnGeneratedKey.apply()

    Usuario(
      idUsuario = id,
      email = email,
      nome = nome,
      cargo = cargo,
      observacao = observacao,
      telefone = telefone,
      senha = senha,
      inicioEmpresa = inicioEmpresa,
      nascimento = nascimento,
      sexo = sexo,
      estadoCivil = estadoCivil,
      createdAt = createdAt
    )
  }

  def destroy(idUsuario: Long)(implicit session: DBSession = autoSession): Unit = withSQL {
    update(Usuario).set(column.deletedAt -> DateTime.now).where.eq(column.idUsuario, idUsuario)
  }
}