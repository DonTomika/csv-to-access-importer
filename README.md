# csv-to-access-importer

Command-line tool for importing CSV data to MS Access databases (.mdb, .accdb) from Linux and macOS. Works on Windows as well, but more sophisticated options exist there.

This project heavily relies on [Jackcess](https://jackcess.sourceforge.io/).

## Usage

```
Usage: import [-chtV] [-o=<dependencyOrder>] <targetFile> <inputDir>
Imports a set of CSV files to the target MDB or ACCDB database.
      <targetFile>      Path to the target MDB or ACCDB database.
      <inputDir>        Path to the directory that contains the CSV files.
  -c, --[no-]create     Automatically create missing tables (default: true).
  -h, --help            Show this help message and exit.
  -o, --order=<dependencyOrder>
                        Comma separated list of table names to process in this
                          order. All other tables will be processed afterwards,
                          in alphabetical order.
  -t, --[no-]truncate   Truncate tables before the import (default: false).
  -V, --version         Print version information and exit.

```

For example:

```
java -jar csv-to-access-importer-1.0.0-all.jar --no-create --truncate /data/out.accdb /data/input-folder/
```

## License

See [LICENSE.md](./LICENSE.md) for details.
