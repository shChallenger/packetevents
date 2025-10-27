package com.github.retrooper.packetevents.protocol.world.chunk.palette;

import com.github.retrooper.packetevents.protocol.stream.NetStreamInput;

public abstract class PaletteFactory {

    public abstract Palette readPalette(PaletteType paletteType, int bitsPerEntry,
                                        NetStreamInput in, boolean allowSingletonPalette);

}
