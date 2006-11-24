package org.codehaus.modello.plugin.jpox.metadata;

/*
 * Copyright (c) 2005, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import org.codehaus.modello.metadata.ClassMetadata;

import java.util.List;

/**
 * @author <a href="mailto:evenisse@codehaus.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class JPoxClassMetadata
    implements ClassMetadata
{
    public static final String ID = JPoxClassMetadata.class.getName();

    private boolean enabled;

    private boolean detachable;
    
    private String table;
    
    private String identityType;
    
    private String identityClass;
    
    private boolean useIdentifiersAsPrimaryKey;
    
    private List notPersisted;

    public void setDetachable( boolean detachable )
    {
        this.detachable = detachable;
    }

    public boolean isDetachable()
    {
        return detachable;
    }

    public void setTable( String table )
    {
        this.table = table;
    }

    public String getTable()
    {
        return table;
    }

    public String getIdentityClass()
    {
        return identityClass;
    }

    public void setIdentityClass( String identityClass )
    {
        this.identityClass = identityClass;
    }

    public String getIdentityType()
    {
        return identityType;
    }

    public void setIdentityType( String identityType )
    {
        this.identityType = identityType;
    }

    public boolean useIdentifiersAsPrimaryKey()
    {
        return useIdentifiersAsPrimaryKey;
    }

    public void setUseIdentifiersAsPrimaryKey( boolean userIdentifiersAsIdentity )
    {
        this.useIdentifiersAsPrimaryKey = userIdentifiersAsIdentity;
    }

    public List getNotPersisted()
    {
        return notPersisted;
    }

    public void setNotPersisted( List notPersisted )
    {
        this.notPersisted = notPersisted;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled( boolean persisted )
    {
        this.enabled = persisted;
    }
}
