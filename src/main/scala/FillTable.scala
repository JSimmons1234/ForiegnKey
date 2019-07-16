import slick.jdbc.MySQLProfile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}
import TableSetup._

object FillTable {


  val db = Database.forConfig("mysqlDB")

  val dropPeopleCmd = DBIO.seq(suppliers.schema.dropIfExists)
  val dropPeopleAddressesCmd = DBIO.seq(coffees.schema.dropIfExists)
  val initPeopleCmd = DBIO.seq(suppliers.schema.createIfNotExists)
  val initPeopleAddressesCmd = DBIO.seq(coffees.schema.createIfNotExists)

  def dropDB: Future[Unit] = {
    val dropFuture = Future{
      db.run(DBIO.seq(dropPeopleCmd, dropPeopleAddressesCmd))
    }
    Await.result(dropFuture, Duration.Inf).andThen{
      case Success(_) =>  initialisePeople
      case Failure(error) => println("Dropping the table failed due to: " + error.getMessage)
        initialisePeople
    }
  }

  def initialisePeople: Future[Unit] = {
    val setupFuture =  Future {
      db.run(DBIO.seq(initPeopleCmd, initPeopleAddressesCmd))
    }
    Await.result(setupFuture, Duration.Inf).andThen{
      case Success(_) => addToTable
      case Failure(error) => println("Initialising the table failed due to: " + error.getMessage)
    }
  }

  def addToTable {
    val insertPeople = Future {
      val query = DBIO.seq(
        suppliers ++= Seq (
          (101, "Acme, Inc.", "99 Market Street", "Groundsville", "CA", "95199"),
          (49, "Superior Coffee", "1 Party Place", "Mendocino", "CA", "95460"),
          (150, "The High Ground", "100 Coffee Lane", "Meadows", "CA", "93966")
        ),
        coffees ++= Seq(
          ("Colombian", 101, 7.99, 0, 0),
          ("French_Roast", 49, 8.99, 0, 0),
          ("Espresso", 150, 9.99, 0, 0),
          ("Colombian_Decaf", 101, 8.99, 0, 0),
          ("French_Roast_Decaf", 49, 9.99, 0, 0)
        )
      )
      db.run(query)
    }
    Await.result(insertPeople, Duration.Inf).andThen {
      case Success(_) => getData
      case Failure(error) => println("Welp! Something went wrong! " + error.getMessage)
    }
  }


  def getData: Unit = {
    println("Coffees:")
    db.run(coffees.result).map(_.foreach {
      case (name, supID, price, sales, total) =>
        println("  " + name + "\t" + supID + "\t" + price + "\t" + sales + "\t" + total)

    })
      val q1 = for (c <- coffees)
      yield LiteralColumn("  ") ++ c.name ++ "\t" ++ c.supID.asColumnOf[String] ++
        "\t" ++ c.price.asColumnOf[String] ++ "\t" ++ c.sales.asColumnOf[String] ++
        "\t" ++ c.total.asColumnOf[String]

    db.stream(q1.result).foreach(println)

    val q2 = for {
      c <- coffees if c.price < 9.0
      s <- suppliers if s.id === c.supID
    } yield (c.name, s.name)

    db.stream(q2.result).foreach(println)
  }
}





