import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class GenerateMayaCode {
    public static void main(String[] args) {
        String fileName ="";
        try {
            generateEntity(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void generateEntity(String fileName) throws Exception{
        File file = getResourceFile("/xml/tenant.xml");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document document = db.parse(file);
        document.getDocumentElement().normalize();

        String entityFileContentStr = "package hu.maya.apps.models;\n" +
                "\n" +
                "import javax.persistence.*;\n" +
                "import java.io.Serializable;\n" +
                "import java.util.Date;\n" +
                "\n" +
                "/**\n" +
                " * {comment}\n" +
                " */\n" +
                "@Entity\n" +
                "@Table\n" +
                "public class {entityName} extends BaseEntity implements Serializable {\n ";

        String entityComment = document.getElementsByTagName("comment").item(0).getTextContent();
        String entityName = document.getElementsByTagName("entityName").item(0).getTextContent();

        entityFileContentStr = entityFileContentStr.replace("{comment}", entityComment).replace("{entityName}", entityName);
        NodeList nList = document.getElementsByTagName("field");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String fieldType = eElement.getElementsByTagName("fieldType").item(0).getTextContent();
                String fieldName = eElement.getElementsByTagName("fieldName").item(0).getTextContent();
                String comment   = eElement.getElementsByTagName("comment").item(0).getTextContent();

                // parent field
                if(eElement.getElementsByTagName("idName").item(0) != null){
                    String idName = eElement.getElementsByTagName("idName").item(0).getTextContent();

                    entityFileContentStr = entityFileContentStr.concat("\n\n@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)" +
                            "\n@JoinColumn(name = \""+idName+"\")");
                }
                entityFileContentStr = entityFileContentStr.concat("\nprivate "+fieldType+" "+fieldName+"; // "+comment);
            }
        }

        // generate entity getter and setter
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;


                String fieldType = eElement.getElementsByTagName("fieldType").item(0).getTextContent();
                String fieldName = eElement.getElementsByTagName("fieldName").item(0).getTextContent();

                // get
                entityFileContentStr = entityFileContentStr.concat("\n\npublic " + fieldType + " get"
                        + (fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1)) + "() " +
                        "{\n\treturn this." + fieldName + " = " + fieldName + ";\n}\n");

                // set
                entityFileContentStr = entityFileContentStr.concat("public void set" +
                        (fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1))  + "(" + fieldType + " " + fieldName + ") " +
                        "{\n\tthis." + fieldName + " = " + fieldName + ";\n}\n");
            }
        }

        // toString

        entityFileContentStr = entityFileContentStr.concat("\n@Override public String toString() { \n\treturn \""+entityName
                +"{\"+" );
        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;

                String fieldName = eElement.getElementsByTagName("fieldName").item(0).getTextContent();
                entityFileContentStr = entityFileContentStr.concat("\""+fieldName+"=\" + "+fieldName+" +" + (temp%2==0? "\n": ""));

            }
        }
        entityFileContentStr = entityFileContentStr.substring(0, entityFileContentStr.length()-1).concat(";");
        entityFileContentStr = entityFileContentStr.concat("\n}\n}");
        System.out.println("Generate "+entityName +" done.");
    }
    private static File getResourceFile(final String fileName) throws Exception {
        File resource = new ClassPathResource(fileName).getFile();
        return resource;
    }
}
