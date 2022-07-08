import groovy.transform.CompileStatic

import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant

@CompileStatic
class App {

    static final Map<String, String> storeIds = ['335': 'Химки',
                                                 '336': 'Теплый Стан',
                                                 '344': 'Белая Дача',
                                                  //'616': 'ИКЕА Сити в ТРЦ Мозаика',
                                                  //'609' : 'ИКЕА Сити в ТРЦ АВИАПАРК',
                                                  //'643' : 'ИКЕА Сити в ТРК Европолис'
    ]

    static final String shoppingListCsvFile = './final-ever-sale-shopping-list.csv'

    static void main(String[] args) {
        ShoppingList shoppingList = new ShoppingListImpl()
        InputStream is = Files.newInputStream(Paths.get(shoppingListCsvFile))
        shoppingList.load(is)
        ProductAvailabilityChecker availabilityChecker = new IngkaProductAvailabilityChecker()
        List<String> productIds = shoppingList.extractProductIds()
        Map<String, Stock> stocks = availabilityChecker.checkStocks(productIds, storeIds.keySet().asList())
        OutputStream os = Files.newOutputStream(Paths.get(shoppingListCsvFile + '-report-' + Instant.now() + '.csv'))
        shoppingList.dumpAvailabilityReport(stocks, storeIds, os)
    }

}
