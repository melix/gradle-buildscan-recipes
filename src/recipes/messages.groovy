// This recipe will automatically add values and tags based on project properties
// by convention. All project properties that start with `buildScanValue`, `buildScanLink` and
// `buildScanTag` will be converted appropriately

buildScan.buildFinished {
    gradle.rootProject.properties.each { prop ->
        String pName = prop.key
        def value = prop.value?.toString()
        if (pName.startsWith('buildScanValue')) {
            buildScan.value pName-'buildScanValue', value
        } else if (pName.startsWith('buildScanTag')) {
            buildScan.tag pName-'buildScanTag'
        } else if (pName.startsWith('buildScanLink')) {
            buildScan.link pName-'buildScanLink', value
        }
    }
}
