package com.github.retrooper.packetevents.protocol.world.chunk.palette.generic;

import com.github.retrooper.packetevents.protocol.stream.NetStreamInput;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.Palette;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.PaletteFactory;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.PaletteType;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.reader.CustomPaletteReader;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.reader.GlobalPaletteReader;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.reader.SingletonPaletteReader;

public final class PaletteGenericFactory extends PaletteFactory {

    @Override
    public Palette readPalette(PaletteType paletteType, int bitsPerEntry,
                                        NetStreamInput in, boolean allowSingletonPalette) {
        if (bitsPerEntry == 0 && allowSingletonPalette) {
            return new SingletonPalette(in);
        } else if (bitsPerEntry <= paletteType.getMaxBitsPerEntryForList()) {
            // vanilla forces a blockstate-list-palette to always be the maximum size
            int bits = paletteType.isForceMaxListPaletteSize() ? paletteType.getMaxBitsPerEntryForList() : bitsPerEntry;
            return new ListPalette(bits, in);
        } else if (bitsPerEntry <= paletteType.getMaxBitsPerEntryForMap()) {
            return new MapPalette(bitsPerEntry, in);
        } else {
            return GlobalPalette.INSTANCE;
        }
    }

}
