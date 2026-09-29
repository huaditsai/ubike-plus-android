# UBike+

UBike+ is an Android app for riders of YouBike, Taipei's public bike-sharing system. A student team built it in 2013 and 2014 at National Taipei University of Education (國立臺北教育大學) during my master's program, updating the team's earlier FreeBike app to Google Maps Android API v2.

**Status: archived historical project, no longer maintained.** It has not been built or tested against current Android versions or online services.

## Award

Third place in the Information Technology Application category (Group 8) of the 18th InnoServe Awards (2013), a national ICT innovation competition for college students in Taiwan. This was a team award under faculty supervision, not an individual award.

- Competition: 2013 第18屆全國大專校院資訊應用服務創新競賽
- Award: 資訊技術應用組八 第三名
- Entry: UBike+
- Advisor: 林仁智教授
- Team: 王庭筠、林育陞、蔡華棣 (HuaDi Tsai)、陳怡如、黃俊豪

Sources:

- NTUE meeting materials (June 2014), department honor roll on the page labelled -432-: https://secretariatntue.ntue.edu.tw/var/file/43/1043/img/271/837143212.pdf
- Preserved copy of that page, in case the original link stops working: https://github.com/huaditsai/ubike-plus-android/blob/main/docs/ntue-honor-roll-2014-page-432.pdf
- Project post (Traditional Chinese, 2014): https://dotblogs.com.tw/huadi73/2014/05/16/145146

The preserved page comes from a document published by National Taipei University of Education, which retains its rights. It is kept only as a reference for this award; the other entries on the page are unrelated to this project.

## Features

- Nearby stations: YouBike stations within 1 km, with available bikes and open docks.
- Station lookup covering every station in the network.
- Ride timer: reminders at 25 and 30 minutes as the free ride period runs out, then average speed, distance, and calories burned.
- Rider profile (height and weight) for the calorie estimate, and sharing ride results on Facebook.

## Source and history

- Imported on 2026-09-29 from the Team Foundation Version Control (TFVC) project `$/UBikePlus` on `huadi.visualstudio.com`. Each TFVC changeset is one git commit with its original author, date, and comment.
- Cleaned before publication: the Google Maps API key is replaced with `YOUR_GOOGLE_MAPS_API_KEY`, compiled build output (`bin/`) is removed from every commit, and a teammate's email address is replaced with a placeholder. Names are kept.
- Earlier app by the same team: https://github.com/huaditsai/freebike-android

## License

No open-source license was found. Do not assume the repository grants any license; third-party libraries keep their own licenses.
