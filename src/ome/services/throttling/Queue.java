/* *   $Id$ * *   Copyright 2008 Glencoe Software, Inc. All rights reserved. *   Use is subject to license terms supplied in LICENSE.txt */package ome.services.throttling;import java.util.concurrent.BlockingQueue;import java.util.concurrent.LinkedBlockingQueue;import java.util.concurrent.atomic.AtomicBoolean;/** * Manages AMD-based method dispatches from blitz. *  */public class Queue {    interface Callback {        void response(Object rv);        void exception(Exception ex);        Boolean ioIntensive();        Boolean dbIntensive();    }    class CancelledException extends Exception {    }    private final BlockingQueue<Callback> q = new LinkedBlockingQueue<Callback>();    private final AtomicBoolean done = new AtomicBoolean();    public Queue() {        done.set(false);    }    public void put(Callback callback) {        boolean cont = !done.get();        if (cont) {            q.put(callback);        } else {            callback.exception(new CancelledException());        }    }    public Callback take() {        return q.take();    }    public void destroy() {        boolean wasDone = done.getAndSet(true);        if (!wasDone) {            for (Callback cb : q) {                cb.exception(new CancelledException());            }        }    }}