package hsxin;


import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.BaseStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author harpreet.singh
 * @since 8/22/2017.
 */
class ChainedStreams {

    public static <T> Stream<T> from(SteamProvider<T> provider) {
        final ChainedSpliterator<T> spliterator = new ChainedSpliterator<>(provider);
        return StreamSupport.stream(spliterator, false).onClose(spliterator::close);
    }

    private static class ChainedSpliterator<T> extends Spliterators.AbstractSpliterator<T> {

        private volatile boolean streamOpen = true;
        private volatile boolean first = false;
        private volatile boolean empty = true;
        private final SteamProvider<T> provider;
        private final AtomicReference<Pair<Stream<T>, Spliterator<T>>> cursorRef = new AtomicReference<>();

        ChainedSpliterator(SteamProvider<T> provider) {
            super(Long.MAX_VALUE, 0);
            this.provider = provider;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            return next(action);
        }

        public void close() {
            closeAndReset();
        }


        private boolean next(Consumer<? super T> action) {
            if (!first) {
                first = true;
            }
            boolean nextStream = true;
            while (streamOpen) {
                if (emptyStream()) {
                    final Stream<T> next = provider.next();
                    cursorRef.set(new Pair<>(next, next.spliterator()));
                    if (emptyStream()) {
                        closeAndReset();
                        return false;
                    }
                }
                if (tryAdvance0(action)) {
                    empty = false;
                    return true;
                } else {

                    //if first and no result dnt look for next cycle and return instead
                    if (first && empty) {
                        streamOpen = false;
                    }

                    if (nextStream) {
                        closeAndReset();
                        nextStream = false;
                    } else {
                        streamOpen = false;
                        return false;
                    }
                }
            }
            return false;
        }

        private boolean tryAdvance0(Consumer<? super T> action) {
            return cursorRef.get().getY().tryAdvance(action);
        }

        private void closeAndReset() {
            cursorRef.getAndUpdate(cursor ->
            {
                Optional.ofNullable(cursor.getX()).ifPresent(BaseStream::close);
                return null;
            });
        }

        private boolean emptyStream() {
            return cursorRef.get() == null
                    || cursorRef.get().getY() == null
                    || Spliterators.emptySpliterator().equals(cursorRef.get().getY());
        }

        @Override
        public Spliterator<T> trySplit() {
            throw new UnsupportedOperationException();
        }
    }
}