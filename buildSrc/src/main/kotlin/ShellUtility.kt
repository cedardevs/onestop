import java.io.File

fun String.runShell(dir: File? = null, quiet: Boolean = true): String? {
    val builder: ProcessBuilder = ProcessBuilder("/bin/sh", "-c", this)
            .redirectErrorStream(true)
            .directory(dir)
    val process: Process = builder.start()
    val exitCode: Int = process.waitFor()
    var output: String? = process.inputStream.readBytes().toString(Charsets.UTF_8).trim()
    if(exitCode != 0) {
        // print stderr for visibility, since we return `null` on error
        println("\nError running: `${this}`\n")
        println("${output}\n")
        output = null
    }
    if(!quiet && exitCode == 0) {
        // print stdout when `quiet = false` to prevent excessive output by default
        println("\nRunning: `${this}`\n")
        println("${output}\n")
    }
    return output
}

fun String.runShellExitCode(dir: File? = null): Int {
    val builder: ProcessBuilder = ProcessBuilder("/bin/sh", "-c", this)
            .redirectErrorStream(true)
            .directory(dir)
    val process: Process = builder.start()
    return process.waitFor()
}