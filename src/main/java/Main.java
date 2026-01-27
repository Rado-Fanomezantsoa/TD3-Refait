import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Log before changes
        DataRetriever dataRetriever = new DataRetriever();

        Dish dish = dataRetriever.findDishById(4);
        System.out.println(dish);

        /*Dish toSave = new Dish();
        toSave.setId(9);
        toSave.setName("Riz au poulet");
        toSave.setPrice(10000.5);
        toSave.setDishType(DishTypeEnum.MAIN);
        List<Ingredient> painChocoIngredients = new ArrayList<>();
        painChocoIngredients.add();

        Dish saved = dataRetriever.saveDish(toSave);*/

        // Afficher le résultat
        //System.out.println("Plat sauvegardé : " + saved.getName());
        //System.out.println("ID attribué : " + saved.getId());


        //Test du TD4

        Ingredient sugar = new Ingredient();
        sugar.setName("Sugar");
        sugar.setPrice(2500.0);
        sugar.setCategory(CategoryEnum.OTHER);

        List<StockMovement> movements = new ArrayList<>();

        // ===============================
        // 2️⃣ Mouvement IN
        // ===============================
        StockMovement inMovement = new StockMovement();
        inMovement.setType(MouvementTypeEnum.IN);
        inMovement.setCreationDatetime(
                Instant.parse("2024-01-05T08:00:00Z")
        );
        inMovement.setValue(
                new StockValue(5.0, UnitType.KG)
        );
        movements.add(inMovement);

        // ===============================
        // 3️⃣ Mouvement OUT
        // ===============================
        StockMovement outMovement = new StockMovement();
        outMovement.setType(MouvementTypeEnum.OUT);
        outMovement.setCreationDatetime(
                Instant.parse("2024-01-06T12:00:00Z")
        );
        outMovement.setValue(
                new StockValue(0.2, UnitType.KG)
        );
        movements.add(outMovement);

        sugar.setStockMovementList(movements);

        // ===============================
        // 4️⃣ Sauvegarde
        // ===============================
        Ingredient savedIngredient = dataRetriever.saveIngredient(sugar);

        System.out.println("Ingredient saved with id = " + savedIngredient.getId());

        // ===============================
        // 5️⃣ Rechargement depuis la BD
        // ===============================
        Ingredient reloadedIngredient =
                dataRetriever.findIngredientById(savedIngredient.getId());

        // ===============================
        // 6️⃣ Test getStockValueAt
        // ===============================
        Instant testInstant = Instant.parse("2024-01-06T12:00:00Z");

        StockValue stockAtInstant =
                reloadedIngredient.getStockValueAt(testInstant);

        System.out.println("Stock at " + testInstant + " : "
                + stockAtInstant.getQuantity()
                + " "
                + stockAtInstant.getUnit());

        // ===============================
        // 7️⃣ Résultat attendu
        // ===============================
        System.out.println("EXPECTED : 4.8 KG");
    }
        // Log after changes
//        dish.setIngredients(List.of(new Ingredient(1), new Ingredient(2)));
//        Dish newDish = dataRetriever.saveDish(dish);
//        System.out.println(newDish);

        // Ingredient creations
        //List<Ingredient> createdIngredients = dataRetriever.createIngredients(List.of(new Ingredient(null, "Fromage", CategoryEnum.DAIRY, 1200.0)));
        //System.out.println(createdIngredients);
}
