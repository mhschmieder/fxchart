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
package com.mhschmieder.fxchart.action;

import com.mhschmieder.fxcontrols.action.ActionFactory;
import com.mhschmieder.fxcontrols.action.LabeledActionFactory;
import com.mhschmieder.fxcontrols.action.XAction;
import com.mhschmieder.fxcontrols.action.XActionGroup;
import com.mhschmieder.jcommons.util.ClientProperties;
import org.controlsfx.control.action.Action;

import java.util.Collection;

/**
 * This is a utility class for making labeled actions for Polar Response.
 */
public class PolarResponseLabeledActionFactory {

    // NOTE: We must substitute "." for resource directory tree delimiters.
    public static final String BUNDLE_NAME = "properties.PolarResponseActionLabels";

    @SuppressWarnings("nls")
    public static final XActionGroup getScaleChoiceGroup( final ClientProperties pClientProperties,
                                                          final PolarResponseAmplitudeScaleChoices polarResponseAmplitudeScaleChoices ) {
        final Collection< Action > scaleChoiceCollection = polarResponseAmplitudeScaleChoices.getScaleChoiceCollection();

        final XActionGroup scaleChoiceGroup = ActionFactory.makeChoiceGroup( pClientProperties,
                                                                             scaleChoiceCollection,
                                                                             BUNDLE_NAME,
                                                                             "scale",
                                                                             null );

        return scaleChoiceGroup;
    }

    @SuppressWarnings("nls")
    public static final XAction getScaleDiv10dbChoice( final ClientProperties pClientProperties ) {
        return getScaleDivChoice( pClientProperties, "div10db" );
    }

    @SuppressWarnings("nls")
    public static final XAction getScaleDiv5dbChoice( final ClientProperties pClientProperties ) {
        return getScaleDivChoice( pClientProperties, "div5db" );
    }

    @SuppressWarnings("nls")
    public static final XAction getScaleDiv6dbChoice( final ClientProperties pClientProperties ) {
        return getScaleDivChoice( pClientProperties, "div6db" );
    }

    @SuppressWarnings("nls")
    private static final XAction getScaleDivChoice( final ClientProperties pClientProperties,
                                                    final String itemName ) {
        return ActionFactory.makeChoice( pClientProperties, BUNDLE_NAME, "scale", itemName, null );
    }

    @SuppressWarnings("nls")
    public static final XActionGroup getTestActionGroup( final ClientProperties pClientProperties,
                                                         final TestActions testActions ) {
        final Collection< Action > testActionCollection = testActions.getTestActionCollection();

        final XActionGroup testActionGroup = ActionFactory
                .makeActionGroup( pClientProperties,
                                  testActionCollection,
                                  LabeledActionFactory.BUNDLE_NAME,
                                  "test",
                                  null );

        return testActionGroup;
    }

    @SuppressWarnings("nls")
    public static final XActionGroup getViewActionGroup( final ClientProperties pClientProperties,
                                                         final PolarResponseViewActions polarResponseViewActions ) {
        final Collection< Action > viewActionCollection = polarResponseViewActions
                .getViewActionCollection( pClientProperties );

        final XActionGroup viewActionGroup = ActionFactory
                .makeActionGroup( pClientProperties,
                                  viewActionCollection,
                                  LabeledActionFactory.BUNDLE_NAME,
                                  "view",
                                  null );

        return viewActionGroup;
    }
}
