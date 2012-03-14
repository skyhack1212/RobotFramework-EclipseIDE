/**
 * Copyright 2012 Nitor Creations Oy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nitorcreations.robotframework.eclipseide.editors;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ColorManager {

    protected Map<IRFTColorConstants, Color> fColorTable = new HashMap<IRFTColorConstants, Color>(10);
    private boolean isDarkBackground;

    public void dispose() {
        Iterator<Color> e = fColorTable.values().iterator();
        while (e.hasNext())
            e.next().dispose();
    }

    public Color getColor(IRFTColorConstants irftColor) {
        if (irftColor == null) {
            return null;
        }
        Color color = fColorTable.get(irftColor);
        if (color == null) {
            color = new Color(Display.getCurrent(), irftColor.getColor(isDarkBackground));
            fColorTable.put(irftColor, color);
        }
        return color;
    }

    public void setDarkBackgroundScheme(boolean isDarkBackground) {
        this.isDarkBackground = isDarkBackground;
        dispose();
        fColorTable.clear();
    }
}
