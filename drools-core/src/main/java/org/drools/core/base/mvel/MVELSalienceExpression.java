/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.core.base.mvel;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.drools.core.WorkingMemory;
import org.drools.core.common.AgendaItem;
import org.drools.core.common.InternalWorkingMemory;
import org.drools.core.reteoo.LeftTuple;
import org.drools.core.rule.MVELDialectRuntimeData;
import org.drools.core.rule.Package;
import org.drools.core.spi.KnowledgeHelper;
import org.drools.core.spi.Salience;
import org.kie.api.definition.rule.Rule;
import org.mvel2.MVEL;
import org.mvel2.integration.VariableResolverFactory;

public class MVELSalienceExpression
    implements
    Salience,
    MVELCompileable,
    Externalizable {

    private static final long   serialVersionUID = 510l;

    private MVELCompilationUnit unit;
    private String              id;

    private Serializable        expr;

    public MVELSalienceExpression() {
    }

    public MVELSalienceExpression(final MVELCompilationUnit unit,
                                  final String id) {
        this.unit = unit;
        this.id = id;
    }

    public void readExternal(ObjectInput in) throws IOException,
                                            ClassNotFoundException {
        unit = (MVELCompilationUnit) in.readObject();
        id = in.readUTF();
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject( unit );
        out.writeUTF( id );
    }
    
    public MVELCompilationUnit getMVELCompilationUnit() {
        return this.unit;
    }

    public void compile(MVELDialectRuntimeData runtimeData) {
        expr = unit.getCompiledExpression( runtimeData );
    }

    public int getValue(final KnowledgeHelper khelper,
                        final Rule rule,
                        final WorkingMemory workingMemory) {
        VariableResolverFactory factory = unit.getFactory( khelper,  ((AgendaItem)khelper.getMatch()).getTerminalNode().getSalienceDeclarations(), 
                                                           rule, null, (LeftTuple) khelper.getMatch().getTuple(), null, (InternalWorkingMemory) workingMemory, workingMemory.getGlobalResolver() );
        
        // do we have any functions for this namespace?
        Package pkg = workingMemory.getRuleBase().getPackage( "MAIN" );
        if ( pkg != null ) {
            MVELDialectRuntimeData data = (MVELDialectRuntimeData) pkg.getDialectRuntimeRegistry().getDialectData( this.id );
            factory.setNextFactory( data.getFunctionFactory() );
        }

        return ((Number) MVEL.executeExpression( this.expr,
                                                 factory )).intValue();
    }
    
    public String toString() {
        return this.unit.getExpression();
    }

}
