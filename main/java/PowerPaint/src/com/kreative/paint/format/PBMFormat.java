/*
 * Copyright &copy; 2009-2011 Rebecca G. Bettencourt / Kreative Software
 * <p>
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * <a href="http://www.mozilla.org/MPL/">http://www.mozilla.org/MPL/</a>
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Alternatively, the contents of this file may be used under the terms
 * of the GNU Lesser General Public License (the "LGPL License"), in which
 * case the provisions of LGPL License are applicable instead of those
 * above. If you wish to allow use of your version of this file only
 * under the terms of the LGPL License and not to allow others to use
 * your version of this file under the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and
 * other provisions required by the LGPL License. If you do not delete
 * the provisions above, a recipient may use your version of this file
 * under either the MPL or the LGPL License.
 * @since PowerPaint 1.0
 * @author Rebecca G. Bettencourt, Kreative Software
 */

package com.kreative.paint.format;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.kreative.paint.Canvas;
import com.kreative.paint.form.Form;
import com.kreative.paint.io.Monitor;

public class PBMFormat implements Format {
	public String getName() { return "PBM"; }
	public String getExpandedName() { return "Portable Bitmap"; }
	public String getExtension() { return "pbm"; }
	public int getMacFileType() { return 0x50424D20; }
	public int getMacResourceType() { return 0x50424D20; }
	public long getDFFType() { return 0x496D61676550424DL; }
	
	public MediaType getMediaType() { return MediaType.IMAGE; }
	public GraphicType getGraphicType() { return GraphicType.BITMAP; }
	public SizeType getSizeType() { return SizeType.ARBITRARY; }
	public ColorType getColorType() { return ColorType.BLACK_AND_WHITE; }
	public AlphaType getAlphaType() { return AlphaType.OPAQUE; }
	public LayerType getLayerType() { return LayerType.FLAT; }
	
	public boolean onlyUponRequest() { return false; }
	public int usesMagic() { return 3; }
	public boolean acceptsMagic(byte[] start, long length) {
		try {
			DataInputStream in = new DataInputStream(new ByteArrayInputStream(start));
			short magic = in.readShort();
			if (!(magic == (short)0x5031 || magic == (short)0x5034)) return false;
			if (!isSpace(in.readByte())) return false;
			in.close();
			return true;
		} catch (IOException ioe) {
			return false;
		}
	}
	public boolean acceptsExtension(String ext) { return ext.equalsIgnoreCase("pbm"); }
	public boolean acceptsMacFileType(int type) { return type == 0x50424D20 || type == 0x50424D66 || type == 0x50424D6D; }
	public boolean acceptsMacResourceType(int type) { return type == 0x50424D20 || type == 0x50424D66 || type == 0x50424D6D; }
	public boolean acceptsDFFType(long type) { return type == 0x496D61676550424DL || type == 0x496D672050424D20L; }
	
	public boolean supportsRead() { return true; }
	public boolean usesReadOptionForm() { return false; }
	public Form getReadOptionForm() { return null; }
	public Canvas read(DataInputStream in, Monitor m) throws IOException {
		short magic = in.readShort();
		if (magic == (short)0x5031) {
			int w = readValue(in);
			int h = readValue(in);
			int[] pixels = new int[w*h];
			for (int i = 0; i < pixels.length; i++) {
				int p = readValue(in);
				if (p != 0) pixels[i] = 0xFF000000;
				else pixels[i] = 0xFFFFFFFF;
			}
			Canvas c = new Canvas(w, h);
			c.get(0).setRGB(0, 0, w, h, pixels, 0, w);
			return c;
		} else if (magic == (short)0x5034) {
			int w = readValue(in);
			int h = readValue(in);
			int bpr = w; if ((bpr & 7) != 0) bpr = (bpr | 7) + 1;
			int[] pixels = new int[bpr*h];
			for (int i = 0; i < pixels.length; i += 8) {
				byte b = in.readByte();
				for (int j = 0; j < 8; j++) {
					if ((b & 0x80) != 0) pixels[i+j] = 0xFF000000;
					else pixels[i+j] = 0xFFFFFFFF;
					b <<= 1;
				}
			}
			Canvas c = new Canvas(w, h);
			c.get(0).setRGB(0, 0, w, h, pixels, 0, bpr);
			return c;
		} else {
			throw new NotThisFormatException();
		}
	}
	
	public boolean supportsWrite() { return true; }
	public boolean usesWriteOptionForm() { return false; }
	public Form getWriteOptionForm() { return null; }
	public int approximateFileSize(Canvas c) {
		return c.getWidth()*c.getHeight()/8;
	}
	public void write(Canvas c, DataOutputStream out, Monitor m) throws IOException {
		int w = c.getWidth();
		int h = c.getHeight();
		int bpr = w; if ((bpr & 7) != 0) bpr = (bpr | 7) + 1;
		BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		c.paint(g);
		g.dispose();
		int[] pixels = new int[bpr*h];
		bi.getRGB(0, 0, w, h, pixels, 0, bpr);
		out.writeShort(0x5034);
		out.writeByte(0x0A);
		out.writeBytes(Integer.toString(w));
		out.writeByte(0x20);
		out.writeBytes(Integer.toString(h));
		out.writeByte(0x0A);
		for (int i = 0; i < pixels.length; i += 8) {
			byte b = 0;
			for (int j = 0; j < 8; j++) {
				b <<= 1;
				if (isOpaque(pixels[i+j]) && isBlack(pixels[i+j])) b |= 1;
			}
			out.writeByte(b);
		}
	}
	
	private static boolean isSpace(byte b) {
		return (((b >= (byte)0x09) && (b <= (byte)0x0D)) || (b == (byte)0x20));
	}
	
	private static boolean isLineBreak(byte b) {
		return ((b == (byte)0x0A) || (b == (byte)0x0D));
	}

	private static boolean isBlack(int pixel) {
		int r = ((pixel >>> 16) & 0xFF);
		int g = ((pixel >>>  8) & 0xFF);
		int b = ((pixel >>>  0) & 0xFF);
		int k = (30*r + 59*g + 11*b) / 100;
		return (k < 0x80);
	}
	
	private static boolean isOpaque(int pixel) {
		int a = ((pixel >>> 24) & 0xFF);
		return (a >= 0x80);
	}
	
	private static int readValue(DataInputStream in) throws IOException {
		boolean inComment = false;
		boolean inValue = false;
		int value = 0;
		while (true) {
			byte b = in.readByte();
			if (inComment) {
				if (isLineBreak(b)) inComment = false;
			}
			else if (b == (byte)'#') {
				inComment = true;
			}
			else if ((b >= (byte)'0') && (b <= (byte)'9')) {
				inValue = true;
				value = value * 10 + (int)(b-'0');
			}
			else if (isSpace(b)) {
				if (inValue) return value;
			}
		}
	}
}
