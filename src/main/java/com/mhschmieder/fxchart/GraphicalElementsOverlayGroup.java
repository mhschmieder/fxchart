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
 * This file is part of the FxChart Library
 *
 * You should have received a copy of the MIT License along with the
 * FxChart Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxchart
 */
package com.mhschmieder.fxchart;

import com.mhschmieder.fxgraphics.shape.ShapeGroup;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;

/**
 * This is a Group container for all the Graphical Elements that will display
 * over a Cartesian Chart background. Usually these are the shapes representing
 * interactive application domain objects.
 * <p>
 * It is important to manage any connection the associated shapes (generally
 * added as ShapeGroup wrappers) have with application domain objects. This is
 * a Group container, so it is only appropriate (and scalable) to have it store
 * the generated graphics for such objects. The application must maintain the
 * mouse interaction, contextual scaling updates, etc. This is just a container
 * and is here for convenience so that such graphics can be handled similarly to
 * other overlay groups, such as generated images from visualization calls.
 */
public class GraphicalElementsOverlayGroup extends ChartContentGroup {

    /**
     * This is the full constructor, when all parameters are known.
     */
    public GraphicalElementsOverlayGroup() {
        // Always call the superclass constructor first!
        super();

        try {
            initialize();
        }
        catch ( final Exception e ) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the children of the Graphical Elements Overlay Group.
     *
     * @return The children of the Graphical Elements Overlay Group.
     */
    public final ObservableList< Node > getGraphicalElements() {
        return getChildren();
    }

    /**
     * Set up the configuration of the Graphical Elements.
     */
    private final void initialize() {
        // Mark Graphical Elements as unmanaged as the overall node group's
        // preferred size changes should not affect our layout.
        setManaged( false );

        // Do not auto-size children, as we are managing the nodes ourselves.
        setAutoSizeChildren( false );
    }

    public final void setForeground( final Color foreColor ) {
        // Make sure the Graphical Elements are all visible against the new
        // Background Color, but only change Black and White vs. other Colors.
        getGraphicalElements().forEach( node -> {
            // First, we have to de-encapsulate the Graphical Nodes.
            if ( node instanceof ShapeGroup ) {
                // Delegate the real work to the various Graphical Node types.
                ( ( ShapeGroup ) node ).setForeground( foreColor, false );
            }
        } );
    }
}
