package xml.test;

public class Test {

    private double firstField;
    TestSub classField;
    String stringField;

    public Test(double a, String str, TestSub i) {
        this.firstField = a;
        this.stringField = str;
        classField = i;
    }

    public Test() {
    }
}
