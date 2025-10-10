/**
 * MIT License
 *
 * Copyright (c) 2020, 2022 Mark Schmieder
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

import com.mhschmieder.commonstoolkit.util.ClientProperties;
import com.mhschmieder.fxcharttoolkit.chart.GridResolution;
import com.mhschmieder.fxguitoolkit.action.XAction;
import org.controlsfx.control.action.Action;

import java.util.Arrays;
import java.util.Collection;

/**
 * This is a struct-like container for generic Grid Resolution choices,
 * generally referring to the granularity or resolution of chart grid lines,
 * from coarse to fine as well as on/off.
 */
public final class GridResolutionChoices {

    // Declare all of the Grid Resolution choices.
    public XAction _gridOffChoice;
    public XAction _gridCoarseChoice;
    public XAction _gridMediumChoice;
    public XAction _gridFineChoice;

    // Default constructor
    public GridResolutionChoices( final ClientProperties clientProperties ) {
        _gridOffChoice = ChartLabeledActionFactory.getGridOffChoice( clientProperties );
        _gridCoarseChoice = ChartLabeledActionFactory.getGridCoarseChoice( clientProperties );
        _gridMediumChoice = ChartLabeledActionFactory.getGridMediumChoice( clientProperties );
        _gridFineChoice = ChartLabeledActionFactory.getGridFineChoice( clientProperties );
    }

    public GridResolution getGridResolution() {
        final GridResolution gridResolution = _gridOffChoice.isSelected()
            ? GridResolution.OFF
            : _gridCoarseChoice.isSelected()
                ? GridResolution.COARSE
                : _gridMediumChoice.isSelected()
                    ? GridResolution.MEDIUM
                    : _gridFineChoice.isSelected()
                        ? GridResolution.FINE
                        : GridResolution.defaultValue();
        return gridResolution;
    }

    public Collection< Action > getGridResolutionChoiceCollection() {
        final Collection< Action > gridResolutionChoiceCollection = Arrays
                .asList( _gridOffChoice, _gridCoarseChoice, _gridMediumChoice, _gridFineChoice );
        return gridResolutionChoiceCollection;
    }

    public void setGridResolution( final GridResolution gridResolution ) {
        switch ( gridResolution ) {
        case OFF:
            _gridOffChoice.setSelected( true );
            break;
        case COARSE:
            _gridCoarseChoice.setSelected( true );
            break;
        case MEDIUM:
            _gridMediumChoice.setSelected( true );
            break;
        case FINE:
            _gridFineChoice.setSelected( true );
            break;
        default:
            break;
        }
    }

}
