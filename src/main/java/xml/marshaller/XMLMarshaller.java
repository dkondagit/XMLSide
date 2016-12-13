package xml.marshaller;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public final class XMLMarshaller {

    private static boolean isNumberExtender(Object objToCheck) {
        return objToCheck != null ? Number.class.isAssignableFrom(objToCheck.getClass()) : false;
    }

    private static boolean isStringExtender(Class<?> classToCheck) {
        return String.class.isAssignableFrom(classToCheck);
    }

    private static Element marshallList(Object listToMarshall, String name, Document doc) throws ParserConfigurationException, IllegalArgumentException, IllegalAccessException {
        Element listNode = doc.createElement(name);
        listNode.setAttribute("type", listToMarshall.getClass().getName());
        for (Object collectionElem : ((Collection<Object>) listToMarshall)) {
            listNode.appendChild(marshallObject(collectionElem, "element", doc));//possible to add annotated Name
        }
        return listNode;
    }

    private static Element marshallMap(Object mapToMarshall, String name, Document doc) throws ParserConfigurationException, IllegalArgumentException, IllegalAccessException {
        Element listNode = doc.createElement(name);
        listNode.setAttribute("type", mapToMarshall.getClass().getName());
        for (Entry<Object, Object> collectionElem : ((Map<Object, Object>) mapToMarshall).entrySet()) {
            Element entry = doc.createElement("entry");
            entry.setAttribute("type", collectionElem.getClass().getName());

            entry.appendChild(marshallObject(collectionElem.getKey(), "key", doc));

            entry.appendChild(marshallObject(collectionElem.getValue(), "value", doc));
            listNode.appendChild(entry);
        }
        return listNode;
    }

    public static void marshallObject(Object objectToMarshall, String name, String filePath) throws ParserConfigurationException, IllegalArgumentException, IllegalAccessException, TransformerConfigurationException, TransformerException {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.newDocument();
        doc.appendChild(marshallObject(objectToMarshall, name, doc));
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(new File(filePath)));
        System.out.println("File saved!");
    }

    private static Element marshallObject(Object objectToMarshall, String name, Document doc) throws ParserConfigurationException, IllegalArgumentException, IllegalAccessException {

        if (objectToMarshall instanceof Collection<?>) {
            return marshallList(objectToMarshall, name, doc);
        }
        if (objectToMarshall instanceof Map<?, ?>) {
            return marshallMap(objectToMarshall, name, doc);
        }
        Element parent = doc.createElement(name);
        parent.setAttribute("type", objectToMarshall.getClass().getName());
        // добавится String, наследники Number  и char 
        if (isNumberExtender(objectToMarshall)) {
            parent.appendChild(doc.createTextNode(objectToMarshall.toString()));
            return parent;
        } else if (isStringExtender(objectToMarshall.getClass())) {
            parent.appendChild(doc.createTextNode(objectToMarshall != null ? ((objectToMarshall).toString()) : "null"));
            return parent;
        }
        Field[] fields = objectToMarshall.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            Element subElem = doc.createElement(field.getName());
            //if primitive - write, if complex- recursion. Need to add Char
            if (isNumberExtender(field.get(objectToMarshall))) {
                subElem.appendChild(doc.createTextNode(field.get(objectToMarshall).toString()));
                parent.appendChild(subElem);
            } else if (isStringExtender(field.getType())) {
                subElem.appendChild(doc.createTextNode(field.get(objectToMarshall) != null ? (field.get(objectToMarshall).toString()) : "null"));
                parent.appendChild(subElem);
            } else {
                parent.appendChild(marshallObject(field.get(objectToMarshall), field.getName(), doc));
            }
            field.setAccessible(false);
        }
        //  parent.appendChild(elem);
        return parent;
    }

}
