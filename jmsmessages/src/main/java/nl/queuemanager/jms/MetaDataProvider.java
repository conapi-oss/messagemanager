package nl.queuemanager.jms;

import java.util.Map;

public interface MetaDataProvider {
    public Map<String, Object> getMetaData();
    public void setMetaData(Map<String, Object> metaData);
}
