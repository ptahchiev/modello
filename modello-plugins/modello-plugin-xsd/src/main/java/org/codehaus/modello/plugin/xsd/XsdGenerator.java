package org.codehaus.modello.plugin.xsd;

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

import org.codehaus.modello.ModelloException;
import org.codehaus.modello.model.Model;
import org.codehaus.modello.model.ModelAssociation;
import org.codehaus.modello.model.ModelClass;
import org.codehaus.modello.model.ModelField;
import org.codehaus.modello.plugin.AbstractModelloGenerator;
import org.codehaus.modello.plugin.model.ModelClassMetadata;
import org.codehaus.plexus.util.xml.PrettyPrintXMLWriter;
import org.codehaus.plexus.util.xml.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:brett@codehaus.org">Brett Porter</a>
 * @version $Id$
 */
public class XsdGenerator
    extends AbstractModelloGenerator
{
    public void generate( Model model, Properties parameters )
        throws ModelloException
    {
        initialize( model, parameters );

        try
        {
            generateXsd();
        }
        catch ( IOException ex )
        {
            throw new ModelloException( "Exception while generating xsd.", ex );
        }
    }

    private void generateXsd()
        throws IOException
    {
        Model objectModel = getModel();

        String directory = getOutputDirectory().getAbsolutePath();

        if ( isPackageWithVersion() )
        {
            directory += "/" + getGeneratedVersion();
        }

        File f = new File( directory, objectModel.getId() + "-" + getGeneratedVersion() + ".xsd" );

        if ( !f.getParentFile().exists() )
        {
            f.getParentFile().mkdirs();
        }

        FileWriter writer = new FileWriter( f );

        XMLWriter w = new PrettyPrintXMLWriter( writer );

        writer.write( "<?xml version=\"1.0\"?>\n" );

        // TODO: the writer should be knowledgable of namespaces, but this works
        w.startElement( "xs:schema" );
        w.addAttribute( "xmlns:xs", "http://www.w3.org/2001/XMLSchema" );
        w.addAttribute( "elementFormDefault", "qualified" );
        // TODO: make configurable
        w.addAttribute( "targetNamespace", "http://maven.apache.org/POM/4.0.0" );
        w.addAttribute( "xmlns", "http://maven.apache.org/POM/4.0.0" );

        ModelClass root = objectModel.getClass( objectModel.getRoot( getGeneratedVersion() ), getGeneratedVersion() );

        w.startElement( "xs:element" );
        String tagName = getTagName( root );
        w.addAttribute( "name", tagName );
        w.addAttribute( "type", root.getName() );

        writeClassDocumentation( w, root );

        w.endElement();

        // Element descriptors
        // Traverse from root so "abstract" models aren't included
        int initialCapacity = objectModel.getClasses( getGeneratedVersion() ).size();
        writeComplexTypeDescriptor( w, objectModel, root, new HashSet( initialCapacity ) );

        w.endElement();

        writer.flush();

        writer.close();
    }

    private static void writeClassDocumentation( XMLWriter w, ModelClass modelClass )
    {
        writeDocumentation( w, modelClass.getVersionRange().toString(), modelClass.getDescription() );
    }

    private static void writeFieldDocumentation( XMLWriter w, ModelField field )
    {
        writeDocumentation( w, field.getVersionRange().toString(), field.getDescription() );
    }

    private static void writeDocumentation( XMLWriter w, String version, String description )
    {
        if ( version != null || description != null )
        {
            w.startElement( "xs:annotation" );

            if ( version != null )
            {
                w.startElement( "xs:documentation" );
                w.addAttribute( "source", "version" );
                w.writeText( version );
                w.endElement();
            }

            if ( description != null )
            {
                w.startElement( "xs:documentation" );
                w.addAttribute( "source", "description" );
                w.writeText( description );
                w.endElement();
            }

            w.endElement();
        }
    }

    private void writeComplexTypeDescriptor( XMLWriter w, Model objectModel, ModelClass modelClass, Set written )
    {
        written.add( modelClass );

        w.startElement( "xs:complexType" );
        w.addAttribute( "name", modelClass.getName() );

        writeClassDocumentation( w, modelClass );

        w.startElement( "xs:all" );

        List fields = new ArrayList();
        while ( modelClass != null )
        {
            fields.addAll( modelClass.getFields( getGeneratedVersion() ) );
            String superClass = modelClass.getSuperClass();
            if ( superClass != null )
            {
                modelClass = objectModel.getClass( superClass, getGeneratedVersion() );
            }
            else
            {
                modelClass = null;
            }
        }

        Set toWrite = new HashSet();
        for ( Iterator j = fields.iterator(); j.hasNext(); )
        {
            ModelField field = (ModelField) j.next();

            w.startElement( "xs:element" );
            w.addAttribute( "name", field.getName() );

            // Usually, would only do this if the field is not "required", but due to inheritence, it may be present,
            // even if not here, so we need to let it slide
            w.addAttribute( "minOccurs", "0" );

            String xsdType = getXsdType( field.getType() );
            if ( xsdType != null )
            {
                w.addAttribute( "type", xsdType );

                if ( field.getDefaultValue() != null )
                {
                    w.addAttribute( "default", field.getDefaultValue() );
                }
                writeFieldDocumentation( w, field );
            }
            else
            {
                if ( field instanceof ModelAssociation &&
                    isClassInModel( ( (ModelAssociation) field ).getTo(), objectModel ) )
                {
                    ModelAssociation association = (ModelAssociation) field;
                    ModelClass fieldModelClass = objectModel.getClass( association.getTo(), getGeneratedVersion() );

                    toWrite.add( fieldModelClass );
                    if ( "*".equals( association.getMultiplicity() ) )
                    {
                        writeFieldDocumentation( w, field );
                        writeListElement( w, field, fieldModelClass.getName() );
                    }
                    else
                    {
                        w.addAttribute( "type", fieldModelClass.getName() );
                        writeFieldDocumentation( w, field );
                    }
                }
                else
                {
                    if ( List.class.getName().equals( field.getType() ) )
                    {
                        writeFieldDocumentation( w, field );
                        writeListElement( w, field, getXsdType( "String" ) );
                    }
                    else if ( Properties.class.getName().equals( field.getType() ) || "DOM".equals( field.getType() ) )
                    {
                        writeFieldDocumentation( w, field );
                        writePropertiesElement( w );
                    }
                    else
                    {
                        throw new IllegalStateException(
                            "Non-association field of a non-primitive type '" + field.getType() + "' for '" + field.getName() + "'" );
                    }
                }
            }

            w.endElement();
        }

        w.endElement();

        w.endElement();

        for ( Iterator iter = toWrite.iterator(); iter.hasNext(); )
        {
            ModelClass fieldModelClass = (ModelClass) iter.next();
            if ( !written.contains( fieldModelClass ) )
            {
                writeComplexTypeDescriptor( w, objectModel, fieldModelClass, written );
            }
        }
    }

    private static String getTagName( ModelClass modelClass )
    {
        ModelClassMetadata metadata = (ModelClassMetadata) modelClass.getMetadata( ModelClassMetadata.ID );

        String tagName;
        if ( metadata == null || metadata.getTagName() == null )
        {
            tagName = uncapitalise( modelClass.getName() );
        }
        else
        {
            tagName = metadata.getTagName();
        }
        return tagName;
    }

    private void writePropertiesElement( XMLWriter w )
    {
        w.startElement( "xs:complexType" );

        w.startElement( "xs:sequence" );

        w.startElement( "xs:any" );
        w.addAttribute( "minOccurs", "0" );
        w.addAttribute( "maxOccurs", "unbounded" );
        w.addAttribute( "processContents", "lax" );

        w.endElement();

        w.endElement();

        w.endElement();
    }

    private void writeListElement( XMLWriter w, ModelField field, String type )
    {
        w.startElement( "xs:complexType" );

        w.startElement( "xs:sequence" );

        w.startElement( "xs:element" );
        w.addAttribute( "name", singular( field.getName() ) );
        w.addAttribute( "minOccurs", "0" );
        w.addAttribute( "maxOccurs", "unbounded" );
        w.addAttribute( "type", type );

        w.endElement();

        w.endElement();

        w.endElement();
    }

    private static String getXsdType( String type )
    {
        if ( "String".equals( type ) )
        {
            return "xs:string";
        }
        else if ( "boolean".equals( type ) )
        {
            return "xs:boolean";
        }
        else
        {
            return null;
        }
    }

}