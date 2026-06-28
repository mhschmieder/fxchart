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
 * This file is part of the fxchart Library
 *
 * You should have received a copy of the MIT License along with the fxchart
 * Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxchart
 */
package com.mhschmieder.fxchart.input;

import com.mhschmieder.fxgraphics.input.MouseToolManager;
import com.mhschmieder.fxgraphics.input.RotationManager;
import com.mhschmieder.jgraphics.input.MouseToolMode;
import com.mhschmieder.jphysics.measure.DistanceUnit;
import com.mhschmieder.jphysics.measure.UnitConversion;
import javafx.geometry.Point2D;
import javafx.scene.chart.ValueAxis;
import org.apache.commons.math3.util.FastMath;

/**
 * A specialization of the Rotation Manager, for Cartesian Space, which is the
 * most general case that covers most applications other than cartographic ones.
 */
public class CartesianRotationManager extends RotationManager {

    /**
     * Declare a variable to keep track of the rotation center point location (meters).
     */
    public Point2D _centerOfRotationMeters;
    
    /** Cache a local copy of the Mouse Tool Manager to check Rotation context. */
    public MouseToolManager _mouseToolManager;

    /** Cache a local copy of the x-axis so we can apply proper scaling. */
    protected ValueAxis< Number > _xAxis;

    /** Cache a local copy of the y-axis so we can apply proper scaling. */
    protected ValueAxis< Number > _yAxis;

    public CartesianRotationManager( final MouseToolManager mouseToolManager,
                                     final ValueAxis< Number > xAxis,
                                     final ValueAxis< Number > yAxis ) {
        // Always call the superclass constructor first!
        super();
        
        _mouseToolManager = mouseToolManager;
        _xAxis = xAxis;
        _yAxis = yAxis;
        
        _centerOfRotationMeters = new Point2D( 10.0d, 10.0d );
    }
    
    public void reset() {
        _centerOfRotationMeters = new Point2D( 10.0d, 10.0d );
    }
    
    public void initRotation( final Point2D clickPointMeters ) {
        // Find the initial angle between the mouse and the point of
        // rotation, so that we can use the atan2 method for the most
        // reliable computation of the angle differential on each Mouse
        // Dragged event.
        // NOTE: Due to switching from pixels to meters, we need to invert
        //  the y-axis offsets or else we get the wrong angle direction.
        final double rotateTheta = FastMath.atan2( 
                _centerOfRotationMeters.getY() - clickPointMeters.getY(),
                clickPointMeters.getX() - _centerOfRotationMeters.getX() );
        
        // Pass this pre-calculated value to the Rotation Manager. It cannot
        // do the calculation itself, as the formula requires local units.
        super.initRotation( rotateTheta );
    }

    public void updateCenterOfRotationLocation( final double centerX, 
                                                final double centerY,
                                                final DistanceUnit distanceUnit ) {
        // Update the cached location for the Center of Rotation, in Meters.
        _centerOfRotationMeters = new Point2D( centerX, centerY );

        // Update the modified Center of Rotation node in the Scene Graph.
        updateCenterOfRotationGraphics( distanceUnit );
    }

    /**
     * Update the Center of Rotation Node's graphics content, and re-scale for
     * the current Distance Unit.
     */
    public void updateCenterOfRotationGraphics( final DistanceUnit distanceUnit ) {
        // NOTE: Make sure Rotate Tool is active before modifying graphics.
        if ( MouseToolMode.ROTATE != _mouseToolManager._activeMouseTool ) {
            return;
        }
        
        // Position the Center of Rotation at the mouse click, in the current
        // user Distance Unit.
        final double centerOfRotationLocalX = UnitConversion
                .convertDistance( _centerOfRotationMeters.getX(),
                                  DistanceUnit.METERS,
                                  distanceUnit );
        final double centerOfRotationLocalY = UnitConversion
                .convertDistance( _centerOfRotationMeters.getY(),
                                  DistanceUnit.METERS,
                                  distanceUnit );
        final double centerOfRotationXPx = _xAxis.getDisplayPosition(
                centerOfRotationLocalX );
        final double centerOfRotationYPx = _yAxis.getDisplayPosition(
                centerOfRotationLocalY );
        
        super.updateCenterOfRotationGraphics( centerOfRotationXPx, 
                                              centerOfRotationYPx );
    }
}
