package org.crimson.v3.netty.pipeline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.TypeParameterMatcher;
import lombok.extern.log4j.Log4j2;

/**
 * A handler that has the ability to accept certain messages.
 * <p>
 * Based off CloudburstMC Network components 2.0
 */
@Log4j2
public abstract class AcceptableInboundMessageHandler<T> extends ChannelInboundHandlerAdapter {

    /**
     * Matcher for certain types extending classes want.
     */
    private final TypeParameterMatcher matcher;

    /**
     * Initialize
     *
     * @param desire the desired object/message type.
     */
    public AcceptableInboundMessageHandler(Class<? extends T> desire) {
        this.matcher = TypeParameterMatcher.get(desire);
    }

    /**
     * Check if a message should be accepted by this consumer
     *
     * @param context the context
     * @param message the message
     * @return {@code true} if so
     */
    @SuppressWarnings("unchecked")
    private boolean acceptOrDeclineMessage(ChannelHandlerContext context, Object message) {
        return this.matcher.match(message) && acceptOrDeclineMessage0(context, (T) message);
    }

    /**
     * Check if a message should be accepted by this consumer
     *
     * @param context the context
     * @param message the message
     * @return {@code true} if so
     */
    protected abstract boolean acceptOrDeclineMessage0(ChannelHandlerContext context, T message);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)  {
        boolean release = true;

        try {
            if (acceptOrDeclineMessage(ctx, msg)) {
                log.info("Inbound message accepted");
                channelRead0(ctx, (T) msg);
            } else {
                log.info("Inbound message rejected.");
                release = false;
                ctx.fireChannelRead(msg);
            }
        } finally {
            if (release) ReferenceCountUtil.release(msg);
        }
    }

    /**
     * Read.
     *
     * @param context the context
     * @param message the message
     */
    protected abstract void channelRead0(ChannelHandlerContext context, T message);

}
