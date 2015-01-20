package akka.persistence.snapshot.android

import akka.persistence.helper.MySQLInitializer
import akka.persistence.snapshot.SnapshotStoreSpec

class MySQLSnapshotStoreSpec extends SnapshotStoreSpec with MySQLInitializer
