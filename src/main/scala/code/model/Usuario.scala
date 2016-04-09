package code.model

import code.lib.Settings
import org.joda.time.DateTime
import scalikejdbc._

case class Usuario(idUsuario: Long,
                   email: String,
                   nome: String,
                   cargo: Option[String] = None,
                   observacao: Option[String] = None,
                   telefone: Long,
                   senha: String,
                   createdAt: DateTime,
                   deletedAt: Option[DateTime] = None)
{
  def save()(implicit session: DBSession = Usuario.autoSession): Usuario = Usuario.save(this)(session)

  def destroy()(implicit session: DBSession = Usuario.autoSession): Unit = Usuario.destroy(idUsuario)(session)

  private val (u) = (Usuario.u)
}

object Usuario extends SQLSyntaxSupport[Usuario] with Settings {

  override val tableName = "usuario"

  override val columns = Seq("id_usuario", "email", "nome", "cargo", "observacao", "telefone", "senha", "created_at", "deleted_at")

  def apply(u: SyntaxProvider[Usuario])(rs: WrappedResultSet): Usuario = apply(u.resultName)(rs)
  def apply(u: ResultName[Usuario])(rs: WrappedResultSet): Usuario = new Usuario(
    idUsuario = rs.get(u.idUsuario),
    email = rs.get(u.email),
    nome = rs.get(u.nome),
    cargo = rs.stringOpt(u.cargo),
    observacao = rs.stringOpt(u.observacao),
    telefone = rs.get(u.telefone),
    senha = rs.string(u.senha),
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
    withSQL {
      update(Usuario).set(
        Usuario.column.email -> usuario.email,
        Usuario.column.nome -> usuario.nome,
        Usuario.column.cargo -> usuario.cargo,
        Usuario.column.observacao -> usuario.observacao,
        Usuario.column.senha -> usuario.senha,
        Usuario.column.telefone -> usuario.telefone,
        Usuario.column.createdAt -> usuario.createdAt
      ).where.eq(Usuario.column.idUsuario, usuario.idUsuario).and.isNull(column.deletedAt)
    }
    usuario
  }

  def create(email: String, nome: String, cargo: Option[String] = None, observacao: Option[String] = None,
             telefone: Long, senha: String, createdAt: DateTime = DateTime.now)(implicit session: DBSession = autoSession): Usuario = {

    val id = withSQL {
      insert.into(Usuario).namedValues(
        column.email -> email,
        column.nome -> nome,
        column.cargo -> cargo,
        column.observacao -> observacao,
        column.telefone -> telefone,
        column.senha -> senha,
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
      createdAt = createdAt
    )
  }

  def destroy(idUsuario: Long)(implicit session: DBSession = autoSession): Unit = withSQL {
    update(Usuario).set(column.deletedAt -> DateTime.now).where.eq(column.idUsuario, idUsuario)
  }
}