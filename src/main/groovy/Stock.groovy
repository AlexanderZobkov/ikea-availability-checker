import groovy.transform.CompileStatic

@CompileStatic
class Stock {

    String productId
    Map<String, Availability> whereAvailable // [shopId:Availability]

    static class Availability {
        String storeId
        String storeName
        Integer stock
    }

}
