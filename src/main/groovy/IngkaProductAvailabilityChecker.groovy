import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic

/**
 *  Product availability checker based on calling IKEA ingka API.
 *
 *  https://api.ingka.ikea.com/cia/availabilities/ru/ru?itemNos=00305888,90213182&expand=StoresList,Restocks
 */
@CompileDynamic
class IngkaProductAvailabilityChecker implements ProductAvailabilityChecker {

    private final Map<String, String> headers = ['Connection' : 'close',
                                                 'Accept'     : 'application/json;version=2',
                                                 'Referer'    : 'https://www.ikea.com/',
                                                 'X-Client-Id': 'b6c117e5-ae61-4ef5-b4cc-e0b1e37f0631',
    ]

    private final Map<String, String> storesIdToName = ['344': 'Белая Дача',
                                                        '336': 'Теплый Стан',
                                                        '335': 'Химки',
    ]

    @Override
    Map<String, Stock> checkStocks(List<String> productIds, List<String> stores) {
        String urlSpec = "https://api.ingka.ikea.com" +
                "/cia/availabilities/ru/ru" +
                "?itemNos=${productIds.join(',')}&expand=StoresList,Restocks"
        String responseJson = new URL(urlSpec)
                .getText(connectTimeout: 5000,
                        readTimeout: 10000,
                        useCaches: false,
                        allowUserInteraction: false,
                        requestProperties: headers)
        Object productsAvailability = new JsonSlurper().parseText(responseJson)
        List<Object> availabilities = productsAvailability.availabilities
        return availabilities.findAll { availability ->
            availability.classUnitKey.classUnitType == 'STO' &&
                    availability.classUnitKey.classUnitCode in stores &&
                    availability.itemKey.itemType in ['ART', 'SPR']
        }.collect { availability ->
            [product  : availability.itemKey.itemNo,
             storeId  : availability.classUnitKey.classUnitCode,
             storeName: storesIdToName[availability.classUnitKey.classUnitCode],
             stock    : availability.buyingOption.cashCarry?.availability?.quantity ?: 0,
            ]
        }.groupBy { it.product }
                .collectEntries { productId, tmpAvailability ->
                    Map<String, Stock.Availability> whereAvailable =
                            tmpAvailability.collectEntries { tmpGroupedAvailability ->
                                Stock.Availability availability =
                                        new Stock.Availability(stock: tmpGroupedAvailability.stock,
                                                storeName: tmpGroupedAvailability.storeName,
                                                storeId: tmpGroupedAvailability.storeId
                                        )
                                [(tmpGroupedAvailability.storeId): availability]
                            }
                    [(productId): new Stock(productId: productId, whereAvailable: whereAvailable)]
                }
    }
}
