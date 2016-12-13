package xml.marshaller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

interface Converter {

    Object method(String ob);
}

public class XMLUnmarshaller {
//Converting map

    Map<Class<?>, Converter> convertMap = new HashMap<>();

    public XMLUnmarshaller() {
        convertMap.put(Integer.class, (Converter) (String ob) -> Integer.parseInt(ob));
        convertMap.put(String.class, (Converter) (String ob) -> ob);
        convertMap.put(int.class, (Converter) (String ob) -> Integer.parseInt(ob));
        convertMap.put(Byte.class, (Converter) (String ob) -> Byte.parseByte(ob));
        convertMap.put(Long.class, (Converter) (String ob) -> Long.getLong(ob));
        convertMap.put(double.class, (Converter) (String ob) -> Double.parseDouble(ob));
        //fill it 
    }

    public Stack<Object> stackBuilder = new Stack();

    private static boolean isNumberExtender(Class<?> objToCheck) {
        return Number.class.isAssignableFrom(objToCheck);
    }

    private static boolean isCharacterExtender(Class<?> objToCheck) {
        return Character.class.isAssignableFrom(objToCheck);
    }

    private static boolean isMapEntry(Object objToCheck) {
        return (objToCheck instanceof MapEntry);
    }

    private static boolean isStringExtender(Class<?> classToCheck) {
        return String.class.isAssignableFrom(classToCheck);
    }

    public Object parse(String filePath) throws ParserConfigurationException, SAXException, IOException, VerifyError {

        SAXParserFactory saxDoc = SAXParserFactory.newInstance();
        SAXParser saxParser = saxDoc.newSAXParser();

        DefaultHandler handler = new DefaultHandler() {
            //имя текущей атомарной переменной
            String curValue = null;

            @Override
            public void startElement(String uri, String localName, String qName,
                    Attributes attributes) throws SAXException {
                String attrValue = attributes.getValue("type");
                if (attrValue != null) {
                    try {
                        Class<?> clazz = Class.forName(attrValue);
                        Object object;
//Добавится ещё обработка Строк и char (Примитивов + String)
                        if (isNumberExtender(clazz)) {
                            object = clazz.getDeclaredConstructor(String.class).newInstance("0");// Когда наткнемся нa characters - заменим на реальное значение
                            curValue = qName;
                        } else {
                            if (isStringExtender(clazz)) {
                                object = clazz.getDeclaredConstructor().newInstance();
                                curValue = qName;
                            } else {
                                if (Entry.class.isAssignableFrom(clazz)) {
                                    object = new MapEntry();
                                } else {
                                    Constructor<?> plainConstructor = clazz.getDeclaredConstructor();
                                    plainConstructor.setAccessible(true);
                                    object = plainConstructor.newInstance();
                                    plainConstructor.setAccessible(false);
                                }
                            }
                        }
                        stackBuilder.push(object);
                    } catch (Exception e) {
                        int i = 0;
                    }
                } else {
                    curValue = qName;
                }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
                if (curValue != null) {
                    curValue = null;
                } else {
                    if (stackBuilder.size() > 1) {
                        Object lastElem = stackBuilder.pop();
                        Object preLastElem = stackBuilder.pop();
                        if (preLastElem instanceof Collection<?>) {
                            ((Collection<Object>) preLastElem).add(lastElem);
                        } else {
                            if (preLastElem instanceof Map<?, ?>) {
                                ((Map<Object, Object>) preLastElem).put(((MapEntry) lastElem).getKey(), ((MapEntry) lastElem).getValue());
                            } else {
                                if (isMapEntry(preLastElem)) {
                                    if (((MapEntry) preLastElem).getKey() == null) {
                                        ((MapEntry) preLastElem).setKey(lastElem);
                                    } else {
                                        ((MapEntry) preLastElem).setValue(lastElem);
                                    }
                                } else {
                                    try {
                                        Field field = preLastElem.getClass().getDeclaredField(qName);
                                        field.setAccessible(true);
                                        field.set(preLastElem, lastElem);
                                        field.setAccessible(false);
                                    } catch (Exception ex) {
                                    }
                                }
                            }
                        }
                        stackBuilder.push(preLastElem);
                    }
                }
            }

            @Override
            public void characters(char ch[], int start, int length) throws SAXException {
                String tvalue = new String(ch, start, length);
                //Если есть куда класть
                if (curValue != null) {
                    try {
                        Object lastElem = stackBuilder.pop();
                        //Добавить остальные примитивы
                        if (isNumberExtender(lastElem.getClass()) || isStringExtender(lastElem.getClass())) {
                            Constructor<?> plainConstructor = lastElem.getClass().getDeclaredConstructor(String.class);
                            lastElem = plainConstructor.newInstance(tvalue);
                            curValue = null;
                        } else {
                            //Заполняем по имени поля
                            Field field = lastElem.getClass().getDeclaredField(curValue);
                            field.setAccessible(true);
                            field.set(lastElem, convertMap.get(field.getType()).method(tvalue));
                            field.setAccessible(false);

                        }
                        stackBuilder.push(lastElem);
                    } catch (Exception ex) {
                    }
                }
            }
        };
        saxParser.parse(
                new File(filePath), handler);
        return stackBuilder.pop();
    }

}
