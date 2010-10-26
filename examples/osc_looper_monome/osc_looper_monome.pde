/*

    OSC looper library for Processing.
    Copyright (c) 2010 held jointly by the individual authors.

    This file is part of OSC looper library for Processing.

    OSC looper library for Processing is free software: you can redistribute it and/or
    modify it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OSC looper library for Processing is distributed in the hope that it will be
    useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OSC looper library for Processing.  If not, see
    <http://www.gnu.org/licenses/>.

*/

import oscP5.*;
import osclooper.*;

OscP5 oscP5;
OscLooper looper;

String host = "127.0.0.1";
int sendPort = 9092;
int receivePort = 9090;

void setup()
{
  OscProperties properties = new OscProperties();
  properties.setListeningPort(receivePort);
  properties.setRemoteAddress(host, sendPort);
  oscP5 = new OscP5(this, properties);
  looper = new OscLooper(oscP5);
}

void draw()
{
  // empty
}

void oscEvent(OscMessage message)
{
  if ("/40h/press".equals(message.addrPattern()))
  {
    int x = message.get(0).intValue();
    int y = message.get(1).intValue();
    int value = message.get(2).intValue();
    if ((x == 15) && (y == 15) && (value == 1))
    {
      println("Record");
      looper.record();
    }
    else if ((x == 15) && (y == 14) && (value == 1))
    {
      println("Overdub");
      looper.overdub();
    }
    else if ((x == 15) && (y == 13) && (value == 1))
    {
      println("Undo");
      looper.undo();
    }
    else if ((x == 15) && (y == 12) && (value == 1))
    {
      println("Redo");
      looper.redo();
    }
  }
  println("forwarding " + message.toString());
  oscP5.send(message);
}


