package code
package model

import code.lib.Settings
import scalikejdbc._
import net.liftweb.common._

case class Usuario(id_usuario: Int, email: String, nome: String, cargo: Option[String] = None,
                   observacao: Option[String] = None, telefone: Long, id_perfil: Option[Int] = None,id_papel: Option[Int] = None,
                   id_ambiente: Option[Int] = None, id_planejamento: Option[Int] = None, permissao: Option[String] = None, senha: Option[String] = None)

object Usuario extends SQLSyntaxSupport[Usuario] with Settings {

  override val tableName = "usuario"

  override val columns = Seq("id_usuario", "email", "nome", "cargo", "observacao", "telefone", "id_papel", "id_ambiente",
    "id_perfil", "id_planejamento", "permissao", "senha")

  def apply(u: SyntaxProvider[Usuario])(rs: WrappedResultSet): Usuario = apply(u.resultName)(rs)

  def apply(u: ResultName[Usuario])(rs: WrappedResultSet): Usuario = new Usuario(
    id_usuario = rs.get(u.id_usuario),
    email = rs.get(u.email),
    nome = rs.get(u.nome),
    cargo = rs.stringOpt(u.cargo),
    observacao = rs.stringOpt(u.observacao),
    telefone = rs.get(u.telefone),
    id_papel = rs.intOpt(u.id_papel),
    id_ambiente = rs.intOpt(u.id_ambiente),
    id_perfil = rs.intOpt(u.id_perfil),
    id_planejamento = rs.intOpt(u.id_planejamento),
    permissao = rs.stringOpt(u.permissao),
    senha = rs.stringOpt(u.senha)
  )

  val u = Usuario.syntax("u")

  def findByEmail(email: String)(implicit session: DBSession = autoSession): Option[String] = withSQL {
      select(u.result.email).from(Usuario as u).where.eq(u.email, email)
    }.map(_.string(u.resultName.email)).single().apply()

  def findByLogin(email: String, senha: String)(implicit session: DBSession = autoSession): Option[String] = withSQL {
    select(u.email).from(Usuario as u).where.eq(u.email, email).and.eq(u.senha, senha)
  }.map(_.string(u.email)).single().apply()

  def findAll()(implicit session: DBSession = autoSession): List[Usuario] = withSQL {
    select.from(Usuario as u)
  }.map(Usuario(u)).list().apply()

  def findUser(email: String)(implicit session: DBSession = autoSession): Option[Usuario] = withSQL {
      select.from(Usuario as u).where.eq(u.email, email)
    }.map(Usuario(u)).single().apply()

  def save(usuario: Usuario)(implicit session:DBSession = autoSession) = applyUpdate {
    update(Usuario).set(
      Usuario.column.email -> usuario.email,
      Usuario.column.nome -> usuario.nome,
      Usuario.column.cargo -> usuario.cargo,
      Usuario.column.observacao -> usuario.observacao,
      Usuario.column.telefone -> usuario.telefone
    ).where.eq(Usuario.column.id_usuario, usuario.id_usuario)
  }

}