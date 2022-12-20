package eu.appcorner.importer

import com.healthmarketscience.jackcess.Database
import com.healthmarketscience.jackcess.DatabaseBuilder
import com.healthmarketscience.jackcess.util.ImportUtil
import picocli.CommandLine.Command
import picocli.CommandLine.Parameters
import java.io.File
import java.io.IOException
import java.util.concurrent.Callable

@Command(name = "import", mixinStandardHelpOptions = true, version = ["import 1.0"],
    description = ["Imports a set of CSV files to the target MDB or ACCDB database."])
class ImportCommand : Callable<Int> {
    @Parameters(index = "0", description = ["The target MDB or ACCDB database."])
    lateinit var targetFile: File

    @Parameters(index = "1", description = ["The directory that contains the CSV files."])
    lateinit var inputDir: File

    override fun call(): Int {
        DatabaseBuilder.open(targetFile).use { db ->
            val files = inputDir.listFiles { file ->
                file.isFile && file.canRead() && file.name.endsWith(".csv")
            }?.sorted() ?: throw IOException("inputDir does not exists or an IO error occurred")

            println("Removing data:")

            for (file in files.reversed()) {
                val tableName = file.nameWithoutExtension.removePrefix("ptv_")

                println(" - ${tableName}...")

                db.getTable(tableName).removeAll { true }
            }

            println("Importing data:")

            for (file in files) {
                val tableName = file.nameWithoutExtension.removePrefix("ptv_")

                println(" - ${tableName}...")

                importFile(db, file, tableName)
            }
        }

        println("done")

        return 0
    }

    private fun importFile(db: Database, file: File, tableName: String) {
        ImportUtil.Builder(db)
            .setDelimiter(",")
            .setQuote('"')
            .setTableName(tableName)
            .setUseExistingTable(true)
            .setHeader(true)
            .setFilter(DefaultImportFilter(db.getTable(tableName).columns))
            .importFile(file)
    }
}
