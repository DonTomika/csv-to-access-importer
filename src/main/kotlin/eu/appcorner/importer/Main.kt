package eu.appcorner.importer

import picocli.CommandLine
import kotlin.system.exitProcess

fun main(args: Array<String>): Unit = exitProcess(CommandLine(ImportCommand()).execute(*args))
