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

import org.hisp.dhis.artemis.MessageType;
import org.hisp.dhis.program.ProgramInstance;
import org.hisp.dhis.program.ProgramStageInstance;
import org.hisp.dhis.tracker.TrackerImportStrategy;
import org.hisp.dhis.tracker.domain.Enrollment;
import org.hisp.dhis.tracker.domain.Event;
import org.hisp.dhis.tracker.job.TrackerSideEffectDataBundle;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * @author Zubair Asghar
 */
public class TrackerSideEffectDataBundleTest
{
    @Test
    public void testSideEffectDataBundleForEnrollment()
    {
        Enrollment enrollment = new Enrollment();
        ProgramInstance programInstance = new ProgramInstance();

        TrackerSideEffectDataBundle bundle = TrackerSideEffectDataBundle.builder()
            .enrollmentRuleEffect( enrollment, Arrays.asList() )
            .accessedBy( "testUser" )
            .importStrategy( TrackerImportStrategy.CREATE )
            .object( programInstance )
            .klass( ProgramInstance.class )
            .build();

        assertEquals( programInstance, bundle.getObject() );
        assertEquals( ProgramInstance.class, bundle.getKlass() );
        assertTrue( bundle.getEnrollmentRuleEffects().containsKey( enrollment ) );
        assertTrue( bundle.getEventRuleEffects().isEmpty() );
        assertEquals( TrackerImportStrategy.CREATE, bundle.getImportStrategy() );
        assertEquals( MessageType.TRACKER_SIDE_EFFECT, bundle.getMessageType() );
    }

    @Test
    public void testSideEffectDataBundleForEvent()
    {
        Event event = new Event();
        ProgramStageInstance programStageInstance = new ProgramStageInstance();

        TrackerSideEffectDataBundle bundle = TrackerSideEffectDataBundle.builder()
            .eventRuleEffect( event, Arrays.asList() )
            .object( programStageInstance )
            .klass( ProgramStageInstance.class )
            .build();

        assertEquals( programStageInstance, bundle.getObject() );
        assertEquals( ProgramStageInstance.class, bundle.getKlass() );
        assertTrue( bundle.getEventRuleEffects().containsKey( event ) );
        assertTrue( bundle.getEnrollmentRuleEffects().isEmpty() );
    }
}
