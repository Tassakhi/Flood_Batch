package net.floodlightcontroller.batchtask;

/**
 * Created by tasneem on 2/6/16.
 */

import java.lang.*;
import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.Map.Entry;

import net.floodlightcontroller.core.IOFSwitchListener;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryListener;
import net.floodlightcontroller.virtualnetworkallocator.VirtualNetworkAllocator;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;


import javafx.util.Pair;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.PortChangeType;
import net.floodlightcontroller.core.internal.IOFSwitchService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscoveryService;
import net.floodlightcontroller.linkdiscovery.LinkInfo;
import net.floodlightcontroller.multipathrouting.IMultiPathRoutingService;
import net.floodlightcontroller.queuepusher.QueuePusherResponse;
import net.floodlightcontroller.queuepusher.Utils;
import net.floodlightcontroller.routing.IRoutingService;
import net.floodlightcontroller.routing.Link;
import net.floodlightcontroller.routing.Route;
import net.floodlightcontroller.topology.NodePortTuple;
import org.projectfloodlight.openflow.protocol.*;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.action.OFActionSetQueue;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;
import net.floodlightcontroller.packet.Ethernet;
import org.slf4j.Logger;
import java.util.ArrayList;
import org.slf4j.LoggerFactory;




public class BatchTask implements Runnable, IFloodlightModule,
        IOFSwitchListener, ILinkDiscoveryListener {


    protected static IFloodlightProviderService floodlightProvider;
    protected static IOFSwitchService switchService;
    protected static ILinkDiscoveryService linkService;
    protected static IMultiPathRoutingService multipathService;
    protected static HashMap<String, Double> hostsFreeCPU;
    protected static HashMap<String, Pair<String, Integer>> hostToSwitchLink;
    protected static HashMap<String, Integer> hostToSwitchPort;
    protected static HashMap<String, String> nextHostMap;
    protected static ArrayList<String> commandsToSWs;
    protected static HashMap<Pair<Link, Link>, Integer> changesToLinks;
    protected static HashMap<String, Integer> changesToHostSWLinks;
    protected static HashMap<String, Integer> virtualHostsToIPAddr;
    protected static HashMap<String, Integer> virtualHostToID;
    protected static Logger logger;
    protected static Double highestCPUAvailable;
    protected static Double sumCPUforReqSplit;
    protected static int currentId;
    protected static int tempCurrentId;
    protected static Integer VNRHopCount;
    protected static Set<String> zeroToTen;
    protected static Set<String> tenToTwenty;
    protected static Set<String> twentyToThirty;
    protected static Set<String> thirtyToForty;
    protected static Set<String> fortyToFifty;
    protected static Set<String> fiftyToSixty;
    protected static Set<String> sixtyToSeventy;
    protected static Set<String> seventyToEighty;
    protected static Set<String> eightyToNinety;
    protected static Set<String> ninetyToOneHundred;

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        return null;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
        return null;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        l.add(IOFSwitchService.class);
        l.add(ILinkDiscoveryService.class);
        l.add(IRoutingService.class);
        l.add(IMultiPathRoutingService.class);
        return l;
    }

    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {
        floodlightProvider = context
                .getServiceImpl(IFloodlightProviderService.class);
        switchService = context.getServiceImpl(IOFSwitchService.class);
        linkService = context.getServiceImpl(ILinkDiscoveryService.class);
        multipathService = context.getServiceImpl(IMultiPathRoutingService.class);
        hostsFreeCPU = new HashMap<String, Double>();
        hostToSwitchLink = new HashMap<String, Pair<String, Integer>>();
        hostToSwitchPort = new HashMap<String, Integer>();
        nextHostMap = new HashMap<String, String>();
        zeroToTen = new HashSet<String>();
        tenToTwenty = new HashSet<String>();
        twentyToThirty = new HashSet<String>();
        thirtyToForty = new HashSet<String>();
        fortyToFifty = new HashSet<String>();
        fiftyToSixty = new HashSet<String>();
        sixtyToSeventy = new HashSet<String>();
        seventyToEighty = new HashSet<String>();
        eightyToNinety = new HashSet<String>();
        ninetyToOneHundred = new HashSet<String>();
        highestCPUAvailable = new Double(-1);
        sumCPUforReqSplit = new Double(-1);
        currentId = 1;
        tempCurrentId = 1;
        VNRHopCount = 0;
        commandsToSWs = new ArrayList<String>();
        changesToLinks = new HashMap<Pair<Link, Link>, Integer>();
        changesToHostSWLinks = new HashMap<String, Integer>();
        virtualHostsToIPAddr = new HashMap<String, Integer>();
        virtualHostToID = new HashMap<String, Integer>();
        logger = LoggerFactory.getLogger(VirtualNetworkAllocator.class);
    }

    public void startUp(FloodlightModuleContext context) {
        switchService.addOFSwitchListener(this);
        linkService.addListener(this);
    }


    public void run() {
        for (int i = 2; i < 41; i++) {
            for (int k = 0; k < 50000; k++) {
                boolean result = processMapReduce("/home/tasneem/Desktop/flood/Floodlight/floodlight/src/main/java/net/floodlightcontroller/batchtask/mr.xml");
            }
        }
    }

    boolean processMapReduce(String fileName) {
        HashMap<String, Integer> mapInput = new HashMap<>();
        HashMap<String, Integer> mapInputBW=new HashMap<String, Integer>();
        HashMap<String, Integer> mapOutputBW=new HashMap<String, Integer>();
        HashMap<String, Integer> reduceInputBW=new HashMap<String, Integer>();
        HashMap<String, Integer> reduceOutputBW=new HashMap<String, Integer>();
        HashMap<String, Integer> mapCPULoad=new HashMap<String, Integer>();
        HashMap<String, Integer> reduceCPULoad=new HashMap<String, Integer>();
        Double totalMapReduceCPULoad = new Double(0);
        Integer totalMapReduceBWLoad= new Integer(0);
        //List<Double> listCPULoad= new ArrayList<>();
        List<Integer> listBWLoad = new ArrayList<>();
        HashMap<String,Integer> botBWLoad= new HashMap<>();
        HashMap<String,Double> botCPULoad= new HashMap<>();
        System.out.println("testing");
        try {
            File inputFile = new File("/home/tasneem/Desktop/flood/Floodlight/floodlight/src/main/java/net/floodlightcontroller/batchtask/mr.xml");
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
                    String mrExample= myElement.getElementsByTagName("example").item(0).getTextContent();
                    Integer mrInput = Integer.parseInt(myElement.getElementsByTagName("input")
                            .item(0).getTextContent());
                    Integer mrMapper = Integer.parseInt(myElement.getElementsByTagName("mappers")
                            .item(0).getTextContent());
                    Integer mrReducer = Integer.parseInt(myElement.getElementsByTagName("reducer")
                            .item(0).getTextContent());
                    Integer mrShuffle = Integer.parseInt(myElement.getElementsByTagName("shuffle")
                            .item(0).getTextContent());
                    Integer mrBMI = Integer.parseInt(myElement.getElementsByTagName("bmi")
                            .item(0).getTextContent());
                    Integer mrBMO = Integer.parseInt(myElement.getElementsByTagName("bmo")
                            .item(0).getTextContent());
                    Integer mrBRI = Integer.parseInt(myElement.getElementsByTagName("bri")
                            .item(0).getTextContent());
                    Integer mrBRO = Integer.parseInt(myElement.getElementsByTagName("bro")
                            .item(0).getTextContent());
                    Double vmMrCPU = Double.parseDouble(myElement.getElementsByTagName("vmcpu")
                            .item(0).getTextContent());
                    Double vrMrCPU = Double.parseDouble(myElement.getElementsByTagName("vrcpu")
                            .item(0).getTextContent());
                    Double vmCPU= vmMrCPU+vrMrCPU;
                    Integer vmBW=mrBMI+mrBMO+mrBRI+mrBRO;
                    System.out.println("Staff id : " + myElement.getAttribute("name"));
                    System.out.println("First Name : " + myElement.getElementsByTagName("bro").item(0).getTextContent());
                    System.out.println("Last Name : " + myElement.getElementsByTagName("cpu").item(0).getTextContent());

                    if (vmMrCPU <= 0 || vmMrCPU == null) {
                        System.err.println("This MapReduce Request has a null or negative CPU in " + myElement.getAttribute("name") + "! Please correct it and try again!");
                        System.exit(-1);
                    } else if (vmMrCPU > 100) {
                        System.err.println("This MapReduce Request has a CPU higher than 100% in " + myElement.getAttribute("name") + "! Please correct this and try again!");
                        System.exit(-1);
                    }
                    /*listCPULoad.add(vmMrCPU);
                    listCPULoad.add(vrMrCPU);
                    listBWLoad.add(mrInput);

                    listBWLoad.add(mrBMI);
                    listBWLoad.add(mrBMO);
                    listBWLoad.add(mrBRI);
                    listBWLoad.add(mrBRO);*/

                    botCPULoad.put(mrExample,vmCPU);
                    botBWLoad.put(mrExample, vmBW);

                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        for (Double myDouble : botCPULoad.values()) {
            totalMapReduceCPULoad += myDouble;
            System.out.println(totalMapReduceCPULoad);

        }

        return false;

    }



    @Override
    public void switchAdded(DatapathId switchId) {
    }

    @Override
    public void switchRemoved(DatapathId switchId) {
    }

    @Override
    public void switchActivated(DatapathId switchId) {
    }

    @Override
    public void switchPortChanged(DatapathId switchId, OFPortDesc port,
                                  PortChangeType type) {
    }

    @Override
    public void switchChanged(DatapathId switchId) {
    }

    @Override
    public void linkDiscoveryUpdate(LDUpdate update) {
    }

    @Override
    public void linkDiscoveryUpdate(List<LDUpdate> updateList) {
    }

}


