import groovy.transform.CompileDynamic
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord

import java.nio.charset.StandardCharsets

@CompileDynamic
class ShoppingListImpl implements ShoppingList {

    private List<CSVRecord> records

    @Override
    void load(InputStream is) {
        CSVFormat format = CSVFormat.EXCEL.builder()
                .setAllowMissingColumnNames(false)
                .build()
        records = CSVParser.parse(is, StandardCharsets.UTF_8, format).getRecords()
    }

    @Override
    List<String> extractProductIds() {
        records.drop(1).findAll { CSVRecord record ->
            record.get(1) &&
                    record.get(1)[1].isInteger()
        }.collect { CSVRecord record ->
            sanitize(record.get(1))
        }.unique()
    }

    @Override
    void dumpAvailabilityReport(Map<String, Stock> stocks, Map<String, String> storeIds, OutputStream os) {
        List<Item> shoppingItems = records.drop(1).collect {
            new Item(product: it.get(0),
                    productId: it.get(1),
                    price: it.get(2).isNumber() ? Integer.parseInt(it.get(2)) : null,
                    requiredCount: it.get(3).isNumber() ? Integer.parseInt(it.get(3)) : null,
                    url: it.get(4),
            )
        }

        List<Tuple2<String, String>> shops = storeIds.collect { storeId, storeName ->
             new Tuple2<String, String>(storeId, storeName)
        }

        List<String> shopNames = shops.collect { it.v2 }

        List<String> headers = records.first().take(5) + [*shopNames]
        CSVFormat csvFormat = CSVFormat.EXCEL.builder()
                .setHeader(*headers)
                .build()
        try (CSVPrinter printer = new CSVPrinter(new OutputStreamWriter(os, StandardCharsets.UTF_8), csvFormat)) {
            shoppingItems.each { Item item ->
                if (item.productId && item.productId[0].isNumber()) {
                    item.availability = shops.collect { shopTuple ->
                        Stock stock = stocks[sanitize(item.productId)]
                        Stock.Availability availability = stock.whereAvailable[shopTuple.v1]
                        availability.stock
                    }
                } else {
                    item.availability = [''] * shops.size()
                }
                printer.printRecord(item.getRecord())
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(e)
        }
    }

    private String sanitize(String productId){
        productId.replace('.', '')
    }

    static private class Item {
        String product
        String productId
        Integer price
        Integer requiredCount
        String url
        String[] availability

        Iterable getRecord() {
            [product,
             productId,
             price,
             requiredCount,
             url,
             *availability
            ]
        }
    }
}
