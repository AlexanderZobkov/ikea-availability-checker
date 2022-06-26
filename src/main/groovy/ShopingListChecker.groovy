import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord;

String ikeaAvailabilityCheckerTool = 'npx ikea-availability-checker@2.0.0-alpha.3'
List<String> storesId = ['344', // Белая Дача
                         '336', // Теплый Стан
                         '335', //Химки
]

Reader file = new FileReader("/home/user/Documents/ikea-final-sale-checker/final-sale-panic.csv")
Iterable<CSVRecord> records = CSVFormat.EXCEL.builder()
        .setHeader()
        .setSkipHeaderRecord(true)
        .build()
        .parse(file)
Map<String, String> productIdToHumanMapping = records
        .findAll { record ->
            record.get(1)
        }
        .collectEntries { record ->
            String humanProductName = record.get(0)
            String productId = record.get(1).replace('.', '')
            [(String.valueOf(productId)): humanProductName]
        }

List<String> products = productIdToHumanMapping.keySet().asList()

String productAvailabilityCmd = "${ikeaAvailabilityCheckerTool} " +
        "stock --store ${storesId.join(',')} " +
        "--json " +
        "${products.join(' ')}"

println productAvailabilityCmd
String productsAvailabilityJson = productAvailabilityCmd.execute().text

println productsAvailabilityJson
List<Map<String, Object>> productsAvailability = new groovy.json.JsonSlurper().parseText(productsAvailabilityJson)

productsAvailability.collect { product ->
    [product: "${productIdToHumanMapping[product.productId]} - ${product.productId}",
    availability:"${product.'availability'.'store'.'name'} ${product.'availability'.'stock'}"]
}.groupBy {it.product}
.each {key, value ->
    println "${key} => ${value.collect {it.availability}.join(', ')}"
}


