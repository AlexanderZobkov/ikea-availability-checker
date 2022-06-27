import groovy.transform.CompileStatic

import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant

@CompileStatic
class App {

    static List<String> storesId = ['344', // Белая Дача
                                    '336', // Теплый Стан
                                    '335', //Химки
    ]

    static String shoppingListCsvFile = './final-ever-sale-shopping-list.csv'

    static void main(String[] args) {
        ShoppingList shoppingList = new ShoppingListImpl()
        InputStream is = Files.newInputStream(Paths.get(shoppingListCsvFile))
        shoppingList.load(is)
        ProductAvailabilityChecker availabilityChecker = new ProductAvailabilityCheckerImpl()
        List<String> productIds = shoppingList.extractProductIds()
        Map<String, Stock> stocks = availabilityChecker.checkStocks(productIds, storesId)
        OutputStream os = Files.newOutputStream(Paths.get(shoppingListCsvFile + '-report-' + Instant.now() + '.csv'))
        shoppingList.dumpAvailabilityReport(stocks, os)
    }

}
