package eu.appcorner.importer

import com.healthmarketscience.jackcess.Column
import com.healthmarketscience.jackcess.ColumnBuilder
import com.healthmarketscience.jackcess.DataType
import com.healthmarketscience.jackcess.util.ImportFilter
import java.io.IOException
import java.sql.ResultSetMetaData
import java.time.Duration
import java.time.LocalDateTime

class DefaultImportFilter(private val columns: List<Column>? = null) : ImportFilter {
    private var columnTypes: Array<DataType>? = null

    init {
        if (columns != null)
            columnTypes = columns.map { it.type }.toTypedArray()
    }

    override fun filterColumns(destColumns: MutableList<ColumnBuilder>, srcColumns: ResultSetMetaData?): MutableList<ColumnBuilder> {
        columnTypes = destColumns.map { it.type }.toTypedArray()

        return destColumns
    }

    override fun filterRow(row: Array<Any?>): Array<Any?> {
        for (index in row.indices) {
            // convert empty strings to null
            if (row[index] == "") {
                row[index] = null
                continue
            }

            // handle special column types
            when (columnTypes!![index]) {
                DataType.SHORT_DATE_TIME, DataType.EXT_DATE_TIME -> {
                    if (row[index] != null)
                        row[index] = parseDateTimeColumn(row[index] as String)
                }
                else -> {
                    // nothing to do
                }
            }
        }

        return row
    }

    private fun parseDateTimeColumn(value: String): Any? {
        when {
            TIME_REGEX.matches(value) -> {
                // HH:MM:SS duration
                val iso = value.replaceFirst(Regex("^(\\d{2}):(\\d{2}):(\\d{2})$"), "PT$1H$2M$3S")
                return ZERO_DATE.plus(Duration.parse(iso))
            }
            else -> {
                // TODO: add support for other date/time formats
                throw IOException("Unsupported date format: $value")
            }
        }
    }

    companion object {
        // Zero date used by all MS Office products.
        // Furthermore, MS Access seems to use the local time zone when storing dates.
        private val ZERO_DATE = LocalDateTime.of(1899, 12, 30, 0, 0, 0, 0)

        private val TIME_REGEX = Regex("[0-9][0-9]:[0-9][0-9]:[0-9][0-9]")
    }
}
