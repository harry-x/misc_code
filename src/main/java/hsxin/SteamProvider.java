package hsxin;

import java.util.stream.Stream;

/**
 * @author harpreet.singh
 * @since 8/22/2017.
 */
interface SteamProvider<T> {
    Stream<T> next();
}