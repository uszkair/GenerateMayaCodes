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
                "public class {entityName} extends BaseEntity implements Serializable { ";

        NodeList nList = document.getElementsByTagName("field");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;

                String fieldType = eElement.getElementsByTagName("fieldType").item(0).getTextContent();
                String fieldName = eElement.getElementsByTagName("fieldName").item(0).getTextContent();
                String comment   = eElement.getElementsByTagName("comment").item(0).getTextContent();

                // parent field
                if(fieldType != null && fieldType.equals("parent")){
                    String fieldTypeName = eElement.getElementsByTagName("fieldTypeName").item(0).getTextContent();
                    String idName = eElement.getElementsByTagName("idName").item(0).getTextContent();

                    entityFileContentStr = entityFileContentStr.concat("\n\n@ManyToOne(cascade = CascadeType.REFRESH, fetch = FetchType.LAZY)" +
                            "\n@JoinColumn(name = "+idName+")"+
                            "\nprivate "+fieldTypeName+" "+fieldName+"; // "+comment);

                }else {
                // sample field
                    entityFileContentStr = entityFileContentStr.concat("\n private "+fieldType+" "+fieldName+"; // "+comment);
                }



            }
        }

        System.out.println(entityFileContentStr);
    }
    private static File getResourceFile(final String fileName) throws Exception {
        File resource = new ClassPathResource(fileName).getFile();
        return resource;
    }
}
