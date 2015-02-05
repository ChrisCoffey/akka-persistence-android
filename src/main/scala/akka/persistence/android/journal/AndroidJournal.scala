package akka.persistence.android.journal

import java.sql.SQLException

import akka.actor.ActorLogging
import akka.persistence.android.common.DbHelper
import akka.persistence.journal.SyncWriteJournal
import akka.persistence.{ PersistentConfirmation, PersistentId, PersistentRepr }
import akka.serialization.{ Serialization, SerializationExtension }
import android.content.ContentValues
import org.scaloid.common._

import scala.collection.immutable
import scala.concurrent.Future

class AndroidJournal extends SyncWriteJournal with ActorLogging {
  implicit val ec = context.system.dispatcher
  private val dbHelper = new DbHelper(context.system)

  private[this] val serialization: Serialization = SerializationExtension(context.system)

  override def writeMessages(messages: immutable.Seq[PersistentRepr]): Unit = {
    log.debug(s"Write messages, $messages")

    dbHelper.db.beginTransaction()
    try {
      messages.foreach { m =>
        val content = new ContentValues()
        content.put(DbHelper.columns.persistenceId, m.persistenceId)
        content.put(DbHelper.columns.sequenceNumber, m.sequenceNr.asInstanceOf[java.lang.Long])
        content.put(DbHelper.columns.marker, AndroidJournal.AcceptedMarker)
        content.put(DbHelper.columns.message, persistenceToBytes(m))

        dbHelper.db.insertOrThrow(DbHelper.tables.journal, null, content)
      }
      dbHelper.db.setTransactionSuccessful()
    } catch {
      case e: SQLException => log.error(s"Error writing messages: $e")
    } finally {
      dbHelper.db.endTransaction()
    }
  }

  @deprecated("writeConfirmations will be removed.", since = "2.3.4")
  override def writeConfirmations(confirmations: immutable.Seq[PersistentConfirmation]): Unit = {
    log.debug(s"Write confirmations, $confirmations")

    def select(persistenceId: String, sequenceNr: Long): Option[PersistentRepr] = {
      val c = new RichCursor(dbHelper.db.query(
        DbHelper.tables.journal,
        Array(DbHelper.columns.marker, DbHelper.columns.message),
        s"${DbHelper.columns.persistenceId} = ? and ${DbHelper.columns.sequenceNumber} = ?",
        Array(persistenceId, sequenceNr.toString),
        null,
        null,
        null))

      c.iterator.toList.map(r => (r.getString(0), r.getBlob(1))).map {
        case (AndroidJournal.DeletedMarker, message) => None
        case (_, message) => Some(persistenceFromBytes(message))
      }
    }.headOption.getOrElse(None)

    def update(message: PersistentRepr): Unit = {
      val content = new ContentValues()
      content.put(DbHelper.columns.message, persistenceToBytes(message))

      dbHelper.db.update(
        DbHelper.tables.journal,
        content,
        s"${DbHelper.columns.persistenceId} = ? and ${DbHelper.columns.sequenceNumber} = ?",
        Array(message.persistenceId, message.sequenceNr.toString))
    }

    dbHelper.db.beginTransaction()
    try {
      confirmations.foreach { c =>
        select(c.persistenceId, c.sequenceNr) match {
          case Some(message) =>
            val confirmationIds = message.confirms :+ c.channelId
            val newMessage = message.update(confirms = confirmationIds)
            update(newMessage)
          case None =>
        }
      }

      dbHelper.db.setTransactionSuccessful()
    } catch {
      case e: SQLException => log.error(s"Error writing confirmations: $e")
    } finally {
      dbHelper.db.endTransaction()
    }
  }

  @deprecated("deleteMessages will be removed.", since = "2.3.4")
  override def deleteMessages(messageIds: immutable.Seq[PersistentId], permanent: Boolean): Unit = {
    dbHelper.db.beginTransaction()
    try {
      messageIds.foreach { m =>
        if (permanent) {
          dbHelper.db.delete(
            DbHelper.tables.journal,
            s"${DbHelper.columns.persistenceId} = ? and ${DbHelper.columns.sequenceNumber} = ?",
            Array(m.persistenceId, m.sequenceNr.toString))
        } else {
          val content = new ContentValues()
          content.put(DbHelper.columns.marker, AndroidJournal.DeletedMarker)
          dbHelper.db.update(
            DbHelper.tables.journal,
            content,
            s"${DbHelper.columns.persistenceId} = ? and ${DbHelper.columns.sequenceNumber} = ?",
            Array(m.persistenceId, m.sequenceNr.toString))
        }
      }

      dbHelper.db.setTransactionSuccessful()
    } catch {
      case e: SQLException => log.error(s"Error deleting messages: $e")
    } finally {
      dbHelper.db.endTransaction()
    }
  }

  override def deleteMessagesTo(persistenceId: String, toSequenceNr: Long, permanent: Boolean): Unit = {
    log.debug(s"Delete messages, persistenceId = $persistenceId, toSequenceNr = $toSequenceNr")

    if (permanent) {
      dbHelper.db.delete(
        DbHelper.tables.journal,
        s"${DbHelper.columns.persistenceId} = ? and ${DbHelper.columns.sequenceNumber} <= ?",
        Array(persistenceId, toSequenceNr.toString))
    } else {
      val content = new ContentValues()
      content.put(DbHelper.columns.marker, AndroidJournal.DeletedMarker)
      dbHelper.db.update(
        DbHelper.tables.journal,
        content,
        s"${DbHelper.columns.persistenceId} = ? and ${DbHelper.columns.sequenceNumber} <= ? and ${DbHelper.columns.marker} != ?",
        Array(persistenceId, toSequenceNr.toString, AndroidJournal.DeletedMarker))
    }
  }

  override def asyncReplayMessages(persistenceId: String, fromSequenceNr: Long, toSequenceNr: Long, max: Long)(replayCallback: (PersistentRepr) => Unit): Future[Unit] = {
    log.debug(s"Replay messages, persistenceId = $persistenceId, fromSequenceNr = $fromSequenceNr, toSequenceNr = $toSequenceNr")

    Future {
      val c = new RichCursor(dbHelper.db.query(
        DbHelper.tables.journal,
        Array(DbHelper.columns.marker, DbHelper.columns.message),
        s"${DbHelper.columns.persistenceId} = ? and ${DbHelper.columns.sequenceNumber} >= ? and ${DbHelper.columns.sequenceNumber} <= ?",
        Array(persistenceId, fromSequenceNr.toString, toSequenceNr.toString),
        null,
        null,
        s"${DbHelper.columns.sequenceNumber} ASC",
        max.toString))

      c.iterator.toStream.map(r => (r.getString(0), r.getBlob(1))).foreach {
        case (marker, bytes) =>
          val raw = persistenceFromBytes(bytes)
          // It is possible that marker is incompatible with message, for batch updates.
          val message = if (marker == AndroidJournal.DeletedMarker) raw.update(deleted = true) else raw
          replayCallback(message)
      }
    }
  }

  override def asyncReadHighestSequenceNr(persistenceId: String, fromSequenceNr: Long): Future[Long] = {
    log.debug(s"Read the highest sequence number, persistenceId = $persistenceId, fromSequenceNr = $fromSequenceNr")

    Future {
      val c = new RichCursor(dbHelper.db.query(
        DbHelper.tables.journal,
        Array(DbHelper.columns.sequenceNumber),
        s"${DbHelper.columns.persistenceId} = ?",
        Array(persistenceId),
        null,
        null,
        s"${DbHelper.columns.sequenceNumber} DESC",
        1.toString
      ))

      c.iterator.toList.map(r => r.getLong(0)).headOption.getOrElse(0L)
    }
  }

  private[this] def persistenceToBytes(repr: PersistentRepr): Array[Byte] = {
    serialization.serialize(repr).get
  }
  private[this] def persistenceFromBytes(bytes: Array[Byte]): PersistentRepr = {
    serialization.deserialize(bytes, classOf[PersistentRepr]).get
  }
}

object AndroidJournal {
  private val AcceptedMarker = "A"
  private val DeletedMarker = "D"
}
