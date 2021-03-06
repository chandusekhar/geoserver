/* (c) 2016 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.gwc.wmts;

import org.geoserver.catalog.*;
import org.geoserver.catalog.DimensionDefaultValueSetting.Strategy;
import org.geoserver.catalog.impl.DimensionInfoImpl;
import org.geoserver.catalog.testreader.CustomFormat;
import org.geoserver.gwc.wmts.dimensions.Dimension;
import org.geoserver.gwc.wmts.dimensions.DimensionsUtils;
import org.geoserver.gwc.wmts.dimensions.RasterElevationDimension;
import org.junit.Test;
import org.opengis.filter.Filter;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * This class contains tests that check that elevation dimensions values are correctly extracted from rasters.
 */
public class RasterElevationDimensionTest extends TestsSupport {

    @Test
    public void testDisabledDimension() throws Exception {
        // enable a elevation dimension
        DimensionInfo dimensionInfo = new DimensionInfoImpl();
        dimensionInfo.setEnabled(true);
        CoverageInfo rasterInfo = getCoverageInfo();
        rasterInfo.getMetadata().put(ResourceInfo.ELEVATION, dimensionInfo);
        getCatalog().save(rasterInfo);
        // check that we correctly retrieve the elevation dimension
        assertThat(DimensionsUtils.extractDimensions(wms, getLayerInfo()).size(), is(1));
        // disable the elevation dimension
        dimensionInfo.setEnabled(false);
        rasterInfo.getMetadata().put(ResourceInfo.ELEVATION, dimensionInfo);
        getCatalog().save(rasterInfo);
        // no dimensions should be available
        assertThat(DimensionsUtils.extractDimensions(wms, getLayerInfo()).size(), is(0));
    }

    @Test
    public void testGetDefaultValue() {
        testDefaultValueStrategy(Strategy.MINIMUM, "0.0");
        testDefaultValueStrategy(Strategy.MAXIMUM, "100.0");
    }

    @Test
    public void testGetDomainsValues() throws Exception {
        testDomainsValuesRepresentation(DimensionPresentation.LIST, "0.0", "100.0");
        testDomainsValuesRepresentation(DimensionPresentation.CONTINUOUS_INTERVAL, "0.0--100.0");
        testDomainsValuesRepresentation(DimensionPresentation.DISCRETE_INTERVAL, "0.0--100.0");
    }

    @Override
    protected Dimension buildDimension(DimensionInfo dimensionInfo) {
        return new RasterElevationDimension(wms, getLayerInfo(), dimensionInfo);
    }

    @Test
    public void testGetHistogram() {
        DimensionInfo dimensionInfo = createDimension(true, DimensionPresentation.LIST, null);
        Dimension dimension = buildDimension(dimensionInfo);
        Tuple<String, List<Integer>> histogram = dimension.getHistogram(Filter.INCLUDE, "50");
        assertThat(histogram.first, is("0.0/100.0/50.0"));
        assertThat(histogram.second, containsInAnyOrder(1, 1));
    }

    /**
     * Helper method that just returns the current layer info.
     */
    private LayerInfo getLayerInfo() {
        return catalog.getLayerByName(RASTER_ELEVATION.getLocalPart());
    }

    /**
     * Helper method that just returns the current coverage info.
     */
    private CoverageInfo getCoverageInfo() {
        LayerInfo layerInfo = getLayerInfo();
        assertThat(layerInfo.getResource(), instanceOf(CoverageInfo.class));
        return (CoverageInfo) layerInfo.getResource();
    }
}
