/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2012, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package org.locationtech.udig.catalog.tests.ui;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.locationtech.udig.catalog.CatalogPlugin;
import org.locationtech.udig.catalog.ICatalog;
import org.locationtech.udig.catalog.IService;
import org.locationtech.udig.catalog.internal.wms.WMSServiceImpl;
import org.locationtech.udig.catalog.tests.ui.workflow.Assertion;
import org.locationtech.udig.catalog.tests.ui.workflow.DialogDriver;
import org.locationtech.udig.catalog.tests.ui.workflow.DummyMonitor;
import org.locationtech.udig.catalog.ui.ConnectionErrorPage;
import org.locationtech.udig.catalog.ui.wizard.CatalogImport;
import org.locationtech.udig.ui.WaitCondition;
import org.locationtech.udig.ui.tests.support.UDIGTestUtil;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.junit.Before;
import org.junit.Test;

public abstract class CatalogImportTest {

	CatalogImport catalogImport;

	@Before
	public void setUp() throws Exception {
		catalogImport = new CatalogImport();
	}

    @Test
	public void testNormal() throws Exception{
			Object context = getContext();

			final ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();

			List members = catalog.members(new DummyMonitor());
			if (!members.isEmpty()) {
				//clear the catalog
				for (Iterator itr = members.iterator(); itr.hasNext();) {
					IService service = (IService)itr.next();
					catalog.remove(service);
				}
			}
			members = catalog.members(new DummyMonitor());
			assertTrue(members.isEmpty());

			catalogImport.getDialog().getWorkflowWizard().getWorkflow()
				.setContext(context);
			catalogImport.run(new DummyMonitor(),context);

            //sleep for 10 seconds, if dialog still active by then kill it
            UDIGTestUtil.inDisplayThreadWait(2000000, new WaitCondition(){

                public boolean isTrue() {
                    try {
                        return !catalog.members(new DummyMonitor()).isEmpty();
                    } catch (IOException e) {
                        return false;
                    }
                }

            }, true);

			members = catalog.members(new DummyMonitor());
			assertTrue(!members.isEmpty());
			for (Iterator itr = members.iterator(); itr.hasNext();) {
				assertServiceType((IService)itr.next());
			}
	}

	abstract Object getContext() throws Exception;

	abstract void assertServiceType(IService service);
}
