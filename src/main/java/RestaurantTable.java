import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RestaurantTable {
    private int id;
    private int number;
    private List<Order> orders = new ArrayList<>();

    public RestaurantTable(int id, int number) {
        this.id = id;
        this.number = number;
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public List<Order> getOrders() {
        return orders;
    }


    public void addOrder(Order order) {
        orders.add(order);
    }

    public boolean isAvailableAt(Instant t) {
        for (Order order : orders) {
            TableOrder tableOrder = order.getTableOrder();
            if (tableOrder != null &&
                    !t.isBefore(tableOrder.getArrivalDatetime()) &&
                    !t.isAfter(tableOrder.getDepartureDatetime())) {
                return false;
            }
        }
        return true;
    }
}