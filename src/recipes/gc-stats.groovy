/**
 * This recipe provides information about GC collection time, number of GC events, during a build.
 * Original code contributed by Hans Docker (https://github.com/hansd)
 */
import groovy.transform.Canonical

import java.lang.management.GarbageCollectorMXBean
import java.lang.management.ManagementFactory

def gcValues = new GcValues()
def gcStart = gcValues.gcTimes

buildScan.buildFinished {
    def gcResult = GcValues.getGcDelta(gcStart, gcValues.gcTimes)
    [GcValues.PS_SCAVENGE, GcValues.PS_MARK_SWEEP, GcValues.UNKNOWN].each { event ->
        buildScan.value("${event} Count:", "${gcResult[event].count}")
        buildScan.value("${event} Time (s):", "${gcResult[event].time / 1000d}")
    }
}

class GcValues {
    public static final String PS_SCAVENGE = "PS Scavenge"
    public static final String PS_MARK_SWEEP = "PS MarkSweep"
    public static final String UNKNOWN = "Unknown"
    List<GarbageCollectorMXBean> mxBeans = ManagementFactory.getGarbageCollectorMXBeans()

    public Map<String, GcActivity> getGcTimes() {
        Map<String, GcActivity> result = [:].withDefault { new GcActivity(0,0) }
        for (GarbageCollectorMXBean gc : mxBeans) {
            if (gc.name == PS_SCAVENGE) {
                result[PS_SCAVENGE] = new GcActivity(gc.collectionCount, gc.collectionTime)
            } else if (gc.name == PS_MARK_SWEEP) {
                result[PS_MARK_SWEEP] = new GcActivity(gc.collectionCount, gc.collectionTime)
            } else {
                result[UNKNOWN] = new GcActivity(gc.collectionCount, gc.collectionTime)
            }
        }

        result
    }

    public static Map<String, GcValues.GcActivity> getGcDelta(Map<String, GcActivity> startData, Map<String, GcActivity> endData) {
        def result = [:]
        result[PS_SCAVENGE] = new GcActivity(endData[PS_SCAVENGE].count - startData[PS_SCAVENGE].count, endData[PS_SCAVENGE].time - startData[PS_SCAVENGE].time)
        result[PS_MARK_SWEEP] = new GcActivity(endData[PS_MARK_SWEEP].count - startData[PS_MARK_SWEEP].count, endData[PS_MARK_SWEEP].time - startData[PS_MARK_SWEEP].time)
        result[UNKNOWN] = new GcActivity(endData[UNKNOWN].count - startData[UNKNOWN].count, endData[UNKNOWN].time - startData[UNKNOWN].time)
        
        result
    }

    @Canonical
    public static class GcActivity {
        long count
        long time
    }
}