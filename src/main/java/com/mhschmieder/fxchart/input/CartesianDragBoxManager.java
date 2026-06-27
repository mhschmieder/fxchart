/*
 * MIT License
 *
 * Copyright (c) 2020, 2026 Mark Schmieder. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * This file is part of the fxgraphics Library
 *
 * You should have received a copy of the MIT License along with the fxgraphics
 * Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxgraphics
 */
package com.mhschmieder.fxchart.input;

import com.mhschmieder.fxgraphics.input.ClickLocation;
import com.mhschmieder.fxgraphics.input.DragBoxManager;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import org.apache.commons.math3.util.FastMath;

/**
 * A specialization of the Drag Box Manager, for Cartesian Space, which is the
 * most general case that covers most applications other than cartographic ones.
 */
public class CartesianDragBoxManager extends DragBoxManager {
    
    /**
     * Declare a variable to keep track of the Drag Box origin (initial click).
     */
    public Point2D _dragBoxOriginMeters;

    /**
     * Declare a Bounds for the Drag Box, for efficient containment tests.
     */
    public Bounds _dragBoxMeters;
    
    public CartesianDragBoxManager() {
        // Always call the superclass constructor first!
        super();
        
        _dragBoxOriginMeters = new Point2D( 0.0d, 0.0d );
        _dragBoxMeters = new BoundingBox( 0.0d, 0.0d, 0.0d, 0.0d );        
    }

    // TODO: Switch to JavaFX gesture-based Drag Boxes from this old AWT way?
    public void initDragBox( final ClickLocation clickPointPixels,
                             final Point2D clickPointMeters ) {
        final double xMeters = clickPointMeters.getX();
        final double yMeters = clickPointMeters.getY();

        _dragBoxOriginMeters = new Point2D( xMeters, yMeters );
        _dragBoxMeters = new BoundingBox( xMeters, yMeters, 0.0d, 0.0d );

        super.initDragBox( clickPointPixels );
    }

    public void updateDragBoxSize( final ClickLocation mouseLocationPixels,
                                   final Point2D mouseLocationMeters ) {
        // Update the Drag Box size based on the deltas in all dimensions.
        // NOTE: Due to the possibility of dynamically swapping the vertices
        //  based on drag direction, we have to take both min and max to be
        //  quadrant sensitive, but stay aware of the API's upper left bias.
        final double ulx = FastMath.min( _dragBoxOriginMeters.getX(), mouseLocationMeters.getX() );
        final double uly = FastMath.min( _dragBoxOriginMeters.getY(), mouseLocationMeters.getY() );
        final double lrx = FastMath.max( _dragBoxOriginMeters.getX(), mouseLocationMeters.getX() );
        final double lry = FastMath.max( _dragBoxOriginMeters.getY(), mouseLocationMeters.getY() );

        final double width = FastMath.abs( ulx - lrx );
        final double height = FastMath.abs( uly - lry );

        _dragBoxMeters = new BoundingBox( ulx, uly, width, height );

        super.updateDragBoxSize( mouseLocationPixels );
    }
}
