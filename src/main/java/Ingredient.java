import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class Ingredient {
    private Integer id;
    private String name;
    private CategoryEnum category;
    private Double price;
    private List<StockMovement> stockMovementList;


    public List<StockMovement> getStockMovementList() {
        return stockMovementList;
    }

    public void setStockMovementList(List<StockMovement> stockMovementList) {
        this.stockMovementList = stockMovementList;
    }


    public StockValue getStockValueAt(Instant instant){
        if (stockMovementList == null || stockMovementList.isEmpty()) {
            return new StockValue(0.0, null);
        }

        double quantity = 0.0;
        UnitType unit = null;
        List<StockMovement> mouvValide = stockMovementList.stream()
                .filter(sm -> !sm.getCreationDatetime().isAfter(instant))
                .toList();

        for (StockMovement sm : mouvValide) {
            if (unit == null) {
                unit = sm.getValue().getUnit();
            }

            if(sm.getType() == MouvementTypeEnum.OUT){
                quantity -= sm.getValue().getQuantity();
            } else {
                quantity += sm.getValue().getQuantity();
            }
        }
        return new StockValue(quantity, unit);
    }


    public Ingredient(CategoryEnum category, Integer id, Double price, String name, List<StockMovement> stockMovementList) {
        this.category = category;
        this.id = id;
        this.price = price;
        this.name = name;
        this.stockMovementList = stockMovementList;
    }

    public Ingredient(CategoryEnum category, Integer id, String name, Double price) {
        this.category = category;
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Ingredient() {

    }
    public CategoryEnum getCategory() {
        return category;
    }

    public void setCategory(CategoryEnum category) {
        this.category = category;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }


    @Override
    public String toString() {
        return "Ingredient{" +
                "category=" + category +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Ingredient that = (Ingredient) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && category == that.category && Objects.equals(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category, price);
    }

}
