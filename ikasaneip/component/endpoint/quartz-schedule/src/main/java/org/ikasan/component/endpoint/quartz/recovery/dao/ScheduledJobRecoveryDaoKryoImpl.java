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
package org.ikasan.component.endpoint.quartz.recovery.dao;

import ch.qos.logback.core.util.FileUtil;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.ikasan.component.endpoint.quartz.recovery.model.ScheduledJobRecoveryModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.FileSystems;

/**
 * Serialiser for IkasanProcess.
 *
 * @author Ikasan Development Team
 */
public class ScheduledJobRecoveryDaoKryoImpl implements ScheduledJobRecoveryDao<ScheduledJobRecoveryModel>
{
    /** logger instance */
    private static Logger logger = LoggerFactory.getLogger(ScheduledJobRecoveryDaoKryoImpl.class);

    /** persistence directory */
    String persistenceDir;

    /**
     * Thread local instance of Kyro instance.
     */
    private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>()
    {
        @Override
        protected Kryo initialValue()
        {
            Kryo kryo = new Kryo();
            kryo.register(org.ikasan.component.endpoint.quartz.recovery.model.ScheduledJobRecoveryModel.class);
            kryo.register(java.util.Date.class);
            return kryo;
        }
    };

    /**
     * Constructor
     * @param persistenceDir
     */
    public ScheduledJobRecoveryDaoKryoImpl(String persistenceDir)
    {
        this.persistenceDir = persistenceDir;
        if(persistenceDir == null)
        {
            throw new IllegalArgumentException("persistence directory cannot be 'null");
        }
    }

    @Override
    public ScheduledJobRecoveryModel find(String group, String name)
    {
        Kryo kryo = kryoThreadLocal.get();
        String path = getScheduledPersistence(group, name);

        try
        {
            Input input = new Input(new FileInputStream(path));
            return (ScheduledJobRecoveryModel)kryo.readClassAndObject(input);
        }
        catch(FileNotFoundException e)
        {
            logger.debug("File [" + path + "] not found", e);
            return null;
        }
    }

    @Override
    public void save(ScheduledJobRecoveryModel scheduledJobRecoveryModel)
    {
        Kryo kryo = kryoThreadLocal.get();
        String path = getScheduledPersistence(scheduledJobRecoveryModel.getGroup(), scheduledJobRecoveryModel.getName());
        FileUtil.createMissingParentDirectories(new File(path) );

        try
        {
            Output output = new Output(new FileOutputStream(path) );
            kryo.writeClassAndObject(output, scheduledJobRecoveryModel);
            output.close(); // flush should be called within the close() method
        }
        catch(FileNotFoundException e)
        {
            throw new RuntimeException("Failed to save the IkasanProcess", e);
        }
    }

    protected String getScheduledPersistence(String group, String name)
    {
        return persistenceDir + FileSystems.getDefault().getSeparator() + group + "_" + name;
    }

}