/**
 * Copyright 2016-2017 The Reaktivity Project
 *
 * The Reaktivity Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.reaktivity.nukleus.http_cache.internal;

import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

public class HttpCacheCounters
{
    public final Function<String, LongSupplier> supplyCounter;
    public final Function<String, LongConsumer> supplyAccumulator;

    public final LongSupplier requests;
    public final LongSupplier requestsCacheable;
    public final LongSupplier requestsPreferWait;
    public final LongSupplier responses;
    public final LongSupplier responsesCached;
    public final LongSupplier responsesAborted;
    public final LongSupplier promises;
    public final LongSupplier promisesCanceled;

    public final LongSupplier scheduledRetries;
    public final LongSupplier executedRetries;
    public final LongSupplier sentRetries;

    public HttpCacheCounters(
        Function<String, LongSupplier> supplyCounter,
        Function<String, LongConsumer> supplyAccumulator)
    {
        this.supplyCounter = supplyCounter;
        this.supplyAccumulator = supplyAccumulator;

        this.requests = supplyCounter.apply("requests");
        this.requestsCacheable = supplyCounter.apply("requests.cacheable");
        this.requestsPreferWait = supplyCounter.apply("requests.prefer.wait");
        this.responses = supplyCounter.apply("responses");
        this.responsesCached = supplyCounter.apply("responses.cached");
        this.responsesAborted = supplyCounter.apply("responses.aborted");
        this.promises = supplyCounter.apply("promises");
        this.promisesCanceled = supplyCounter.apply("promises.canceled");

        // TODO replace with above (plus status specific)
        this.sentRetries = supplyCounter.apply("sent.retries");

        // TODO replace with nukleus-http counters (per route, status specific)
        this.scheduledRetries = supplyCounter.apply("scheduled.retries");
        this.executedRetries = supplyCounter.apply("executed.retries");
    }
}