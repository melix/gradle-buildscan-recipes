// WARNING: This recipe will scan your $HOME/.gradle directory at the
// beginning of each build, which can, depending on your drive type or
// FS be very slow

import org.gradle.internal.os.OperatingSystem // remember kids, depending on internal API is WRONG!

String diskUsage(dir) {
   ['du', '-hs' , dir].execute().text.trim().split('\\s')[0]
}

def os = OperatingSystem.current()
if (os.unix || os.macOsX) {
   def home = System.getProperty("user.home")
   def wrapperCount = new File("$home/.gradle/wrapper/dists").listFiles().size()
   def projectDir = gradle.rootProject.projectDir

   ['caches': 'Artifact Cache', 'task-cache': 'Task Output Cache', 'wrapper': "Wrappers ($wrapperCount versions)"].each { dir ->
       buildScan.value "Disk usage ($dir.value)", diskUsage("$home/.gradle/$dir.key")
   }
   buildScan.value "Disk usage (Project directory)", """${diskUsage(projectDir)} (incl. ${diskUsage(gradle.rootProject.file("$projectDir/.gradle"))} .gradle)"""
}

