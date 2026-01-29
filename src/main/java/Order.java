import java.time.Instant;
import java.util.List;

public class Order {
    private int id;
    private String reference;
    private Instant creationDatetime;
    private List<DishOrder> dishOrders;
    private TableOrder tableOrder; // Nouveau attribut obligatoire

    // Constructor (adapte à tes besoins, ex. sans id pour création)
    public Order(String reference, Instant creationDatetime, List<DishOrder> dishOrders, TableOrder tableOrder) {
        this.reference = reference;
        this.creationDatetime = creationDatetime;
        this.dishOrders = dishOrders;
        this.tableOrder = tableOrder;
    }

    // Getters existants (assume déjà présents)
    public int getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public List<DishOrder> getDishOrders() {
        return dishOrders;
    }

    // Nouveau getter/setter pour tableOrder
    public TableOrder getTableOrder() {
        return tableOrder;
    }

    public void setTableOrder(TableOrder tableOrder) {
        this.tableOrder = tableOrder;
    }

    /**
     * Calcule le montant total sans VAT.
     * Sum (dish.price * quantity) pour tous les dishOrders.
     */
    public Double getTotalAmountWithoutVAT() {
        double total = 0.0;
        for (DishOrder dishOrder : dishOrders) {
            total += dishOrder.getDish().getPrice() * dishOrder.getQuantity();
        }
        return total;
    }

    /**
     * Calcule le montant total avec VAT (assume 20% comme exemple ; adapte si besoin).
     */
    public Double getTotalAmountWithVAT() {
        double withoutVAT = getTotalAmountWithoutVAT();
        return withoutVAT * 1.20; // 20% VAT
    }
}