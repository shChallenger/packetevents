package com.github.retrooper.packetevents.protocol.world.chunk.palette;

import com.github.retrooper.packetevents.protocol.world.chunk.palette.generic.PaletteGenericFactory;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.reader.PaletteReaderFactory;

/**
 * A palette reader for mapping block states to storage IDs.
 */
public interface Palette {

    PaletteGenericFactory GENERIC_FACTORY = new PaletteGenericFactory();
    PaletteReaderFactory READER_FACTORY = new PaletteReaderFactory();

    /**
     * Gets the number of block states known by this palette.
     *
     * @return The palette's size.
     */
    int size();

    /**
     * Converts a block state to a storage ID. If the state has not been mapped,
     * the palette will attempt to map it, returning -1 if it cannot.
     *
     * @param state Block state to convert.
     * @return The resulting storage ID.
     */
    default int stateToId(int state) {
        throw new IllegalStateException("PaletteReader does not support state writing");
    }

    /**
     * Converts a storage ID to a block state. If the storage ID has no mapping,
     * it will return a block state of 0.
     *
     * @param id Storage ID to convert.
     * @return The resulting block state.
     */
    int idToState(int id);

    int getBits();

}