package luyen.tradebot.Trade.util.enumTraderBot;

public enum OrderTypeSystem {
    NEW_ORDERS("NEW_ORDER"),
    CLOSE_ORDERS("CLOSE_POSITION");

    private String name;

    OrderTypeSystem(String name) {
        this.name = name;
    }
    public String getName() {
        return this.name;
    }
    public static OrderTypeSystem getOrderTypeByName(String name){
        for(OrderTypeSystem order : values()){
            if(order.getName().equals(name)){
                return order;
            }
        }
        return null;
    }


}
