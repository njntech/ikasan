/*
 * $Id$  
 * $URL$
 * 
 * ====================================================================
 * Ikasan Enterprise Integration Platform
 * 
 * Distributed under the Modified BSD License.
 * Copyright notice: The copyright for this software and a full listing 
 * of individual contributors are as shown in the packaged copyright.txt 
 * file. 
 * 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *
 *  - Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *
 *  - Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *
 *  - Neither the name of the ORGANIZATION nor the names of its contributors may
 *    be used to endorse or promote products derived from this software without 
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 */
package org.ikasan.replay.dao;

import org.ikasan.replay.model.HibernateReplayEvent;
import org.ikasan.spec.replay.ReplayDao;
import org.ikasan.spec.replay.ReplayEvent;
import org.ikasan.spec.serialiser.SerialiserFactory;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Ikasan Development Team
 *
 */
@SuppressWarnings("unqualified-field-access")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "/replay-service-conf.xml",
        "/hsqldb-config.xml",
        "/substitute-components.xml",
        "/mock-components.xml"
})
public class HibernateReplayDaoTest
{	
	/**
     * Mockery for mocking concrete classes
     */
	@Resource Mockery mockery;
	
	@Resource
    ReplayDao replayDao;
	
	@Resource SerialiserFactory ikasanSerialiserFactory;
	
	@Test
	@DirtiesContext
	public void testSaveReplayEvent_success()
	{
		HibernateReplayEvent replayEvent = new HibernateReplayEvent("errorUri", "event".getBytes(), "event", "moduleName", "flowName", 30);
      
		this.replayDao.saveOrUpdate(replayEvent);

		ReplayEvent replayEvent1 = this.replayDao.getReplayEventById(replayEvent.getId());

		Assert.assertEquals("Replay events equal", replayEvent, replayEvent1);
	}

    @Test
    @DirtiesContext
    public void testSaveReplayEvents_success()
    {
        List<ReplayEvent> replayEvents = new ArrayList<>();

        for(int i=0; i<100; i++)
        {
            HibernateReplayEvent replayEvent = new HibernateReplayEvent("errorUri", "event".getBytes(), "event", "moduleName", "flowName", 30);

            replayEvents.add(replayEvent);
        }

        this.replayDao.save(replayEvents);

        List<ReplayEvent> replayEventsResults = this.replayDao.getReplayEvents("moduleName", null, null, null, 1000);

        Assert.assertEquals("Result size == 100", 100, replayEventsResults.size());
    }

    @Test
    @DirtiesContext
    public void test_harvest_success()
    {
        this.replayDao.setHarvestQueryOrdered(true);

        List<ReplayEvent> replayEventList = new ArrayList<>();

        for(int i=0; i<1000; i++)
        {
            HibernateReplayEvent replayEvent = new HibernateReplayEvent("errorUri", "event".getBytes(), "event", "moduleName", "flowName", 30);

            this.replayDao.saveOrUpdate(replayEvent);

            replayEventList.add(replayEvent);
        }

        Assert.assertEquals("Harvestable records == 1000", this.replayDao.getHarvestableRecords(5000).size(), 1000);

        this.replayDao.updateAsHarvested(replayEventList);

        Assert.assertEquals("Harvestable records == 0", this.replayDao.getHarvestableRecords(5000).size(), 0);
    }

    @Test
    @DirtiesContext
    public void test_harvest_success_with_gap()
    {
        this.replayDao.setHarvestQueryOrdered(true);

        List<ReplayEvent> replayEventList = new ArrayList<>();

        for(int i=0; i<1000; i++)
        {
            HibernateReplayEvent replayEvent = new HibernateReplayEvent("errorUri", "event".getBytes(), "event", "moduleName", "flowName", 30);

            this.replayDao.saveOrUpdate(replayEvent);

            replayEventList.add(replayEvent);
        }

        List<ReplayEvent> events = this.replayDao.getHarvestableRecords(3);

        this.replayDao.updateAsHarvested(List.of(events.get(1)));

        events = this.replayDao.getHarvestableRecords(3);

        Assert.assertEquals("Id equals!",Long.valueOf(2L), Long.valueOf(events.get(1).getId() - events.get(0).getId()));
        Assert.assertEquals("Id equals!",Long.valueOf(1L), Long.valueOf(events.get(2).getId() - events.get(1).getId()));
    }

    @Test
    @DirtiesContext
    public void test_harvest_success_with_order_by()
    {
        this.replayDao.setHarvestQueryOrdered(true);
        List<ReplayEvent> replayEventList = new ArrayList<>();

        for(int i=0; i<1000; i++)
        {
            HibernateReplayEvent replayEvent = new HibernateReplayEvent("errorUri", "event".getBytes(), "event", "moduleName", "flowName", 30);

            this.replayDao.saveOrUpdate(replayEvent);

            replayEventList.add(replayEvent);
        }

        Assert.assertEquals("Harvestable records == 1000", this.replayDao.getHarvestableRecords(5000).size(), 1000);

        this.replayDao.updateAsHarvested(replayEventList);

        Assert.assertEquals("Harvestable records == 0", this.replayDao.getHarvestableRecords(5000).size(), 0);
    }
}
