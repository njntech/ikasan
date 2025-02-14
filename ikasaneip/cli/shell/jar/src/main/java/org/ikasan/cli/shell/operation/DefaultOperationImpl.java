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
package org.ikasan.cli.shell.operation;

import ch.qos.logback.core.util.FileUtil;
import org.ikasan.cli.shell.operation.model.ProcessType;
import org.ikasan.cli.shell.operation.service.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of the Operation contract.
 *
 * @author Ikasan Development Team
 */
public class DefaultOperationImpl implements Operation
{
    /** logger instance */
    private static Logger logger = LoggerFactory.getLogger(DefaultOperationImpl.class);

    private static String MINUS_D_MODULE_NAME = "-Dmodule.name=";
    private static String EQUALS = "=";

    /** handle to persistence service */
    PersistenceService persistenceService;

    /**
     * Constructor
     * @param persistenceService
     */
    public DefaultOperationImpl(PersistenceService persistenceService)
    {
        this.persistenceService = persistenceService;
        if(persistenceService == null)
        {
            throw new IllegalArgumentException("persistenceService cannot be 'null'");
        }
    }

    /**
     * Factory method to aid testing
     * @param commands
     * @return
     */
    protected ProcessBuilder getProcessBuilder(List<String> commands)
    {
        return new ProcessBuilder(commands);
    }

    public Process start(ProcessType processType, List<String> commands, String name) throws IOException
    {
        ProcessBuilder processBuilder = getProcessBuilder(commands);
        if(processType.getOutputLog() != null && !processType.getOutputLog().isEmpty())
        {
            File outputLog = new File(processType.getOutputLog());
            FileUtil.createMissingParentDirectories(outputLog);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(outputLog));
        }
        if(processType.getErrorLog() == null || processType.getErrorLog().isEmpty())
        {
            // redirect stdErr to stdOut
            processBuilder.redirectError();
        }
        else
        {
            // stdErr still going to same place as stdOut
            if(processType.getOutputLog().equals(processType.getErrorLog()))
            {
                processBuilder.redirectError();
            }
            else
            {
                File errorLog = new File(processType.getErrorLog());
                FileUtil.createMissingParentDirectories(errorLog);
                processBuilder.redirectError(ProcessBuilder.Redirect.appendTo(errorLog));
            }
        }

        Process process = processBuilder.start();

        // persist the process
        try
        {
            if(processType.isPersist())
            {
                persistenceService.persist(processType.getName(), name, process);
            }
        }
        catch(Exception e)
        {
            logger.error(e.getMessage(), e);
        }

        return process;
    }

    boolean isSameModuleName(Optional<String> commandLine, String name)
    {
        if(commandLine.isEmpty())
        {
            return false;
        }

        String[] params = commandLine.get().split(" ");
        for(String param:params)
        {
            if(param.startsWith(MINUS_D_MODULE_NAME))
            {
                String[] moduleNameParts = param.split("=");
                if(moduleNameParts.length == 2)
                {
                    return moduleNameParts[1].trim().toLowerCase().equals(name.toLowerCase());
                }
            }
        }

        return false;
    }

    @Override
    public List<ProcessHandle> getProcessHandles(ProcessType processType, String name, String username)
    {
        // can we find by the persisted process
        ProcessHandle processHandle = persistenceService.find(processType.getName(), name);
        if (processHandle != null)
        {
            // check we have a live process matching the persisted pid
            if(processHandle.isAlive())
            {
                List<ProcessHandle> processHandles = new ArrayList<ProcessHandle>();
                processHandles.add(processHandle);
                return processHandles;
            }

            // if not clean up the persisted pid and continue checking
            persistenceService.remove(processType.getName(), name);
        }

        // not persisted, try checking if we can see a process running for that module of that type for that user
        Stream<ProcessHandle> liveProcesses = ProcessHandle.allProcesses();

        // filter processes by name
        liveProcesses = liveProcesses.filter(ProcessHandle::isAlive)
            .filter(ph -> isSameModuleName(ph.info().commandLine(), name) );

        // filter processes by username
        if(username != null)
        {
            liveProcesses = liveProcesses
                .filter(ph -> !ph.info().user().isEmpty()
                    && ph.info().user().get().toLowerCase().equals(username.toLowerCase()));
        }

        // filter by type
        if(processType.getCommandSignature() != null && processType.getCommandSignature().length() > 0)
        {
            liveProcesses = liveProcesses
                .filter(ph -> !ph.info().commandLine().isEmpty()
                    && ph.info().commandLine().get().toLowerCase().contains(processType.getCommandSignature().toLowerCase()));
        }

        // return whats left
        return liveProcesses.collect(Collectors.toList());
    }

    @Override
    public void stop(ProcessType processType, String name, String username) throws IOException
    {
        List<ProcessHandle> processHandles = getProcessHandles(processType, name, username);
        if(processHandles == null || processHandles.size() == 0)
        {
            throw new IOException("No matching processes");
        }

        // TODO - check how many instances found
        for(ProcessHandle processHandle:processHandles)
        {
            processHandle.destroy();
        }

        // remove persistence
        persistenceService.remove(processType.getName(), name);
    }

    @Override
    public void kill(ProcessType processType, String name, String username) throws IOException
    {
        List<ProcessHandle> processHandles = getProcessHandles(processType, name, username);
        if(processHandles == null || processHandles.size() == 0)
        {
            throw new IOException("No matching processes");
        }

        // TODO - identify if more than one instance
        for(ProcessHandle processHandle:processHandles)
        {
            processHandle.destroyForcibly();
        }

        // remove persistence
        persistenceService.remove(processType.getName(), name);
    }

}