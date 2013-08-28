/* -*-mode:java; c-basic-offset:2; -*- */
/* JCTerm
 * Copyright (C) 2002-2004 ymnk, JCraft,Inc.
 *  
 * Written by: 2002 ymnk<ymnk@jcaft.com>
 *   
 *   
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.
 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 * 
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package org.jetbrains.plugins.terminal;

import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.util.JBHiDPIScaledImage;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.UIUtil;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.display.BackBuffer;
import com.jediterm.terminal.display.StyleState;
import com.jediterm.terminal.ui.SystemSettingsProvider;
import com.jediterm.terminal.ui.TerminalPanel;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.util.List;

public class JBTerminalPanel extends TerminalPanel {
  private final EditorColorsScheme myColorScheme;

  public JBTerminalPanel(@NotNull SystemSettingsProvider settingsProvider,
                         @NotNull BackBuffer backBuffer,
                         @NotNull StyleState styleState,
                         @NotNull EditorColorsScheme scheme) {
    super(settingsProvider, backBuffer, styleState);
    myColorScheme = scheme;


    styleState.setDefaultStyle(new TextStyle(TerminalColor.awt(myColorScheme.getDefaultForeground()), TerminalColor.awt(
      myColorScheme.getDefaultBackground())));

    setSelectionColor(new TextStyle(TerminalColor.awt(myColorScheme.getColor(EditorColors.SELECTION_FOREGROUND_COLOR)),
                                    TerminalColor.awt(myColorScheme.getColor(EditorColors.SELECTION_BACKGROUND_COLOR))));

    setLineSpace(myColorScheme.getConsoleLineSpacing());
    
    JBTabbedTerminalWidget.convertActions(this, getActions());
  }

  protected Font createFont() {

    Font normalFont = Font.decode(getFontName());

    if (normalFont == null) {
      normalFont = super.createFont();
    }

    normalFont = normalFont.deriveFont((float)myColorScheme.getConsoleFontSize());

    return normalFont;
  }

  protected void setupAntialiasing(Graphics graphics, boolean antialiasing) {
    GraphicsUtil.setupAntialiasing(graphics, antialiasing, false);
  }

  @Override
  public Color getBackground() {
    if (myColorScheme != null) {
      return myColorScheme.getDefaultBackground();
    }
    return super.getBackground();
  }

  @Override
  public Color getForeground() {
    if (myColorScheme != null) {
      return myColorScheme.getDefaultForeground();
    }
    return super.getBackground();
  }

  @Override
  protected void setCopyContents(StringSelection selection) {
    CopyPasteManager.getInstance().setContents(selection);
  }

  @Override
  protected void drawImage(Graphics2D gfx, BufferedImage image, int x, int y, ImageObserver observer) {
    UIUtil.drawImage(gfx, image, x, y, observer);
  }

  @Override
  protected void drawImage(Graphics2D g, BufferedImage image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2) {
    drawImage(g, image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
  }

  public static void drawImage(Graphics g, Image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer) {
    if (image instanceof JBHiDPIScaledImage) {
      final Graphics2D newG = (Graphics2D)g.create(0, 0, image.getWidth(observer), image.getHeight(observer));
      newG.scale(0.5, 0.5);
      Image img = ((JBHiDPIScaledImage)image).getDelegate();
      if (img == null) {
        img = image;
      }
      newG.drawImage(img, 2*dx1, 2*dy1, 2*dx2, 2*dy2, sx1, sy1, sx2, sy2, observer);
      newG.scale(1, 1);
      newG.dispose();
    } else {
      g.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer);
    }
  }

  @Override
  protected boolean isRetina() {
    return UIUtil.isRetina();
  }

  @Override
  protected String getClipboardContent() throws IOException, UnsupportedFlavorException {
    Transferable contents = CopyPasteManager.getInstance().getContents();
    if (contents == null) {
      return null;
    }
    return (String)contents.getTransferData(DataFlavor.stringFlavor);
  }

  @Override
  protected BufferedImage createBufferedImage(int width, int height) {
    return UIUtil.createImage(width, height, BufferedImage.TYPE_INT_ARGB);
  }
  
  

  public String getFontName() {
    List<String> fonts = myColorScheme.getConsoleFontPreferences().getEffectiveFontFamilies();

    for (String font : fonts) {
      if (isApplicable(font)) {
        return font;
      }
    }
    return "Monospaced-14";
  }

  private static boolean isApplicable(String font) {
    if ("Source Code Pro".equals(font)) {
      return false;
    }
    return true;
  }
}

