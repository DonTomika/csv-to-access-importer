import com.healthmarketscience.jackcess.Database
import com.healthmarketscience.jackcess.DatabaseBuilder
import com.healthmarketscience.jackcess.util.ImportUtil
import java.io.File
import java.io.IOException

fun importFile(db: Database, file: File, tableName: String) {
    ImportUtil.Builder(db)
        .setDelimiter(",")
        .setQuote('"')
        .setTableName(tableName)
        .setUseExistingTable(true)
        .setHeader(true)
        .setFilter(DefaultImportFilter(db.getTable(tableName).columns))
        .importFile(file)
}

fun main(args: Array<String>) {
    val inputDir = args[0]
    val targetFile = args[1]

    DatabaseBuilder.open(File(targetFile)).use { db ->
        val files = File(inputDir).listFiles { file ->
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
}
