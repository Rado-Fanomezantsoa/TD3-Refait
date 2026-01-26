import java.time.Instant;

public class StockMovement {
    private Integer id;
    private StockValue value;
    private MouvementTypeEnum type;
    private Instant creationDatetime;

    public StockMovement() {
    }

    public StockMovement(Instant creationDatetime, StockValue value, MouvementTypeEnum type, Integer id) {
        this.creationDatetime = creationDatetime;
        this.value = value;
        this.type = type;
        this.id = id;
    }

    public Instant getCreationDatetime() {
        return creationDatetime;
    }

    public void setCreationDatetime(Instant creationDatetime) {
        this.creationDatetime = creationDatetime;
    }

    public MouvementTypeEnum getType() {
        return type;
    }

    public void setType(MouvementTypeEnum type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public StockValue getValue() {
        return value;
    }

    public void setValue(StockValue value) {
        this.value = value;
    }
}
