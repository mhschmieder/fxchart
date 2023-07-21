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
package com.mhschmieder.fxcharttoolkit.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import com.mhschmieder.acousticstoolkit.RelativeBandwidth;
import com.mhschmieder.charttoolkit.layout.PolarAmplitudeChart;
import com.mhschmieder.fxguitoolkit.swing.RenderedGraphicsPanel;
import com.mhschmieder.graphicstoolkit.color.ColorUtilities;

public final class PolarResponsePanel extends RenderedGraphicsPanel {
    /**
     * Unique Serial Version ID for this class, to avoid class loader conflicts.
     */
    private static final long  serialVersionUID = 8938733992377167662L;

    // Declare and instantiate all of the UI components.
    private JPanel             _polarPlotGraphicsPanel;
    public PolarAmplitudeChart _polarPlotHz;
    public PolarAmplitudeChart _polarPlotVt;

    public PolarResponsePanel( final int polarResponseViewerWidth,
                               final int polarResponseViewerHeight,
                               final double angleIncrementDegrees ) {
        // Always call the superclass constructor first!
        super();

        try {
            initPanel( polarResponseViewerWidth, 
                       polarResponseViewerHeight,
                       angleIncrementDegrees );
        }
        catch ( final Exception ex ) {
            ex.printStackTrace();
        }
    }

    /**
     * Vectorize this panel to a specific Vector Graphics output format.
     * <p>
     * NOTE: This is an override implementation, which avoids multiple
     * titles due to there being multiple sub-panels to export.
     *
     * @param graphicsContent
     *            The wrapped Graphics Context to vectorize the content to
     * @return The status of whether this export succeeded or not
     */
    @Override
    public boolean vectorize( final Graphics2D graphicsContent ) {
        // First take care of any title or header graphics common to all panels.
        super.vectorize( graphicsContent );

        // Save the chart as vector graphics.
        //
        // The ratios are screen-dependent, but we should instead make them more
        // rigid in their relationship and aspect ratios.
        //
        // Full-screen mode truncates the grid overlay for the bottom portion of
        // the chart, for some reason, but not the data.
        boolean panelSaved = _polarPlotHz.vectorize( graphicsContent );
        if ( panelSaved ) {
            graphicsContent.translate( _polarPlotHz.getWidth(), 0 );
            panelSaved = _polarPlotVt.vectorize( graphicsContent );
            graphicsContent.translate( -_polarPlotHz.getWidth(), 0 );
        }

        return panelSaved;
    }

    public int getGridSpacing() {
        return _polarPlotHz.getGridSpacing();
    }

    private void initPanel( final int polarResponseViewerWidth,
                            final int polarResponseViewerHeight,
                            final double angleIncrementDegrees )
            throws Exception {
        // Make the individual plots for horizontal and vertical polar patterns.
        _polarPlotHz = new PolarAmplitudeChart( polarResponseViewerWidth,
                                                polarResponseViewerHeight,
                                                "Horizontal", //$NON-NLS-1$
                                                angleIncrementDegrees );
        _polarPlotVt = new PolarAmplitudeChart( polarResponseViewerWidth,
                                                polarResponseViewerHeight,
                                                "Vertical",//$NON-NLS-1$
                                                angleIncrementDegrees );

        // Use the default box layout in the horizontal orientation to get the
        // horizontal and vertical polar response plots to line up next to each
        // other.
        _polarPlotGraphicsPanel = new JPanel();
        _polarPlotGraphicsPanel
                .setLayout( new BoxLayout( _polarPlotGraphicsPanel, BoxLayout.LINE_AXIS ) );
        _polarPlotGraphicsPanel.setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 0 ) );
        _polarPlotGraphicsPanel.add( _polarPlotHz );
        _polarPlotGraphicsPanel.add( _polarPlotVt );

        // Add the main graphics panel to the top of the main panel's container.
        setLayout( new BoxLayout( this, BoxLayout.PAGE_AXIS ) );
        setBorder( BorderFactory.createEmptyBorder( 0, 0, 0, 0 ) );
        add( _polarPlotGraphicsPanel );
    }

    // This method sets the background color, and where appropriate, the
    // foreground color is set to complement it for text-based components.
    @Override
    public void setForegroundFromBackground( final Color backColor ) {
        super.setForegroundFromBackground( backColor );

        // Forward this method to the subcomponents.
        final Color foreColor = ColorUtilities.getForegroundFromBackground( backColor );

        _polarPlotGraphicsPanel.setBackground( backColor );
        _polarPlotGraphicsPanel.setForeground( foreColor );

        _polarPlotHz.setForegroundFromBackground( backColor );
        _polarPlotVt.setForegroundFromBackground( backColor );
    }

    public void setGridRange( final float gridRange ) {
        // Sync up the traces with the new grid range.
        _polarPlotHz.setGridRange( gridRange );
        _polarPlotVt.setGridRange( gridRange );
    }

    public void setGridSpacing( final int gridSpacing ) {
        // Sync up the traces with the new grid spacing.
        _polarPlotHz.setGridSpacing( gridSpacing );
        _polarPlotVt.setGridSpacing( gridSpacing );
    }

    @Override
    public void setRenderingHints( final RenderingHints renderingHints ) {
        // Set the shared component rendering hints.
        super.setRenderingHints( renderingHints );

        // Forward the global rendering hints to all top-level components.
        _polarPlotHz.setRenderingHints( renderingHints );
        _polarPlotVt.setRenderingHints( renderingHints );
    }

    public void updateHorizontalPolarResponse( final double[] amplitude,
                                               final String loudspeakerModel,
                                               final RelativeBandwidth relativeBandwidth,
                                               final double centerFrequency ) {
        _polarPlotHz.updatePolarAmplitudeTrace( amplitude,
                                                loudspeakerModel,
                                                relativeBandwidth,
                                                centerFrequency );
    }

    public void updateVerticalPolarResponse( final double[] amplitude,
                                             final String loudspeakerModel,
                                             final RelativeBandwidth relativeBandwidth,
                                             final double centerFrequency ) {
        _polarPlotVt.updatePolarAmplitudeTrace( amplitude,
                                                loudspeakerModel,
                                                relativeBandwidth,
                                                centerFrequency );
    }

}// class PolarResponsePanel