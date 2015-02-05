package akka.persistence.android.common

import akka.actor.ActorSystem
import android.database.sqlite.{ SQLiteDatabase, SQLiteOpenHelper }
import scala.reflect.runtime.universe

class DbHelper(val system: ActorSystem) {

  val config = AndroidConfig(system)
  lazy val db = dbHelper.getWritableDatabase

  // load the specified class via Java. using Scala reflection will fail because Scala reflection is dependent on
  // java.rmi which doesn't exist on Android
  val lookup = getClass.getClassLoader.loadClass(config.contextLookupClass).newInstance()

  if (!lookup.isInstanceOf[ContextLookup]) {
    throw new IllegalArgumentException(config.contextLookupClass + " must extend the trait akka.persistence.android.common.ContextLookup")
  }

  val context = lookup.asInstanceOf[ContextLookup].getContext match {
    case Some(c) => c
    case None => throw new IllegalArgumentException("the Android context provided by " + config.contextLookupClass + " was null")
  }

  // scalastyle:off null - Android SDK uses null here
  private lazy val dbHelper = new SQLiteOpenHelper(context, config.name, null, DbHelper.version) {
    // onUpgrade in a no-op because there is only only version of the schema
    override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int): Unit = {}

    override def onCreate(db: SQLiteDatabase): Unit = DbHelper.schema.foreach { s =>
      db.execSQL(s)
    }
  }
  // scalastyle:on null
}

object DbHelper {
  final val version = 1

  object Tables {
    final val journal = "journal"
    final val snapshot = "snapshot"
  }

  object Columns {
    final val persistenceId = "persistence_id"
    final val sequenceNumber = "sequence_nr"
    final val marker = "marker"
    final val message = "message"
    final val createdAt = "created_at"
    final val snapshot = "snapshot"
  }

  final val schema = Array(
    s"""
      |CREATE TABLE IF NOT EXISTS ${Tables.journal} (
      |  ${Columns.persistenceId} VARCHAR(255) NOT NULL,
      |  ${Columns.sequenceNumber} INTEGER(8) NOT NULL,
      |  ${Columns.marker} CHAR(1) NOT NULL,
      |  ${Columns.message} BLOB NOT NULL,
      |  ${Columns.createdAt} INTEGER(8) NOT NULL DEFAULT (strftime('%s','now')),
      |  PRIMARY KEY (${Columns.persistenceId}, ${Columns.sequenceNumber})
      |);
    """.stripMargin,
    s"""
      |CREATE TABLE IF NOT EXISTS ${Tables.snapshot} (
      |  ${Columns.persistenceId} VARCHAR(255) NOT NULL,
      |  ${Columns.sequenceNumber} INTEGER(8) NOT NULL,
      |  ${Columns.createdAt} INTEGER(8) NOT NULL DEFAULT (strftime('%s','now')),
      |  ${Columns.snapshot} BLOB NOT NULL,
      |  PRIMARY KEY (${Columns.persistenceId}, ${Columns.sequenceNumber})
      |);
    """.stripMargin
  )
}
