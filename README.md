RTTYTranslator
==============

Android app to send and receive live Baudot RTTY.

I wrote this application as a University project.

It is built from two different pieces of source code - an encoder to convert entered text to RTTY code and a decoder 
to listen to the RTTY tones and translate it into text.

This app is not fully functional. There are some issues:

1) The encoder works, but the RTTY's shift is too wide at 350Hz. 
   The shift can be altered in the Rtty.java class to 170Hz which is standard for Amateur (ham) radio, however doing so
   creates a large amount of audio distortion on the audio output. (Sounds like loud buzzing)
   
2) The decoder is not implemented, however the code is in the repository. It will require an audio buffer implemented
   somewhere to make it work. Also, the decoder works with ASCII RTTY, however there is an unfinished ASCII to Baudot
   conversion class called baudot.java

Feel free to use any of the code.
