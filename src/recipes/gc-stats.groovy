import java.lang.management.GarbageCollectorMXBean
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit

long gcTime = (Long) ManagementFactory.garbageCollectorMXBeans.inject(0L) { long acc, GarbageCollectorMXBean val ->
    acc + val.collectionTime
}

buildScan.value 'GC time (daemon time)', "${TimeUnit.SECONDS.convert(gcTime, TimeUnit.MILLISECONDS)}s"
