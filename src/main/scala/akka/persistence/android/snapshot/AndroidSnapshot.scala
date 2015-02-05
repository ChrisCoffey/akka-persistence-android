package akka.persistence.android.snapshot

import _root_.android.content.ContentValues
import akka.actor.ActorLogging
import akka.persistence._
import akka.persistence.android.common.DbHelper
import akka.persistence.serialization.Snapshot
import akka.persistence.snapshot.SnapshotStore
import akka.serialization.{ Serialization, SerializationExtension }
import org.scaloid.common.RichCursor

import scala.concurrent.Future

class AndroidSnapshot extends SnapshotStore with ActorLogging {
  implicit val ec = context.system.dispatcher
  private val dbHelper = new DbHelper(context.system)

  private[this] val serialization: Serialization = SerializationExtension(context.system)

  override def loadAsync(persistenceId: String, criteria: SnapshotSelectionCriteria): Future[Option[SelectedSnapshot]] = {
    log.debug(s"Load a snapshot, persistenceId = $persistenceId, criteria = $criteria")

    Future {
      // scalastyle:off null - Android SDK uses null here
      val c = new RichCursor(dbHelper.db.query(
        DbHelper.Tables.snapshot,
        Array(DbHelper.Columns.snapshot, DbHelper.Columns.persistenceId, DbHelper.Columns.sequenceNumber, DbHelper.Columns.createdAt),
        s"${DbHelper.Columns.persistenceId} = ? and ${DbHelper.Columns.sequenceNumber} <= ? and ${DbHelper.Columns.createdAt} <= ?",
        Array(persistenceId, criteria.maxSequenceNr.toString, criteria.maxTimestamp.toString),
        null,
        null,
        s"${DbHelper.Columns.sequenceNumber} DESC",
        1.toString
      ))
      // scalastyle:on null

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
      content.put(DbHelper.Columns.persistenceId, metadata.persistenceId)
      content.put(DbHelper.Columns.sequenceNumber, metadata.sequenceNr.asInstanceOf[java.lang.Long])
      content.put(DbHelper.Columns.createdAt, metadata.timestamp.asInstanceOf[java.lang.Long])
      content.put(DbHelper.Columns.snapshot, serialization.serialize(Snapshot(snapshot)).get)

      // scalastyle:off null - Android SDK uses null here
      dbHelper.db.insert(DbHelper.Tables.snapshot, null, content)
      // scalastyle:on null
    }
  }

  override def saved(metadata: SnapshotMetadata): Unit = {
    log.debug(s"Saved $metadata")
  }

  override def delete(metadata: SnapshotMetadata): Unit = {
    log.debug(s"Delete the snapshot, $metadata")

    dbHelper.db.delete(
      DbHelper.Tables.snapshot,
      s"${DbHelper.Columns.persistenceId} = ? and ${DbHelper.Columns.sequenceNumber} = ?",
      Array(metadata.persistenceId, metadata.sequenceNr.toString)
    )
  }

  override def delete(persistenceId: String, criteria: SnapshotSelectionCriteria): Unit = {
    log.debug(s"Delete the snapshot for $persistenceId, criteria = $criteria")

    dbHelper.db.delete(
      DbHelper.Tables.snapshot,
      s"${DbHelper.Columns.persistenceId} = ? and ${DbHelper.Columns.sequenceNumber} <= ? and ${DbHelper.Columns.createdAt} <= ?",
      Array(persistenceId, criteria.maxSequenceNr.toString, criteria.maxTimestamp.toString)
    )
  }
}
