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

import static java.util.Objects.requireNonNull;
import static org.reaktivity.nukleus.buffer.BufferPool.NO_SLOT;

import java.util.Objects;

import org.agrona.MutableDirectBuffer;
import org.reaktivity.nukleus.buffer.BufferPool;
import org.reaktivity.nukleus.function.MessageConsumer;
import org.reaktivity.nukleus.http_cache.internal.types.HttpHeaderFW;
import org.reaktivity.nukleus.http_cache.internal.types.ListFW;

public class Correlation
{
    private final int requestURLHash;
    private final MessageConsumer consumer;
    private final boolean follow304;

    private MessageConsumer connectReplyThrottle;
    private final BufferPool bufferPool;
    private int correlationRequestHeadersSlot;
    private int requestSize;
    private long connectReplyStreamId;
    private final String connectName;
    private final long connectRef;

    public Correlation(
        int requestURLHash,
        MessageConsumer consumer,
        BufferPool bufferPool,
        int correlationRequestHeadersSlot,
        int requestSize,
        boolean follow304,
        String connectName,
        long connectRef
    )
    {
        this.requestURLHash = requireNonNull(requestURLHash);
        this.consumer = consumer;
        this.bufferPool = bufferPool;
        this.correlationRequestHeadersSlot = correlationRequestHeadersSlot;
        this.requestSize = requestSize;
        this.follow304 = follow304;
        this.connectName = connectName;
        this.connectRef = connectRef;
    }

    public int requestURLHash()
    {
        return requestURLHash;
    }

    public ListFW<HttpHeaderFW> headers(ListFW<HttpHeaderFW> headersRO)
    {
        final MutableDirectBuffer buffer = bufferPool.buffer(correlationRequestHeadersSlot);
        return headersRO.wrap(buffer, 0, requestSize);
    }

    public MessageConsumer consumer()
    {
        return this.consumer;
    }

    public void setConnectReplyThrottle(MessageConsumer connectReplyThrottle)
    {
        this.connectReplyThrottle = connectReplyThrottle;
    }

    public MessageConsumer getConnectReplyThrottle()
    {
        return this.connectReplyThrottle;
    }

    public void setConnectReplyStreamId(long streamId)
    {
        this.connectReplyStreamId = streamId;
    }

    public long getConnectReplyStreamId()
    {
        return this.connectReplyStreamId;
    }

    public void cleanUp()
    {
        if(correlationRequestHeadersSlot != NO_SLOT)
        {
            bufferPool.release(correlationRequestHeadersSlot);
            correlationRequestHeadersSlot = NO_SLOT;
        }
    }

    public boolean follow304()
    {
        return follow304;
    }

    public String connectName()
    {
        return connectName;
    }

    public long connectRef()
    {
        return connectRef;
    }

  @Override
  public int hashCode()
  {
      int result = requestURLHash;
      result = 31 * result + consumer.hashCode();
      return result;
  }

  @Override
  public boolean equals(Object obj)
  {
      if (!(obj instanceof Correlation))
      {
          return false;
      }

      Correlation that = (Correlation) obj;
      return this.requestURLHash == that.requestURLHash &&
              Objects.equals(this.consumer, that.consumer);
  }

  @Override
  public String toString()
  {
      return String.format("[requestURLHash=\"%s\", consumer=\"%s\"]",
              requestURLHash,
              consumer.toString());
  }
}
