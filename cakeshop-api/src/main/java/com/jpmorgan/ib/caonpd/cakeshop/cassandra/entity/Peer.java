package com.jpmorgan.ib.caonpd.cakeshop.cassandra.entity;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
//import org.springframework.cassandra.core.Ordering;
//import org.springframework.cassandra.core.PrimaryKeyType;
//import org.springframework.data.cassandra.mapping.Column;
//import org.springframework.data.cassandra.mapping.PrimaryKeyColumn;
//import org.springframework.data.cassandra.mapping.Table;


//@Table(value="peer")
public class Peer  {

    /**
     * Node status
     */
    private String status;

    /**
     * Node ID
     */
//    @PrimaryKeyColumn(
//            name = "id",
//            ordinal = 1,
//            type = PrimaryKeyType.CLUSTERED,
//            ordering = Ordering.DESCENDING)
    private String id;

    /**
     * Node Address
     */
//    @PrimaryKeyColumn(
//            name = "nodeUrl", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    private String nodeUrl;

    /**
     * Node Name
     */
//    @Column
    private String nodeName;

    /**
     * Node IP
     */
//    @Column
    private String nodeIP;

    public String getStatus() {
         return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public void setNodeIP(String ip) {
        this.nodeIP = ip;
    }

    public String getNodeIP() {
        return nodeIP;
    }

    @Override
    public String toString()  {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj, false);
    }

}
