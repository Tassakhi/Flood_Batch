#!/usr/bin/python
from mininet.topo import Topo
from mininet.net import Mininet
from mininet.node import RemoteController
from mininet.node import OVSKernelSwitch
from mininet.link import OVSLink
from mininet.cli import CLI
from mininet.log import setLogLevel

class FatTreeTopo(Topo):
    CoreSwitchList = []
    AggSwitchList = []
    EdgeSwitchList = []
    HostList = []
    iNUMBER = 0
    def __init__(self):
    
        f = open('physical_network_fat.xml', 'w')
        f.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<physical_network>\n")
        f.close()        

        self.sWsDict = {}

        iNUMBER = 5
        
        self.currentSwitch = 1
        self.currentHost = 1
        self.iNUMBER = iNUMBER
        self.iCoreLayerSwitch = iNUMBER
        self.iAggLayerSwitch = iNUMBER * 2
        self.iEdgeLayerSwitch = iNUMBER * 2
        self.iHost = self.iEdgeLayerSwitch * 2 
    
    
        #Init Topo
        Topo.__init__(self)

    def createTopo(self):    
        self.createCoreLayerSwitch(self.iCoreLayerSwitch)
        self.createAggLayerSwitch(self.iAggLayerSwitch)
        self.createEdgeLayerSwitch(self.iEdgeLayerSwitch)
        self.createHost(self.iHost)

    """
    Create Switch and Host
    """

    def createCoreLayerSwitch(self, NUMBER):
        for x in range(1, NUMBER+1):
            self.CoreSwitchList.append(self.addSwitch("s" + str(self.currentSwitch)))
            self.currentSwitch += 1

    def createAggLayerSwitch(self, NUMBER):
        for x in range(1, NUMBER+1):
            self.AggSwitchList.append(self.addSwitch("s" + str(self.currentSwitch)))
            self.currentSwitch += 1
        

    def createEdgeLayerSwitch(self, NUMBER):
        for x in range(1, NUMBER+1):
            self.EdgeSwitchList.append(self.addSwitch("s" + str(self.currentSwitch)))
            self.sWsDict["s" + str(self.currentSwitch)] = 0            
            self.currentSwitch += 1
            
    
    def createHost(self, NUMBER):
        f = open('physical_network_fat.xml', 'a')
        for x in range(1, NUMBER+1):
            self.HostList.append(self.addHost("h" + str(self.currentHost)))
            if self.currentHost == self.iHost:
                nextHostNum = 1
            else:
                nextHostNum = self.currentHost + 1
            f.write("<host>\n<name>h"+ str(self.currentHost) + "</name>\n<CPU>100</CPU>\n<next>h" + str(nextHostNum) + "</next>\n</host>\n")
            self.currentHost += 1
        f.close()

    """
    Create Link 
    """
    def createLink(self):
        f = open('physical_network_fat.xml', 'a')
        for x in range(0, self.iAggLayerSwitch, 2):
            firstSwNumber = int(self.CoreSwitchList[0][1:])
            secondSwNumber = int(self.AggSwitchList[x][1:])
            firstSwNumberHexa = '%016x'%firstSwNumber
            secondSwNumberHexa = '%016x'%secondSwNumber
            firstFinalSwNumber = ':'.join(firstSwNumberHexa[i:i+2] for i in range(0,16,2))
            secondFinalSwNumber = ':'.join(secondSwNumberHexa[i:i+2] for i in range(0,16,2))
            f.write("<ISLink>\n<from>"+ firstFinalSwNumber + "</from>\n<to>" + secondFinalSwNumber + "</to>\n<bandwidth>1000</bandwidth>\n</ISLink>\n")
            self.addLink(self.CoreSwitchList[0], self.AggSwitchList[x])
            firstSwNumber = int(self.CoreSwitchList[1][1:])
            secondSwNumber = int(self.AggSwitchList[x][1:])
            firstSwNumberHexa = '%016x'%firstSwNumber
            secondSwNumberHexa = '%016x'%secondSwNumber
            firstFinalSwNumber = ':'.join(firstSwNumberHexa[i:i+2] for i in range(0,16,2))
            secondFinalSwNumber = ':'.join(secondSwNumberHexa[i:i+2] for i in range(0,16,2))
            f.write("<ISLink>\n<from>"+ firstFinalSwNumber + "</from>\n<to>" + secondFinalSwNumber + "</to>\n<bandwidth>1000</bandwidth>\n</ISLink>\n")
            self.addLink(self.CoreSwitchList[1], self.AggSwitchList[x])
        for x in range(1, self.iAggLayerSwitch, 2):
            firstSwNumber = int(self.CoreSwitchList[2][1:])
            secondSwNumber = int(self.AggSwitchList[x][1:])
            firstSwNumberHexa = '%016x'%firstSwNumber
            secondSwNumberHexa = '%016x'%secondSwNumber
            firstFinalSwNumber = ':'.join(firstSwNumberHexa[i:i+2] for i in range(0,16,2))
            secondFinalSwNumber = ':'.join(secondSwNumberHexa[i:i+2] for i in range(0,16,2))
            f.write("<ISLink>\n<from>"+ firstFinalSwNumber + "</from>\n<to>" + secondFinalSwNumber + "</to>\n<bandwidth>1000</bandwidth>\n</ISLink>\n")
            self.addLink(self.CoreSwitchList[2], self.AggSwitchList[x])
            firstSwNumber = int(self.CoreSwitchList[3][1:])
            secondSwNumber = int(self.AggSwitchList[x][1:])
            firstSwNumberHexa = '%016x'%firstSwNumber
            secondSwNumberHexa = '%016x'%secondSwNumber
            firstFinalSwNumber = ':'.join(firstSwNumberHexa[i:i+2] for i in range(0,16,2))
            secondFinalSwNumber = ':'.join(secondSwNumberHexa[i:i+2] for i in range(0,16,2))
            f.write("<ISLink>\n<from>"+ firstFinalSwNumber + "</from>\n<to>" + secondFinalSwNumber + "</to>\n<bandwidth>1000</bandwidth>\n</ISLink>\n")
            self.addLink(self.CoreSwitchList[3], self.AggSwitchList[x])
        
        for x in range(0, self.iAggLayerSwitch, 2):
            firstSwNumber = int(self.AggSwitchList[x][1:])
            secondSwNumber = int(self.EdgeSwitchList[x][1:])
            firstSwNumberHexa = '%016x'%firstSwNumber
            secondSwNumberHexa = '%016x'%secondSwNumber
            firstFinalSwNumber = ':'.join(firstSwNumberHexa[i:i+2] for i in range(0,16,2))
            secondFinalSwNumber = ':'.join(secondSwNumberHexa[i:i+2] for i in range(0,16,2))
            self.sWsDict[self.EdgeSwitchList[x]] += 1
            f.write("<ISLink>\n<from>"+ firstFinalSwNumber + "</from>\n<to>" + secondFinalSwNumber + "</to>\n<bandwidth>1000</bandwidth>\n</ISLink>\n")
            self.addLink(self.AggSwitchList[x], self.EdgeSwitchList[x])
            firstSwNumber = int(self.AggSwitchList[x][1:])
            secondSwNumber = int(self.EdgeSwitchList[x+1][1:])
            firstSwNumberHexa = '%016x'%firstSwNumber
            secondSwNumberHexa = '%016x'%secondSwNumber
            firstFinalSwNumber = ':'.join(firstSwNumberHexa[i:i+2] for i in range(0,16,2))
            secondFinalSwNumber = ':'.join(secondSwNumberHexa[i:i+2] for i in range(0,16,2))
            self.sWsDict[self.EdgeSwitchList[x+1]] += 1
            f.write("<ISLink>\n<from>"+ firstFinalSwNumber + "</from>\n<to>" + secondFinalSwNumber + "</to>\n<bandwidth>1000</bandwidth>\n</ISLink>\n")
            self.addLink(self.AggSwitchList[x], self.EdgeSwitchList[x+1])
            firstSwNumber = int(self.AggSwitchList[x+1][1:])
            secondSwNumber = int(self.EdgeSwitchList[x][1:])
            firstSwNumberHexa = '%016x'%firstSwNumber
            secondSwNumberHexa = '%016x'%secondSwNumber
            firstFinalSwNumber = ':'.join(firstSwNumberHexa[i:i+2] for i in range(0,16,2))
            secondFinalSwNumber = ':'.join(secondSwNumberHexa[i:i+2] for i in range(0,16,2))
            self.sWsDict[self.EdgeSwitchList[x]] += 1
            f.write("<ISLink>\n<from>"+ firstFinalSwNumber + "</from>\n<to>" + secondFinalSwNumber + "</to>\n<bandwidth>1000</bandwidth>\n</ISLink>\n")
            self.addLink(self.AggSwitchList[x+1], self.EdgeSwitchList[x])
            firstSwNumber = int(self.AggSwitchList[x+1][1:])
            secondSwNumber = int(self.EdgeSwitchList[x+1][1:])
            firstSwNumberHexa = '%016x'%firstSwNumber
            secondSwNumberHexa = '%016x'%secondSwNumber
            firstFinalSwNumber = ':'.join(firstSwNumberHexa[i:i+2] for i in range(0,16,2))
            secondFinalSwNumber = ':'.join(secondSwNumberHexa[i:i+2] for i in range(0,16,2))
            self.sWsDict[self.EdgeSwitchList[x+1]] += 1
            f.write("<ISLink>\n<from>"+ firstFinalSwNumber + "</from>\n<to>" + secondFinalSwNumber + "</to>\n<bandwidth>1000</bandwidth>\n</ISLink>\n")
            self.addLink(self.AggSwitchList[x+1], self.EdgeSwitchList[x+1])

        for x in range(0, self.iEdgeLayerSwitch):
            ## limit = 2 * x + 1 
            swNumber = int(self.EdgeSwitchList[x][1:])
            swNumberHexa = '%016x'%swNumber
            finalSwNumber = ':'.join(swNumberHexa[i:i+2] for i in range(0,16,2))
            self.sWsDict[self.EdgeSwitchList[x]] += 1
            f.write("<HSLink>\n<from>"+ self.HostList[2 * x] + "</from>\n<to>" + finalSwNumber + "</to>\n<port>" + str(self.sWsDict[self.EdgeSwitchList[x]]) + "</port>\n<bandwidth>1000</bandwidth>\n</HSLink>\n")
            self.addLink(self.EdgeSwitchList[x], self.HostList[2 * x])
            swNumber = int(self.EdgeSwitchList[x][1:])
            swNumberHexa = '%016x'%swNumber
            finalSwNumber = ':'.join(swNumberHexa[i:i+2] for i in range(0,16,2))
            self.sWsDict[self.EdgeSwitchList[x]] += 1
            f.write("<HSLink>\n<from>"+ self.HostList[2 * x + 1] + "</from>\n<to>" + finalSwNumber + "</to>\n<port>" + str(self.sWsDict[self.EdgeSwitchList[x]]) + "</port>\n<bandwidth>1000</bandwidth>\n</HSLink>\n")
            self.addLink(self.EdgeSwitchList[x], self.HostList[2 * x + 1])
        f.close()

def runMininet():
    "Create and test a simple network"
    topo = FatTreeTopo()
    topo.createTopo() 
    topo.createLink()
    f = open('physical_network_fat.xml', 'a')
    f.write("</physical_network>\n")
    f.close()
    rmController = RemoteController("c0", ip="127.0.0.1", port=6653)	
    net = Mininet(controller=rmController, topo=topo, link=OVSLink, switch=OVSKernelSwitch, xterms=False, autoSetMacs=False, autoStaticArp=True, cleanup=False)
    net.start()
    CLI(net)
    net.stopXterms()
    net.stop()

if __name__ == '__main__':
    # Tell mininet to print useful information
    setLogLevel('info')
    runMininet()
