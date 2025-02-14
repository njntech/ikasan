package org.ikasan.topology.metadata.module;

import org.ikasan.spec.configuration.ConfiguredResource;
import org.ikasan.spec.flow.Flow;
import org.ikasan.spec.module.Module;
import org.ikasan.spec.module.ModuleType;
import org.ikasan.topology.metadata.configuration.DummyConfiguration;

import java.util.ArrayList;
import java.util.List;

public class TestConfiguredModule implements Module<Flow>, ConfiguredResource<DummyConfiguration>
{
    private List<Flow> flows;

    @Override
    public void setType(ModuleType moduleType) {

    }

    @Override
    public ModuleType getType() {
        return ModuleType.SCHEDULER_AGENT;
    }

    @Override
    public String getVersion()
    {
        return "module version";
    }

    @Override
    public String getName()
    {
        return "module name";
    }

    @Override
    public List<Flow> getFlows()
    {
        if(flows == null)
        {
            flows = new ArrayList<>();
        }

        return flows;
    }

    @Override
    public Flow getFlow(String name)
    {
        return null;
    }

    @Override
    public String getDescription()
    {
        return "module description";
    }

    @Override
    public void setDescription(String description)
    {

    }

    @Override
    public String getUrl()
    {
        return "url";
    }

    @Override
    public void setUrl(String url)
    {

    }

    @Override
    public String getConfiguredResourceId() {
        return "configurationId";
    }

    @Override
    public void setConfiguredResourceId(String id) {

    }

    @Override
    public DummyConfiguration getConfiguration() {
        return null;
    }

    @Override
    public void setConfiguration(DummyConfiguration configuration) {

    }
}
