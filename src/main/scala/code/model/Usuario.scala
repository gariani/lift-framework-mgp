package code
package model

import code.lib.Settings
import scalikejdbc._

case class Usuario(usuario_id: Int, nome: String, email: String, cargo: Option[String] = None,
                   observacao: Option[String] = None, telefone: Option[Long] = None, id_perfil: Option[Int] = None,id_papel: Option[Int] = None,
                   id_ambiente: Option[Int] = None, id_planejamento: Option[Int] = None, permissao: Option[String] = None, senha: String)

object Usuario extends SQLSyntaxSupport[Usuario] with Settings {

  override val tableName = "usuario"

  override val columns = Seq("usuario_id", "nome", "email", "cargo", "observacao", "telefone", "id_papel", "id_ambiente",
    "id_perfil", "id_planejamento", "permissao", "senha")

  def apply(u: SyntaxProvider[Usuario])(rs: WrappedResultSet): Usuario = apply(u.resultName)(rs)

  def apply(u: ResultName[Usuario])(rs: WrappedResultSet): Usuario = new Usuario(
    usuario_id = rs.get(u.usuario_id),
    nome = rs.get(u.nome),
    email = rs.get(u.email),
    cargo = rs.stringOpt(u.cargo),
    observacao = rs.stringOpt(u.cargo),
    telefone = rs.longOpt(u.telefone),
    id_papel = rs.intOpt(u.id_papel),
    id_ambiente = rs.intOpt(u.id_ambiente),
    id_perfil = rs.intOpt(u.id_perfil),
    id_planejamento = rs.intOpt(u.id_planejamento),
    permissao = rs.stringOpt(u.permissao),
    senha = rs.get(u.senha)
  )

  val u = Usuario.syntax("u")

  def findByEmail(email: String)(implicit session: DBSession = autoSession): Option[String] = withSQL {
      select(u.result.email).from(Usuario as u).where.eq(u.email, email)
    }.map(_.string(u.resultName.email)).single().apply()

  def findByLogin(email: String, senha: String)(implicit session: DBSession = autoSession): Option[(String, String)] = withSQL {
    select(u.result.usuario_id, u.result.email, u.result.senha).from(Usuario as u).where.eq(u.email,email).and.eq(u.senha, senha)
  }.map(rs => (u.resultName.email.toString(), u.resultName.senha.toString())).single().apply()

  def findAll()(implicit session: DBSession = autoSession): List[Usuario] = withSQL
    { select.from(Usuario as u) }.map(Usuario(u)).list().apply()

}