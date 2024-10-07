rem using https://github.com/relikd/icnsutil
rem pip3 install icnsutil
rem pip install Pillow
rem https://relikd.github.io/icnsutil/html/viewer.html

rem see messagemanager-commercial\media\03....cmd
rem convert to rgb first 
icnsutil img  ../src/main/resources/icons/conapi_16x16.argb ../src/main/resources/icons/conapi_16x16.png
icnsutil img  ../src/main/resources/icons/conapi_32x32.argb ../src/main/resources/icons/conapi_32x32.png
icnsutil img  ../src/main/resources/icons/conapi_48x48.argb ../src/main/resources/icons/conapi_48x48.png
icnsutil img  ../src/main/resources/icons/conapi_64x64.argb ../src/main/resources/icons/conapi_64x64.png
icnsutil img  ../src/main/resources/icons/conapi_128x128.argb ../src/main/resources/icons/conapi_128x128.png

icnsutil img  ../src/main/resources/icons/conapi_16x16.rgb ../src/main/resources/icons/conapi_16x16.png
icnsutil img  ../src/main/resources/icons/conapi_32x32.rgb ../src/main/resources/icons/conapi_32x32.png
icnsutil img  ../src/main/resources/icons/conapi_48x48.rgb ../src/main/resources/icons/conapi_48x48.png
icnsutil img  ../src/main/resources/icons/conapi_64x64.rgb ../src/main/resources/icons/conapi_64x64.png
icnsutil img  ../src/main/resources/icons/conapi_128x128.rgb ../src/main/resources/icons/conapi_128x128.png

rem icnsutil c -f ../src/main/resources/icons/conapi.icns ../src/main/resources/icons/conapi_16x16.argb ../src/main/resources/icons/conapi_32x32.argb ../src/main/resources/icons/conapi_16x16.rgb ../src/main/resources/icons/conapi_32x32.rgb ../src/main/resources/icons/conapi_48x48.rgb ../src/main/resources/icons/conapi_64x64.rgb ../src/main/resources/icons/conapi_128x128.rgb ../src/main/resources/icons/conapi_16x16.rgb.mask ../src/main/resources/icons/conapi_32x32.rgb.mask ../src/main/resources/icons/conapi_48x48.rgb.mask ../src/main/resources/icons/conapi_64x64.rgb.mask ../src/main/resources/icons/conapi_128x128.rgb.mask

icnsutil c -f ../src/main/resources/icons/conapi.icns  ../src/main/resources/icons/conapi_16x16.rgb ../src/main/resources/icons/conapi_32x32.rgb ../src/main/resources/icons/conapi_16x16.rgb.mask ../src/main/resources/icons/conapi_32x32.rgb.mask ../src/main/resources/icons/conapi_48x48.rgb ../src/main/resources/icons/conapi_48x48.rgb.mask ../src/main/resources/icons/conapi_128x128.rgb ../src/main/resources/icons/conapi_128x128.rgb.mask
rem ../src/main/resources/icons/conapi_48x48.rgb ../src/main/resources/icons/conapi_64x64.rgb ../src/main/resources/icons/conapi_128x128.rgb  ../src/main/resources/icons/conapi_48x48.rgb.mask ../src/main/resources/icons/conapi_64x64.rgb.mask ../src/main/resources/icons/conapi_128x128.rgb.mask

 rem not sure why but below do not work
 rem ../src/main/resources/icons/conapi_48x48.argb
 rem ../src/main/resources/icons/conapi_64x64.argb
 rem ../src/main/resources/icons/conapi_128x128.argb
icnsutil i ../src/main/resources/icons/conapi.icns


pause
cd ../src/main/resources/icons/
copy /Y conapi_128x128.png  messagemanager-icon-large.png
copy /Y conapi_64x64.png  messagemanager-icon-medium.png
copy /Y conapi_32x32.png  messagemanager-icon-small.png
copy /Y conapi_16x16.png  messagemanager-icon-tiny.png
PAUSE