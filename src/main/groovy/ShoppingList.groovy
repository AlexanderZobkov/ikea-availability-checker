import groovy.transform.CompileStatic

@CompileStatic
interface ShoppingList {

    void load(InputStream is)

    List<String> extractProductIds()

    void dumpAvailabilityReport(Map<String, Stock> stocks, Map<String, String> storeIds, OutputStream os)
}
