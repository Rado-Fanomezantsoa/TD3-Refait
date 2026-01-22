import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Log before changes
        DataRetriever dataRetriever = new DataRetriever();
        //Dish dish = dataRetriever.findDishById(4);
        //System.out.println(dish);

        Dish dish = dataRetriever.findDishById(4);
        System.out.println(dish);

        /*Dish toSave = new Dish();
        toSave.setId(9);
        toSave.setName("Riz au poulet");
        toSave.setPrice(10000.5);
        toSave.setDishType(DishTypeEnum.MAIN);
        List<Ingredient> painChocoIngredients = new ArrayList<>();
        painChocoIngredients.add();

        Dish saved = dataRetriever.saveDish(toSave);

        // Afficher le résultat
        System.out.println("Plat sauvegardé : " + saved.getName());
        System.out.println("ID attribué : " + saved.getId());
*/
    }
        // Log after changes
//        dish.setIngredients(List.of(new Ingredient(1), new Ingredient(2)));
//        Dish newDish = dataRetriever.saveDish(dish);
//        System.out.println(newDish);

        // Ingredient creations
        //List<Ingredient> createdIngredients = dataRetriever.createIngredients(List.of(new Ingredient(null, "Fromage", CategoryEnum.DAIRY, 1200.0)));
        //System.out.println(createdIngredients);
}
