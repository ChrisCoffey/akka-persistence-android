package akka.persistence.snapshot.android

import akka.actor.ActorLogging
import akka.persistence._
import akka.persistence.common.DbHelper
import akka.persistence.serialization.Snapshot
import akka.persistence.snapshot.SnapshotStore
import akka.serialization.{Serialization, SerializationExtension}
import android.content.ContentValues
import org.scaloid.common.RichCursor

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class AndroidSnapshot extends SnapshotStore with ActorLogging {
  private val dbHelper = new DbHelper(context.system)

  private[this] val serialization: Serialization = SerializationExtension(context.system)

  override def loadAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Option[SelectedSnapshot]] = {
    log.debug(s"Load a snapshot, persistenceId = $persistenceId, criteria = $criteria")

    Future {
      val c = new RichCursor(dbHelper.db.query(
        DbHelper.tables.snapshot,
        Array(DbHelper.columns.snapshot, DbHelper.columns.persistenceId, DbHelper.columns.sequenceNumber, DbHelper.columns.createdAt),
        s"${DbHelper.columns.persistenceId} = ? and ${DbHelper.columns.sequenceNumber} <= ? and ${DbHelper.columns.createdAt} <= ?",
        Array(persistenceId, criteria.maxSequenceNr.toString, criteria.maxTimestamp.toString),
        null,
        null,
        s"${DbHelper.columns.sequenceNumber} DESC",
        1.toString
      ))

      c.iterator.toList.map(r => (r.getBlob(0), r.getString(1), r.getLong(2), r.getLong(3))).map {
        case (ss, id, seq, created) =>
          val Snapshot(snapshot) = serialization.deserialize(ss, classOf[Snapshot]).get
          SelectedSnapshot(
            SnapshotMetadata(id, seq, created),
            snapshot
          )
      }.headOption
    }
  }

  override def saveAsync(metadata: SnapshotMetadata, snapshot: Any): Future[Unit] = {
    log.debug(s"Save the snapshot, metadata = $metadata, snapshot = $snapshot")

    Future {
      val content = new ContentValues()
      content.put(DbHelper.columns.persistenceId, metadata.persistenceId)
      content.put(DbHelper.columns.sequenceNumber, metadata.sequenceNr.asInstanceOf[java.lang.Long])
      content.put(DbHelper.columns.createdAt, metadata.timestamp.asInstanceOf[java.lang.Long])
      content.put(DbHelper.columns.snapshot, serialization.serialize(Snapshot(snapshot)).get)

      dbHelper.db.insert(DbHelper.tables.snapshot, null, content)
    }
  }

  override def saved(metadata: SnapshotMetadata): Unit = {
    log.debug(s"Saved $metadata")
  }

  override def delete(metadata: SnapshotMetadata): Unit = {
    log.debug(s"Delete the snapshot, $metadata")

    dbHelper.db.delete(
      DbHelper.tables.snapshot,
      s"${DbHelper.columns.persistenceId} = ? and ${DbHelper.columns.sequenceNumber} = ?",
      Array(metadata.persistenceId, metadata.sequenceNr.toString)
    )
  }

  override def delete(persistenceId: String, criteria: SnapshotSelectionCriteria): Unit = {
    log.debug(s"Delete the snapshot for $persistenceId, criteria = $criteria")

    dbHelper.db.delete(
      DbHelper.tables.snapshot,
      s"${DbHelper.columns.persistenceId} = ? and ${DbHelper.columns.sequenceNumber} <= ? and ${DbHelper.columns.createdAt} <= ?",
      Array(persistenceId, criteria.maxSequenceNr.toString, criteria.maxTimestamp.toString)
    )
  }
}
