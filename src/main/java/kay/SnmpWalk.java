package kay;

import com.vaadin.spring.annotation.SpringComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.CommunityTarget;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
@SpringComponent
public class SnmpWalk {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${snmp.port}")
    private String port;
    private int snmpVersion = SnmpConstants.version2c;
    private Snmp snmp;
    private TableUtils tableUtils;
    private static SnmpWalk instance;

    private SnmpWalk() throws IOException {
        TransportMapping transport = new DefaultUdpTransportMapping();
        transport.listen();
        snmp = new Snmp(transport);
        logger.info("SnmpWalk is listening UPD on: "+transport.getListenAddress().toString());
        DefaultPDUFactory defaultPDUFactory = new DefaultPDUFactory();
        tableUtils = new TableUtils(snmp, defaultPDUFactory);

    }
    @PreDestroy
    private void preDestroy() throws IOException {
       if (snmp != null) {
           snmp.close();
           logger.info("SNMP closed");
       }
    }

    public static SnmpWalk getInstance() {
        if (instance != null) return instance;
        try {
            instance = new SnmpWalk();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return instance;
    }


    public List<FDBentry> getFDB(String ipAddress, String community) {
        CommunityTarget communityTarget = getCommunityTarget(ipAddress, community);
        logger.info("Getting FDB from "+ipAddress+"...");
        HashMap<Integer, String[]> ports = getPorts(communityTarget);
        //ports.forEach((k,v)-> System.out.println("ports = getPorts "+k+":"+v[0]+":"+v[1]));
        List<TableEvent> tableEvents = tableUtils.getTable(communityTarget, new OID[]{new OID("1.3.6.1.2.1.17.7.1.2.2.1.2")}, null, null);

        if (isNotValid(tableEvents)) {
            logger.info("tableUtils.getTable: No answer from "+ipAddress);
            return null;
        }
        ArrayList<FDBentry> result = new ArrayList<>();
        tableEvents.forEach(tableEvent -> {
            if (!tableEvent.isError()) {
                Integer portNum = Integer.parseInt(tableEvent.getColumns()[0].toValueString());
                if (portNum != 0) {
                    FDBentry fdb = new FDBentry();
                    fdb.setVlan(tableEvent.getIndex().getValue()[0]);
                    fdb.setMac(String.format("%02x", tableEvent.getIndex().getValue()[1])
                       + ":" + String.format("%02x", tableEvent.getIndex().getValue()[2])
                       + ":" + String.format("%02x", tableEvent.getIndex().getValue()[3])
                       + ":" + String.format("%02x", tableEvent.getIndex().getValue()[4])
                       + ":" + String.format("%02x", tableEvent.getIndex().getValue()[5])
                       + ":" + String.format("%02x", tableEvent.getIndex().getValue()[6])
                    );
                    fdb.setPortNum(portNum);
                    String[] s = ports.get(portNum);
                    if (s != null) {
                        fdb.setPortName(s[0]);
                        fdb.setPortAlias(s[1]);
                    }
                    result.add(fdb);
                }
            }
        });
        logger.info("received " + result.size()+ " FDB entries from " + ipAddress);
        return result;

    }

    private CommunityTarget getCommunityTarget(String ipAddress, String community) {
        CommunityTarget communityTarget = new CommunityTarget();
        communityTarget.setCommunity(new OctetString(community));
        communityTarget.setAddress(new UdpAddress(ipAddress + "/" + port));
        communityTarget.setRetries(2);
        communityTarget.setTimeout(2000);
        communityTarget.setVersion(snmpVersion);
        return communityTarget;
    }

    private HashMap<Integer, String> getHashMap(CommunityTarget communityTarget, String oid) {
        List<TableEvent> tableEvents = tableUtils.getTable(communityTarget, new OID[]{new OID(oid)}, null, null);
        if (isNotValid(tableEvents)) {
            return null;
        }
        HashMap<Integer, String> result = new HashMap<>();
        tableEvents.forEach(tableEvent -> {
            if (!tableEvent.isError()) {
                if (!tableEvent.getColumns()[0].toValueString().equals(""))
                    result.put(tableEvent.getIndex().get(0), tableEvent.getColumns()[0].toValueString());
            }
        });
        return result;
    }
    private HashMap<Integer,String> getLLDPremoteIp(CommunityTarget communityTarget, String oid){
        List<TableEvent> tableEvents = tableUtils.getTable(communityTarget, new OID[]{new OID(oid)}, null, null);
        if (isNotValid(tableEvents)) {
            return null;
        }
        HashMap<Integer, String> result = new HashMap<>();
        tableEvents.forEach(tableEvent -> {
            if (!tableEvent.isError()) {
                result.put(tableEvent.getIndex().get(1)
                            ,String.valueOf(tableEvent.getIndex().get(5))
                        +"."+String.valueOf(tableEvent.getIndex().get(6))
                        +"."+String.valueOf(tableEvent.getIndex().get(7))
                        +"."+String.valueOf(tableEvent.getIndex().get(8))
                );
            }
        });
        return result;
    }

    private HashMap<Integer, VariableBinding[]> getSnmpTable(CommunityTarget communityTarget, String[] oids, int keyOidIndex) {
        OID[] OIDS = new OID[oids.length];
        for (int i = 0; i < oids.length; i++) {
            OIDS[i] = new OID(oids[i]);
        }

        List<TableEvent> tableEvents = tableUtils.getTable(communityTarget, OIDS, null, null);
        if (isNotValid(tableEvents)) {
            return null;
        }

        HashMap<Integer, VariableBinding[]> result = new HashMap<>(128);
        tableEvents.forEach(tableEvent -> {
            if (!tableEvent.isError()) {
                result.put(tableEvent.getIndex().get(keyOidIndex), tableEvent.getColumns());
            }
        });
        return result;
    }

    public HashMap<Integer, String[]> getPorts(CommunityTarget communityTarget) {
        HashMap<Integer, String[]> result = new HashMap<>();
        HashMap<Integer, String> portDescriptions = getHashMap(communityTarget, "1.3.6.1.2.1.2.2.1.2");
        HashMap<Integer, String> portAliases = getHashMap(communityTarget, "1.3.6.1.2.1.31.1.1.1.18");
        HashMap<Integer, String> fdbPorts = getHashMap(communityTarget, "1.3.6.1.2.1.17.1.4.1.2");

//        portDescriptions.forEach((k,v) -> System.out.println("getPorts portDescriptions "+k+":"+v));
//        portAliases.forEach((k,v) -> System.out.println("getPorts portAliases "+k+":"+v));
//        fdbPorts.forEach((k,v) -> System.out.println("getPorts fdbPorts "+k+":"+v));

        if (fdbPorts != null)
            fdbPorts.forEach((k, v) ->
                    result.put(k, new String[]{
                            (portDescriptions != null) ? portDescriptions.get(Integer.parseInt(v)):null
                            ,(portAliases != null) ? portAliases.get(Integer.parseInt(v)):null}));
        return result;
    }

    public List<String> getTable(String ipAddress, String community, String[] oids){
        OID[] OIDS = new OID[oids.length];
        for (int i = 0; i < oids.length; i++) {
            OIDS[i] = new OID(oids[i]);
        }
        CommunityTarget communityTarget = getCommunityTarget(ipAddress, community);
        List<TableEvent> tableEvents = tableUtils.getTable(communityTarget, OIDS, null, null);
        System.out.println("tableEvents.size() = "+tableEvents.size());
        tableEvents.forEach(e-> System.out.println(e));

        if (isNotValid(tableEvents)) {
            logger.info("testOID: No answer from "+ipAddress);
            return null;
        }
        ArrayList<String> result = new ArrayList<>();
        tableEvents.forEach(tableEvent -> {
            if (!tableEvent.isError()) {
                    result.add(tableEvent.toString());
            }
        });
        return result;
    }

    private boolean isNotValid(List<TableEvent> tableEvents) {
        return tableEvents.size() == 0 || (tableEvents.get(0).isError() && tableEvents.size() == 1);
    }

    public List<LLDPentry> getLLDP(String ipAddress, String community){
        CommunityTarget communityTarget = getCommunityTarget(ipAddress, community);
        logger.info("Getting LLDP from "+ipAddress+"...");
        HashMap<Integer, VariableBinding[]> lldp = getSnmpTable(communityTarget
                ,new String[]{"1.0.8802.1.1.2.1.4.1.1.7","1.0.8802.1.1.2.1.4.1.1.8"
                        ,"1.0.8802.1.1.2.1.4.1.1.9","1.0.8802.1.1.2.1.4.1.1.10"},1);
        if (lldp == null) {
            logger.info("LLDP is not supported on "+ipAddress);
            return null;};

        HashMap<Integer, VariableBinding[]> localPorts = getSnmpTable(communityTarget
                ,new String[]{"1.0.8802.1.1.2.1.3.7.1.3","1.0.8802.1.1.2.1.3.7.1.4"},0);

        HashMap<Integer,String> remoteIp = getLLDPremoteIp(communityTarget,"1.0.8802.1.1.2.1.4.2.1.3");

        List<LLDPentry> result = new ArrayList<>();
        lldp.forEach((k,v)->{
            LLDPentry entry = new LLDPentry();
            entry.setLocalPortNum(k);
            entry.setRemotePortName(v[0].toValueString());
            entry.setRemotePortAlias(v[1].toValueString());
            entry.setRemoteSysName(v[2].toValueString());
            entry.setRemoteSysDesc(v[3].toValueString());
            entry.setLocalPortName(localPorts.get(k)[0].toValueString());
            entry.setLocalPortAlias(localPorts.get(k)[1].toValueString());
            if (remoteIp != null) entry.setRemoteIpAddress(remoteIp.get(k));

            result.add(entry);
        });
        logger.info("received " + result.size()+ " LLDP entries from " + ipAddress);
        return result;
    }

    public List<IFentry> getInterfaces(String ipAddress, String community){
        CommunityTarget communityTarget = getCommunityTarget(ipAddress, community);
        logger.info("Getting Interfaces from "+ipAddress+"...");
        HashMap<Integer, VariableBinding[]> ifc = getSnmpTable(communityTarget
                ,new String[]{"1.3.6.1.2.1.2.2.1.2"
                        , "1.3.6.1.2.1.2.2.1.3"
                        //,"1.3.6.1.2.1.2.2.1.4"
                        //,"1.3.6.1.2.1.2.2.1.5"
                        //,"1.3.6.1.2.1.2.2.1.6"
                        ,"1.3.6.1.2.1.2.2.1.7"
                        ,"1.3.6.1.2.1.2.2.1.8"
                        ,"1.3.6.1.2.1.31.1.1.1.18"
                },0);
        if (ifc == null) {
            logger.info("IfMIB is not supported on "+ipAddress);
            return null;};


        List<IFentry> result = new ArrayList<>();
        ifc.forEach((k,v)->{
            IFentry entry = new IFentry();
            entry.setIfIndex(k);
            entry.setIfDescr(v[0].toValueString());
            entry.setIfType(Integer.parseInt(v[1].toValueString()));
            //entry.setIfMTU(Integer.parseInt(v[2].toValueString()));
            //entry.setIfSpeed(Integer.parseInt(v[2].toValueString()));
            //entry.setIfPhysAddress(v[4].toValueString());
            entry.setIfAdminStatus(v[2].toValueString());
            entry.setIfOperStatus(v[3].toValueString());
            entry.setIfAlias(v[4].toValueString());
            if (entry.isValidIfType()) result.add(entry);
        });
        logger.info("received " + result.size()+ " ifMIB entries from " + ipAddress);
        return result;
    }


}