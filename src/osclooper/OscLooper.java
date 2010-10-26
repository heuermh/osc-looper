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
package osclooper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import netP5.NetAddress;
import netP5.NetAddressList;

import oscP5.OscEventListener;
import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscStatus;

/**
 * OSC looper.
 */
public final class OscLooper
{
    /** Stack of loops. */
    private final Stack<Loop> loops = new Stack<Loop>();

    /** Undo stack of loops. */
    private final Stack<Loop> undo = new Stack<Loop>();

    /** OSC P5. */
    private OscP5 oscP5;

    /** Address. */
    private NetAddress address;

    /** Address list. */
    private NetAddressList addressList;


    /**
     * Create a new OSC looper with the specified osc p5.  The osc p5 must have
     * been configured using OscProperties.
     *
     * @param oscP5 osc p5, must not be null
     */
    public OscLooper(final OscP5 oscP5)
    {
        if (oscP5 == null)
        {
            throw new IllegalArgumentException("oscP5 must not be null");
        }
        this.oscP5 = oscP5;
    }

    /**
     * Create a new OSC looper with the specified osc p5 and address.
     *
     * @param oscP5 osc p5, must not be null
     * @param address address, must not be null
     */
    public OscLooper(final OscP5 oscP5, final NetAddress netAddress)
    {
        if (oscP5 == null)
        {
            throw new IllegalArgumentException("oscP5 must not be null");
        }
        if (address == null)
        {
            throw new IllegalArgumentException("address must not be null");
        }
        this.oscP5 = oscP5;
        this.address = address;
    }

    /**
     * Create a new OSC looper with the specified osc p5 and address list.
     *
     * @param oscP5 osc p5, must not be null
     * @param addressList address list, must not be null
     */
    public OscLooper(final OscP5 oscP5, final NetAddressList addressList)
    {
        if (oscP5 == null)
        {
            throw new IllegalArgumentException("oscP5 must not be null");
        }
        if (addressList == null)
        {
            throw new IllegalArgumentException("addressList must not be null");
        }
        this.oscP5 = oscP5;
        this.addressList = addressList;
    }


    /**
     * Start recording a new loop or stop recording and start playing the current
     * loop if the current loop is recording.
     */
    public void record()
    {
        if (loops.empty())
        {
            start();
        }
        else
        {
            Loop current = loops.peek();
            if (current.isRecording())
            {
                stop();
            }
            else
            {
                start();
            }
        }
    }

    /**
     * Stop recording the current loop, start playing it, and start recording a new loop.
     */
    public void overdub()
    {
        stop();
        start();
    }

    /**
     * Undo the current loop, unless it is recording.
     */
    public void undo()
    {
        if (!loops.empty())
        {
            Loop current = loops.peek();
            if (current.isPlaying())
            {
                loops.pop();
                current.stop();
            }
        }
    }

    /**
     * Redo the last undo operation, unless the current loop is recording.
     */
    public void redo()
    {
        if (loops.empty())
        {
            if (!undo.empty())
            {
                Loop last = undo.pop();
                last.play();
                loops.push(last);
            }
        }
        else
        {
            Loop current = loops.peek();
            if (!current.isRecording())
            {
                if (!undo.empty())
                {
                    Loop last = undo.pop();
                    last.play();
                    loops.push(last);
                }
            }
        }
    }

    /**
     * Return the number of loops in the loop stack.
     *
     * @return the number of loops in the loop stack
     */
    public int getLoopCount()
    {
        return loops.size();
    }

    /**
     * Return the number of loops in the undo stack.
     *
     * @return the number of loops in the undo stack
     */
    public int getUndoCount()
    {
        return undo.size();
    }

    /**
     * Start recording a new loop.
     */
    private void start()
    {
        loops.push(new Loop());
    }

    /**
     * Stop recording the current loop and start playing it.
     */
    private void stop()
    {
        if (!loops.empty())
        {
            Loop current = loops.peek();
            current.stop();
            current.play();
        }
    }

    /**
     * Loop.
     */
    private class Loop implements OscEventListener, Runnable
    {
        /** Last timestamp, in milliseconds. */
        private long last;

        /** True if this loop is playing. */
        private boolean playing;

        /** True if this loop is recording. */
        private boolean recording;

        /** Executor. */
        private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        /** List of events. */
        private final List<Event> events = new ArrayList<Event>();


        /**
         * Create a new loop and start recording.
         */
        Loop()
        {
            last = System.currentTimeMillis();
            playing = false;
            recording = true;
            oscP5.addListener(this);
        }


        /**
         * Stop this loop.
         */
        void stop()
        {
            if (playing)
            {
                playing = false;
                executor.shutdownNow();
            }
            if (recording)
            {
                recording = false;
                oscP5.removeListener(this);
            }
        }

        /**
         * Play this loop.
         */
        void play()
        {
            playing = true;
            // try ... catch RejectedExecutionException?
            executor.scheduleWithFixedDelay(this, 1L, 1L, TimeUnit.NANOSECONDS);
        }

        /**
         * Return true if this loop is playing.
         *
         * @return true if this loop is playing
         */
        boolean isPlaying()
        {
            return playing;
        }

        /**
         * Return true if this loop is recording.
         *
         * @return true if this loop is recording
         */
        boolean isRecording()
        {
            return recording;
        }

        /** {@inheritDoc} */
        public void run()
        {
            for (Event event : events)
            {
                event.run();
            }
        }

        /** {@inheritDoc} */
        public void oscStatus(final OscStatus status)
        {
            // empty
        }

        /** {@inheritDoc} */
        public void oscEvent(final OscMessage message)
        {
            long current = System.currentTimeMillis();
            events.add(new Wait(current - last));
            events.add(new Message(message));
            last = current;
        }
    }

    /**
     * Event.
     */
    private interface Event extends Runnable
    {
        // empty
    }

    /**
     * Wait event.
     */
    private class Wait implements Event
    {
        /** Delay in milliseconds. */
        private final long delay;


        /**
         * Create a new wait event with the specified delay in millieseconds.
         *
         * @param delay delay in milliseconds
         */
        Wait(final long delay)
        {
            this.delay = delay;
        }


        /** {@inheritDoc} */
        public void run() 
        {
            try
            {
                Thread.currentThread().sleep(delay);
            }
            catch (InterruptedException e)
            {
                // ignore
            }
        }
    }

    /**
     * Message event.
     */
    private class Message implements Event
    {
        /** OSC message. */
        private final OscMessage message;


        /**
         * Create a new message event with the specified OSC message.
         *
         * @param message OSC message
         */
        Message(final OscMessage message)
        {
            this.message = message;
        }


        /** {@inheritDoc} */
        public void run()
        {
            if (address != null)
            {
                oscP5.send(message, address);
            }
            else if (addressList != null)
            {
                oscP5.send(message, addressList);
            }
            else
            {
                oscP5.send(message);
            }
        }
    }
}