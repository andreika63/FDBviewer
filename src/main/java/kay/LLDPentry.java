package kay;

public class LLDPentry {
    private Integer localPortNum;
    private String localPortName;
    private String localPortAlias;
    private String remotePortName;
    private String remotePortAlias;
    private String remoteIpAddress;
    private String remoteSysName;
    private String remoteSysDesc;

    public Integer getLocalPortNum() {
        return localPortNum;
    }

    public void setLocalPortNum(Integer localPortNum) {
        this.localPortNum = localPortNum;
    }

    public String getLocalPortName() {
        return localPortName;
    }

    public void setLocalPortName(String localPortName) {
        this.localPortName = localPortName;
    }

    public String getLocalPortAlias() {
        return localPortAlias;
    }

    public void setLocalPortAlias(String localPortAlias) {
        this.localPortAlias = localPortAlias;
    }

    public String getRemotePortName() {
        return remotePortName;
    }

    public void setRemotePortName(String remotePortName) {
        this.remotePortName = remotePortName;
    }

    public String getRemotePortAlias() {
        return remotePortAlias;
    }

    public void setRemotePortAlias(String remotePortAlias) {
        this.remotePortAlias = remotePortAlias;
    }

    public String getRemoteIpAddress() {
        return remoteIpAddress;
    }

    public void setRemoteIpAddress(String remoteIpAddress) {
        this.remoteIpAddress = remoteIpAddress;
    }

    public String getRemoteSysName() {
        return remoteSysName;
    }

    public void setRemoteSysName(String remoteSysName) {
        this.remoteSysName = remoteSysName;
    }

    public String getRemoteSysDesc() {
        return remoteSysDesc;
    }

    public void setRemoteSysDesc(String remoteSysDesc) {
        this.remoteSysDesc = remoteSysDesc;
    }

    @Override
    public String toString() {
        return "LLDPentry{" +
                "localPortNum=" + localPortNum +
                ", localPortName='" + localPortName + '\'' +
                ", localPortAlias='" + localPortAlias + '\'' +
                ", remotePortName='" + remotePortName + '\'' +
                ", remotePortAlias='" + remotePortAlias + '\'' +
                ", remoteIpAddress='" + remoteIpAddress + '\'' +
                ", remoteSysName='" + remoteSysName + '\'' +
                ", remoteSysDesc='" + remoteSysDesc + '\'' +
                '}';
    }
}
