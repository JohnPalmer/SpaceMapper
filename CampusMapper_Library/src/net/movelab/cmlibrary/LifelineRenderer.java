/*
 * Campus Mobility is a mobile phone app for studying activity spaces on campuses. It is based in part on code from the Human Mobility Project.
 *
 * Copyright (c) 2015 John R.B. Palmer.
 *
 * Campus Mobility is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Campus Mobility is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see http://www.gnu.org/licenses.
 *
 *
 * The code incorporated from the Human Mobility Project is subject to the following terms:
 *
 * Copyright 2010, 2011 Human Mobility Project
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.movelab.cmlibrary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

/**
 * Renderer for creating lifeline graphics using OpenGL ES 1.0.
 * 
 * @author John R.B. Palmer
 * 
 */

public class LifelineRenderer implements GLSurfaceView.Renderer {

	// private static final String TAG = "LifelineRenderer";
	private FloatBuffer lineVB;
	private FloatBuffer lineVBxax;
	private FloatBuffer lineVByax;
	private FloatBuffer lineVBzax;
	private FloatBuffer triVBzarrowF;
	private FloatBuffer triVBzarrowP;
	private FloatBuffer triVBxarrowE;
	private FloatBuffer triVBxarrowW;
	private FloatBuffer triVByarrowN;
	private FloatBuffer triVByarrowS;

	public volatile int mWidth;
	public volatile int mHeight;

	public volatile boolean showZax;
	public volatile boolean showXYax;
	
	public volatile float mAngle = 0;
	public volatile float mZpos = 0.15f;
	public volatile float currentZoom = 1f;

	public static final int frustumFront = 3;
	public static final int frustumBack = 7;

	public static final float MAXZPOS = 5f;
	public static final float MINZPOS = -5f;

	public static final float MAXZOOM = 1000f;
	public static final float MINZOOM = 0.001f;
	public static final float zoomInc = 1.01f;

	public static final int SCREENSHOT_DONE = 1;
	public static final int SCREENSHOT_SD_ERROR = 2;
	public static final int SCREENSHOT_SD_UNAVAIL = 3;
	public String mFilename = null;

	private Handler handler = null;
	
	private float[] lineCoords;

	public LifelineRenderer(float[] input){
		
		lineCoords = input;
		
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

		// Set the background frame color
		gl.glClearColor(0, 0, 0, 1.0f);

		// Enable use of vertex arrays
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		
		// initialize the triangle vertex array
		initShapes();


	}

	public void onDrawFrame(GL10 gl) {

		float ratio = (float) mWidth / mHeight;
		gl.glMatrixMode(GL10.GL_PROJECTION); // set matrix to projection mode
		gl.glLoadIdentity(); // reset the matrix to its default state
		gl.glFrustumf(-ratio / currentZoom, ratio / currentZoom, -1
				/ currentZoom, 1 / currentZoom, 3,7); // apply
		// the
		// projection
		// matrix

		// Redraw background color
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// Set GL_MODELVIEW transformation mode
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity(); // reset the matrix to its default state
		// When using GL_MODELVIEW, you must set the view point
		GLU.gluLookAt(gl, -3.536f, 0, 3.536f + mZpos, 0f, 0f, 0f + mZpos, 0f,
				0f, 1f);

		// rotation
		// long time = SystemClock.uptimeMillis() % 4000L;
		// / float angle = 0.090f * ((int) time);
		gl.glRotatef(mAngle, 0.0f, 0.0f, 1.0f);

		// Draw the axes
		gl.glColor4f(0f, 0.5f, 1f, 1.0f);

		if (showXYax == true) {
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineVBxax);
			gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, 2);

			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, triVBxarrowE);
			gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);

			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, triVBxarrowW);
			gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);

			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineVByax);
			gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, 2);

			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, triVByarrowN);
			gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);

			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, triVByarrowS);
			gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);

		}

		if (showZax == true) {
			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineVBzax);
			gl.glDrawArrays(GL10.GL_LINE_STRIP, 0, 2);

			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, triVBzarrowF);
			gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);

			gl.glVertexPointer(3, GL10.GL_FLOAT, 0, triVBzarrowP);
			gl.glDrawArrays(GL10.GL_TRIANGLES, 0, 3);

		}

		// Draw the line segments
		gl.glColor4f(1.0f, 1.0f, 0, 1.0f);

		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, lineVB);
		gl.glDrawArrays(GL10.GL_LINE_STRIP, 0,
				lineCoords.length / 3);

		if (handler != null) {
			int screenshotSize = mWidth * mHeight;
			ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
			bb.order(ByteOrder.nativeOrder());
			gl.glReadPixels(0, 0, mWidth, mHeight, GL10.GL_RGBA,
					GL10.GL_UNSIGNED_BYTE, bb);
			int pixelsBuffer[] = new int[screenshotSize];
			bb.asIntBuffer().get(pixelsBuffer);
			bb = null;
			Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight,
					Bitmap.Config.RGB_565);
			bitmap.setPixels(pixelsBuffer, screenshotSize - mWidth, -mWidth, 0,
					0, mWidth, mHeight);
			pixelsBuffer = null;

			short sBuffer[] = new short[screenshotSize];
			ShortBuffer sb = ShortBuffer.wrap(sBuffer);
			bitmap.copyPixelsToBuffer(sb);

			// Making created bitmap (from OpenGL points) compatible with
			// Android bitmap
			for (int i = 0; i < screenshotSize; ++i) {
				short v = sBuffer[i];
				sBuffer[i] = (short) (((v & 0x1f) << 11) | (v & 0x7e0) | ((v & 0xf800) >> 11));
			}
			sb.rewind();
			bitmap.copyPixelsFromBuffer(sb);

			try {
				File root = Environment.getExternalStorageDirectory();
				File directory = new File(root, "SpaceMapper");
				directory.mkdirs();
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"yyyy-MM-dd_HH-mm-ss");
				Date date = new Date();
				String stringDate = dateFormat.format(date);
				String filename = "myLifeline" + stringDate + ".jpg";
				if (directory.canWrite()) {
					File f = new File(directory, filename);
					FileOutputStream out = new FileOutputStream(f);
					Bitmap bmp = bitmap;
					bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.close();

					if (filename != null) {
						mFilename = filename;
						int flag = LifelineRenderer.SCREENSHOT_DONE;
						handler.dispatchMessage(Message.obtain(handler, flag));
					}
				} else {
					int flag = LifelineRenderer.SCREENSHOT_SD_UNAVAIL;
					handler.dispatchMessage(Message.obtain(handler, flag));

				}

			} catch (IOException e) {
				int flag = LifelineRenderer.SCREENSHOT_SD_ERROR;
				handler.dispatchMessage(Message.obtain(handler, flag));

			}

			handler = null;

		}
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {

		gl.glViewport(0, 0, width, height);
		// make adjustments for screen ratio

	}

	public void initShapes() {

		// initialize vertex Buffer
		ByteBuffer vbbL = ByteBuffer.allocateDirect(
		// (# of coordinate values * 4 bytes per float)
				lineCoords.length * 4);
		vbbL.order(ByteOrder.nativeOrder());// use the device hardware's native
											// byte order
		lineVB = vbbL.asFloatBuffer(); // create a floating point buffer from
										// the ByteBuffer
		lineVB.put(lineCoords); // add the coordinates to the
								// FloatBuffer
		lineVB.position(0); // set the buffer to read the first coordinate

		float[] xax = { 1, 0, 0, -1, 0, 0 };

		// initialize vertex Buffer
		ByteBuffer vbbxax = ByteBuffer.allocateDirect(
		// (# of coordinate values * 4 bytes per float)
				xax.length * 4);
		vbbxax.order(ByteOrder.nativeOrder());// use the device hardware's
												// native
												// byte order
		lineVBxax = vbbxax.asFloatBuffer(); // create a floating point buffer
											// from
		// the ByteBuffer
		lineVBxax.put(xax); // add the coordinates to the
							// FloatBuffer
		lineVBxax.position(0); // set the buffer to read the first coordinate

		float[] yax = { 0, 1, 0, 0, -1, 0 };

		// initialize vertex Buffer
		ByteBuffer vbbyax = ByteBuffer.allocateDirect(
		// (# of coordinate values * 4 bytes per float)
				yax.length * 4);
		vbbyax.order(ByteOrder.nativeOrder());// use the device hardware's
												// native
												// byte order
		lineVByax = vbbyax.asFloatBuffer(); // create a floating point buffer
											// from
		// the ByteBuffer
		lineVByax.put(yax); // add the coordinates to the
							// FloatBuffer
		lineVByax.position(0); // set the buffer to read the first coordinate

		float[] zax = { 0, 0, 1.7f, 0, 0, -1.7f };

		// initialize vertex Buffer
		ByteBuffer vbbzax = ByteBuffer.allocateDirect(
		// (# of coordinate values * 4 bytes per float)
				zax.length * 4);
		vbbzax.order(ByteOrder.nativeOrder());// use the device hardware's
												// native
												// byte order
		lineVBzax = vbbzax.asFloatBuffer(); // create a floating point buffer
											// from
		// the ByteBuffer
		lineVBzax.put(zax); // add the coordinates to the
							// FloatBuffer
		lineVBzax.position(0); // set the buffer to read the first coordinate

		float[] zarrowF = { 0, 0, 1.7f, 0, 0.05f, 1.6f, 0, -0.05f, 1.6f };

		// initialize vertex Buffer
		ByteBuffer vbbzarrowF = ByteBuffer.allocateDirect(
		// (# of coordinate values * 4 bytes per float)
				zarrowF.length * 4);
		vbbzarrowF.order(ByteOrder.nativeOrder());// use the device hardware's
													// native
		// byte order
		triVBzarrowF = vbbzarrowF.asFloatBuffer(); // create a floating point
													// buffer from
		// the ByteBuffer
		triVBzarrowF.put(zarrowF); // add the coordinates to the
									// FloatBuffer
		triVBzarrowF.position(0); // set the buffer to read the first coordinate

		float[] zarrowP = { 0, 0, -1.7f, 0, 0.05f, -1.6f, 0, -0.05f, -1.6f };

		// initialize vertex Buffer
		ByteBuffer vbbzarrowP = ByteBuffer.allocateDirect(
		// (# of coordinate values * 4 bytes per float)
				zarrowP.length * 4);
		vbbzarrowP.order(ByteOrder.nativeOrder());// use the device hardware's
													// native
		// byte order
		triVBzarrowP = vbbzarrowP.asFloatBuffer(); // create a floating point
													// buffer from
		// the ByteBuffer
		triVBzarrowP.put(zarrowP); // add the coordinates to the
									// FloatBuffer
		triVBzarrowP.position(0); // set the buffer to read the first coordinate

		float[] xarrowE = { 1, 0, 0, 0.9f, 0.05f, 0f, 0.9f, -0.05f, 0f };

		// initialize vertex Buffer
		ByteBuffer vbbxarrowE = ByteBuffer.allocateDirect(
		// (# of coordinate values * 4 bytes per float)
				xarrowE.length * 4);
		vbbxarrowE.order(ByteOrder.nativeOrder());// use the device hardware's
													// native
		// byte order
		triVBxarrowE = vbbxarrowE.asFloatBuffer(); // create a floating point
													// buffer from
		// the ByteBuffer
		triVBxarrowE.put(xarrowE); // add the coordinates to the
									// FloatBuffer
		triVBxarrowE.position(0); // set the buffer to read the first coordinate

		float[] xarrowW = { -1, 0, 0, -0.9f, 0.05f, 0f, -0.9f, -0.05f, 0f };

		// initialize vertex Buffer
		ByteBuffer vbbxarrowW = ByteBuffer.allocateDirect(
		// (# of coordinate values * 4 bytes per float)
				xarrowW.length * 4);
		vbbxarrowW.order(ByteOrder.nativeOrder());// use the device hardware's
													// native
		// byte order
		triVBxarrowW = vbbxarrowW.asFloatBuffer(); // create a floating point
													// buffer from
		// the ByteBuffer
		triVBxarrowW.put(xarrowW); // add the coordinates to the
									// FloatBuffer
		triVBxarrowW.position(0); // set the buffer to read the first coordinate

		float[] yarrowN = { 0, 1, 0, .05f, 0.9f, 0f, -0.05f, 0.9f, 0f };

		// initialize vertex Buffer
		ByteBuffer vbbyarrowN = ByteBuffer.allocateDirect(
		// (# of coordinate values * 4 bytes per float)
				yarrowN.length * 4);
		vbbyarrowN.order(ByteOrder.nativeOrder());// use the device hardware's
													// native
		// byte order
		triVByarrowN = vbbyarrowN.asFloatBuffer(); // create a floating point
													// buffer from
		// the ByteBuffer
		triVByarrowN.put(yarrowN); // add the coordinates to the
									// FloatBuffer
		triVByarrowN.position(0); // set the buffer to read the first coordinate

		float[] yarrowS = { 0, -1, 0, .05f, -0.9f, 0f, -0.05f, -0.9f, 0f };

		// initialize vertex Buffer
		ByteBuffer vbbyarrowS = ByteBuffer.allocateDirect(
		// (# of coordinate values * 4 bytes per float)
				yarrowS.length * 4);
		vbbyarrowS.order(ByteOrder.nativeOrder());// use the device hardware's
													// native
		// byte order
		triVByarrowS = vbbyarrowS.asFloatBuffer(); // create a floating point
													// buffer from
		// the ByteBuffer
		triVByarrowS.put(yarrowS); // add the coordinates to the
									// FloatBuffer
		triVByarrowS.position(0); // set the buffer to read the first coordinate

	}

	public void startScreenShot(Handler handler) {
		this.handler = handler;
	}

}