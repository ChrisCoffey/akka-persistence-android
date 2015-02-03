# akka-persistence-android

A journal and snapshot store plugin for [akka-persistence](http://doc.akka.io/docs/akka/2.3.8/scala/persistence.html)
using Android's built in SQLite database.

This library is tested against [akka-persistence-tck](http://doc.akka.io/docs/akka/2.3.8/scala/persistence.html#plugin-tck).

## Usage

### Dependency

You should add the following dependency.

```
libraryDependencies += "me.leaf" %% "akka-persistence-android" % "0.2"
```

### Configuration

The journal and snapshot plugins are configured as normal. There are two additional configuration items that need to be
set `name` and `context-lookup.class`.

`name` is the name of the SQLite DB on the Android filesystem.

`context-lookup.class` is an implementation of the [ContextLookup](https://github.com/leafme/akka-persistence-android/blob/master/src/main/scala/akka/persistence/android/common/ContextLookup.scala)
trait. A simple implementation (used by the unit tests) is included in [SimpleContextLookup](https://github.com/leafme/akka-persistence-android/blob/master/src/main/scala/akka/persistence/android/common/SimpleContextLookup.scala).

Example `application.conf`:

```
akka {
  persistence {
    journal.plugin = "akka-persistence-android.journal"
    snapshot-store.plugin = "akka-persistence-android.snapshot"
  }
}

akka-persistence-android {
  journal.class = "akka.persistence.android.journal.AndroidJournal"
  snapshot.class = "akka.persistence.android.snapshot.AndroidSnapshot"
  context-lookup.class = "akka.persistence.android.common.SimpleContextLookup"

  name = "my-db-name"
}
```

## Table schema

The table schema is created automatically by DbHelper using Android's SQLiteOpenHelper.

## Testing

Testing is enable via the [RoboTest](https://github.com/zbsz/robotest) Scala wrapper for [Robolectric](http://robolectric.org).

Robolectric requires the Google APIs for Android (specifically the maps JAR) and and the Android support-v4 library to
be in your local Maven repository.

To install these, first download them via the Android SDK tools, and then run the following:

```
mvn install:install-file -DgroupId=com.google.android.maps \
  -DartifactId=maps \
  -Dversion=18_r3 \
  -Dpackaging=jar \
  -Dfile="$ANDROID_HOME/add-ons/addon-google_apis-google-18/libs/maps.jar"

mvn install:install-file -DgroupId=com.android.support \
  -DartifactId=support-v4 \
  -Dversion=19.0.1 \
  -Dpackaging=jar \
  -Dfile="$ANDROID_HOME/extras/android/support/v4/android-support-v4.jar"
```

## Release Notes

### 0.1 - 2015-01-22
- The first release

### 0.2 - 2015-02-03
- Add ContextLookup to get the context from the Android world into the Akka world

## License

[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0)

## References

This project is descended from [okumin](https://github.com/okumin)'s [akka-persistence-sql-async](https://github.com/okumin/akka-persistence-sql-async) project.
