/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package org.locationtech.udig.project.internal;

import org.locationtech.udig.project.IPersister;

import org.eclipse.ui.IMemento;

/**
 * Persister for persisting text on blackboard.
 * 
 * @author Jesse
 * @since 1.1.0
 */
public class TextPersister extends IPersister {

    @Override
    public Class getPersistee() {
        return String.class;
    }

    @Override
    public Object load( IMemento memento ) {
        String text = memento.getString("text"); //$NON-NLS-1$
        return text;
    }

    @Override
    public void save( Object object, IMemento memento ) {
    	String text = (String) object;
        memento.putString("text", text); //$NON-NLS-1$
    }

}
