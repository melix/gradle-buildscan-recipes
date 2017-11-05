import groovy.transform.CompileDynamic

import java.lang.management.ManagementFactory
import org.gradle.api.invocation.Gradle
import org.gradle.api.Task

/**
 * This recipe will try to find which tasks leak file handles. Leaking file handles will often prevent the daemon from working
 * properly, because Java will not automatically close files after a build. Instead, subsequent builds trying to access the same
 * file would fail (typically on Windows, with things like "cannot delete file").
 *
 */

if (gradle.rootProject.hasProperty('disableLeakDetection')) {
    return
}

Class loadAgent(Gradle gradle) {
    def agent = gradle.rootProject.dependencies.create('org.kohsuke:file-leak-detector:1.8:jar-with-dependencies@jar')
    def config = gradle.rootProject.configurations.detachedConfiguration(agent)
    String currentVMName = ManagementFactory.runtimeMXBean.name
    String pid = currentVMName.substring(0, currentVMName.indexOf('@'))
    def vmClazz = Class.forName('com.sun.tools.attach.VirtualMachine')
    def vm = vmClazz.invokeMethod('attach', pid)
    vm.invokeMethod('loadAgent', config.asPath)
    vm.invokeMethod('detach', null)
    Class.forName('org.kohsuke.file_leak_detector.Listener', false, ClassLoader.systemClassLoader)
}

@CompileDynamic
Set<Record> listOpenFiles(Class listener) {
    (listener.getCurrentOpenFiles()).collect {
        File file = it.class.name == 'org.kohsuke.file_leak_detector.Listener$FileRecord'?it.'file' : null
        new Record((Exception)it.'stackTrace', (String) it.'threadName', (Long) it.'time', file)
    }.findAll { Record it ->
        // filter out Gradle files which are closed after build is finished
        !it.stackTrace.stackTrace.any {
            it.className.startsWith('org.gradle.api.internal') ||
            it.className.startsWith('org.gradle.cache.internal')
        }
    } as Set<Record>
}

def listener = loadAgent(gradle)
def tagged = false
Map<String, Set<Record>> taskStart = [:]
Map<String, Set<Record>> potentiallyLeaking = [:]

gradle.taskGraph.beforeTask { Task it ->
    taskStart[it.path] = listOpenFiles(listener)
}

gradle.taskGraph.afterTask { Task task ->
    def openFiles = listOpenFiles(listener) - taskStart[task.path]
    if (openFiles) {
        potentiallyLeaking[task.path] = openFiles
    }
    taskStart.remove(task.path)
}

buildScan.buildFinished {
    def id = System.currentTimeMillis()
    int cpt = 0
    def openFiles = listOpenFiles(listener)
    potentiallyLeaking.each { String task, Set<Record> leaking ->
        Set<Record> actuallyLeaking = leaking.findAll { openFiles.contains(it ) }
        if (actuallyLeaking) {
            if (!tagged) {
                buildScan.tag('LEAKS FILE HANDLES')
                tagged = true
            }
            def details = gradle.rootProject.file("${gradle.rootProject.buildDir}/leaking-${id}-${cpt++}")
            buildScan.value("Leaking file handles", "Task $task leaks file handles: ${actuallyLeaking*.file}. See $details for details.")
            details.withWriter('utf-8') { wrt ->
                actuallyLeaking.each {
                    wrt.println("File $it.file")
                    wrt.println("-------------------------------------------")
                    it.stackTrace.printStackTrace(new PrintWriter(wrt))
                    wrt.println("")
                }
            }
        }
    }
    potentiallyLeaking.clear()
}

@groovy.transform.Canonical
class Record {
    Exception stackTrace
    String threadName
    long time
    File file
}