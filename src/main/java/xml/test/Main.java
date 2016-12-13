package xml.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;
import xml.marshaller.XMLMarshaller;
import xml.marshaller.XMLUnmarshaller;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException, ParserConfigurationException, TransformerConfigurationException, TransformerException, SAXException, IOException {
        TestSub testSub = new TestSub(78);
        Test test = new Test(2, "checking it up", testSub);
        List<Test> list = new ArrayList<>();
        List<List<Test>> listIn = new LinkedList<>();
        List<List<List<Test>>> listInIn = new ArrayList<>();

        list.add(test);
        list.add(test);
        listIn.add(list);
        listIn.add(list);
        listInIn.add(listIn);
        listInIn.add(listIn);

        /*   Map<String, Test> map = new HashMap<>();
        Map<String, Map<String, Test>> mappy = new HashMap<>();
        map.put("lie", test);
        map.put("lie1", test);

        mappy.put("he", map);
        mappy.put("hd", map);
        mappy.put("ht", map);
        mappy.put("hf", map);*/
        XMLMarshaller.marshallObject(listInIn, "TestList", "file.xml");
        List<List<List<Test>>> tst = (List<List<List<Test>>>) new XMLUnmarshaller().parse("file.xml");

    }
}
