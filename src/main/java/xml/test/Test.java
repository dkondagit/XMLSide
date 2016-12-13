package xml.test;

public class Test {

    private int firstField;
    TestSub classField;
    String stringField;

    public Test(int a, String str, TestSub i) {
        this.firstField = a;
        this.stringField = str;
        classField = i;
    }

    public Test() {
    }
}
