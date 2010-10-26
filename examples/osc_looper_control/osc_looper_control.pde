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

import controlP5.*;
import oscP5.*;
import osclooper.*;

OscP5 oscP5;
OscLooper looper;
ControlP5 controlP5;
Toggle record;
controlP5.Button overdub;
controlP5.Button undo;
controlP5.Button redo;
Matrix loopMatrix;
Matrix undoMatrix;

String host = "127.0.0.1";
int sendPort = 9092;
int receivePort = 9090;

void setup()
{
  size(16*30, 9*30);
  smooth();
  background(20);

  OscProperties properties = new OscProperties();
  properties.setListeningPort(receivePort);
  properties.setRemoteAddress(host, sendPort);
  oscP5 = new OscP5(this, properties);
  looper = new OscLooper(oscP5);
  controlP5 = new ControlP5(this);
  record = controlP5.addToggle("record", 20, 20, 80, 30);
  overdub = controlP5.addButton("overdub", 0.0, 120, 20, 80, 30);
  undo = controlP5.addButton("undo", 0.0, 16*30 - 80 - 20 - 80 - 20, 20, 80, 30);
  redo = controlP5.addButton("redo", 0.0, 16*30 - 80 - 20, 20, 80, 30);
  controlP5.addTextlabel("loopsLabel", "Loops", 20, 100);
  loopMatrix = controlP5.addMatrix("loopsMatrix", 10, 1, 20, 120, 16*30 - 40, 40);
  loopMatrix.setInterval(Integer.MAX_VALUE);
  controlP5.addTextlabel("undoLabel", "Undo", 20, 180);
  undoMatrix = controlP5.addMatrix("undoMatrix", 10, 1, 20, 200, 16*30 - 40, 40);
  undoMatrix.setInterval(Integer.MAX_VALUE);
}

void draw()
{
  // empty
}

void oscEvent(OscMessage message)
{
  println("forwarding " + message.toString());
  oscP5.send(message);
}

void record()
{
  println("Record");
  looper.record();
}

void overdub()
{
  println("Overdub");
  looper.overdub();
}

void undo()
{
  println("Undo");
  looper.undo();
}

void redo()
{
  println("Redo");
  looper.redo();
}

