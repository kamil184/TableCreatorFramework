import framework.TableCreator;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
        TableCreator.create(Employee.class,"jdbc:mysql://localhost:3306/mydbtest", "root", "root");
    }
}
