package org.locationtech.udig.mapgraphic;

import static org.junit.Assert.*;

import org.locationtech.udig.catalog.CatalogPlugin;
import org.locationtech.udig.catalog.ID;
import org.locationtech.udig.catalog.IGeoResource;
import org.locationtech.udig.catalog.IRepository;
import org.locationtech.udig.mapgraphic.internal.MapGraphicService;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;

public class CatalogIntegrationTest {

    @Test
    public void testAquire() throws Exception {
        IRepository local = CatalogPlugin.getDefault().getLocal();
        local.acquire(MapGraphicService.SERVICE_URL, null);
        
        // Test ScaleBar
        final ID SCALE_BAR = new ID("mapgraphic:/localhost/mapgraphic#scalebar", null); //$NON-NLS-1$
        IGeoResource scalebarResource = local.getById(IGeoResource.class, SCALE_BAR,
                new NullProgressMonitor());
        assertNotNull(scalebarResource);
        
        // Test Graticule
        final ID GRATICULE = new ID("mapgraphic:/localhost/mapgraphic#graticule", null); //$NON-NLS-1$
        IGeoResource graticuleResource = local.getById(IGeoResource.class, GRATICULE,
                new NullProgressMonitor());
        assertNotNull(graticuleResource);
        
    }

}
