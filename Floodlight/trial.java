package net.floodlightcontroller.virtualnetworkallocator;

/**
 * Created by tasneem on 28/5/16.
 *
 */
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.lang.*;

public class trial {

    public static void main(String[] args) {

        try {
            File inputFile = new File("/home/tasneem/Desktop/flood/Floodlight/floodlight/src/main/java/net/floodlightcontroller/virtualnetworkallocator/mr");
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList mrList = doc.getElementsByTagName("map");
            for (int currentmr = 0; currentmr < mrList.getLength(); currentmr++) {
                Node mrNode = mrList.item(currentmr);
                if (mrNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element myElement = (Element) mrNode;
                    Integer mrInput = Integer.parseInt(myElement.getElementsByTagName("input")
                            .item(0).getTextContent());
                    Integer mrMapper = Integer.parseInt(myElement.getElementsByTagName("mappers")
                            .item(0).getTextContent());
                    Integer mrReducer = Integer.parseInt(myElement.getElementsByTagName("reducer")
                            .item(0).getTextContent());
                    Integer mrShuffle = Integer.parseInt(myElement.getElementsByTagName("shuffle")
                            .item(0).getTextContent());
                    Integer mrBMI =Integer.parseInt(myElement.getElementsByTagName("bmi")
                            .item(0).getTextContent());
                    Integer mrBMO =Integer.parseInt(myElement.getElementsByTagName("bmo")
                            .item(0).getTextContent());
                    Integer mrBRI =Integer.parseInt(myElement.getElementsByTagName("bri")
                            .item(0).getTextContent());
                    Integer mrBRO =Integer.parseInt( myElement.getElementsByTagName("bro")
                            .item(0).getTextContent());
                    Double mrCPU = Double.parseDouble(myElement.getElementsByTagName("cpu")
                            .item(0).getTextContent());
                    System.out.println("Staff id : " + myElement.getAttribute("name"));
                    System.out.println("First Name : " + myElement.getElementsByTagName("bro").item(0).getTextContent());
                    System.out.println("Last Name : " + myElement.getElementsByTagName("cpu").item(0).getTextContent());

                    if (mrCPU <= 0 || mrCPU == null) {
                        System.err.println("This MapReduce Request has a null or negative CPU in " + myElement.getAttribute("name") + "! Please correct it and try again!");
                        System.exit(-1);
                    } else if (mrCPU > 100) {
                        System.err.println("This MapReduce Request has a CPU higher than 100% in " + myElement.getAttribute("name") + "! Please correct this and try again!");
                        System.exit(-1);
                    }


                }
            }

        }catch(Exception e) {
            e.printStackTrace();
        }
    }

}