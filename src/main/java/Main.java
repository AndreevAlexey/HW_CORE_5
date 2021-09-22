import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {
    // Лог программы
    static StringBuilder LOG;
    // парсер CSV файла
    static List<Employee> parseCSV(String fileName, String[] columnPosition) {
        List<Employee> stuff = null;
        // чтение файла
        try(CSVReader csvReader = new CSVReader(new FileReader(fileName))){
            // параметры чтения в класс
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnPosition);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            // получение списка объектов класса
            stuff = csv.parse();
        } catch (IOException exp) {
            LOG.append(exp.getMessage());
        }
        // результат
        return stuff;
    }
    // получаем значение элемента по указанному тегу
    private static String getTagValue(String tag, Element element) {
        // получаем список элементов по тэгу, берем child первого, в котором значение
        NodeList nodeList = element.getElementsByTagName(tag).item(0).getChildNodes();
        // значение тэга
        Node node = nodeList.item(0);
        return node.getNodeValue();
    }
    // получить объект класса из узла
    static Employee getEmployeeFromNode(Node node) {
        // создаем объект
        Employee employee = new Employee();
        try {
            Element element = (Element) node;
            // получить и установить id
            employee.setId(Integer.parseInt(getTagValue("id", element)));
            // получить и установить firstName
            employee.setFirstName(getTagValue("firstName", element));
            // получить и установить lastName
            employee.setLastName(getTagValue("lastName", element));
            // получить и установить country
            employee.setCountry(getTagValue("country", element));
            // получить и установить age
            employee.setAge(Integer.parseInt(getTagValue("age", element)));
        } catch (Exception exp) {
            LOG.append(exp.getMessage());
        }
        // результат
        return employee;
    }
    // парсер XML
    static List<Employee> parserXML(String fileName) {
        // список объектов
        List<Employee> stuff = new ArrayList<>();
        // начало
        try {
            // DOM-парсер файла
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));
            // корневой узел
            Node root = doc.getDocumentElement();
            // список потомков
            NodeList nodeList = root.getChildNodes();
            // цикл по списку потомков
            for(int i = 0; i < nodeList.getLength(); i++) {
                // узел
                Node node = nodeList.item(i);
                // нужный тип узла
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    // блок данных employee
                    if("employee".equals(node.getNodeName())) {
                        // добавляем новый объект в список
                        stuff.add(getEmployeeFromNode(node));
                    }
                }
            }
        } catch (Exception exp) {
            LOG.append(exp.getMessage());
        }
        return stuff;
    }
    // получить json по списку объектов
    static String listToJason(List<Employee> stuff) {
        String json = null;
        // начало
        try {
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            // определение типа списка объектов для преобразования
            Type listType = new TypeToken<List<Employee>>() {
            }.getType();
            // преобразование
            json = gson.toJson(stuff, listType);
        } catch (Exception exp) {
            LOG.append(exp.getMessage());
        }
        // результат
        return json;
    }
    // запись в файл
    static void writeToFile(String fileName, String str) {
        try(FileWriter fw = new FileWriter(fileName)) {
            fw.write(str);
            fw.flush();
        } catch (IOException exp) {
            LOG.append(exp.getMessage());
        }
    }
    // чтение файла
    static String readFromFile(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch(IOException exp) {
            LOG.append(exp.getMessage());
        }
        return stringBuilder.toString();
    }
    static List<Employee> jsonToList(String json) {
        List<Employee> stuff = new ArrayList<>();
        Employee employee;
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(json);
            JSONArray jsonArray = (JSONArray) obj;
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            for(Object emp : jsonArray) {
                employee = gson.fromJson(emp.toString(), Employee.class);
                stuff.add(employee);
            }
        } catch (org.json.simple.parser.ParseException exp) {
            LOG.append(exp.getMessage());
        }
        return stuff;
    }
    // MAIN
    public static void main(String[] args) {
        LOG = new StringBuilder();
        String fileNameCSV = "data.csv";
        String fileNameJSON = "stuff.json";
        String fileNameJSON2 = "stuff2.json";
        String fileNameXML = "data.xml";
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        List<Employee> employeeList;
        String json;
        /*********** Задание №1 ************/
        // список объектов класса из файла CSV
        employeeList = parseCSV(fileNameCSV, columnMapping);
        // json по списку объектов класса
        json = listToJason(employeeList);
        // запись json в файл
        writeToFile(fileNameJSON, json);
        /*********** Задание №2 ************/
        employeeList = parserXML(fileNameXML);
        // json по списку объектов класса
        json = listToJason(employeeList);
        // запись json в файл
        writeToFile(fileNameJSON2, json);
        /*********** Задание №3 ************/
        // получение json из файла
        json = readFromFile(fileNameJSON);
        // получить список объектов из json
        employeeList = jsonToList(json);
        // вывод объектов в консоль
        for(Employee emp : employeeList) {
            System.out.println(emp);
        }
        // вывод лога
        if(LOG.length() != 0) {
            System.out.println(LOG);
        }
    }
}
