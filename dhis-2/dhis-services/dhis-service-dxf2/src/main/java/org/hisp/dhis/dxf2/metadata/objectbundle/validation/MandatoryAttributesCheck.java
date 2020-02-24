package org.hisp.dhis.dxf2.metadata.objectbundle.validation;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.hisp.dhis.attribute.Attribute;
import org.hisp.dhis.attribute.AttributeValue;
import org.hisp.dhis.common.IdentifiableObject;
import org.hisp.dhis.common.IdentifiableObjectUtils;
import org.hisp.dhis.dxf2.metadata.objectbundle.ObjectBundle;
import org.hisp.dhis.feedback.ErrorCode;
import org.hisp.dhis.feedback.ErrorReport;
import org.hisp.dhis.feedback.ObjectReport;
import org.hisp.dhis.feedback.TypeReport;
import org.hisp.dhis.importexport.ImportStrategy;
import org.hisp.dhis.preheat.Preheat;
import org.hisp.dhis.schema.Schema;

import static org.hisp.dhis.dxf2.metadata.objectbundle.validation.ValidationUtils.addObjectReport;

/**
 * @author Luciano Fiandesio
 */
public class MandatoryAttributesCheck
    implements
    ValidationCheck
{

    @Override
    public TypeReport check( ObjectBundle bundle, Class<? extends IdentifiableObject> klass,
        List<IdentifiableObject> persistedObjects, List<IdentifiableObject> nonPersistedObjects,
        ImportStrategy importStrategy, ValidationContext ctx )
    {
        TypeReport typeReport = new TypeReport( klass );
        Schema schema = ctx.getSchemaService().getDynamicSchema( klass );
        List<IdentifiableObject> objects = selectObjects( persistedObjects, nonPersistedObjects, importStrategy );

        if ( objects.isEmpty() || !schema.havePersistedProperty( "attributeValues" ) )
        {
            return typeReport;
        }

        Iterator<IdentifiableObject> iterator = objects.iterator();

        while ( iterator.hasNext() )
        {
            IdentifiableObject object = iterator.next();
            List<ErrorReport> errorReports = checkMandatoryAttributes( klass, object, bundle.getPreheat() );

            if ( !errorReports.isEmpty() )
            {
                addObjectReport( errorReports, typeReport, object, bundle );

                iterator.remove();
            }
        }

        return typeReport;
    }

    private List<ErrorReport> checkMandatoryAttributes( Class<? extends IdentifiableObject> klass,
        IdentifiableObject object, Preheat preheat )
    {
        List<ErrorReport> errorReports = new ArrayList<>();

        if ( object == null || preheat.isDefault( object ) || !preheat.getMandatoryAttributes().containsKey( klass ) )
        {
            return errorReports;
        }

        Set<AttributeValue> attributeValues = object.getAttributeValues();
        Set<String> mandatoryAttributes = new HashSet<>( preheat.getMandatoryAttributes().get( klass ) ); // make copy
                                                                                                          // for
                                                                                                          // modification
        if ( mandatoryAttributes.isEmpty() )
        {
            return errorReports;
        }

        attributeValues
            .forEach( attributeValue -> mandatoryAttributes.remove( attributeValue.getAttribute().getUid() ) );
        mandatoryAttributes.forEach( att -> errorReports.add(
            new ErrorReport( Attribute.class, ErrorCode.E4011, att ).setMainId( att ).setErrorProperty( "value" ) ) );

        return errorReports;
    }
}
