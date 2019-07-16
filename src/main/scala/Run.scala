

import FillTable._
import slick.jdbc.MySQLProfile.api._
import scala.concurrent.ExecutionContext.Implicits.global

object Run extends App {

  val db = Database.forConfig("mysqlDB")
  try{
    dropDB()

  } finally db.close
  Thread.sleep(10000)


}

