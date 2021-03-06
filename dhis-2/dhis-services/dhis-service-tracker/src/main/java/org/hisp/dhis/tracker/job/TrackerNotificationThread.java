package org.hisp.dhis.tracker.job;

/*
 * Copyright (c) 2004-2020, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import com.google.common.collect.ImmutableMap;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.program.notification.ProgramNotificationService;
import org.hisp.dhis.security.SecurityContextRunnable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Class represents a thread which will be triggered as soon as tracker notification consumer consumes a message from
 * tracker notification queue.
 *
 * @author Zubair Asghar
 */

@Component
@Scope( BeanDefinition.SCOPE_PROTOTYPE )
public class TrackerNotificationThread   extends SecurityContextRunnable
{
    private ProgramNotificationService programNotificationService;
    private TrackerSideEffectDataBundle sideEffectDataBundle;

    private final ImmutableMap<Class<? extends BaseIdentifiableObject>, Consumer<Long>> serviceMapper = new
        ImmutableMap.Builder<Class<? extends BaseIdentifiableObject>, Consumer<Long>>()
        .put( ProgramInstance.class, id -> programNotificationService.sendEnrollmentNotifications( id ) )
        .put( ProgramStageInstance.class, id -> programNotificationService.sendEventCompletionNotifications( id ) )
        .build();

    public TrackerNotificationThread( ProgramNotificationService programNotificationService )
    {
        this.programNotificationService = programNotificationService;
    }

    @Override
    public void call()
    {
        if ( sideEffectDataBundle != null )
        {
            BaseIdentifiableObject object = sideEffectDataBundle.getObject();

            if ( serviceMapper.containsKey( object.getClass() ) )
            {
                serviceMapper.get( object.getClass() ).accept( object.getId() );
            }
        }
    }

    public void setSideEffectDataBundle( TrackerSideEffectDataBundle sideEffectDataBundle )
    {
        this.sideEffectDataBundle = sideEffectDataBundle;
    }
}
