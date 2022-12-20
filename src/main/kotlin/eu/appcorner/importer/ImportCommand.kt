package eu.appcorner.importer

import com.healthmarketscience.jackcess.Database
import com.healthmarketscience.jackcess.DatabaseBuilder
import com.healthmarketscience.jackcess.Table
import com.healthmarketscience.jackcess.util.ImportUtil
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.File
import java.io.IOException
import java.util.concurrent.Callable

@Command(name = "import", mixinStandardHelpOptions = true, version = ["import 1.0"],
    description = ["Imports a set of CSV files to the target MDB or ACCDB database."])
class ImportCommand : Callable<Int> {
    @Parameters(
        index = "0",
        description = ["Path to the target MDB or ACCDB database."]
    )
    lateinit var targetFile: File

    @Parameters(
        index = "1",
        description = ["Path to the directory that contains the CSV files."]
    )
    lateinit var inputDir: File

    @Option(
        names = ["-c", "--create"],
        description = ["Automatically create missing tables (default: true)."],
        negatable = true
    )
    var createTables: Boolean = true

    @Option(
        names = ["-t", "--truncate"],
        description = ["Truncate tables before the import (default: false)."],
        negatable = true
    )
    var truncateTables: Boolean = false

    @Option(
        names = ["-o", "--order"],
        description = ["Comma separated list of table names to process in this order. " +
                "All other tables will be processed afterwards, in alphabetical order."]
    )
    var dependencyOrder: String = ""

    override fun call(): Int {
        DatabaseBuilder.open(targetFile).use { db ->
            val files = inputDir.listFiles { file ->
                file.isFile && file.canRead() && file.name.endsWith(".csv")
            } ?: throw IOException("Input dir does not exists or an IO error occurred")

            val parsedDependencyOrder = dependencyOrder.split(",").map { it.trim().lowercase() }

            // sort files based on dependencyOrder, then alphabetically
            val sortedFiles = files.sortedWith(compareBy({
                parsedDependencyOrder.indexOf(it.nameWithoutExtension.lowercase()).takeIf { index -> index != -1 }
                    ?: Int.MAX_VALUE
            }, { it.nameWithoutExtension.lowercase() }))

            if (truncateTables) {
                println("Truncating ${sortedFiles.size} tables in ${targetFile}...")

                for (file in sortedFiles.reversed()) {
                    val tableName = file.nameWithoutExtension

                    println(" - ${tableName}...")

                    db.getTable(tableName).removeAll { true }
                }
            }

            println("Importing ${sortedFiles.size} CSV file(s) to ${targetFile}...")

            for (file in sortedFiles) {
                val tableName = file.nameWithoutExtension

                println(" - ${tableName}...")

                importFile(db, file, tableName, createTables)
            }
        }

        println("done")

        return 0
    }

    private fun importFile(db: Database, file: File, tableName: String, canCreateTable: Boolean) {
        val existingTable: Table? = db.getTable(tableName)
        if (existingTable == null && !canCreateTable)
            throw IOException("Table $tableName does not exist.")

        ImportUtil.Builder(db)
            .setDelimiter(",")
            .setQuote('"')
            .setTableName(tableName)
            .setUseExistingTable(existingTable != null)
            .setHeader(true)
            .setFilter(DefaultImportFilter(existingTable?.columns))
            .importFile(file)
    }
}
