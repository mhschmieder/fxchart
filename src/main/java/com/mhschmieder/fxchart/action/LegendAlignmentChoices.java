/*
 * MIT License
 *
 * Copyright (c) 2020, 2025 Mark Schmieder
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
 * This file is part of the FxJPhysics Library
 *
 * You should have received a copy of the MIT License along with the
 * FxJPhysics Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxjphysics
 */
package com.mhschmieder.fxchart.action;

import com.mhschmieder.fxchart.chart.LegendAlignment;
import com.mhschmieder.fxguitoolkit.action.ActionFactory;
import com.mhschmieder.fxguitoolkit.action.XAction;
import com.mhschmieder.fxguitoolkit.action.XActionGroup;
import com.mhschmieder.jcommons.util.ClientProperties;
import org.controlsfx.control.action.Action;

import java.util.Arrays;
import java.util.Collection;

/**
 * This is a struct-like container for generic Legend Alignment choices.
 */
public final class LegendAlignmentChoices {

    // Declare all of the Legend Alignment choices.
    public XAction      _legendAlignmentBelowChoice;
    public XAction      _legendAlignmentAboveChoice;
    public XAction      _legendAlignmentBetweenChoice;
    public XAction      _legendAlignmentLeftChoice;
    public XAction      _legendAlignmentRightChoice;

    // Cache the associated choice group, for ease of overall enablement.
    public XActionGroup _legendAlignmentChoiceGroup;

    // Default constructor
    @SuppressWarnings("nls")
    public LegendAlignmentChoices( final ClientProperties clientProperties ) {
        _legendAlignmentBelowChoice = ChartLabeledActionFactory
                .getLegendAlignmentBelowChoice( clientProperties );
        _legendAlignmentAboveChoice = ChartLabeledActionFactory
                .getLegendAlignmentAboveChoice( clientProperties );
        _legendAlignmentBetweenChoice = ChartLabeledActionFactory
                .getLegendAlignmentBetweenChoice( clientProperties );
        _legendAlignmentLeftChoice = ChartLabeledActionFactory
                .getLegendAlignmentLeftChoice( clientProperties );
        _legendAlignmentRightChoice = ChartLabeledActionFactory
                .getLegendAlignmentRightChoice( clientProperties );

        // Get the collection pertinent to shared vs. individual legend context.
        final Collection< Action > legendAlignmentChoiceCollection = Arrays
                .asList( _legendAlignmentBelowChoice,
                         _legendAlignmentAboveChoice,
                         _legendAlignmentBetweenChoice,
                         _legendAlignmentLeftChoice,
                         _legendAlignmentRightChoice );

        _legendAlignmentChoiceGroup = ActionFactory
                .makeChoiceGroup( clientProperties,
                                  legendAlignmentChoiceCollection,
                                  ChartLabeledActionFactory.BUNDLE_NAME,
                                  "legendAlignment",
                                  null,
                                  true );
    }

    public LegendAlignment getLegendAlignment() {
        if ( _legendAlignmentBelowChoice.isSelected() ) {
            return LegendAlignment.BELOW;
        }
        else if ( _legendAlignmentAboveChoice.isSelected() ) {
            return LegendAlignment.ABOVE;
        }
        else if ( _legendAlignmentBetweenChoice.isSelected() ) {
            return LegendAlignment.BETWEEN;
        }
        else if ( _legendAlignmentLeftChoice.isSelected() ) {
            return LegendAlignment.LEFT;
        }
        else if ( _legendAlignmentRightChoice.isSelected() ) {
            return LegendAlignment.RIGHT;
        }
        else {
            return LegendAlignment.defaultValue();
        }
    }

    public XActionGroup getLegendAlignmentChoiceGroup() {
        return _legendAlignmentChoiceGroup;
    }

    public void setDisabled( final boolean disabled ) {
        _legendAlignmentChoiceGroup.setDisabled( disabled );
    }

    public void setLegendAlignment( final LegendAlignment legendAlignment ) {
        // Sync up the Radio Button Menu Items with the current Legend
        // Alignment.
        switch ( legendAlignment ) {
        case BELOW:
            _legendAlignmentBelowChoice.setSelected( true );
            break;
        case ABOVE:
            _legendAlignmentAboveChoice.setSelected( true );
            break;
        case BETWEEN:
            _legendAlignmentBetweenChoice.setSelected( true );
            break;
        case LEFT:
            _legendAlignmentLeftChoice.setSelected( true );
            break;
        case RIGHT:
            _legendAlignmentRightChoice.setSelected( true );
            break;
        default:
            break;
        }
    }

}
