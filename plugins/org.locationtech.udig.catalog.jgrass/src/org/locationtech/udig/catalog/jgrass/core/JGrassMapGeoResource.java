/*
 * uDig - User Friendly Desktop Internet GIS client
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the HydroloGIS BSD
 * License v1.0 (http://udig.refractions.net/files/hsd3-v10.html).
 */
package org.locationtech.udig.catalog.jgrass.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.locationtech.udig.catalog.ID;
import org.locationtech.udig.catalog.IGeoResource;
import org.locationtech.udig.catalog.IGeoResourceInfo;
import org.locationtech.udig.catalog.IResolve;
import org.locationtech.udig.catalog.IResolveFolder;
import org.locationtech.udig.catalog.IService;
import org.locationtech.udig.catalog.URLUtils;
import org.locationtech.udig.catalog.ui.CatalogUIPlugin;
import org.locationtech.udig.catalog.ui.ISharedImages;
import org.locationtech.udig.project.internal.render.impl.RendererImpl;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.gce.grassraster.GrassCoverageReader;
import org.geotools.gce.grassraster.JGrassConstants;
import org.geotools.gce.grassraster.JGrassMapEnvironment;
import org.geotools.gce.grassraster.JGrassRegion;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import org.locationtech.udig.catalog.jgrass.JGrassPlugin;
import org.locationtech.udig.catalog.jgrass.utils.JGrassCatalogUtilities;
import org.locationtech.udig.renderer.jgrass.RasterRenderer;

public class JGrassMapGeoResource extends IGeoResource {

    public static final String READERID = "eu.hydrologis.udig.catalog.internal.jgrass.JGrassMapGeoResource.readerid"; //$NON-NLS-1$

    /** the parent georesource field */
    private IResolveFolder parent = null;

    /** name field */
    private String name = null;

    /** map type field */
    private String type = null;

    /** error message field */
    private Throwable msg = null;

    /** the map file region window field */
    private JGrassRegion fileWindow = null;

    /** metadata info field */
    private IGeoResourceInfo info = null;

    private final IService parentService;

    private JGrassMapEnvironment jGrassMapEnvironment;

    private CoordinateReferenceSystem locationCrs;

    private ImageDescriptor gridIconID;

    private ImageDescriptor gridMissingIconID;

    private ImageDescriptor problemIconID;

    public JGrassMapGeoResource( IService parentService, JGrassMapsetGeoResource parentMapset, String mapName,
            String mapTypeAndPath ) {
        this.parentService = parentService;
        this.parent = parentMapset;
        this.name = mapName;

        String[] typeAndPath = mapTypeAndPath.split("\\|"); //$NON-NLS-1$
        type = typeAndPath[0].trim();

        // URL parentIdentifier = parent.getIdentifier();
        // ID parentID = new ID(parentIdentifier);

        jGrassMapEnvironment = new JGrassMapEnvironment(parentMapset.getFile(), name);
        locationCrs = parentMapset.getLocationCrs();
        // id = new ID(parentID, name);

        gridIconID = CatalogUIPlugin.getDefault().getImageDescriptor(ISharedImages.GRID_OBJ);
        gridMissingIconID = CatalogUIPlugin.getDefault().getImageDescriptor(ISharedImages.GRID_MISSING);
        problemIconID = AbstractUIPlugin.imageDescriptorFromPlugin(JGrassPlugin.PLUGIN_ID, "icons/obj16/problem.gif");
    }

    public <T> boolean canResolve( Class<T> adaptee ) {
        // garbage in, garbage out
        if (adaptee == null)
            return false;

        /*
         * in this case our resource is a folder, therefore of type File
         */
        return adaptee.isAssignableFrom(IService.class) || adaptee.isAssignableFrom(IGeoResource.class)
                || adaptee.isAssignableFrom(JGrassMapGeoResource.class) || adaptee.isAssignableFrom(GridCoverage.class)
                || adaptee.isAssignableFrom(AbstractGridCoverage2DReader.class) || adaptee.isAssignableFrom(File.class)
                || adaptee.isAssignableFrom(RendererImpl.class) || adaptee.isAssignableFrom(CoordinateReferenceSystem.class)
                || adaptee.isAssignableFrom(GridGeometry2D.class) || super.canResolve(adaptee);
    }

    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if (adaptee == null)
            return null;

        if (adaptee.isAssignableFrom(IService.class)) {
            return adaptee.cast(parent.parent(monitor));
        }
        if (adaptee.isAssignableFrom(IGeoResourceInfo.class)) {
            return adaptee.cast(getInfo(monitor));
        }
        if (adaptee.isAssignableFrom(File.class)) {
            return adaptee.cast(jGrassMapEnvironment.getMapFile());
        }
        if (adaptee.isAssignableFrom(JGrassMapGeoResource.class)) {
            return adaptee.cast(this);
        }
        if (adaptee.isAssignableFrom(IGeoResource.class)) {
            return adaptee.cast(this);
        }
        if (adaptee.isAssignableFrom(RendererImpl.class)) {
            RasterRenderer renderer = new RasterRenderer();
            return adaptee.cast(renderer);
        }
        if (adaptee.isAssignableFrom(AbstractGridCoverage2DReader.class)) {
            GrassCoverageReader reader = new GrassCoverageReader(jGrassMapEnvironment.getMapFile());

            return adaptee.cast(reader);
        }
        if (adaptee.isAssignableFrom(CoordinateReferenceSystem.class)) {
            return adaptee.cast(locationCrs);
        }
        if (adaptee.isAssignableFrom(GridGeometry2D.class)) {
            try {
                JGrassRegion r = jGrassMapEnvironment.getFileRegion();
                Envelope2D envelope = new Envelope2D(locationCrs, r.getWest(), r.getSouth(), r.getEast() - r.getWest(),
                        r.getNorth() - r.getSouth());
                GridEnvelope2D gridRange = new GridEnvelope2D(0, 0, r.getCols(), r.getRows());
                GridGeometry2D gridGeometry2D = new GridGeometry2D(gridRange, (org.opengis.geometry.Envelope) envelope);
                return adaptee.cast(gridGeometry2D);
            } catch (Exception e) {
                msg = e;
            }
        }
        if (adaptee.isAssignableFrom(GridCoverage.class)) {
            try {
                JGrassRegion jGrassRegion = jGrassMapEnvironment.getFileRegion();
                GridCoverage2D mapCoverage = JGrassCatalogUtilities.getGridcoverageFromGrassraster(jGrassMapEnvironment,
                        jGrassRegion);
                return adaptee.cast(mapCoverage);
            } catch (Exception e) {
                msg = e;
            }
        }
        // bad call to resolve
        return super.resolve(adaptee, monitor);
    }
    // public ID getID() {
    // return id;
    // }

    public URL getIdentifier() {
        File mapFile = jGrassMapEnvironment.getMapFile();
        int mapsetPathLength = mapFile.getParentFile().getParent().length();
        String relativePath = mapFile.getAbsolutePath().substring(mapsetPathLength);
        try {
            String parenturlString = URLUtils.urlToString(parent.getIdentifier(), false);
            relativePath = relativePath.replace("\\", "/");
            return new URL(parenturlString + relativePath);
        } catch (MalformedURLException e) {
            JGrassPlugin.log(
                    "JGrassPlugin problem: eu.hydrologis.udig.catalog.internal.jgrass#JGrassMapGeoResource#getIdentifier", e); //$NON-NLS-1$

            e.printStackTrace();
            return null;
        }
    }

    public ID getID() {
        File mapFile = jGrassMapEnvironment.getMapFile();
        try {
            URL identifier = getIdentifier();
            URI uri = identifier.toURI();
            String asciiString = uri.toASCIIString();
            return new ID(asciiString, identifier, mapFile, uri, "grass");
            // return new ID(getIdentifier());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return new ID(mapFile, "grass");
    }

    public IService service( IProgressMonitor monitor ) throws IOException {
        return parentService;
    }

    public IResolve parent( IProgressMonitor monitor ) throws IOException {
        return parent;
    }

    public Throwable getMessage() {
        return msg;
    }

    public Status getStatus() {
        // error occured
        if (msg != null) {
            return Status.BROKEN;
        }

        return Status.CONNECTED;
    }

    public String getType() {
        return type;
    }

    public File getMapFile() {
        return jGrassMapEnvironment.getMapFile();
    }

    public File getMapsetFile() {
        return jGrassMapEnvironment.getMAPSET();
    }

    public File getLocationFile() {
        return jGrassMapEnvironment.getLOCATION();
    }

    /**
     * <p>
     * get some informations about the map resource
     * </p>
     * 
     * @author Andrea Antonello - www.hydrologis.com
     * @since 1.1.0
     */
    class JGrassMapGeoResourceInfo extends IGeoResourceInfo {
        public JGrassMapGeoResourceInfo( IProgressMonitor monitor ) {
            this.name = JGrassMapGeoResource.this.name;
            this.title = this.name;
            this.description = JGrassMapGeoResource.this.type;

            try {
                if (JGrassMapGeoResource.this.type.equals(JGrassConstants.GRASSBINARYRASTERMAP)) {
                    File cellhd = jGrassMapEnvironment.getCELLHD();
                    fileWindow = new JGrassRegion(cellhd.getAbsolutePath());
                    bounds = new ReferencedEnvelope(fileWindow.getEnvelope(), locationCrs);
                    super.icon = gridIconID;
                } else {
                    super.icon = gridMissingIconID;
                }
            } catch (Exception e) {
                super.icon = problemIconID;
                e.printStackTrace();
            }

        }

        public ReferencedEnvelope getBounds() {
            return bounds;
        }

        public void setBounds( ReferencedEnvelope newBounds ) {
            bounds = newBounds;
        }
    }

    /**
     * the real extention and resolution of the map
     * @throws IOException 
     */
    public JGrassRegion getFileWindow() {
        return fileWindow;
    }

    public void resetBoundInfo() throws IOException {
        jGrassMapEnvironment = new JGrassMapEnvironment(jGrassMapEnvironment.getCELL());
        fileWindow = jGrassMapEnvironment.getFileRegion();
        ReferencedEnvelope newBounds = new ReferencedEnvelope(fileWindow.getEnvelope(), locationCrs);
        if (info != null) {
            ((JGrassMapGeoResourceInfo) info).setBounds(newBounds);
        }
        createInfo(new NullProgressMonitor());
    }

    @Override
    public String getTitle() {
        try {
            createInfo(new NullProgressMonitor());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return info.getTitle();
    }

    /**
     * the region how it is seen when doing elaborations. This is the active working region of the
     * mapset.
     * @throws IOException 
     */
    public JGrassRegion getActiveWindow() throws IOException {
        if (JGrassMapGeoResource.this.type.equals(JGrassConstants.GRASSBINARYRASTERMAP)) {
            JGrassRegion activeRegion = jGrassMapEnvironment.getActiveRegion();
            return activeRegion;
        }

        return null;
    }

    public JGrassMapEnvironment getjGrassMapEnvironment() {
        return jGrassMapEnvironment;
    }

    protected IGeoResourceInfo createInfo( IProgressMonitor monitor ) throws IOException {
        // support concurrent access
        synchronized (this) {
            if (info == null) {
                info = new JGrassMapGeoResourceInfo(monitor);
            }
        }

        return info;
    }

}
