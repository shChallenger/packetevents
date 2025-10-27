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

package com.github.retrooper.packetevents.protocol.world.chunk.palette.generic;

import com.github.retrooper.packetevents.protocol.stream.NetStreamInput;
import com.github.retrooper.packetevents.protocol.world.chunk.palette.reader.CustomPaletteReader;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;

import java.util.HashMap;

/**
 * A palette backed by a map.
 */
public class MapPalette extends CustomPaletteReader {

    // TODO: Can we use fastutils here?
    private final HashMap<Integer, Integer> stateToId = new HashMap<>();

    public MapPalette(int bitsPerEntry) {
        super(bitsPerEntry);
    }

    @Deprecated
    public MapPalette(int bitsPerEntry, NetStreamInput in) {
        this(bitsPerEntry);

        int paletteLength = in.readVarInt2Bytes();
        for (int i = 0; i < paletteLength; i++) {
            int state = in.readVarInt2Bytes();
            this.data[i] = state;
            this.stateToId.putIfAbsent(state, i);
        }
        this.nextId = paletteLength;
    }

    public MapPalette(int bitsPerEntry, PacketWrapper<?> wrapper) {
        this(bitsPerEntry);

        int paletteLength = wrapper.readVarInt();
        for (int i = 0; i < paletteLength; i++) {
            int state = wrapper.readVarInt();
            this.data[i] = state;
            this.stateToId.putIfAbsent(state, i);
        }
        this.nextId = paletteLength;
    }

    @Override
    public int stateToId(int state) {
        Integer id = this.stateToId.get(state);
        if (id == null && this.nextId < this.data.length) {
            id = this.nextId++;
            this.data[id] = state;
            this.stateToId.put(state, id);
        }

        if (id != null) {
            return id;
        } else {
            return -1;
        }
    }
}
