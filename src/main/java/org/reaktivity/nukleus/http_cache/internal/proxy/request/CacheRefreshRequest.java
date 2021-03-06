/**
 * Copyright 2016-2018 The Reaktivity Project
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
package org.reaktivity.nukleus.http_cache.internal.proxy.request;

import org.reaktivity.nukleus.buffer.BufferPool;
import org.reaktivity.nukleus.http_cache.internal.proxy.cache.Cache;
import org.reaktivity.nukleus.http_cache.internal.proxy.cache.CacheEntry;
import org.reaktivity.nukleus.http_cache.internal.types.HttpHeaderFW;
import org.reaktivity.nukleus.http_cache.internal.types.ListFW;

public class CacheRefreshRequest extends CacheableRequest
{
    private final CacheEntry updatingEntry;
    private final Cache cache;

    public CacheRefreshRequest(
            CacheableRequest req,
            BufferPool bufferPool,
            int requestSlot,
            String etag,
            CacheEntry cacheEntry,
            Cache cache)
    {
        // TODO eliminate reference /GC duplication (Flyweight pattern?)
        super(req.acceptName,
              req.acceptReply,
              req.acceptReplyStreamId,
              req.acceptCorrelationId,
              req.connect,
              req.connectRef,
              req.supplyCorrelationId,
              req.supplyStreamId,
              req.requestURLHash(),
              bufferPool,
              requestSlot,
              req.router,
              req.authorizationHeader(),
              req.authorization(),
              req.authScope(),
              etag);
        this.updatingEntry = cacheEntry;
        this.cache = cache;
    }

    @Override
    public boolean storeResponseHeaders(
        ListFW<HttpHeaderFW> responseHeaders,
        Cache cache,
        BufferPool bufferPool)
    {
        if (responseHeaders.anyMatch(h ->
            ":status".equals(h.name().asString()) &&
            h.value().asString().startsWith("2")))
        {
            boolean noError = super.storeResponseHeaders(responseHeaders, cache, bufferPool);
            if (!noError)
            {
                this.purge();
            }
            return noError;
        }
        else if (responseHeaders.anyMatch(h ->
            ":status".equals(h.name().asString()) &&
            "304".equals(h.value().asString())))
        {
            updatingEntry.refresh(this);
            this.state = CacheState.COMMITTED;
            this.purge();
            return true;
        }
        else
        {
            this.purge();
            return false;
        }
}

    @Override
    public Type getType()
    {
        return Type.CACHE_REFRESH;
    }

    @Override
    public void purge()
    {
        if (this.state != CacheState.COMMITTED)
        {
            this.cache.purge(updatingEntry);
        }
        super.purge();
    }
}
