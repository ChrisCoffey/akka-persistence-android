package akka.persistence.android.common

import akka.actor.ActorSystem
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper}

class DbHelper(val system: ActorSystem) {

  val config = AndroidConfig(system)
  def context = AppContext.getAppContext
  lazy val db = dbHelper.getWritableDatabase

  private lazy val dbHelper = new SQLiteOpenHelper(context, config.name, null, DbHelper.version) {

    // onUpgrade in a no-op because there is only only version of the schema
    override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int): Unit = {}

    override def onCreate(db: SQLiteDatabase): Unit = db.execSQL(DbHelper.createSchema)
  }
}

object DbHelper {
  final val version = 1
  
  object tables {
    final val journal = "journal"
    final val snapshot = "snapshot"
  }
  
  object columns {
    final val persistenceId = "persistence_id"
    final val sequenceNumber = "sequence_nr"
    final val marker = "marker"
    final val message = "message"
    final val createdAt = "created_at"
    final val snapshot = "snapshot"
  }

  final val createSchema =
    s"""
      |CREATE TABLE IF NOT EXISTS ${tables.journal} (
      |  ${columns.persistenceId} VARCHAR(255) NOT NULL,
      |  ${columns.sequenceNumber} INTEGER(8) NOT NULL,
      |  ${columns.marker} VARCHAR(255) NOT NULL,
      |  ${columns.message} BLOB NOT NULL,
      |  ${columns.createdAt} INTEGER(8) NOT NULL DEFAULT (strftime('%s','now')),
      |  PRIMARY KEY (${columns.persistenceId}, ${columns.sequenceNumber})
      |);
      |
      |CREATE TABLE IF NOT EXISTS ${tables.snapshot} (
      |  ${columns.persistenceId} VARCHAR(255) NOT NULL,
      |  ${columns.sequenceNumber} INTEGER(8) NOT NULL,
      |  ${columns.createdAt} INTEGER(8) NOT NULL DEFAULT (strftime('%s','now')),
      |  ${columns.snapshot} BLOB NOT NULL,
      |  PRIMARY KEY (${columns.persistenceId}, ${columns.sequenceNumber})
      |);
    """.stripMargin
}
