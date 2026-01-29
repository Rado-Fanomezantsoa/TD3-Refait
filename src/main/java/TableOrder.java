import java.time.Instant;

public class TableOrder {
    private RestaurantTable table;
    private Instant arrivalDatetime;
    private Instant departureDatetime;

    public TableOrder(RestaurantTable table, Instant arrivalDatetime, Instant departureDatetime) {
        this.table = table;
        this.arrivalDatetime = arrivalDatetime;
        this.departureDatetime = departureDatetime;
    }

    // Getters
    public RestaurantTable getTable() {
        return table;
    }

    public Instant getArrivalDatetime() {
        return arrivalDatetime;
    }

    public Instant getDepartureDatetime() {
        return departureDatetime;
    }

    // Setters si nécessaires
    public void setTable(RestaurantTable table) {
        this.table = table;
    }

    public void setArrivalDatetime(Instant arrivalDatetime) {
        this.arrivalDatetime = arrivalDatetime;
    }

    public void setDepartureDatetime(Instant departureDatetime) {
        this.departureDatetime = departureDatetime;
    }
}