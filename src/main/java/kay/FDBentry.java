package kay;

public class FDBentry {
    private Integer portNum;
    private String portName;
    private String mac;
    private Integer vlan;
    private String portAlias;

    public String getPortAlias() {
        return portAlias;
    }















    public void setPortAlias(String portAlias) {
        this.portAlias = portAlias;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }



    public void setPortNum(Integer portNum) {
        this.portNum = portNum;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setVlan(Integer vlan) {
        this.vlan = vlan;
    }

    public Integer getPortNum() {
        return portNum;
    }

    public String getMac() {
        return mac;
    }

    public Integer getVlan() {
        return vlan;
    }

    @Override
    public String toString() {
        return (portNum +":"+ portName +":"+ portAlias +":"+ mac.replaceAll(":","") +":"+ vlan).toLowerCase() ;
    }
}
