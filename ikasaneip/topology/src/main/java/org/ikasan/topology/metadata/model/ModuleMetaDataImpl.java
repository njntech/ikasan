package org.ikasan.topology.metadata.model;

import org.ikasan.spec.metadata.FlowMetaData;
import org.ikasan.spec.metadata.ModuleMetaData;
import org.ikasan.spec.module.ModuleType;

import java.util.ArrayList;
import java.util.List;

public class ModuleMetaDataImpl implements ModuleMetaData
{
    private ModuleType moduleType;
    private String url;
    private String name;
    private String description;
    private String version;
    private List<FlowMetaData> flows;
    private String configuredResourceId;

    @Override
    public void setType(ModuleType moduleType) {
        this.moduleType = moduleType;
    }

    @Override
    public ModuleType getType() {
        return this.moduleType;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public String getDescription()
    {
        return this.description;
    }

    @Override
    public void setVersion(String version)
    {
        this.version = version;
    }

    @Override
    public String getVersion()
    {
        return this.version;
    }

    @Override
    public void setFlows(List<FlowMetaData> flows)
    {
        this.flows = flows;
    }

    @Override
    public List<FlowMetaData> getFlows()
    {
        if(flows == null)
        {
            flows = new ArrayList<>();
        }

        return flows;
    }

    @Override
    public String getUrl()
    {
        return this.url;
    }

    @Override
    public void setUrl(String url)
    {
        this.url = url;
    }

    @Override
    public String getConfiguredResourceId() {
        return configuredResourceId;
    }

    @Override
    public void setConfiguredResourceId(String id) {
        this.configuredResourceId = id;
    }
}
