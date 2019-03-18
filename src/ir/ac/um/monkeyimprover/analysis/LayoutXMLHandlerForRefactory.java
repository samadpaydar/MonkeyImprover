package ir.ac.um.monkeyimprover.analysis;

import org.w3c.dom.*;
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
            updateViewWeights(document);

            //write the updated document to file or console
            document.getDocumentElement().normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(xmlFile.getParentFile(), "refactored_" + xmlFile.getName()));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(source, result);
        } catch (SAXException | ParserConfigurationException | IOException | TransformerException e) {
            e.printStackTrace();
        }
    }

   /* private void addElement(Document doc) {
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

    private void deleteElement(Document doc) {
        NodeList employees = doc.getElementsByTagName("Employee");
        Element emp = null;
        //loop for each employee
        for (int i = 0; i < employees.getLength(); i++) {
            emp = (Element) employees.item(i);
            Node genderNode = emp.getElementsByTagName("gender").item(0);
            emp.removeChild(genderNode);
        }

    }

    private void updateAttributeValue(Document doc) {
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


    private void addRootLayoutToGridLayout(Document document, int numberOfViews) {
        Node root = document.getFirstChild();
        Element gridLayoutElement = document.createElement("GridLayout");
        gridLayoutElement.setAttribute("xmlns:android", "http://schemas.android.com/apk/res/android");
        gridLayoutElement.setAttribute("android:layout_width", "match_parent");
        gridLayoutElement.setAttribute("android:layout_height", "match_parent");
        gridLayoutElement.setAttribute("android:rowCount", Integer.toString(numberOfViews));
        gridLayoutElement.setAttribute("android:columnCount", "1");
        NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            gridLayoutElement.appendChild(children.item(i).cloneNode(true));
        }
        document.replaceChild(gridLayoutElement, root);
    }

    private void updateViewWeights(Document document) {
        List<Node> children = getAllViews(document.getFirstChild());
        for(Node child: children) {
            NamedNodeMap attributeMap = child.getAttributes();
            Node node = attributeMap.getNamedItem("android:onClick");
            String value = node.toString();
            monkeyImprover.showMessage("value: " + value);
        }
    }

    private List<Node> getAllViews(Node parent) {
        List<Node> children = new ArrayList<>();
        NodeList childrenNodes = parent.getChildNodes();
        for(int i =0; i<childrenNodes.getLength(); i++) {
            children.add(childrenNodes.item(i));
            children.addAll(getAllViews(childrenNodes.item(i)));
        }
        return children;
    }
}
