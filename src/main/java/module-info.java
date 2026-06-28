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
module fxchart {
    exports com.mhschmieder.fxchart;
    exports com.mhschmieder.fxchart.action;
    exports com.mhschmieder.fxchart.chart;
    exports com.mhschmieder.fxchart.concurrent.service;
    exports com.mhschmieder.fxchart.concurrent.task;
    exports com.mhschmieder.fxchart.control;
    exports com.mhschmieder.fxchart.input;
    exports com.mhschmieder.fxchart.layout;
    exports com.mhschmieder.fxchart.net;
    exports com.mhschmieder.fxchart.stage;
    exports com.mhschmieder.fxchart.swing;
    requires commons.math3;
    requires fxcontrols;
    requires fxgraphics;
    requires fxgui;
    requires java.prefs;
    requires javafx.swing;
    requires jchart;
    requires jcommons;
    requires jgraphics;
    requires jmath;
    requires jphysics;
    requires org.apache.commons.io;
    requires org.controlsfx.controls;
}