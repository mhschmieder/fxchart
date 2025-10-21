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
 * This file is part of the FxChartT Library
 *
 * You should have received a copy of the MIT License along with the
 * FxChart Library. If not, see <https://opensource.org/licenses/MIT>.
 *
 * Project: https://github.com/mhschmieder/fxchart
 */
package com.mhschmieder.fxchart.action;

import com.mhschmieder.fxchart.chart.GridResolution;
import com.mhschmieder.fxguitoolkit.action.XAction;
import com.mhschmieder.jcommons.util.ClientProperties;
import org.controlsfx.control.action.Action;

import java.util.Arrays;
import java.util.Collection;

/**
 * This is a struct-like container for generic Minor Tick Resolution choices,
 * generally referring to the granularity or resolution of chart minor ticks,
 * from coarse to fine as well as on/off.
 */
public final class MinorTickResolutionChoices {

    // Declare all of the Minor Tick Resolution choices.
    public XAction _minorTicksOffChoice;
    public XAction _minorTicksCoarseChoice;
    public XAction _minorTicksMediumChoice;
    public XAction _minorTicksFineChoice;

    // Default constructor
    public MinorTickResolutionChoices( final ClientProperties clientProperties ) {
        _minorTicksOffChoice = ChartLabeledActionFactory.getMinorTicksOffChoice( clientProperties );
        _minorTicksCoarseChoice = ChartLabeledActionFactory
                .getMinorTicksCoarseChoice( clientProperties );
        _minorTicksMediumChoice = ChartLabeledActionFactory
                .getMinorTicksMediumChoice( clientProperties );
        _minorTicksFineChoice =
                              ChartLabeledActionFactory.getMinorTicksFineChoice( clientProperties );
    }

    public GridResolution getMinorTickResolution() {
        final GridResolution minorTickResolution = _minorTicksOffChoice.isSelected()
            ? GridResolution.OFF
            : _minorTicksCoarseChoice.isSelected()
                ? GridResolution.COARSE
                : _minorTicksMediumChoice.isSelected()
                    ? GridResolution.MEDIUM
                    : _minorTicksFineChoice.isSelected()
                        ? GridResolution.FINE
                        : GridResolution.defaultValue();
        return minorTickResolution;
    }

    public Collection< Action > getMinorTickResolutionChoiceCollection() {
        final Collection< Action > minorTickResolutionChoiceCollection = Arrays
                .asList( _minorTicksOffChoice,
                         _minorTicksCoarseChoice,
                         _minorTicksMediumChoice,
                         _minorTicksFineChoice );
        return minorTickResolutionChoiceCollection;
    }

    public void setMinorTickResolution( final GridResolution minorTickResolution ) {
        switch ( minorTickResolution ) {
        case OFF:
            _minorTicksOffChoice.setSelected( true );
            break;
        case COARSE:
            _minorTicksCoarseChoice.setSelected( true );
            break;
        case MEDIUM:
            _minorTicksMediumChoice.setSelected( true );
            break;
        case FINE:
            _minorTicksFineChoice.setSelected( true );
            break;
        default:
            break;
        }
    }

}
