package code.model

import code.lib.Settings
import org.joda.time.DateTime
import scalikejdbc._
import net.liftweb.common._

/**
  * Created by daniel on 27/03/16.
  */

case class Cliente(idCliente: Long, nomeCliente: String, createdAt: DateTime, deletedAt: Option[DateTime])

object Cliente extends SQLSyntaxSupport[Cliente] with Settings {

  override val tableName = "cliente"

  override val columns = Seq("id_cliente", "nome_cliente", "created_at", "deleted_at")

  def apply(c: SyntaxProvider[Cliente])(rs: WrappedResultSet): Cliente = apply(c.resultName)(rs)

  def apply(c: ResultName[Cliente])(rs: WrappedResultSet): Cliente = new Cliente(
    idCliente = rs.get(c.idCliente),
    nomeCliente = rs.get(c.nomeCliente),
    createdAt = rs.jodaDateTime(c.createdAt),
    deletedAt = rs.jodaDateTimeOpt(c.deletedAt)
  )

  val c = Cliente.syntax("c")

  def findAll()(implicit session: DBSession = autoSession): List[Cliente] = withSQL {
    select.from(Cliente as c)
  }.map(Cliente(c)).list().apply()

  def findClienteById(idCliente: Long)(implicit sesession: DBSession = AutoSession): Option[Cliente] = withSQL {
    select.from(Cliente as c).where.eq(c.idCliente, idCliente)
  }.map(Cliente(c)).single().apply()

  def create(nomeCliente: String, createdAt: DateTime)(implicit session: DBSession = AutoSession): Cliente = {

    val id = withSQL {
      insert.into(Cliente).namedValues(
        column.nomeCliente -> nomeCliente,
        column.createdAt -> createdAt
      )
    }.updateAndReturnGeneratedKey.apply()

    Cliente(
      idCliente = id,
      nomeCliente = nomeCliente,
      createdAt = createdAt,
      None
    )
  }

  def save(c: Cliente)(implicit session: DBSession = autoSession): Cliente = {
    withSQL {
      update(Cliente).set(
        Cliente.column.nomeCliente -> c.nomeCliente,
        Cliente.column.createdAt -> c.createdAt,
        Cliente.column.deletedAt -> c.deletedAt).where.eq(Cliente.column.idCliente, c.idCliente)
    }.update().apply()
    c
  }


  def destroy(idCliente: Long)(implicit session: DBSession = AutoSession): Unit = withSQL {
    update(Cliente).set(column.deletedAt -> DateTime.now).where.eq(column.idCliente, idCliente)
  }.update.apply()

}
