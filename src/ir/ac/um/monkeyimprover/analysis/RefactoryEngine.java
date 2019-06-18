package ir.ac.um.monkeyimprover.analysis;

import ir.ac.um.monkeyimprover.analysis.layouts.LayoutInformationExtractor;
import ir.ac.um.monkeyimprover.analysis.methods.CallbackMethodInfo;
import ir.ac.um.monkeyimprover.analysis.utils.AnalysisUtils;
import ir.ac.um.monkeyimprover.utils.Utils;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Samad Paydar
 */
public class RefactoryEngine {
    private MonkeyImprover monkeyImprover;
    //no interactable widget should have less than 5% of the space
    private final static int MIN_WEIGHT_IN_100_SCALE = 5;

    public RefactoryEngine(MonkeyImprover monkeyImprover) {
        this.monkeyImprover = monkeyImprover;
    }

    public void refactorLayout(File xmlFile, List<CallbackMethodInfo> callbackMethodInfos) {
        LayoutInformationExtractor layoutInformationExtractor = new LayoutInformationExtractor(monkeyImprover);
        if (layoutInformationExtractor.isFragment(xmlFile)) {
            Utils.showMessage("\t\tLayout is Fragment: " + xmlFile.getName());
            return;
        }
        try {
            Document document = createNewXMLDocument(xmlFile);
            int numberOfViews = layoutInformationExtractor.getNumberOfViews(xmlFile);
            String[] rootLayoutInfo = layoutInformationExtractor.getRootLayoutInfo(xmlFile);
            String rootLayoutId = rootLayoutInfo[0];
            String rootLayoutContext = rootLayoutInfo[1];

            Utils.showMessage("\t\t" + xmlFile.getName() + " has " + numberOfViews + " views");
            refactorElements(document, rootLayoutId, rootLayoutContext, callbackMethodInfos);

            saveXMLDocument(document, xmlFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Document createNewXMLDocument(File xmlFile) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder;
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlFile);
            document.getDocumentElement().normalize();
            return document;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void saveXMLDocument(Document document, File xmlFile) {
        try {
            document.getDocumentElement().normalize();
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(xmlFile.getParentFile(), xmlFile.getName()));
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(source, result);
            Utils.showMessage("\t\tSaved successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Element createRootLinearLayout(Document document, String rootLayoutId, String rootLayoutContext) {
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

    private void refactorElements(Document document, String rootLayoutId, String rootLayoutContext, List<CallbackMethodInfo> callbackMethodInfos) {
        Element newRootLayout = createRootLinearLayout(document, rootLayoutId, rootLayoutContext);
        Element childLayout1 = createChildLinearLayout(document, 1);
        Element childLayout2 = createChildLinearLayout(document, 0);
        Node currentRootLayout = document.getFirstChild();

        newRootLayout.appendChild(childLayout1);
        newRootLayout.appendChild(childLayout2);

        updateViewWeights(document, callbackMethodInfos);

        addViews(childLayout1, currentRootLayout);
        addNonViewElements(childLayout2, currentRootLayout);

        document.replaceChild(newRootLayout, currentRootLayout);
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

    private void updateViewWeights(Document document, List<CallbackMethodInfo> callbackMethodInfos) {
        List<Node> children = getAllViews(document.getFirstChild());
        List<Element> elementsWithNonZeroWeight = new ArrayList<>();
        List<Element> elementsWithZeroWeight = new ArrayList<>();
        List<Integer> nonZeroWeights = new ArrayList<>();
        for (Node child : children) {
            if (child instanceof Element) {
                Element childElement = (Element) child;
                int weight = computeWeight(childElement, callbackMethodInfos);
                if (weight > 0) {
                    elementsWithNonZeroWeight.add(childElement);
                    nonZeroWeights.add(weight);
                } else {
                    elementsWithZeroWeight.add(childElement);
                }
            }
        }
        if (isAnyWidgetTooSmall(nonZeroWeights) && hasAnyWidgetExtraWeight(nonZeroWeights)) {
            redistributeWeights(nonZeroWeights);
        }
        for (int i = 0; i < elementsWithNonZeroWeight.size(); i++) {
            setAttributes(elementsWithNonZeroWeight.get(i), nonZeroWeights.get(i));
        }
        for (Element childElement : elementsWithZeroWeight) {
            setAttributes(childElement, 0);
        }
    }

    /**
     * This method is used to make sure that no interactable widget is too small.
     * It find the weights that are less than the minimum weight,
     * then borrows weight from other elements
     *
     * @param weights
     */
    private void redistributeWeights(List<Integer> weights) {
        Utils.showMessage("Weights before redistribution: " + weights.toString());
        int requiredWeight = 0;
        int sumOfGoodWeights = 0;
        for (Integer weight : weights) {
            /**
             * in the following statement <= is used instead of <
             * This is to prevent from a widget weight increase to MIN and then decrease
             * by the else block in the next iteration
             */
            if (weight <= RefactoryEngine.MIN_WEIGHT_IN_100_SCALE) {
                requiredWeight += (RefactoryEngine.MIN_WEIGHT_IN_100_SCALE - weight);
            } else {
                sumOfGoodWeights += weight;
            }
        }
        int newScale = sumOfGoodWeights - requiredWeight;
        for (int i = 0; i < weights.size(); i++) {
            int weight = weights.get(i);
            /**
             * in the following statement <= is used instead of <
             * This is to prevent from a widget weight increase to MIN and then decrease
             * by the else block in the next iteration
             */
            if (weight <= RefactoryEngine.MIN_WEIGHT_IN_100_SCALE) {
                weights.set(i, RefactoryEngine.MIN_WEIGHT_IN_100_SCALE);
            } else {
                int newWeight = (int) (weight * 1.0 / sumOfGoodWeights * newScale);
                weights.set(i, newWeight);
            }
        }
        Utils.showMessage("Weights after redistribution: " + weights.toString());
        if (isAnyWidgetTooSmall(weights) && hasAnyWidgetExtraWeight(weights)) {
            redistributeWeights(weights);
        }
    }

    private boolean hasAnyWidgetExtraWeight(List<Integer> weights) {
        boolean result = false;
        for (Integer weight : weights) {
            if (weight > RefactoryEngine.MIN_WEIGHT_IN_100_SCALE) {
                result = true;
                break;
            }
        }
        return result;
    }

    private boolean isAnyWidgetTooSmall(List<Integer> weights) {
        boolean result = false;
        for (Integer weight : weights) {
            if (weight < RefactoryEngine.MIN_WEIGHT_IN_100_SCALE) {
                result = true;
                break;
            }
        }
        return result;
    }

    private int computeWeight(Element childElement, List<CallbackMethodInfo> callbackMethodInfos) {
        int weight = 0;
        String onClick = childElement.getAttribute("android:onClick");
        String viewId = childElement.getAttribute("android:id");
        //NOTE: even when a view has no onclick, the value returned by childElement.getAttribute("android:onClick"); is not null
        if (onClick != null && !onClick.isEmpty()) {
            String callbackMethodName = onClick.trim();
            weight = getWeight(callbackMethodInfos, callbackMethodName);
        } else {
            int index = viewId.lastIndexOf('/');
            if (index != -1) {
                viewId = viewId.substring(index + 1).trim();
            }
            if (viewId != null) {
                weight = getWeightForAnnotatedView(callbackMethodInfos, viewId);
            }
        }
        return weight;
    }

    private void setAttributes(Element childElement, int weight) {
        childElement.setAttribute("android:layout_width", "match_parent");
        childElement.setAttribute("android:layout_height", "0dp");
        childElement.setAttribute("android:layout_weight", Integer.toString(weight));

        childElement.setAttribute("android:layout_margin", "0dp");
        childElement.setAttribute("android:layout_marginLeft", "0dp");
        childElement.setAttribute("android:layout_marginTop", "0dp");
        childElement.setAttribute("android:layout_marginRight", "0dp");
        childElement.setAttribute("android:layout_marginBottom", "0dp");
        childElement.setAttribute("android:padding", "0dp");
        childElement.setAttribute("android:paddingLeft", "0dp");
        childElement.setAttribute("android:paddingTop", "0dp");
        childElement.setAttribute("android:paddingRight", "0dp");
        childElement.setAttribute("android:paddingBottom", "0dp");
    }

    private int getWeightForAnnotatedView(List<CallbackMethodInfo> callbackMethodInfos, String viewId) {
        int weight = 0;
        double complexitySum = 0.0;
        for (CallbackMethodInfo info : callbackMethodInfos) {
            complexitySum += info.getCallbackMethodComplexity().getTotalComplexity();
        }
        for (CallbackMethodInfo info : callbackMethodInfos) {
            if (info.getViewId() != null && info.getViewId().equals(viewId) && info.isBoundByAnnotation()) {
                double complexity = info.getCallbackMethodComplexity().getTotalComplexity();
                weight = (int) ((100.0 * complexity) / complexitySum);
                weight = Math.max(weight, 1);
                break;
            }
        }
        return weight;
    }

    private int getWeight(List<CallbackMethodInfo> callbackMethodInfos, String callbackMethodName) {
        int weight = 0;
        double complexitySum = 0.0;
        for (CallbackMethodInfo info : callbackMethodInfos) {
            complexitySum += info.getCallbackMethodComplexity().getTotalComplexity();
        }
        for (CallbackMethodInfo info : callbackMethodInfos) {
            if (info.getCallbackName().equals(callbackMethodName)) {
                double complexity = info.getCallbackMethodComplexity().getTotalComplexity();
                weight = (int) ((100.0 * complexity) / complexitySum);
                weight = Math.max(weight, 1);
                break;
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
