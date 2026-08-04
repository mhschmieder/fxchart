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
package com.mhschmieder.fxchart.layout;

import com.mhschmieder.fxchart.action.PolarResponseActions;
import com.mhschmieder.fxchart.control.PolarResponseMenuFactory;
import com.mhschmieder.fxcontrols.util.RegionUtilities;
import com.mhschmieder.jchart.layout.PolarAmplitudePlot;
import com.mhschmieder.jcommons.util.ClientProperties;
import com.mhschmieder.jgraphics.util.GraphicsUtilities;
import com.mhschmieder.jphysics.acoustics.RelativeBandwidth;

import java.awt.EventQueue;
import java.awt.RenderingHints;

import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;

public final class PolarResponsePane extends BorderPane {

    // Cache the full Session Context (System Type, Locale, Client Type, etc.).
    public ClientProperties _clientProperties;
    // Declare and instantiate all of the UI components.
    protected SwingNode _polarPlotHzSwingNode;
    protected PolarAmplitudePlot _awtPolarPlotHz;
    protected SwingNode _polarPlotVtSwingNode;
    protected PolarAmplitudePlot _awtPolarPlotVt;
    /**
     * Keep track of which window owns the context menu, for focus and
     * dismissal
     */
    protected Window _contextMenuOwner;

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

    private void initPane( final int polarResponseViewerWidth,
                           final int polarResponseViewerHeight,
                           final double angleIncrementDegrees,
                           final PolarResponseActions polarResponseActions ) {
        _polarPlotHzSwingNode = new SwingNode();
        _polarPlotVtSwingNode = new SwingNode();
        EventQueue.invokeLater( () -> {
            // Make the individual plots for horizontal and vertical polar
            // patterns.
            _awtPolarPlotHz = new PolarAmplitudePlot( polarResponseViewerWidth,
                                                      polarResponseViewerHeight,
                                                      "Horizontal", //$NON
                    // -NLS-1$
                                                      angleIncrementDegrees );
            _awtPolarPlotVt = new PolarAmplitudePlot( polarResponseViewerWidth,
                                                      polarResponseViewerHeight,
                                                      "Vertical", //$NON-NLS-1$
                                                      angleIncrementDegrees );

            // Get Rendering Hints that aim for quality rendering of geometry.
            final RenderingHints renderingHints
                    = GraphicsUtilities.getRenderingHintsForCharting();
            _awtPolarPlotHz.setRenderingHints( renderingHints );
            _awtPolarPlotVt.setRenderingHints( renderingHints );

            _polarPlotHzSwingNode.setContent( _awtPolarPlotHz );
            _polarPlotVtSwingNode.setContent( _awtPolarPlotVt );
        } );

        setLeft( _polarPlotHzSwingNode );
        setRight( _polarPlotVtSwingNode );

        // Build the contextual pop-up menu.
        final ContextMenu contextMenu
                = PolarResponseMenuFactory.getPolarResponseContextMenu(
                _clientProperties,
                polarResponseActions );

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
                contextMenu.show( contextMenuOwner,
                                  mouseEvent.getScreenX(),
                                  mouseEvent.getScreenY() );
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
                contextMenu.show( contextMenuOwner,
                                  mouseEvent.getScreenX(),
                                  mouseEvent.getScreenY() );
            }
        } );
        _polarPlotVtSwingNode.setOnMouseMoved( mouseEvent -> {
            // Update the cursor coordinates.
            updateCursorCoordinates( mouseEvent.getScreenX(),
                                     mouseEvent.getScreenY(),
                                     _awtPolarPlotVt );
        } );
    }

    protected void updateCursorCoordinates( final double cursorX,
                                            final double cursorY,
                                            final PolarAmplitudePlot polarAmplitudeChart ) {
        // TODO: Implement this after we replace the AWT version of the Polar
        // Chart, which has its own data tracking at the moment (using AWT).
    }

    public int getNumberOfPolarDataPoints() {
        // NOTE: This number is always the same for horizontal and vertical.
        return _awtPolarPlotHz.getNumberOfPolarDataPoints();
    }

    public float getGridRange() {
        // NOTE: This number is always the same for horizontal and vertical.
        return _awtPolarPlotHz.getGridRange();
    }

    public void setGridRange( final double gridRange ) {
        // Sync up the traces with the new Grid Range.
        final float awtGridRange = ( float ) gridRange;
        EventQueue.invokeLater( () -> _awtPolarPlotHz.setGridRange( awtGridRange ) );
        EventQueue.invokeLater( () -> _awtPolarPlotVt.setGridRange( awtGridRange ) );
    }

    public int getGridSpacing() {
        // NOTE: This number is always the same for horizontal and vertical.
        return _awtPolarPlotHz.getGridSpacing();
    }

    public void setGridSpacing( final int gridSpacing ) {
        // Sync up the traces with the new radial Grid Spacing.
        EventQueue.invokeLater( () -> _awtPolarPlotHz.setGridSpacing(
                gridSpacing ) );
        EventQueue.invokeLater( () -> _awtPolarPlotVt.setGridSpacing(
                gridSpacing ) );
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

    public void resetVisualizations() {
        // Clear all the plots.
        clearPlots();
    }

    public void clearPlots() {
        // Delegate this method to the subsidiary panels.
        EventQueue.invokeLater( () -> _awtPolarPlotHz.clearPlot() );
        EventQueue.invokeLater( () -> _awtPolarPlotVt.clearPlot() );
    }

    // This method sets the background color, and where appropriate, the
    // foreground color is set to complement it for text-based components.
    public void setForegroundFromBackground( final Color backColor ) {
        // Set the new Background first, so it sets context for CSS derivations.
        final Background background = RegionUtilities.makeRegionBackground(
                backColor );
        setBackground( background );

        // Forward this method to the subcomponents.
        final java.awt.Color awtBackColor
                = new java.awt.Color( ( float ) backColor.getRed(),
                                      ( float ) backColor.getGreen(),
                                      ( float ) backColor.getBlue() );
        EventQueue.invokeLater( () -> _awtPolarPlotHz.setForegroundFromBackground(
                awtBackColor ) );
        EventQueue.invokeLater( () -> _awtPolarPlotVt.setForegroundFromBackground(
                awtBackColor ) );
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
