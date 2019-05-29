# **Space Mapper**

Space Mapper is a mobile phone app that collects information about the spaces we move through as we go about our daily activities. It was used in 2012-2013 by researchers at Princeton University and the Center for Advanced Studies of Blanes (CEAB-CSIC) to study human mobility and social inequality. The study is now over but the app is still available for anyone to use for their own self-tracking.

*Features:*
* Allows you to store, map, and analyze your own movement patterns and to view them in three dimensional spacetime.
* "Refresh GPS" button improves location performance for all of your mapping apps.
* Minimally intrusive and designed to conserve battery power.
* Turn it off and on whenever you want.
* Free, open source software. 
* More on-phone data visualization and analysis tools coming soon.
* Application available in English, Spanish and Catalan.

For more information, please visit the project [website](<http://activityspaceproject.com>) or its [Google Play page]( http://play.google.com/store/apps/details?id=edu.princeton.jrpalmer.asm).

This repository contains the source code for the most recent release of Space Mapper as well as the license agreements to which it is subject (see the res/raw folder). The only differences between this code and that used to compile the Space Mapper binary that can be downloaded from Google Play are as follows:

1. The author's Google Maps API keys have been removed from the map_layout.xml file.

2. The author's public key has been removed from the res/raw folder.

3. The author's server address has been removed from the Util.java file.

Space Mapper is part of an academic study on the spaces people move 
through as they go about their daily activities. It was written by John R.B.Palmer based in part on code from the Human Mobility Project, written by Chang Y. Chung, Kathleen Li, and Necati E. Ozgencil and from Funf, written by Nadav Aharony, Wei Pan, and Alex Pentland, and from range-seek-bar, written by Stephan Tittel, Peter Sinnott, and Thomas Barrasso.

Previous versions of the Space Mapper source code are available at <http://activityspaceproject.com>.

Copyright 2012, 2013 John R.B. Palmer
 
Space Mapper is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or  (at your option) any later version.

Space Mapper is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses.

The code incorporated from the Human Mobility Project is subject to the following terms:

		Copyright 2010, 2011 Human Mobility Project

		Permission is hereby granted, free of charge, to any person obtaining
		a copy of this software and associated documentation files (the
		"Software"), to deal in the Software without restriction, including
		without limitation the rights to use, copy, modify, merge, publish,
		distribute, sublicense, and/or sell copies of the Software, and to
		permit persons to whom the Software is furnished to do so, subject to
		the following conditions:

		The above copyright notice and this permission notice shall be included
		in all copies or substantial portions of the Software.

		THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
		EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
		MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
		IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
		CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
		TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
		SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

The code incorporated from Funf is subject to the following terms:

  		Funf: Open Sensing Framework<br/>
 		Copyright 2010-2011 Nadav Aharony, Wei Pan, Alex Pentland.<br/> 
  		Acknowledgments: Alan Gardner<br/>
  		Contact: nadav@media.mit.edu
  
  		Funf is free software: you can redistribute it and/or modify
  		it under the terms of the GNU Lesser General Public License as 
  		published by the Free Software Foundation, either version 3 of 
  		the License, or (at your option) any later version. 
  
 		Funf is distributed in the hope that it will be useful, but 
  		WITHOUT ANY WARRANTY; without even the implied warranty of 
  		MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
  		See the GNU Lesser General Public License for more details.
  

The code incorporated from range-seek-bar is subject to the following terms:

	   Copyright 2013 Stephan Tittel, Peter Sinnott, and Thomas Barrasso

	   Licensed under the Apache License, Version 2.0 (the "License");
	   you may not use this file except in compliance with the License.
	   You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	   Unless required by applicable law or agreed to in writing, software
	   distributed under the License is distributed on an "AS IS" BASIS,
	   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	   
	   See the License for the specific language governing permissions and
	   limitations under the License. 

