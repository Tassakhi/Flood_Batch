#!/usr/bin/python
from mininet.topo import Topo
from mininet.net import Mininet
from mininet.node import RemoteController
from mininet.node import OVSKernelSwitch
from mininet.link import OVSLink
from mininet.cli import CLI
from mininet.log import setLogLevel


class TreeTopo(Topo):

    def build( self, depth=1, fanout=2 ):
        self.hostNum = 1
        self.switchNum = 1
        self.numOfHosts = fanout ** depth
        self.sWsDict = {}
        f = open('physical_network.xml', 'w')
        f.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<physical_network>\n")
        f.close()
        self.addTree( depth, fanout )
        f = open('physical_network.xml', 'a')
        f.write("</physical_network>\n")
        f.close()

    def addTree( self, depth, fanout ):
        """Add a subtree starting with node n.
           returns: last node added"""
        isSwitch = depth > 0
        f = open('physical_network.xml', 'a')
        if isSwitch:
            node = self.addSwitch( 's%s' % self.switchNum )
            self.sWsDict[node] = 0
            self.switchNum += 1
            for _ in range( fanout ):
                child = self.addTree( depth - 1, fanout )
                self.addLink( node, child )
                self.sWsDict[node] += 1
                if child.startswith('h'):
                    swNumber = int(node[1:])
                    swNumberHexa = '%016x'%swNumber
                    finalSwNumber = ':'.join(swNumberHexa[i:i+2] for i in range(0,16,2))
                    f.write("<HSLink>\n<from>"+ child + "</from>\n<to>" + finalSwNumber + "</to>\n<port>" + str(self.sWsDict[node]) + "</port>\n<bandwidth>1000</bandwidth>\n</HSLink>\n")
                else:
                    firstSwNumber = int(node[1:])
                    secondSwNumber = int(child[1:])
                    firstSwNumberHexa = '%016x'%firstSwNumber
                    secondSwNumberHexa = '%016x'%secondSwNumber
                    firstFinalSwNumber = ':'.join(firstSwNumberHexa[i:i+2] for i in range(0,16,2))
                    secondFinalSwNumber = ':'.join(secondSwNumberHexa[i:i+2] for i in range(0,16,2))
                    f.write("<ISLink>\n<from>"+ firstFinalSwNumber + "</from>\n<to>" + secondFinalSwNumber + "</to>\n<bandwidth>1000</bandwidth>\n</ISLink>\n")
        else:
            node = self.addHost( 'h%s' % self.hostNum )
            if self.hostNum == self.numOfHosts:
                nextHostNum = 1
            else:
                nextHostNum = self.hostNum + 1
            f.write("<host>\n<name>h"+ str(self.hostNum) + "</name>\n<CPU>100</CPU>\n<next>h" + str(nextHostNum) + "</next>\n</host>\n")
            self.hostNum += 1
        f.close()
        return node

        

def runMininet():
    "Create and test a simple network"
    topo = TreeTopo(3, 2)
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
