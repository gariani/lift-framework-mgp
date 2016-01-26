package code.lib

import javax.sql.DataSource
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari._
import scalikejdbc.{DataSourceConnectionPool, ConnectionPool}
import scalikejdbc._

object DataSource {

  private var url: String = "jdbc:mysql://localhost:3306/mgp"
  private var user: String = "root"
  private var password: String = ""

  private[this] val dataSource: DataSource = {
    val ds = new HikariConfig()
    ds.setDriverClassName("com.mysql.jdbc.Driver")
    ds.setJdbcUrl(url)
    ds.setMaximumPoolSize(10);
    ds.addDataSourceProperty("user", user)
    ds.addDataSourceProperty("password", password)
    new HikariDataSource(ds)
  }

  def apply(): javax.sql.DataSource = dataSource

  ConnectionPool.singleton(new DataSourceConnectionPool(dataSource))
}

object InicializarConfiguracao {

  val init = {
    ConnectionPool.singleton(new DataSourceConnectionPool(DataSource()))
  }
}

trait Settings {
  InicializarConfiguracao.init
}