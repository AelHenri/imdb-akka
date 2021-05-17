# IMDB files Akka Parser

## Goal

Query IMDB tsv files using the Scala library Akka Streams.  
The given use cases were:
- As a user, I would like to be able to get a movie's list of principals, from its name.
- As a user, I would like to be able to get the 10 TV series with the most episodes.

## Implementations

There are two implementations, each with their own advantages and disadvantages.  
- The first one uses only streams. There is no setup required, but the queries can take a long time (up to 10mn for the principals).
- The second one uses a SQLite database. The setup can take a very long time (up to 1h) but the subsequent queries are fast.

## Requirements

```
openjdk >= 1.8
sbt >= 1.x
```

All the required files can be found [here](https://drive.google.com/drive/folders/1dcCp46PQ_QtsMm0pGbPGQ-osDemGn9kp?usp=sharing).

## Setup

Download the files from IMDB, and put them either in the `src/main/resources/data` folder, or in your own folder and set the environment variable `IMDB_DATA_FOLDER` to their location. You can set their name in the `src/main/resources/application.conf` file.

If you wish to use the SQLite implementation, you have two choices:  
- Download a ready-to-use version of the database at the link above
- Run the `DatabaseInitializer`

To initialize the database (note: this process can take up to 1 hour, and the resulting file will have a size of roughly 5.2GB): 
```shell
sbt "runMain com.canal.db.DatabaseInitializer"
```

You can tweak the performances by modifying the `parallelism` and `groupSize` parameters in `src/main/resources/application.conf`.

## Run

By default, the application uses the SQLite implementation.  
To retrieve the principals from a movie:
```
sbt "run principalsFromMovie \"Star Wars\""
```

To retrieve the 10 series with the most episodes:
```
sbt "run topTvSeries"
```

If you wish to use the full streams implementation, add `--streams` to the command:
```
sbt "run principalsFromMovie \"Star Wars\" --streams"
```

## Test

To run the tests:
```
sbt test
```
