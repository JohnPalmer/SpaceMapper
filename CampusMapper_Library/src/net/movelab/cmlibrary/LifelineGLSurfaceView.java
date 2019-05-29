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

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class LifelineGLSurfaceView extends GLSurfaceView {

	private LifelineRenderer mRenderer;
	private float mPreviousX = 0;
	private float mPreviousY = 0;
	private float mDensity;

	public float TOUCH_SCALE_FACTOR = 180.0f / 320;

	public LifelineGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LifelineGLSurfaceView(Context context) {
		super(context);

	}

	public void setRenderer(LifelineRenderer renderer, Float density) {
		mDensity = density;
		mRenderer = renderer;
		super.setRenderer(renderer);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		// MotionEvent reports input details from the touch screen
		// and other input controls. In this case, you are only
		// interested in events where the touch position changed.

		float x = e.getX();
		float y = e.getY();

		switch (e.getAction()) {
		case MotionEvent.ACTION_MOVE:

			float dx = (x - mPreviousX) * (TOUCH_SCALE_FACTOR / mDensity);
			float dy = (y - mPreviousY) / (200f / mDensity);

			mRenderer.mAngle += dx;

			if ((dy > 0 && mRenderer.mZpos <= LifelineRenderer.MAXZPOS)
					|| (dy < 0 && mRenderer.mZpos >= LifelineRenderer.MINZPOS)) {
				mRenderer.mZpos += dy;
			}

			requestRender();
		}

		mPreviousX = x;
		mPreviousY = y;
		return true;
	}

}
