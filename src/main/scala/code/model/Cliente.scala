package code.model

import code.lib.Settings
import scalikejdbc._
import net.liftweb.common._

/**
  * Created by daniel on 27/03/16.
  */

case class Cliente(id_cliente: Int, nome_cliente: String, descricao_cliente: Option[String])

object Cliente extends  SQLSyntaxSupport[Cliente] with Settings {

  override val tableName = "cliente"

  override val columns = Seq("id_cliente", "nome_cliente", "descricao_cliente")

  def apply(c: SyntaxProvider[Cliente])(rs: WrappedResultSet): Cliente = apply(c.resultName)(rs)

  def apply(c: ResultName[Cliente])(rs: WrappedResultSet): Cliente = new Cliente(
    id_cliente = rs.get(c.id_cliente),
    nome_cliente = rs.get(c.nome_cliente),
    descricao_cliente = rs.stringOpt(c.descricao_cliente)
  )

  val c = Cliente.syntax("c")

  def findAllCliente()(implicit session: DBSession = autoSession): List[Cliente] = withSQL {
    select.from(Cliente as c)
  }.map(Cliente(c)).list().apply()

  def save(cliente: Cliente)(implicit session: DBSession = autoSession) = withSQL {
    insert.into(Cliente).values(c.nome_cliente, c.descricao_cliente)
  }

}
