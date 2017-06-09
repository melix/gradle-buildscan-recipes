// WARNING: This recipe will scan your $HOME/.gradle directory at the
// beginning of each build, which can, depending on your drive type or
// FS be very slow

import org.gradle.internal.os.OperatingSystem // remember kids, depending on internal API is WRONG!

def os = OperatingSystem.current()
if (os.unix || os.macOsX) {
   def home = System.getProperty("user.home")
   def wrapperCount = new File("$home/.gradle/wrapper/dists").listFiles().size()
   def projectDir = gradle.rootProject.projectDir

   List<Measurement> measurements = []

   def directoriesToMeasure = ['caches': 'Caches', 'wrapper': "Wrappers ($wrapperCount versions)"]

   directoriesToMeasure.each { dir ->
      File dirKey = gradle.rootProject.file("$home/.gradle/$dir.key")
      measurements << new Measurement(dirKey, "Disk usage ($dir.value)")
   }
   def projectDirMeasurement = new Measurement(projectDir, 'Disk usage (Project)')
   def projectDirDotGradle = new Measurement(gradle.rootProject.file("$projectDir/.gradle"), 'Disk usage (Project .gradle)')
   measurements << projectDirMeasurement
   measurements << projectDirDotGradle

   buildScan.buildFinished {
      measurements*.update()
      projectDirMeasurement.initialSize -= projectDirDotGradle.initialSize
      projectDirMeasurement.buildFinishedSize -= projectDirDotGradle.buildFinishedSize
      measurements.each { measurement ->
         buildScan.value measurement.label, measurement.formattedSize
      }
   }
}

class Measurement {
   final File dir
   final String label
   long initialSize
   long buildFinishedSize

   Measurement(File dir, String label) {
      this.dir = dir
      this.label = label
      this.initialSize = diskUsage(dir.absolutePath)
   }

   long getDelta() {
      buildFinishedSize - initialSize
   }

   void update() {
      this.buildFinishedSize = diskUsage(dir.absolutePath)
   }

   private static long diskUsage(String dir) {
      Long.valueOf(['du', '-ks' , dir].execute().text.trim().split('\\s')[0]?:'0')
   }

   private static String format(long amount) {
      if (amount<1024) {
         return "${amount}kB"
      }
      amount /= 1024
      if (amount<1024) {
         return "${amount}MB"
      }
      amount /= 1024
      if (amount<1024) {
         return "${amount}GB"
      }
      amount /= 1024
      return "${amount}TB"
   }

   public String getFormattedSize() {
      String deltaStr = delta==0?'':" (${delta<0?'':'+'}${format(delta)})"
      "${format(buildFinishedSize)}$deltaStr"
   }
}