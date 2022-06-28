import groovy.json.JsonSlurper

class ProductAvailabilityCheckerImpl implements ProductAvailabilityChecker {

    String ikeaAvailabilityCheckerTool = 'npx ikea-availability-checker@2.0.0-alpha.3'

    @Override
    Map<String, Stock> checkStocks(List<String> productIds, List<String> storesId) {
        String productAvailabilityCmd = "${ikeaAvailabilityCheckerTool} " +
                'stock ' +
                "--store ${storesId.join(',')} " +
                '--json ' +
                productIds.join(' ')
        // TODO Add logging like: log.log(Level.SEVERE, "Executing {0}", productAvailabilityCmd)
        // TODO: Handle tool exit with non-zero code.
        String productsAvailabilityJson = productAvailabilityCmd.execute().text
        // TODO Add logging like: log.log(Level.SEVERE, "Result {0}", productsAvailabilityJson)
        List<Map<String, Object>> productsAvailability = new JsonSlurper().parseText(productsAvailabilityJson)
        return productsAvailability.collect { jsonProduct ->
            [product  : jsonProduct.productId,
             storeId  : jsonProduct.availability.store.buCode,
             storeName: jsonProduct.availability.store.name,
             stock    : jsonProduct.availability.stock,
            ]
        }
                .groupBy { it.product }
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
