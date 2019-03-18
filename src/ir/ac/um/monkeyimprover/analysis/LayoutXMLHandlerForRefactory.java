package ir.ac.um.monkeyimprover.analysis;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Samad Paydar
 */
public class LayoutXMLHandlerForRefactory extends DefaultHandler {
    private MonkeyImprover monkeyImprover;
    File xmlFile;

    public LayoutXMLHandlerForRefactory(MonkeyImprover monkeyImprover, File xmlFile) {
        this.monkeyImprover = monkeyImprover;
        this.xmlFile = xmlFile;
    }


    public void run() {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlFile);
            document.getDocumentElement().normalize();

            int numberOfViews = getNumberOfViews();
            monkeyImprover.showMessage(xmlFile.getName() + " has " + numberOfViews + " views");
            //update attribute value
            //  updateAttributeValue(document);

            addRootLayoutToGridLayout(document, numberOfViews);

            //delete element
            //  deleteElement(document);

            //add new element
            //  addElement(document);

            //write the updated document to file or console
            document.getDocumentElement().normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(xmlFile.getParentFile(), "modified" + xmlFile.getName()));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        } catch (SAXException | ParserConfigurationException | IOException | TransformerException e) {
            e.printStackTrace();
        }
    }

   /* private static void addElement(Document doc) {
        NodeList employees = doc.getElementsByTagName("Employee");
        Element emp = null;

        //loop for each employee
        for (int i = 0; i < employees.getLength(); i++) {
            emp = (Element) employees.item(i);
            Element salaryElement = doc.createElement("salary");
            salaryElement.appendChild(doc.createTextNode("10000"));
            emp.appendChild(salaryElement);
        }
    }

    private static void deleteElement(Document doc) {
        NodeList employees = doc.getElementsByTagName("Employee");
        Element emp = null;
        //loop for each employee
        for (int i = 0; i < employees.getLength(); i++) {
            emp = (Element) employees.item(i);
            Node genderNode = emp.getElementsByTagName("gender").item(0);
            emp.removeChild(genderNode);
        }

    }

    private static void updateAttributeValue(Document doc) {
        NodeList employees = doc.getElementsByTagName("Employee");
        Element emp = null;
        //loop for each employee
        for (int i = 0; i < employees.getLength(); i++) {
            emp = (Element) employees.item(i);
            String gender = emp.getElementsByTagName("gender").item(0).getFirstChild().getNodeValue();
            if (gender.equalsIgnoreCase("male")) {
                //prefix id attribute with M
                emp.setAttribute("id", "M" + emp.getAttribute("id"));
            } else {
                //prefix id attribute with F
                emp.setAttribute("id", "F" + emp.getAttribute("id"));
            }
        }
    }*/

    private int getNumberOfViews() {
        int viewCount = 0;
        if (xmlFile.exists() && xmlFile.isFile()) {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();
                LayoutXMLHandler handler = new LayoutXMLHandler();
                saxParser.parse(xmlFile, handler);
                viewCount = handler.getNumberOfViews();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return viewCount;
    }


    private static void addRootLayoutToGridLayout(Document document, int numberOfViews) {
        Element gridLayoutElement = document.createElement("GridLayout");
        gridLayoutElement.setAttribute("xmlns:android", "http://schemas.android.com/apk/res/android");
        gridLayoutElement.setAttribute("android:layout_width", "match_parent");
        gridLayoutElement.setAttribute("android:layout_height", "match_parent");
        gridLayoutElement.setAttribute("android:rowCount", Integer.toString(numberOfViews));
        gridLayoutElement.setAttribute("android:columnCount", "1");

        document.appendChild(gridLayoutElement);
    }
    /*private static void updateElementValue(Document doc) {
        NodeList employees = doc.getElementsByTagName("Employee");
        Element emp = null;
        //loop for each employee
        for(int i=0; i<employees.getLength();i++){
            emp = (Element) employees.item(i);
            Node name = emp.getElementsByTagName("name").item(0).getFirstChild();
            name.setNodeValue(name.getNodeValue().toUpperCase());
        }
    }*/
}
