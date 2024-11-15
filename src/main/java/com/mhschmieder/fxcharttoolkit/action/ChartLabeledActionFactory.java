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
package com.mhschmieder.fxcharttoolkit.action;

import java.util.Collection;

import org.controlsfx.control.action.Action;

import com.mhschmieder.commonstoolkit.util.ClientProperties;
import com.mhschmieder.fxguitoolkit.action.ActionFactory;
import com.mhschmieder.fxguitoolkit.action.XAction;
import com.mhschmieder.fxguitoolkit.action.XActionGroup;

/**
 * This is a utility class for making labeled actions for charts.
 * <p>
 * TODO: Move the icons into the resources folder and reference correctly.
 */
public class ChartLabeledActionFactory {

    // NOTE: We must substitute "." for resource directory tree delimiters.
    @SuppressWarnings("nls") public static final String BUNDLE_NAME =
                                                                    "properties.ChartActionLabels";

    /**
     * The default constructor is disabled, as this is a static factory class.
     */
    private ChartLabeledActionFactory() {}

    @SuppressWarnings("nls")
    public static XActionGroup getMinorTickResolutionChoiceGroup( final ClientProperties clientProperties,
                                                                  final MinorTickResolutionChoices minorTickResolutionChoices ) {
        final Collection< Action > minorTickResolutionChoiceCollection = minorTickResolutionChoices
                .getMinorTickResolutionChoiceCollection();

        final XActionGroup minorTickResolutionChoiceGroup = ActionFactory
                .makeChoiceGroup( clientProperties,
                                  minorTickResolutionChoiceCollection,
                                  BUNDLE_NAME,
                                  "minorTickResolution",
                                  "/icons/fatCow/HorizontalRuler16.png" );

        return minorTickResolutionChoiceGroup;
    }

    @SuppressWarnings("nls")
    public static XAction getMinorTicksOffChoice( final ClientProperties clientProperties ) {
        return ActionFactory
                .makeChoice( clientProperties, BUNDLE_NAME, "minorTickResolution", "off", null );
    }

    @SuppressWarnings("nls")
    public static XAction getMinorTicksCoarseChoice( final ClientProperties clientProperties ) {
        return ActionFactory
                .makeChoice( clientProperties, BUNDLE_NAME, "minorTickResolution", "coarse", null );
    }

    @SuppressWarnings("nls")
    public static XAction getMinorTicksMediumChoice( final ClientProperties clientProperties ) {
        return ActionFactory
                .makeChoice( clientProperties, BUNDLE_NAME, "minorTickResolution", "medium", null );
    }

    @SuppressWarnings("nls")
    public static XAction getMinorTicksFineChoice( final ClientProperties clientProperties ) {
        return ActionFactory
                .makeChoice( clientProperties, BUNDLE_NAME, "minorTickResolution", "fine", null );
    }

    @SuppressWarnings("nls")
    public static XAction getLegendAlignmentChoice( final ClientProperties clientProperties,
                                                    final String itemName ) {
        return ActionFactory.makeChoice( clientProperties,
                                         BUNDLE_NAME,
                                         "legendAlignment",
                                         itemName,
                                         null,
                                         true );
    }

    @SuppressWarnings("nls")
    public static XAction getLegendAlignmentLeftChoice( final ClientProperties clientProperties ) {
        return getLegendAlignmentChoice( clientProperties, "left" );
    }

    @SuppressWarnings("nls")
    public static XAction getLegendAlignmentRightChoice( final ClientProperties clientProperties ) {
        return getLegendAlignmentChoice( clientProperties, "right" );
    }

    @SuppressWarnings("nls")
    public static XAction getLegendAlignmentAboveChoice( final ClientProperties clientProperties ) {
        return getLegendAlignmentChoice( clientProperties, "above" );
    }

    @SuppressWarnings("nls")
    public static XAction getLegendAlignmentBelowChoice( final ClientProperties clientProperties ) {
        return getLegendAlignmentChoice( clientProperties, "below" );
    }

    @SuppressWarnings("nls")
    public static XAction getLegendAlignmentBetweenChoice( final ClientProperties clientProperties ) {
        return getLegendAlignmentChoice( clientProperties, "between" );
    }

    @SuppressWarnings("nls")
    public static XActionGroup getGridResolutionChoiceGroup( final ClientProperties clientProperties,
                                                             final GridResolutionChoices gridResolutionChoices ) {
        final Collection< Action > gridResolutionChoiceCollection = gridResolutionChoices
                .getGridResolutionChoiceCollection();

        final XActionGroup gridResolutionChoiceGroup = ActionFactory
                .makeChoiceGroup( clientProperties,
                                  gridResolutionChoiceCollection,
                                  BUNDLE_NAME,
                                  "gridResolution",
                                  "/icons/led24/Grid16.png" );

        return gridResolutionChoiceGroup;
    }

    @SuppressWarnings("nls")
    public static XAction getGridCoarseChoice( final ClientProperties clientProperties ) {
        return ActionFactory
                .makeChoice( clientProperties, BUNDLE_NAME, "gridResolution", "coarse", null );
    }

    @SuppressWarnings("nls")
    public static XAction getGridOffChoice( final ClientProperties clientProperties ) {
        return ActionFactory
                .makeChoice( clientProperties, BUNDLE_NAME, "gridResolution", "off", null );
    }

    @SuppressWarnings("nls")
    public static XAction getGridMediumChoice( final ClientProperties clientProperties ) {
        return ActionFactory
                .makeChoice( clientProperties, BUNDLE_NAME, "gridResolution", "medium", null );
    }

    @SuppressWarnings("nls")
    public static XAction getGridFineChoice( final ClientProperties clientProperties ) {
        return ActionFactory
                .makeChoice( clientProperties, BUNDLE_NAME, "gridResolution", "fine", null );
    }

    @SuppressWarnings("nls")
    public static XAction getGridColorAction( final ClientProperties clientProperties ) {
        return ActionFactory.makeAction( clientProperties,
                                         BUNDLE_NAME,
                                         "settings",
                                         "gridColor",
                                         "/icons/ahaSoft/Grid16.png" );
    }

    @SuppressWarnings("nls")
    public static XAction getDataTrackerColorAction( final ClientProperties clientProperties ) {
        return ActionFactory.makeAction( clientProperties,
                                         BUNDLE_NAME,
                                         "settings",
                                         "dataTrackerColor",
                                         "/icons/yusukeKamiyamane/fugue/ColorSwatch16.png" );
    }
}
