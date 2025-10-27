package com.github.retrooper.packetevents.protocol.world.chunk.palette.reader;

import com.github.retrooper.packetevents.protocol.stream.NetStreamInput;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.Palette;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.PaletteFactory;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.PaletteType;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.generic.GlobalPalette;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.generic.ListPalette;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.generic.MapPalette;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.generic.SingletonPalette;

public final class PaletteReaderFactory extends PaletteFactory {

    @Override
    public Palette readPalette(PaletteType paletteType, int bitsPerEntry,
                                        NetStreamInput in, boolean allowSingletonPalette) {
        if (bitsPerEntry == 0 && allowSingletonPalette) {
            return new SingletonPaletteReader(in);
        } else if (bitsPerEntry <= paletteType.getMaxBitsPerEntryForList()) {
            // vanilla forces a blockstate-list-palette to always be the maximum size
            int bits = paletteType.isForceMaxListPaletteSize() ? paletteType.getMaxBitsPerEntryForList() : bitsPerEntry;
            return new CustomPaletteReader(bits, in);
        } else if (bitsPerEntry <= paletteType.getMaxBitsPerEntryForMap()) {
            return new CustomPaletteReader(bitsPerEntry, in);
        } else {
            return GlobalPaletteReader.INSTANCE;
        }
    }

}
