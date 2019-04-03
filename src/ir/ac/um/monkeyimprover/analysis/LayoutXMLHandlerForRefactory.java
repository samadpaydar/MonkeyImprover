package ir.ac.um.monkeyimprover.analysis;

import ir.ac.um.monkeyimprover.analysis.layouts.LayoutInformationExtractor;
import org.w3c.dom.*;
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
    private File xmlFile;
    private int numberOfViews;
    private String rootLayoutId;
    private String rootLayoutContext;
    private List<CallbackMethodInfo> callbackMethodInfoList;

    public LayoutXMLHandlerForRefactory(MonkeyImprover monkeyImprover, File xmlFile, List<CallbackMethodInfo> callbackMethodInfoList) {
        this.monkeyImprover = monkeyImprover;
        this.xmlFile = xmlFile;
        this.callbackMethodInfoList = callbackMethodInfoList;
    }


    public void run() {

        LayoutInformationExtractor layoutInformationExtractor = new LayoutInformationExtractor();
        if (layoutInformationExtractor.isFragment(xmlFile)) {
            return;
        }
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlFile);
            document.getDocumentElement().normalize();

            numberOfViews = layoutInformationExtractor.getNumberOfViews(xmlFile);
            String[] rootLayoutInfo = layoutInformationExtractor.getRootLayoutInfo(xmlFile);
            rootLayoutId = rootLayoutInfo[0];
            rootLayoutContext = rootLayoutInfo[1];

            monkeyImprover.showMessage(xmlFile.getName() + " has " + numberOfViews + " views");
            //update attribute value
            //  updateAttributeValue(document);

            addRootLayout(document);

            //write the updated document to file or console
            document.getDocumentElement().normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(xmlFile.getParentFile(), xmlFile.getName()));
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

    private Element createRootLinearLayout(Document document) {
        Element linearLayout = createLinearLayout(document);
        linearLayout.setAttribute("xmlns:android", "http://schemas.android.com/apk/res/android");
        linearLayout.setAttribute("xmlns:tools", "http://schemas.android.com/tools");
        linearLayout.setAttribute("android:layout_height", "match_parent");
        if (rootLayoutId != null) {
            linearLayout.setAttribute("android:id", rootLayoutId);
        }
        if (rootLayoutContext != null) {
            linearLayout.setAttribute("tools:context", rootLayoutContext);
        }
        return linearLayout;
    }

    private Element createChildLinearLayout(Document document, int weight) {
        Element linearLayout = createLinearLayout(document);
        linearLayout.setAttribute("android:layout_height", "0px");
        linearLayout.setAttribute("android:layout_weight", Integer.toString(weight));
        return linearLayout;
    }

    private Element createLinearLayout(Document document) {
        Element linearLayout = document.createElement("LinearLayout");
        linearLayout.setAttribute("android:layout_width", "match_parent");
        linearLayout.setAttribute("android:orientation", "vertical");
        return linearLayout;
    }

    private Node addRootLayout(Document document) {
        Element newRootLayout = createRootLinearLayout(document);
        Element childLayout1 = createChildLinearLayout(document, 9);
        Element childLayout2 = createChildLinearLayout(document, 1);
        Node currentRootLayout = document.getFirstChild();

        newRootLayout.appendChild(childLayout1);
        newRootLayout.appendChild(childLayout2);

        updateViewWeights(document);

        addViews(childLayout1, currentRootLayout);
        addNonViewElements(childLayout2, currentRootLayout);

        document.replaceChild(newRootLayout, currentRootLayout);
        return newRootLayout;
    }

    private void addViews(Node newParent, Node parent) {
        NodeList childrenNodes = parent.getChildNodes();
        for (int i = 0; i < childrenNodes.getLength(); i++) {
            Node child = childrenNodes.item(i);
            if (AnalysisUtils.isAnAndroidView(child.getNodeName())) {
                Node childClone = child.cloneNode(false);
                newParent.appendChild(childClone);
                if (childClone instanceof Element) {
                    removeUnnecessaryAttributes((Element) childClone);
                }
            }
            addViews(newParent, child);
        }
    }

    private void addNonViewElements(Node newParent, Node parent) {
        NodeList childrenNodes = parent.getChildNodes();
        for (int i = 0; i < childrenNodes.getLength(); i++) {
            Node child = childrenNodes.item(i);
            if (!AnalysisUtils.isAnAndroidView(child.getNodeName())) {
                Node childClone = child.cloneNode(false);
                newParent.appendChild(childClone);
                if (childClone instanceof Element) {
                    removeUnnecessaryAttributes((Element) childClone);
                }
            }
            addNonViewElements(newParent, child);
        }
    }

    /**
     * since intermediate layouts are removed during refactory, it is required to remove some attributes that might
     * refer to the removed intermediate layouts
     *
     * @param element
     */
    private void removeUnnecessaryAttributes(Element element) {
        String[] unnecessaryAttributes = {
                "android:layout_above", "android:layout_alignBaseline",
                "android:layout_alignBottom", "android:layout_alignEnd",
                "android:layout_alignLeft", "android:layout_alignParentBottom",
                "android:layout_alignParentEnd", "android:layout_alignParentLeft",
                "android:layout_alignParentRight", "android:layout_alignParentStart",
                "android:layout_alignParentTop", "android:layout_alignRight",
                "android:layout_alignStart", "android:layout_alignTop",
                "android:layout_alignWithParentIfMissing", "android:layout_below",
                "android:layout_centerHorizontal", "android:layout_centerInParent",
                "android:layout_centerVertical", "android:layout_toEndOf",
                "android:layout_toLeftOf", "android:layout_toRightOf", "android:layout_toStartOf"
        };
        for (String attribute : unnecessaryAttributes) {
            element.removeAttribute(attribute);
        }
    }

    private void updateViewWeights(Document document) {
        List<Node> children = getAllViews(document.getFirstChild());
        for (Node child : children) {
            if (child instanceof Element) {
                Element childElement = (Element) child;
                String onClick = childElement.getAttribute("android:onClick");
                int weight = 1;
                if (onClick != null) {
                    String callbackMethodName = onClick.trim();
                    weight = getWeight(callbackMethodName);
                }
                childElement.setAttribute("android:layout_width", "match_parent");
                childElement.setAttribute("android:layout_height", "0dp");
                childElement.setAttribute("android:layout_weight", Integer.toString(weight));
            }
        }
    }

    private int getWeight(String callbackMethodName) {
        int weight = 1;
        double complexitySum = 0.0;
        for (CallbackMethodInfo info : callbackMethodInfoList) {
            complexitySum += info.getCallbackMethodComplexity();
        }
        for (CallbackMethodInfo info : callbackMethodInfoList) {
            if (info.getCallbackName().equals(callbackMethodName)) {
                double complexity = info.getCallbackMethodComplexity();
                weight = (int) ((100.0 * complexity) / complexitySum);
                weight = Math.max(weight, 1);
            }
        }
        return weight;
    }

    private List<Node> getAllViews(Node parent) {
        List<Node> children = new ArrayList<>();
        NodeList childrenNodes = parent.getChildNodes();
        for (int i = 0; i < childrenNodes.getLength(); i++) {
            Node child = childrenNodes.item(i);
            if (AnalysisUtils.isAnAndroidView(child.getNodeName())) {
                children.add(child);
            }
            children.addAll(getAllViews(childrenNodes.item(i)));
        }
        return children;
    }

}
