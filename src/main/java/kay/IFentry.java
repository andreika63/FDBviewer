package kay;

public class IFentry {
    private Integer ifIndex;
    private String ifDescr;
    private Integer ifType;
//    private Integer ifMTU;
//    private Integer ifSpeed;
//    private String ifPhysAddress;
    private String ifAdminStatus;
    private String ifOperStatus;
    private String ifAlias;

    public String getIfAlias() {
        return ifAlias;
    }

    public void setIfAlias(String ifAlias) {
        this.ifAlias = ifAlias;
    }

    public Integer getIfIndex() {
        return ifIndex;
    }

    public void setIfIndex(Integer ifIndex) {
        this.ifIndex = ifIndex;
    }

    public String getIfDescr() {
        return ifDescr;
    }

    public void setIfDescr(String ifDescr) {
        this.ifDescr = ifDescr;
    }

    public Integer getIfType() {
        return ifType;
    }

    public void setIfType(Integer ifType) {
        this.ifType = ifType;
    }

    public String getIfAdminStatus() {
        return ifAdminStatus;
    }

    public void setIfAdminStatus(String ifAdminStatus) {
        this.ifAdminStatus = ifStatus(ifAdminStatus);
    }

    public String getIfOperStatus() {
        return ifOperStatus;
    }

    public void setIfOperStatus(String ifOperStatus) {
        this.ifOperStatus = ifStatus(ifOperStatus);
    }

    private String ifStatus(String numStatus){
        switch (numStatus) {
            case "1": return "up";
            case "2": return "down";
            case "3": return "testing";
            default: return "unknown";
        }


    }

    public boolean isValidIfType() {
        switch (this.ifType){
            case 6: return true;
            case 117: return true;
            default:return false;
        }
    }
}
