/**
 * MIT License
 *
 * Copyright (c) 2020, 2023 Mark Schmieder
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
 * This file is part of the FxChartToolkit Library
 *
 * You should have received a copy of the MIT License along with the
 * FxChartToolkit Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxcharttoolkit
 */
package com.mhschmieder.fxcharttoolkit.layout;

import java.awt.EventQueue;
import java.awt.RenderingHints;

import com.mhschmieder.acousticstoolkit.RelativeBandwidth;
import com.mhschmieder.charttoolkit.layout.PolarAmplitudeChart;
import com.mhschmieder.commonstoolkit.util.ClientProperties;
import com.mhschmieder.fxcharttoolkit.action.PolarResponseActions;
import com.mhschmieder.fxcharttoolkit.control.PolarResponseMenuFactory;
import com.mhschmieder.fxguitoolkit.layout.LayoutFactory;
import com.mhschmieder.graphicstoolkit.GraphicsUtilities;

import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;

public final class PolarResponsePane extends BorderPane {

    // Declare and instantiate all of the UI components.
    protected SwingNode           _polarPlotHzSwingNode;
    protected PolarAmplitudeChart _awtPolarPlotHz;
    protected SwingNode           _polarPlotVtSwingNode;
    protected PolarAmplitudeChart _awtPolarPlotVt;

    /**
     * Keep track of which window owns the context menu, for focus and dismissal
     */
    protected Window              _contextMenuOwner;

    // Cache the full Session Context (System Type, Locale, Client Type, etc.).
    public ClientProperties       _clientProperties;

    public PolarResponsePane( final int polarResponseViewerWidth,
                              final int polarResponseViewerHeight,
                              final double angleIncrementDegrees,
                              final ClientProperties pClientProperties,
                              final Window contextMenuOwner,
                              final PolarResponseActions polarResponseActions ) {
        // Always call the superclass constructor first!
        super();

        _clientProperties = pClientProperties;
        _contextMenuOwner = contextMenuOwner;

        try {
            initPane( polarResponseViewerWidth, 
                      polarResponseViewerHeight, 
                      angleIncrementDegrees, 
                      polarResponseActions );
        }
        catch ( final Exception ex ) {
            ex.printStackTrace();
        }
    }

    public void clearPlots() {
        // Delegate this method to the subsidiary panels.
        EventQueue.invokeLater( () -> _awtPolarPlotHz.clearPlot() );
        EventQueue.invokeLater( () -> _awtPolarPlotVt.clearPlot() );
    }
    
    public int getNumberOfPolarDataPoints() {
        // NOTE: This number is always the same for horizontal and vertical.
        return _awtPolarPlotHz.getNumberOfPolarDataPoints();
    }

    public float getGridRange() {
        // NOTE: This number is always the same for horizontal and vertical.
        return _awtPolarPlotHz.getGridRange();
    }

    public int getGridSpacing() {
        // NOTE: This number is always the same for horizontal and vertical.
        return _awtPolarPlotHz.getGridSpacing();
    }

    public double[] getHorizontalPolarAmplitudeData() {
        return _awtPolarPlotHz.getPolarAmplitudeData();
    }

    public double[] getHorizontalPolarAngleData() {
        return _awtPolarPlotHz.getPolarAngleData();
    }

    public double[] getVerticalPolarAmplitudeData() {
        return _awtPolarPlotVt.getPolarAmplitudeData();
    }

    public double[] getVerticalPolarAngleData() {
        return _awtPolarPlotVt.getPolarAngleData();
    }

    private void initPane( final int polarResponseViewerWidth,
                           final int polarResponseViewerHeight,
                           final double angleIncrementDegrees,
                           final PolarResponseActions polarResponseActions ) {
        _polarPlotHzSwingNode = new SwingNode();
        _polarPlotVtSwingNode = new SwingNode();
        EventQueue.invokeLater( () -> {
            // Make the individual plots for horizontal and vertical polar
            // patterns.
            _awtPolarPlotHz = new PolarAmplitudeChart( polarResponseViewerWidth,
                                                       polarResponseViewerHeight,
                                                       "Horizontal", //$NON-NLS-1$
                                                       angleIncrementDegrees );
            _awtPolarPlotVt = new PolarAmplitudeChart( polarResponseViewerWidth,
                                                       polarResponseViewerHeight,
                                                       "Vertical", //$NON-NLS-1$
                                                       angleIncrementDegrees );

            // Get Rendering Hints that aim for quality rendering of geometry.
            final RenderingHints renderingHints = GraphicsUtilities.getRenderingHintsForCharting();
            _awtPolarPlotHz.setRenderingHints( renderingHints );
            _awtPolarPlotVt.setRenderingHints( renderingHints );

            _polarPlotHzSwingNode.setContent( _awtPolarPlotHz );
            _polarPlotVtSwingNode.setContent( _awtPolarPlotVt );
        } );

        setLeft( _polarPlotHzSwingNode );
        setRight( _polarPlotVtSwingNode );

        // Build the contextual pop-up menu.
        final ContextMenu contextMenu = PolarResponseMenuFactory
                .getPolarResponseContextMenu( _clientProperties, polarResponseActions );

        // Register the pop-up menu and data tracker triggers.
        final Node contextMenuOwner = this;
        _polarPlotHzSwingNode.setOnMouseClicked( mouseEvent -> {
            final MouseButton button = mouseEvent.getButton();
            if ( MouseButton.PRIMARY.equals( button ) ) {
                // Update the cursor coordinates.
                updateCursorCoordinates( mouseEvent.getScreenX(),
                                         mouseEvent.getScreenY(),
                                         _awtPolarPlotHz );
            }
            else if ( MouseButton.SECONDARY.equals( button ) ) {
                contextMenu
                        .show( contextMenuOwner, mouseEvent.getScreenX(), mouseEvent.getScreenY() );
            }
        } );
        _polarPlotHzSwingNode.setOnMouseMoved( mouseEvent -> {
            // Update the cursor coordinates.
            updateCursorCoordinates( mouseEvent.getScreenX(),
                                     mouseEvent.getScreenY(),
                                     _awtPolarPlotHz );
        } );

        _polarPlotVtSwingNode.setOnMouseClicked( mouseEvent -> {
            final MouseButton button = mouseEvent.getButton();
            if ( MouseButton.PRIMARY.equals( button ) ) {
                // Update the cursor coordinates.
                updateCursorCoordinates( mouseEvent.getScreenX(),
                                         mouseEvent.getScreenY(),
                                         _awtPolarPlotVt );
            }
            else if ( MouseButton.SECONDARY.equals( button ) ) {
                contextMenu
                        .show( contextMenuOwner, mouseEvent.getScreenX(), mouseEvent.getScreenY() );
            }
        } );
        _polarPlotVtSwingNode.setOnMouseMoved( mouseEvent -> {
            // Update the cursor coordinates.
            updateCursorCoordinates( mouseEvent.getScreenX(),
                                     mouseEvent.getScreenY(),
                                     _awtPolarPlotVt );
        } );
    }

    public void resetVisualizations() {
        // Clear all the plots.
        clearPlots();
    }

    // This method sets the background color, and where appropriate, the
    // foreground color is set to complement it for text-based components.
    public void setForegroundFromBackground( final Color backColor ) {
        // Set the new Background first, so it sets context for CSS derivations.
        final Background background = LayoutFactory.makeRegionBackground( backColor );
        setBackground( background );

        // Forward this method to the subcomponents.
        final java.awt.Color awtBackColor = new java.awt.Color( ( float ) backColor.getRed(),
                                                                ( float ) backColor.getGreen(),
                                                                ( float ) backColor.getBlue() );
        EventQueue.invokeLater( () -> _awtPolarPlotHz.setForegroundFromBackground( awtBackColor ) );
        EventQueue.invokeLater( () -> _awtPolarPlotVt.setForegroundFromBackground( awtBackColor ) );
    }

    public void setGridRange( final double gridRange ) {
        // Sync up the traces with the new Grid Range.
        final float awtGridRange = ( float ) gridRange;
        EventQueue.invokeLater( () -> _awtPolarPlotHz.setGridRange( awtGridRange ) );
        EventQueue.invokeLater( () -> _awtPolarPlotVt.setGridRange( awtGridRange ) );
    }

    public void setGridSpacing( final int gridSpacing ) {
        // Sync up the traces with the new radial Grid Spacing.
        EventQueue.invokeLater( () -> _awtPolarPlotHz.setGridSpacing( gridSpacing ) );
        EventQueue.invokeLater( () -> _awtPolarPlotVt.setGridSpacing( gridSpacing ) );
    }

    protected void updateCursorCoordinates( final double cursorX,
                                            final double cursorY,
                                            final PolarAmplitudeChart polarAmplitudeChart ) {
        // TODO: Implement this after we replace the AWT version of the Polar
        // Chart, which has its own data tracking at the moment (using AWT).
    }

    public void updateHorizontalPolarResponse( final double[] amplitude,
                                               final String loudspeakerModel,
                                               final RelativeBandwidth relativeBandwidth,
                                               final double centerFrequency ) {
        _awtPolarPlotHz.updatePolarAmplitudeTrace( amplitude,
                                                   loudspeakerModel,
                                                   relativeBandwidth,
                                                   centerFrequency );
    }

    public void updateVerticalPolarResponse( final double[] amplitude,
                                             final String loudspeakerModel,
                                             final RelativeBandwidth relativeBandwidth,
                                             final double centerFrequency ) {
        _awtPolarPlotVt.updatePolarAmplitudeTrace( amplitude,
                                                   loudspeakerModel,
                                                   relativeBandwidth,
                                                   centerFrequency );
    }
}
