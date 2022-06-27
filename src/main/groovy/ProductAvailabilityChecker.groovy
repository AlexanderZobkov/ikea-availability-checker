import groovy.transform.CompileStatic

@CompileStatic
interface ProductAvailabilityChecker {

    Map<String, Stock> checkStocks(List<String> productIds, List<String> stores)

}
