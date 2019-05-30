## Space Mapper Version 4 Specifications

This file contains specifications for programming and design work being sought for a new version of Space Mapper. If you would like to send a proposal for this work, please list price estimates separately for each item. Please also provide an estimate of necessary time to completetion. Send estimates to john.palmer [at] upf.edu.

### Background and Overview of the Project

Space Mapper is a mobile phone app for Android that collects information about the spaces we move through as we go about our daily activities. At its core, Space Mapper collects location information (GPS and network as well as cell tower IDs) at regular intervals, giving the user control to change these intervals or turn tracking off entirely. Information collected by the app is encrypted with a public key embedded in the app, and sent to a research server. 

Space Mapper went through multiple versions and releases during 2012 and 2013. It was also modified and released as a distinct research app called [Campus Mapper](https://github.com/JohnPalmer/CampusMapper) in 2015. The production branch of that release has now been forked back into this repository as the orphan branch v4, which is now the default branch and the one to which this file is also written. The code in this branch should be the starting point for any work on the app. However, parts of the original code that were never used for Campus Mapper may still be relevant, and can be found in the other branches.

### Desired Work

Bids are requested for the following work:

#### Update code to support latest Android versions

The app should function properly on any mobile phone with any existing Android version. (It does not need to be tailored to tablets or other devices.) This will likely require some modifications, as the background tracking component of the app was written prior to Android 8.

#### Add location-based surveys

The app should be capable of delivering surveys to the user that appear at pre-determined times or when the user has been detected within predetermined bounding boxes. The surveys themselves, and the times or locations at which they will be triggered should be able to be designed on the server side and sent to the app during synchronization with the research server. This functionality has already been umplemented in the [Mosquito Alert](https://github.com/MoveLab/tigatrapp-android/tree/Omatech_v1.50+) app (which originally grew out of Space Mapper), so code may be reused from that project. 

#### Record how much time the participant spends on the phone

There should be a new, optional, module that users can turn on or off freely, and which records the amount of time they spend on the phone in different activities (total screen time; time spent with different apps or with calls) and sends this information to the research server along with the location fixes and survey responses.

#### Switch between masked locations and times and actual locations and times

The self-tracking part of the app should be capable of recording either precise locations and times or masked locations and times. The masking should be done by removing information in a manner similar to the following example (for latitude):
```
double maskedLat = Math.floor(lat / Util.latMask) * Util.latMask;
```
where Util.latMask is a constant that can easily be set within the code. The user should be able to switch back and forth between precise and masked locations within the Settings activity.

#### Minor UX improvement

User experience should be improved through minor changes to the interface, as well as improvements to battery consumption. What we are looking for here is a relatively quick assessment of minor improvements and implementation of these.

#### Gather and send to server information from other sensors available on phone 

During the same time windows when location fixes are being recorded, the app should take measures from the phone's other available [sensors](https://developer.android.com/guide/topics/sensors/sensors_overview), and send measures to the server along with the locations. The code should be set up to make it easy to limit the set of targeted sensors to some arbtrary set.


#### iOS version

Once the Android version is complete, an iOS version should be developed that is largely identical.

