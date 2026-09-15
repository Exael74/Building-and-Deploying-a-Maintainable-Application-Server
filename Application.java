public class Application {
    public static void main(String[] args) {



        get("/pi", () -> 
                String.valueOf(Math.PI));

        get("/e", () -> {
            String message = "Hello world",
            return message;
        });
    }
}
