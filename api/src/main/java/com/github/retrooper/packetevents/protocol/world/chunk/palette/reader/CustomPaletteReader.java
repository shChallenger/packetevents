/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * This class was taken from MCProtocolLib.
 *
 * https://github.com/Steveice10/MCProtocolLib
 */

package com.github.retrooper.packetevents.protocol.world.chunk.palette.reader;

import com.github.retrooper.packetevents.protocol.stream.NetStreamInput;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.Palette;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

/**
 * A palette backed by a List.
 */
//TODO Equals & hashcode
public class CustomPaletteReader implements Palette {

    private final int bits;
    protected final int[] data;
    protected int nextId = 0;

    public CustomPaletteReader(int bitsPerEntry) {
        this.bits = bitsPerEntry;
        this.data = new int[1 << bitsPerEntry];
    }

    @Deprecated
    public CustomPaletteReader(int bitsPerEntry, NetStreamInput in) {
        this(bitsPerEntry);

        int paletteLength = in.readVarInt2Bytes();
        for (int i = 0; i < paletteLength; i++) {
            this.data[i] = in.readVarInt2Bytes();
        }
        this.nextId = paletteLength;
    }

    public CustomPaletteReader(int bitsPerEntry, PacketWrapper<?> wrapper) {
        this(bitsPerEntry);

        int paletteLength = wrapper.readVarInt();
        for (int i = 0; i < paletteLength; i++) {
            this.data[i] = wrapper.readVarInt();
        }
        this.nextId = paletteLength;
    }

    @Override
    public int size() {
        return this.nextId;
    }

    @Override
    public int idToState(int id) {
        if (id >= 0 && id < this.nextId) {
            return this.data[id];
        } else {
            return 0;
        }
    }

    @Override
    public int getBits() {
        return this.bits;
    }
}
