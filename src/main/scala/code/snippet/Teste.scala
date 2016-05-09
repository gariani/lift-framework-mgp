package code.snippet

import code.comet.{Excluir, ItemsServer}
import code.model.Usuario
import code.view.{DataTableParams, DataTableObjectSource, DataTable}
import net.liftweb.common.Box
import net.liftweb.http.js.{JsCmd, JsCmds}
import net.liftweb.http.{RequestVar, LiftView, SHtml}
import net.liftweb.util.Helpers._

import scala.xml.{NodeSeq, _}

/**
  * Created by daniel on 29/02/16.
  */

class Teste {


  def editar = {
    val cols = "Col1" :: "Col2" :: Nil

    val fun = (params: DataTableParams) => {
      val rows = List(("row1_col1", "row1_col2"), ("row1_col1", "row1_col2")
        ,("row1_col1", "row1_col2") ,("row1_col1", "row1_col2")
        ,("row1_col1", "row1_col2") ,("row1_col1", "row1_col2")
        ,("row1_col1", "row1_col2") ,("row1_col1", "row1_col2")
        ,("row1_col1", "row1_col2") ,("row1_col1", "row1_col2")
        ,("row1_col1", "row1_col2") ,("row1_col1", "row1_col2")
        ,("row1_col1", "row1_col2") ,("row1_col1", "row1_col2")
        ,("row1_col1", "row1_col2") ,("row1_col1", "row1_col2")
        ,("row1_col1", "row1_col2") ,("row1_col1", "row1_col2"))

      new DataTableObjectSource(18, 10, rows.map(r =>
        List(("0", r._1),
          ("1", r._2),
          ("DT_RowId", "rowid_" + r._1))))
    }


    DataTable(
      cols, // columns
      fun, // our data provider
      "my-table", // html table id
      List(("bFilter", "true"), ("bSort", "true"), ("bProcessing", "false"), ("bAutoWidth", "true"), ("bSearchable", "true"), ("bRegex", "true")), // datatables configuration
      ("class", "table table-striped table-bordered table-hover")) // set css class for table
  }

}
