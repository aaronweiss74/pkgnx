/*
 * The MIT License (MIT)
 *
 * Copyright (C) 2013 Aaron Weiss
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
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package us.aaronweiss.pkgnx;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import us.aaronweiss.pkgnx.util.Decompressor;
import us.aaronweiss.pkgnx.util.SeekableLittleEndianAccessor;

import java.awt.image.BufferedImage;

/**
 * @author Aaron Weiss
 * @version 1.0
 * @since 6/26/13
 */
public class NXTables {
	private final AudioBuf[] audioBufs;
	private final Bitmap[] bitmaps;
	private final String[] strings;
    private final NXHeader header;
    private final SeekableLittleEndianAccessor slea;

	public NXTables(NXHeader header, SeekableLittleEndianAccessor slea) {
        this.header = header;
        this.slea = slea;
		audioBufs = new AudioBuf[(int) header.getSoundCount()];
		bitmaps = new Bitmap[(int) header.getBitmapCount()];
		strings = new String[(int) header.getStringCount()];
	}

	public ByteBuf getAudioBuf(long index, long length) {
		checkIndex(index);

        AudioBuf ret = audioBufs[(int) index];
        if (ret != null) return ret.getAudioBuf(length);

        try {
            slea.mark();
            slea.seek(header.getSoundOffset() + index * 8);
            return (audioBufs[(int) index] = new AudioBuf(slea)).getAudioBuf(length);
        } finally {
            slea.reset();
        }
	}

	public BufferedImage getImage(long index, int width, int height) {
		checkIndex(index);

        Bitmap ret = bitmaps[(int) index];
        if(ret != null) return ret.getImage(width, height);

        try {
            slea.mark();
            slea.seek(header.getBitmapOffset() + index * 8);
		    return (bitmaps[(int) index] = new Bitmap(slea)).getImage(width, height);
        } finally {
            slea.reset();
        }
	}

	public String getString(long index) {
		checkIndex(index);

        String ret = strings[(int) index];
        if (ret != null) return ret;
        try {
            slea.mark();
            slea.seek(header.getStringOffset() + index * 8);
            slea.seek(slea.getLong());
		    return (strings[(int) index] = slea.getUTFString());
        } finally {
            slea.reset();
        }
	}

	private void checkIndex(long index) {
		if (index > Integer.MAX_VALUE)
			throw new NXException("pkgnx cannot support offset indices over " + Integer.MAX_VALUE);
	}

	/**
	 * A lazy-loaded equivalent of {@code ByteBuf}.
	 *
	 * @author Aaron Weiss
	 * @version 1.0
	 * @since 5/27/13
	 */
	private static class AudioBuf {
		private final SeekableLittleEndianAccessor slea;
		private final long audioOffset;
		private ByteBuf audioBuf;

		/**
		 * Creates a lazy-loaded {@code ByteBuf} for audio.
		 *
		 * @param slea
		 */
		public AudioBuf(SeekableLittleEndianAccessor slea) {
			this.slea = slea;
			audioOffset = slea.getLong();
		}

		/**
		 * Loads a {@code ByteBuf} of the desired {@code length}.
		 *
		 * @param length the length of the audio
		 * @return the audio buffer
		 */
		public ByteBuf getAudioBuf(long length) {
			if (audioBuf == null) {
				slea.seek(audioOffset);
				audioBuf = Unpooled.wrappedBuffer(slea.getBytes((int) length));
			}
			return audioBuf;
		}
	}

	/**
	 * A lazy-loaded equivalent of {@code BufferedImage}.
	 *
	 * @author Aaron Weiss
	 * @version 1.0
	 * @since 5/27/13
	 */
	private static class Bitmap {
		private final SeekableLittleEndianAccessor slea;
		private final long bitmapOffset;

		/**
		 * Creates a lazy-loaded {@code BufferedImage}.
		 *
		 * @param slea
		 */
		public Bitmap(SeekableLittleEndianAccessor slea) {
			this.slea = slea;
			bitmapOffset = slea.getLong();
		}

		/**
		 * Loads a {@code BufferedImage} of the desired {@code width} and {@code height}.
		 *
		 * @param width  the width of the image
		 * @param height the height of the image
		 * @return the loaded image
		 */
		public BufferedImage getImage(int width, int height) {
			slea.seek(bitmapOffset);
			ByteBuf image = Unpooled.wrappedBuffer(Decompressor.decompress(slea.getBytes((int) slea.getUnsignedInt()), width * height * 4));
			BufferedImage ret = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			for (int h = 0; h < height; h++) {
				for (int w = 0; w < width; w++) {
					int b = image.readUnsignedByte();
					int g = image.readUnsignedByte();
					int r = image.readUnsignedByte();
					int a = image.readUnsignedByte();
					ret.setRGB(w, h, (a << 24) | (r << 16) | (g << 8) | b);
				}
			}
			return ret;
		}
	}
}
